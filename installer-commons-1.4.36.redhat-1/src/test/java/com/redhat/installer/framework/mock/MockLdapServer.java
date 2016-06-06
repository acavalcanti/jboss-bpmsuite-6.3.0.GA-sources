package com.redhat.installer.framework.mock;

import org.apache.commons.io.FileUtils;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schemaextractor.SchemaLdifExtractor;
import org.apache.directory.api.ldap.schemaextractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.api.ldap.schemaloader.LdifSchemaLoader;
import org.apache.directory.api.ldap.schemamanager.impl.DefaultSchemaManager;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adaption of some code found online to work with 2.0.0-M15, and to be used by our tests
 * Created by thauser on 2/5/14.
 */
public class MockLdapServer {

    /** The directory service */
    private DirectoryService service;

    /** The LDAP server */
    private LdapServer server;


    /**
     * Creates and returns a Partition with the given characteristics
     *
     * @param partitionId The partition Id
     * @param partitionDn The partition DN
     * @param dnFactory the DN factory
     * @return The newly added partition
     * @throws Exception If the partition can't be added
     */
    private Partition createPartition(String partitionId, String partitionDn, DnFactory dnFactory) throws Exception
    {
        // Create a new partition with the given partition id
        JdbmPartition partition = new JdbmPartition(service.getSchemaManager());
        partition.setId( partitionId );
        partition.setPartitionPath( new File( service.getInstanceLayout().getPartitionsDirectory(), partitionId ).toURI() );
        partition.setSuffixDn( new Dn( partitionDn ) );
        service.addPartition( partition );

        return partition;
    }

    /**
     * Convenience method to actually add a partition, rather than create it
     * @param partitionId
     * @param partitionDn
     * @throws Exception
     */
    public void addPartition(String partitionId, String partitionDn) throws Exception {
        Partition partition = createPartition(partitionId, partitionDn, service.getDnFactory());
        addIndex(partition, "ou", "objectClass", "uid");

        try {
            service.getAdminSession().lookup(partition.getSuffixDn());
        } catch (LdapException e){
            Dn dnO = new Dn("o=TestOrganization");
            Entry o = service.newEntry(dnO);
            o.add("objectClass", "top", "organization");
            o.add("o","Organization");
            service.getAdminSession().add(o);
        }
    }

    /**
     * Used to add user entries under a given DN
     * @param dn
     * @param uid
     * @param cn
     * @param sn
     * @throws Exception
     */
    public void addUser(String dn, String uid, String cn, String sn) throws Exception {
        Entry userAdd = service.newEntry(new Dn(dn));
        userAdd.add("objectClass", "inetOrgPerson", "organizationalPerson", "person", "top");
        userAdd.add("cn", cn);
        userAdd.add("sn", sn);
        userAdd.add("uid", uid);
        userAdd.add("userPassword", "testpass");

        try {
            service.getAdminSession().lookup(new Dn(dn));
        } catch (LdapException e){
            service.getAdminSession().add(userAdd);
        }
    }


    /**
     * Add a new set of index on the given attributes
     *
     * @param partition The partition on which we want to add index
     * @param attrs The list of attributes to index
     */
    private void addIndex( Partition partition, String... attrs )
    {
        // Index some attributes on the apache partition
        Set indexedAttributes = new HashSet();

        for ( String attribute : attrs )
        {
            indexedAttributes.add( new JdbmIndex( attribute, false ) );
        }

        ( ( JdbmPartition ) partition ).setIndexedAttributes( indexedAttributes );
    }


    /**
     * initialize the schema manager and add the schema partition to directory service
     *
     * @throws Exception if the schema LDIF files are not found on the classpath
     */
    private void initSchemaPartition() throws Exception
    {
        InstanceLayout instanceLayout = service.getInstanceLayout();

        File schemaPartitionDirectory = new File( instanceLayout.getPartitionsDirectory(), "schema" );

        // Extract the schema on disk (a brand new one) and load the registries
        if ( schemaPartitionDirectory.exists() )
        {
            System.out.println( "schema partition already exists, skipping schema extraction" );
        }
        else
        {
            SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor( instanceLayout.getPartitionsDirectory() );
            extractor.extractOrCopy();
        }

        SchemaLoader loader = new LdifSchemaLoader( schemaPartitionDirectory );
        SchemaManager schemaManager = new DefaultSchemaManager( loader );

        // We have to load the schema now, otherwise we won't be able
        // to initialize the Partitions, as we won't be able to parse
        // and normalize their suffix Dn
        schemaManager.loadAllEnabled();

        List<Throwable> errors = schemaManager.getErrors();

        if ( errors.size() != 0 )
        {
            throw new Exception( "MockLdapServer.initSchemaPartition() : Errors loading schemas : " + errors );
        }

        service.setSchemaManager( schemaManager );

        // Init the LdifPartition with schema
        LdifPartition schemaLdifPartition = new LdifPartition( schemaManager);
        schemaLdifPartition.setPartitionPath( schemaPartitionDirectory.toURI() );

        // The schema partition
        SchemaPartition schemaPartition = new SchemaPartition( schemaManager );
        schemaPartition.setWrappedPartition( schemaLdifPartition );
        service.setSchemaPartition( schemaPartition );
    }


    /**
     * Initialize the server. It creates the partition, adds the index, and
     * injects the context entries for the created partitions.
     *
     * @param workDir the directory to be used for storing the data
     * @throws Exception if there were some problems while initializing the system
     */
    private void initDirectoryService( File workDir ) throws Exception
    {
        // Initialize the LDAP service
        service = new DefaultDirectoryService();
        service.setInstanceLayout( new InstanceLayout( workDir ) );

        CacheService cacheService = new CacheService();
        cacheService.initialize( service.getInstanceLayout() );

        service.setCacheService( cacheService );

        // first load the schema
        initSchemaPartition();

        // then the system partition
        // this is a MANDATORY partition
        // DO NOT add this via createPartition() method, trunk code complains about duplicate partition
        // while initializing
        JdbmPartition systemPartition = new JdbmPartition(service.getSchemaManager());
        systemPartition.setId( "system" );
        systemPartition.setPartitionPath( new File( service.getInstanceLayout().getPartitionsDirectory(), systemPartition.getId() ).toURI() );
        systemPartition.setSuffixDn( new Dn( ServerDNConstants.SYSTEM_DN ) );
        systemPartition.setSchemaManager( service.getSchemaManager() );

        // mandatory to call this method to set the system partition
        // Note: this system partition might be removed from trunk
        service.setSystemPartition( systemPartition );

        // Disable the ChangeLog system
        service.getChangeLog().setEnabled( false );
        service.setDenormalizeOpAttrsEnabled( true );

        // Now we can create as many partitions as we need
        // Create some new partitions named 'foo', 'bar' and 'apache'.
       // Partition redhatPartition = createPartition( "ldaptest", "dc=test1,dc=example,dc=com", service.getDnFactory() );
       /* Partition barPartition = createPartition( "bar", "dc=bar,dc=com", service.getDnFactory() );
        Partition apachePartition = createPartition( "apache", "dc=apache,dc=org", service.getDnFactory() );*/

        // Index some attributes on the apache partition
        // addIndex( apachePartition, "objectClass", "ou", "uid" );

        // And start the service
        service.startup();

        addPartition("TestOrganization", "o=TestOrganization");
    }


    /**
     * Creates a new instance of EmbeddedADS. It initializes the directory service.
     *
     * @throws Exception If something went wrong
     */
    public MockLdapServer(File workDir) throws Exception
    {
        initDirectoryService( workDir );
    }


    /**
     * starts the LdapServer
     *
     * @throws Exception
     */
    public void startServer() throws Exception
    {
        server = new LdapServer();
        int serverPort = 10389;
        server.setTransports( new TcpTransport( serverPort ) );
        server.setDirectoryService( service );

        server.start();
    }

    /**
     * Stop the server. possibly throw NPE if the startServer() wasn't called first
     * @throws Exception
     */
    public void stopServer() throws Exception {
        if (server != null)
            server.stop();
    }

    /**
     * Main class.
     * For running the class standalone
     *
     * @param args Not used.
     */
    public static void main( String[] args )
    {
        try
        {
            File workDir = new File( System.getProperty( "java.io.tmpdir" ) + "/server-work" );
            FileUtils.deleteDirectory(workDir);
            workDir.mkdirs();

            // Create the server
            MockLdapServer ads = new MockLdapServer( workDir );

            ads.startServer();
        }
        catch ( Exception e )
        {
            // Ok, we have something wrong going on ...
            e.printStackTrace();
        }
    }
}

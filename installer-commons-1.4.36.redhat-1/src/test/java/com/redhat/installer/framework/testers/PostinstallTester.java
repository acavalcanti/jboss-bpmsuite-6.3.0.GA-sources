package com.redhat.installer.framework.testers;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.rules.RulesEngine;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommands;
import com.redhat.installer.asconfiguration.ascontroller.ServerManager;
import com.redhat.installer.asconfiguration.processpanel.PostInstallUserHelper;
import com.redhat.installer.asconfiguration.processpanel.postinstallation.PostInstallation;
import com.redhat.installer.framework.mock.MockAbstractUIProcessHandler;
import com.redhat.installer.tests.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by aabulawi on 30/07/15.
 */
public class PostinstallTester {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static final String PASSWORD = "dsadfd#17%";

    public static AutomatedInstallData idata;
    public static MockAbstractUIProcessHandler mockAbstractUIProcessHandler = new MockAbstractUIProcessHandler();
    public ServerMode serverMode  = configureServerMode();

    public enum ServerMode{

        DOMAIN(true, "domain.xml"),
        STANDALONE("standalone.xml"),
        STANDALONE_HA("standalone-ha.xml"),
        STANDALONE_OSGI("standalone-osgi.xml"),
        STANDALONE_FULL("standalone-full.xml"),
        STANDALONE_FULL_HA("standalone-full-ha.xml");

        private String configuration_name;
        private boolean isDomain;
        private String startScript;

        ServerMode(boolean isDomain, String configuration_name){
            this.configuration_name = configuration_name;
            this.isDomain = isDomain;
            if (isDomain)
                this.startScript = "bin/domain.sh";
            else
                this.startScript = "bin/standalone.sh";
        }

        ServerMode(String configuration_name){
            this.configuration_name = configuration_name;
            this.isDomain = false;
            this.startScript = "bin/standalone.sh";
        }

        public boolean isDomain(){
            return isDomain;
        }

        public String getConfigName(){
            return configuration_name;
        }

        public String getStartScript(){
            return startScript;
        }
    }

    @Before
    public void beforeTest() throws Exception {
        temporaryFolder.create();
        idata = new AutomatedInstallData();
        idata.setRules(mock(RulesEngine.class));
        idata.langpack = mock(LocaleDatabase.class);
        when(idata.langpack.getString(anyString())).thenReturn("This test string");
        TestUtils.setVariableSubstitutorIdata(idata);

        File zip = new File(System.getProperty("user.dir")+"/target/test");
        unzipFile(zip.listFiles()[0].getAbsolutePath(), temporaryFolder.getRoot().getAbsolutePath());

        idata.setVariable("INSTALL_PATH", temporaryFolder.getRoot().listFiles()[0].getAbsolutePath());
        idata.setVariable("installation.logfile", TestUtils.testLogFilename);
        idata.setVariable("installerMode", "CLI");
        idata.setVariable(PostInstallUserHelper.USERNAME_VAR, PostInstallUserHelper.username);
        idata.setVariable(PostInstallUserHelper.PWD_VAR, PASSWORD);

        new File(idata.getInstallPath(), "bin/add-user.sh").setExecutable(true);
        createUser();
        startServer(serverMode);
    }

    @After
    public void afterTest() throws Exception{
        ServerCommands serverCommands =  PostInstallation.initServerCommands(PostinstallTester.class);
        serverCommands.connectContext();
        if (serverMode.isDomain())
            serverCommands.shutdownDomainHost();
        else
            serverCommands.shutdownHost();
        TestUtils.destroyVariableSubstitutorIdata();
        TestUtils.destroyIdataSingleton();
        FileUtils.deleteDirectory(temporaryFolder.getRoot());
        idata.getVariables().clear();
        idata = null;
    }

    private void unzipFile(String zipFile, String outputFolder) throws IOException {

        System.out.println(String.format("Uziping %s into %s", zipFile, outputFolder));
        int BUFFER_SIZE = 1024 *4;
        try{
            File folder = new File(outputFolder);
            if(!folder.exists()){
                folder.mkdir();
            }

            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry currentZipEntry = zipInputStream.getNextEntry();

            while(currentZipEntry != null){
                if (currentZipEntry.isDirectory()) {
                    currentZipEntry = zipInputStream.getNextEntry();
                    continue;
                }
                String fileName = currentZipEntry.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                new File(newFile.getParent()).mkdirs();
                BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(newFile));
                int len;
                byte[] data = new byte[BUFFER_SIZE];
                while ((len = zipInputStream.read(data)) > 0) {
                    fileOutputStream.write(data, 0, len);
                }
                fileOutputStream.close();
                currentZipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
            zipInputStream.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void createUser() throws Exception {
        ProcessBuilder adduserprocessbuilder = new ProcessBuilder("bin/add-user.sh", "-s", "-u", PostInstallUserHelper.username, "-p", PASSWORD);
        adduserprocessbuilder.directory(new File(idata.getInstallPath()));
        Process process = adduserprocessbuilder.start();
        process.waitFor();
    }

    private void startServer(ServerMode mode) throws Exception{

        if (mode.isDomain())
            ServerManager.setMode("domain");
        else
            ServerManager.setMode("standalone");

        new File(idata.getInstallPath(), mode.getStartScript()).setExecutable(true);
        ProcessBuilder processBuilder = new ProcessBuilder(new String[] {mode.getStartScript(), "-c", mode.getConfigName()});
        processBuilder.directory(new File(idata.getInstallPath()));
        Process p = processBuilder.start();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
        writer.newLine();
        writer.flush();
        Thread b = new Thread(new Runnable() {
            public void run() {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Check for readiness codes here
                        System.out.println(line);
                        if (line.contains(ServerManager.CODE_START_OK)){
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        b.setDaemon(false);
        b.start();
        b.join(5000);
    }

    public ServerMode configureServerMode(){
        return ServerMode.STANDALONE;
    }

}



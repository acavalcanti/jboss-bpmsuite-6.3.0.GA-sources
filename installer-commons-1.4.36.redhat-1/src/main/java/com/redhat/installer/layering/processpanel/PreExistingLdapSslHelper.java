package com.redhat.installer.layering.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.Debug;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.layering.PreExistingConfigurationConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.HashMap;

/**
 * Responsible for removing an existing ldap and/or ssl configuration from a
 * pre-existing installation in order to make it possible to run other
 * post-install jobs during a layered install.
 *
 * Also responsible for replacing those ldap/ssl config descriptors at the end
 * of the post-install process.
 *
 * Created by fcanas on 2/24/14.
 */
public class PreExistingLdapSslHelper {
    private static HashMap<String,String> manInterfaceMap = new HashMap<String, String>();
    private static HashMap<String, String> httpSocketMap = new HashMap<String, String>();
    private static Elements ldapConnectionsBackup;
    private static ArgumentParser parser;
    private static AutomatedInstallData idata;
    private static Element serverIdentities;

    /**
     * This process should only run if there is a pre-existing ssl and/or ldap
     * configuration in the installation.
     * @param handler
     * @param args
     * @return
     */
    public static boolean run(AbstractUIProcessHandler handler, String [] args) {
        idata = AutomatedInstallData.getInstance();
        parser = new ArgumentParser();
        parser.parse(args);


        String path = idata.getVariable("INSTALL_PATH") + "/standalone/configuration/";
        for (String config : PreExistingConfigurationConstants.standaloneDescriptors) {
            File file = new File(path + config);

            try {
                InputStream stream = new FileInputStream(file);
                Document doc = Jsoup.parse(stream, "UTF-8", "", Parser.xmlParser());
                doc.outputSettings().prettyPrint(false);
                doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

                if (parser.propertyIsTrue("remove")) {
                    if (idata.getVariable(config + ".pre.existing.ldap").equals("true")) removeLdap(doc);
                    if (idata.getVariable(config + ".pre.existing.ssl").equals("true")) removeSsl(doc);
                } else if (parser.propertyIsTrue("restore")) {
                    if (idata.getVariable(config + ".pre.existing.ldap").equals("true")) restoreLdap(doc);
                    if (idata.getVariable(config + ".pre.existing.ssl").equals("true")) restoreSsl(doc);
                }

                PrintWriter writer = new PrintWriter(file,"UTF-8");

                /**
                 * Jsoup normalizes attribute names, so it is necessary to manually
                 * revert this particular attribute as it causes issues: at least
                 * until jsoup can be updated to avoid this issue.
                 */
                String cleanDoc = doc.toString().replace("implclass","implClass");
                writer.write(cleanDoc);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                Debug.log(config + " threw IOException when parsed by JSoup:");
                Debug.log(e.getMessage());
                return false;
            }
        }
        
        return true;
    }

    /**
     * Removes ldap configuration temporarily from config files.
     * @param doc
     */
    public static void removeLdap(Document doc) {
        Elements realms = doc.select("security-realms > security-realm");
        //System.out.println("Realm: " + realms.toString());

        Elements ldapConnections = doc.select("outbound-connections > ldap");
        //System.out.println(ldapConnections.toString());

        if (!ldapConnections.isEmpty()) {
            /**
             * Check each security realm and find those that use
             * ldap authentication.
             */
            for (Element realm : realms) {
                Elements ldapRealms = realm.select("authentication > ldap");

                if (!ldapRealms.isEmpty()) {
                    String ldapRealmName = realm.attr("name");

                    /**
                     * Find each management-interface that uses this ldap-auth'd realm
                     * and replace its security-realm with default ManagementRealm so
                     * that it no longer requires ldap.
                     */
                    Elements managementInterfaces = doc.select("management-interfaces > *");
                    int count = 0;
                    for (Element manInterface : managementInterfaces) {
                        // if it references ldap
                        if (manInterface.attr("security-realm").equals(ldapRealmName)) {
                            // Save the security-realm value for later restoration
                            manInterfaceMap.put(manInterface.tagName() + count,manInterface.attr("security-realm"));
                            // Replace it with the default realm:
                            manInterface.attr("security-realm","ManagementRealm");
                        }
                        count++;
                    }
                }
            }

            /**
             * Remove ldap connections entirely.
             */
            ldapConnectionsBackup = ldapConnections.clone();
            ldapConnections.remove();
        }
    }

    private static void removeSsl(Document doc) {
        Elements sslRealmIdentities = doc.select("security-realms > security-realm[name=ManagementRealm] > server-identities");
        if (sslRealmIdentities.size() == 1) {
            /**
             * Removes the <server-identities> child from ManagementRealm entirely, but first
             * saves it to this temp Element so we can restore it later.
             */
            serverIdentities = sslRealmIdentities.first();
            sslRealmIdentities.remove();

            /**
             * Changes the http-interface back to a regular socket binding instead
             * of the secure socket binding used with ssl.
             */
            Elements httpInterfaces = doc.select("management-interfaces > http-interface");
            int count = 0;
            for (Element httpInterface : httpInterfaces) {
                Element socket = httpInterface.select("socket-binding").first();
                /**
                 * Save the current binding for restoration later
                 */
                String binding = socket.hasAttr("https") ? "https=" + socket.attr("https") : "http=" + socket.attr("http");
                httpSocketMap.put(httpInterface.tagName() + count, binding);
                if (socket.hasAttr("https")){
                    socket.removeAttr("https");
                    socket.attr("http", "management-http");
                }
                count++;
            }
        }
    }

    private static void restoreSsl(Document doc) {
        Elements manRealm = doc.select("security-realms > security-realm[name=ManagementRealm]");
        if (manRealm.size() == 1) {
            manRealm.first().appendChild(serverIdentities);
        }

        Elements httpInterfaces = doc.select("management-interfaces > http-interface");
        int count = 0;
        for (Element httpInterface : httpInterfaces) {
            Element socket = httpInterface.select("socket-binding").first();
            socket.removeAttr("http");
            String [] binding =httpSocketMap.get(httpInterface.tagName() + count).split("=");
            socket.attr(binding[0],binding[1]);
            count++;
        }
    }

    private static void restoreLdap(Document doc) {
        int count = 0;
        Elements managementInterfaces = doc.select("management-interfaces > *");
        for (Element manInterface : managementInterfaces) {
            //doc.select("management-interfaces > " + manInterface).attr("security-realm",manInterfaceMap.get(manInterface));
            if (manInterfaceMap.containsKey(manInterface.tagName() + count)) {
                manInterface.attr("security-realm", manInterfaceMap.get(manInterface.tagName() + count));
            }
            count++;
        }
        /**
         * Restore the ldap connections.
         */
        Elements outboundConnections = doc.select("outbound-connections");

        if (outboundConnections.isEmpty()) {
            Elements manager = doc.select("management");
            manager.first().appendElement("outbound-connections");
            outboundConnections = doc.select("outbound-connections");
        }

        for (Element ldapConn : ldapConnectionsBackup) {
            outboundConnections.first().appendChild(ldapConn);
        }
    }

    public static HashMap<String, String> getManInterfaceMap() {
        return manInterfaceMap;
    }

}


package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import com.redhat.installer.asconfiguration.securitydomain.constant.SecurityDomainConstants;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anestico on 14/08/14.
 */
public class SecurityDomain extends PostInstallation {
    public static boolean run(AbstractUIProcessHandler handler, String[] args) {
        mHandler = handler;
        serverCommands = initServerCommands(SecurityDomain.class);
        try {
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (InterruptedException ie){
            ie.printStackTrace();
            return false;
        }
        List<ModelNode> commandResults = installSecurityDomain();
        serverCommands.terminateSession();

        if (commandResults != null) {
            return installResult(commandResults);
        } else {
            return false;
        }
    }

    private static List<ModelNode> installSecurityDomain() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        ArrayList<String> secDomAuthenCode = new ArrayList<String>();
        ArrayList<String> secDomAuthenFlag = new ArrayList<String>();
        ArrayList<Map<String,String>> secDomAuthenOpts = new ArrayList<Map<String, String>>();
        ArrayList<String> secDomAuthorCode = new ArrayList<String>();
        ArrayList<String> secDomAuthorFlag = new ArrayList<String>();
        ArrayList<Map<String,String>> secDomAuthorOpts = new ArrayList<Map<String, String>>();
        ArrayList<String> secDomMappingCode = new ArrayList<String>();
        ArrayList<String> secDomMappingType = new ArrayList<String>();
        ArrayList<Map<String,String>> secDomMappingOpts = new ArrayList<Map<String, String>>();
        String secDomName = idata.getVariable("securitydomain.name.variable");
        String secDomCache = idata.getVariable("securitydomain.cachetype.variable");

        //onComma will split all the key,value pairs by commas. Ignores commas encapsulated in quotes
        //onEqual will split the keys from values by an equal sign. Ignores equal signs encapsulated in quotes
        String onComma = "[,](?=([^\"]*\"[^\"]*\")*[^\"]*$)";
        String onEqual = "[=](?=([^\"]*\"[^\"]*\")*[^\"]*$)";

        //Name is the name of your option
        //Value is the value of your option, and this part does not like quotes, it breaks things
        //  Although single quotes are okay.


        for (int i = 0; i < Integer.parseInt(idata.getVariable("securitydomain.add.authen.count.variable")) / SecurityDomainConstants.NUM_DYNAMIC_COL; i++) { //This is bad why are you dividing by three?
            secDomAuthenCode.add(idata.getVariable("securitydomain.add.authen.left." + i + ".variable"));
            secDomAuthenFlag.add(idata.getVariable("securitydomain.add.authen.middle." + i + ".variable"));
            Map secDomAuthenOptsMap = new HashMap<String, String>();


            String authenOpt = idata.getVariable("securitydomain.add.authen.right." + i + ".variable");
            if (authenOpt != null) {
                String[] authenOpts = authenOpt.split(onComma);
                for (String opt : authenOpts) {
                    String[] option = opt.split(onEqual);
                    String name = option[0];
                    String value = option[1].replaceAll("\"", "");
                    secDomAuthenOptsMap.put(name, value);
                    // parsing
                }
            }
            secDomAuthenOpts.add(secDomAuthenOptsMap);
        }

        for (int i = 0; i < Integer.parseInt(idata.getVariable("securitydomain.add.author.count.variable")) / SecurityDomainConstants.NUM_DYNAMIC_COL; i++) {
            secDomAuthorCode.add(idata.getVariable("securitydomain.add.author.left." + i + ".variable"));
            secDomAuthorFlag.add(idata.getVariable("securitydomain.add.author.middle." + i + ".variable"));
            Map secDomAuthorOptsMap = new HashMap<String, String>(1);

            String authorOpt = idata.getVariable("securitydomain.add.author.right." + i + ".variable");
            if (authorOpt != null) {
                String[] authorOpts = authorOpt.split(onComma);
                for (String opt : authorOpts) {
                    String[] option = opt.split(onEqual);
                    String name = option[0];
                    String value = option[1].replaceAll("\"", "");
                    secDomAuthorOptsMap.put(name, value); // hacky, but good enough for now
                }
            }
            secDomAuthorOpts.add(secDomAuthorOptsMap);
        }

        for (int i = 0; i < Integer.parseInt(idata.getVariable("securitydomain.add.mapping.count.variable")) / SecurityDomainConstants.NUM_DYNAMIC_COL; i++) {
            secDomMappingCode.add(idata.getVariable("securitydomain.add.mapping.left." + i + ".variable"));
            secDomMappingType.add(idata.getVariable("securitydomain.add.mapping.middle." + i + ".variable"));
            Map secDomMappingOptsMap = new HashMap<String, String>(1);

            String mappingOpt = idata.getVariable("securitydomain.add.mapping.right." + i + ".variable");
            if (mappingOpt != null) {
                String[] mappingOpts = mappingOpt.split(onComma);
                for (String opt : mappingOpts) {
                    String[] option = opt.split(onEqual);
                    String name = option[0];
                    String value = option[1].replaceAll("\"", "");
                    secDomMappingOptsMap.put(name, value); // hacky, but good, enough for now
                }
            }
            secDomMappingOpts.add(secDomMappingOptsMap);
        }


        Map<String,String> secDomJsseAttrs = null;
        Map<String,String> secDomJsseKeystoreAttrs = null;
        Map<String,String> secDomJsseKeystoreManagerAttrs = null;
        Map<String,String> secDomJsseTruststoreAttrs = null;
        Map<String,String> secDomJsseTruststoreManagerAttrs = null;
        Map<String,String> secDomJsseAdditionalProps = null;


        //Only configure JSSE attributes if the JSSE checkbox is checked
        if (Boolean.parseBoolean(idata.getVariable("securityDomainAddJsse"))) {
            // check the other checkboxes
            boolean installKeystore = Boolean.parseBoolean(idata.getVariable("securityDomainJsseAddKeystore"));
            boolean installKeystoreManager = Boolean.parseBoolean(idata.getVariable("securityDomainJsseAddKeystoreManager"));
            boolean installTruststore = Boolean.parseBoolean(idata.getVariable("securityDomainJsseAddTruststore"));
            boolean installTruststoreManager = Boolean.parseBoolean(idata.getVariable("securityDomainJsseAddTruststoreManager"));


            String ciphersuites = idata.getVariable("securitydomain.jsse.ciphersuites");
            String protocols = idata.getVariable("securitydomain.jsse.protocols");
            String clientalias = idata.getVariable("securitydomain.jsse.client-alias");
            String serveralias = idata.getVariable("securitydomain.jsse.server-alias");
            String authtoken = idata.getVariable("securitydomain.jsse.authtoken");

            //We should pick a standard for checkboxes such that true alaways "true" false always "false"
            // We can accept empty values here, but not null ones


            if (ciphersuites != null || protocols != null || clientalias != null || serveralias != null || authtoken != null) {
                secDomJsseAttrs = new HashMap<String, String>();
                secDomJsseAttrs.put("cipher-suites", ciphersuites);
                secDomJsseAttrs.put("protocols", protocols);
                secDomJsseAttrs.put("client-alias", clientalias);
                secDomJsseAttrs.put("server-alias", serveralias);
                secDomJsseAttrs.put("service-auth-token", authtoken);
            }

            if (installKeystore) {
                secDomJsseKeystoreAttrs = new HashMap<String, String>();
                secDomJsseKeystoreAttrs.put("password", idata.getVariable("securitydomain.jsse.keystore.password"));
                secDomJsseKeystoreAttrs.put("provider", idata.getVariable("securitydomain.jsse.keystore.provider"));
                secDomJsseKeystoreAttrs.put("provider-argument", idata.getVariable("securitydomain.jsse.keystore.providerargument"));
                secDomJsseKeystoreAttrs.put("type", idata.getVariable("securitydomain.jsse.keystore.type"));
                secDomJsseKeystoreAttrs.put("url", idata.getVariable("securitydomain.jsse.keystore.url"));
            }

            if (installKeystoreManager) {
                secDomJsseKeystoreManagerAttrs = new HashMap<String, String>();
                secDomJsseKeystoreManagerAttrs.put("algorithm", idata.getVariable("securitydomain.jsse.keystoremanager.algorithm"));
                secDomJsseKeystoreManagerAttrs.put("provider", idata.getVariable("securitydomain.jsse.keystoremanager.provider"));
            }

            if (installTruststore) {
                secDomJsseTruststoreAttrs = new HashMap<String, String>();
                secDomJsseTruststoreAttrs.put("password", idata.getVariable("securitydomain.jsse.truststore.password"));
                secDomJsseTruststoreAttrs.put("provider", idata.getVariable("securitydomain.jsse.truststore.provider"));
                secDomJsseTruststoreAttrs.put("provider-argument", idata.getVariable("securitydomain.jsse.truststore.providerargument"));
                secDomJsseTruststoreAttrs.put("type", idata.getVariable("securitydomain.jsse.truststore.type"));
                secDomJsseTruststoreAttrs.put("url", idata.getVariable("securitydomain.jsse.truststore.url"));
            }

            if (installTruststoreManager) {
                secDomJsseTruststoreManagerAttrs = new HashMap<String, String>();
                secDomJsseTruststoreManagerAttrs.put("algorithm", idata.getVariable("securitydomain.jsse.truststoremanager.algorithm"));
                secDomJsseTruststoreManagerAttrs.put("provider", idata.getVariable("securitydomain.jsse.truststoremanager.provider"));
            }
        }
        return serverCommands.addSecurityDomain(secDomName, secDomCache, secDomAuthenCode, secDomAuthenFlag, secDomAuthenOpts, secDomAuthorCode, secDomAuthorFlag, secDomAuthorOpts, secDomMappingCode,
                secDomMappingType, secDomMappingOpts, secDomJsseAttrs, secDomJsseKeystoreAttrs, secDomJsseKeystoreManagerAttrs, secDomJsseTruststoreAttrs, secDomJsseTruststoreManagerAttrs,
                secDomJsseAdditionalProps);
    }
}

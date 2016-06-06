package com.redhat.installer.asconfiguration.vault.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.VariableSubstitutor;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommands;
import com.redhat.installer.asconfiguration.ascontroller.ServerManager;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import org.jboss.as.cli.CliInitializationException;

import java.util.NoSuchElementException;

/**
 * Process panel job responsible for vaulting keystore passwords passed in as block, attribute,pwd
 * triples in the arguments as 'pwd-args', then storing the resulting pairs of attributes, vaulted
 * passwords into iData and System properties.
 * Created by fcanas on 4/14/14.
 *
 * Args:
 * --username
 * --password
 * --pwd-args=block,attribute,password
 * ...
 */
public class PasswordVaulter {

    private static final java.lang.String USERNAME = "username";
    private static final java.lang.String PASSWORD = "password";
    private static final java.lang.String PWD_ARGS = "pwd-args";
    private static final java.lang.String DELIMITER = ",";
    private static final String VAULTED_ATTRIBUTE_PREFIX = "vaulted.";
    private static final String PROPS_VAULTED_ATTRIBUTE_PREFIX = "propsFile.vaulted.";
    private static final String PROPERTY_TEMPLATE = "${%s}";
    private static final String VAULT_PROPERTY_TEMPLATE = "${vault:%s}";


    /**
     * Entry point into PasswordVaulter. See above notes on arguments.
     * @param handler
     * @param args
     */
    public static void run(AbstractUIProcessHandler handler, String [] args) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        ArgumentParser parser = new ArgumentParser();
        parser.setListDelimiter('\uFFFF');
        parser.parse(args);
        String username, password;
        ServerCommands serverCommands;

        int port = ServerManager.getManagementPort();

        try {
            username = parser.getStringProperty(USERNAME);
            password = parser.getStringProperty(PASSWORD);
        } catch (NoSuchElementException e) {
            ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("PasswordVaulter.args.failure"), e.getMessage()), true);
            return;
        }


        try {
            serverCommands = ServerCommands.createSession(username, password.toCharArray(), port);
        } catch (CliInitializationException e) {
            e.printStackTrace();
            return;
        }

        try {
            vaultPasswords(idata, serverCommands, parser);
        } catch (NoSuchElementException e) {
            ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("PasswordVaulter.args.failure"), e.getMessage()), true);
            return;
        } catch (NullPointerException e) {
            ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("PasswordVaulter.args.triple.failure"), e.getMessage()), true);
            return;
        }
    }

    /**
     * Parses, substitutes, and then vaults each attribute,password pair passed in as a password-pair argument.
     * @param idata
     * @param sc
     * @param parser
     */
    private static void vaultPasswords(AutomatedInstallData idata, ServerCommands sc, ArgumentParser parser) {
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());

        for (String param : parser.getListProperty(PWD_ARGS)) {
            String [] pair = param.split(DELIMITER);
            String block = vs.substitute(pair[0]);
            String attribute = vs.substitute(pair[1]);
            String password = vs.substitute(pair[2]);

            String vaultedPassword = vaultPassword(sc, block, attribute, password);
            storeVaultedPassword(idata, attribute, vaultedPassword);
        }
    }

    /**
     * Calls the ServerCommand that masks the password.
     * @param sc ServerCommands
     * @param block The vault config block this attribute is in.
     * @param attribute The config attribute that holds the password.
     * @param password Plaintext password.
     * @return A masked password.
     */
    private static String vaultPassword(ServerCommands sc, String block, String attribute, String password) {
        String vaultedPassword = sc.maskPasswordPlain(block, attribute, password);

        return vaultedPassword;
    }

    /**
     * Stores the masked password and its attribute in iData and as system properties.
     * @param idata
     * @param attribute
     * @param vaultedPassword
     */
    private static void storeVaultedPassword(AutomatedInstallData idata, String attribute, String vaultedPassword) {
        String vaultedAttribute = VAULTED_ATTRIBUTE_PREFIX + attribute;
        String propsFileVaultedAttribute = PROPS_VAULTED_ATTRIBUTE_PREFIX + attribute;
        String propertyPassword = String.format(PROPERTY_TEMPLATE, vaultedPassword);
        String vaultedPropertyPassword = String.format(VAULT_PROPERTY_TEMPLATE, vaultedPassword);

        idata.setVariable(vaultedAttribute, propertyPassword);
        idata.setVariable(propsFileVaultedAttribute, vaultedPropertyPassword);
        System.setProperty(vaultedAttribute, propertyPassword);
        System.setProperty(propsFileVaultedAttribute, vaultedPropertyPassword);
    }
}


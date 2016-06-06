package com.redhat.installer.tests.installation.action;

import com.redhat.installer.installation.action.ShortcutHelper;
import com.redhat.installer.framework.testers.PanelActionTester;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Class performs simple math, make sure it's correct
 * Created by thauser on 9/3/14.
 */
public class ShortcutHelperTest extends PanelActionTester{
    private final String STANDALONE_MANAGEMENT_NATIVE = "standalone.management-native-with.offset";
    private final String STANDALONE_MANAGEMENT_CONSOLE_HTTP = "standalone.management-http-with.offset";
    private final String STANDALONE_MANAGEMENT_CONSOLE_HTTPS = "standalone.management-https-with.offset";

    private final String SHORTCUT_STANDALONE_MANAGEMENT_NATIVE_PORT = "standalone.management.native.port";
    private final String SHORTCUT_STANDALONE_MANAGEMENT_CONSOLE_PORT = "standalone.management.console.port";

    private final String DOMAIN_MANAGEMENT_NATIVE = "domain.management-native";
    private final String DOMAIN_CONSOLE_HTTP = "domain.management-http";
    private final String DOMAIN_CONSOLE_HTTPS = "domain.management-https";

    private final String SHORTCUT_DOMAIN_MANAGEMENT_CONSOLE_PORT = "domain.management.console.port";
    private final String SHORTCUT_DOMAIN_MANAGEMENT_NATIVE_PORT = "domain.management.native.port";

    private final String SSL_ENABLED = "installSsl";


    @Before
    public void before(){
        panelAction = new ShortcutHelper();
        idata.setVariable(STANDALONE_MANAGEMENT_NATIVE,"${test:9999}");
        idata.setVariable(STANDALONE_MANAGEMENT_CONSOLE_HTTP,"${test:9990}");
        idata.setVariable(STANDALONE_MANAGEMENT_CONSOLE_HTTPS, "${test:9443}");
        idata.setVariable(DOMAIN_CONSOLE_HTTP, "${test:9000}");
        idata.setVariable(DOMAIN_CONSOLE_HTTPS, "${test:9500}");
        idata.setVariable(DOMAIN_MANAGEMENT_NATIVE, "${test:1000}");
    }

    @Test
    public void testNoSSL(){
        idata.setVariable(SSL_ENABLED, "false");
        panelAction.executeAction(idata, handler);
        assertEquals("9999", idata.getVariable(SHORTCUT_STANDALONE_MANAGEMENT_NATIVE_PORT));
        assertEquals("9990", idata.getVariable(SHORTCUT_STANDALONE_MANAGEMENT_CONSOLE_PORT));
        assertEquals("9000", idata.getVariable(SHORTCUT_DOMAIN_MANAGEMENT_CONSOLE_PORT));
        assertEquals("1000", idata.getVariable(SHORTCUT_DOMAIN_MANAGEMENT_NATIVE_PORT));
    }

    @Test
    public void testWithSSL(){
        idata.setVariable(SSL_ENABLED, "true");
        panelAction.executeAction(idata, handler);
        assertEquals("9999", idata.getVariable(SHORTCUT_STANDALONE_MANAGEMENT_NATIVE_PORT));
        assertEquals("9443", idata.getVariable(SHORTCUT_STANDALONE_MANAGEMENT_CONSOLE_PORT));
        assertEquals("9500", idata.getVariable(SHORTCUT_DOMAIN_MANAGEMENT_CONSOLE_PORT));
        assertEquals("1000", idata.getVariable(SHORTCUT_DOMAIN_MANAGEMENT_NATIVE_PORT));
    }



}

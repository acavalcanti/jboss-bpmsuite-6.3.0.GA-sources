package com.redhat.installer.postinstall.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerManager;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Responsible for launching the browser. Only entry point is the run method from
 * a ProcessPanelSpec.xml job. Takes no arguments.
 * Created by fcanas on 3/25/14. Based on LaunchBrowser private class formerly found in ProcessPanelHelper.
 */
public class BrowserLauncher {
    private static AbstractUIProcessHandler mHandler;

    public static void run(AbstractUIProcessHandler handler, String [] args) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        mHandler = handler;

        String browserLaunch = idata.langpack
                .getString("postinstall.processpanel.browserLaunch");
        String browserLaunchAbort = idata.langpack
                .getString("postinstall.processpanel.browserLaunchAbort");

        /**
         * Launch browser job goes here.
         */
        String link = ServerManager.getURL() + ServerManager.getManagementConsolePort();
        if (ServerManager.isServerUp()) {
            handler.logOutput(browserLaunch, false);
            LaunchBrowser br = new LaunchBrowser();
            br.exec(link);
        } else {
            handler.logOutput(browserLaunchAbort, false);
        }
    }

    private static class LaunchBrowser {
        // Entry Point
        private void exec(final String url) {
            try {
                openURI(new URI(url));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        private void openURI(URI uri) {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(uri);
                        mHandler.logOutput("Browser Launched", false);
                    } catch (IOException e) {
                        if (openFallbackFailed(uri)) {
                            mHandler.logOutput("Cannot Open Browser! - BROWSE action does not support the URI", true);
                        }
                    }
                } else {
                    if (openFallbackFailed(uri)) {
                        mHandler.logOutput("Cannot Open Browser! - BROWSE action not supported", true);
                    }
                }
            } else {
                if (openFallbackFailed(uri)) {
                    mHandler.logOutput("Cannot Open Browser! - Desktop not supported", true);
                }
            }
        }

        private static boolean openFallbackFailed(URI uri) {
            // Attempt to use non-portable ways of launching the URI.
            // Return true on success, otherwise false.

            String os       = System.getProperty("os.name");
            Runtime runtime = Runtime.getRuntime();
            String url      = uri.toString();

            try {
                // Windows
                if (os.startsWith("Windows")) {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                    mHandler.logOutput("Browser Launched via fallback Windows-specific call", false);
                    // Mac OS
                } else if (os.startsWith("Mac OS")) {
                    Class.forName("com.apple.eio.FileManager")
                    .getDeclaredMethod("openURL", new Class[]{String.class})
                    .invoke(null, url);
                    mHandler.logOutput("Browser Launched via fallback Mac OS-specific call", false);
                    // Otherwise Assume [LU]nix
                } else {
                    String[] browsers = {"firefox", "opera", "konqueror",
                            "epiphany", "mozilla", "netscape",
                            "conkeror", "midori", "kazehakase"};
                    String browser    = null;

                    for (String b : browsers) {
                        if (browser == null &&
                                Runtime.getRuntime().exec(new String[]{"which", b})
                                .getInputStream().read() != -1) {

                            runtime.exec(new String[] {browser = b, url});
                            mHandler.logOutput("Browser Launched via fallback Linux/Unix call", false);
                        }
                    }
                    if (browser == null) {
                        // No browser found
                        // Last-ditch attempt with xdg-open
                        runtime.exec(new String[] {"xdg-open", url});
                        mHandler.logOutput("Browser Launched via fallback xdg-open call", false);
                    }
                }
            } catch(Exception e) {
                mHandler.logOutput("Cannot Open Browser! - Fallback Failed", true);
                return true;
            }
            return false;
        }
    }
}

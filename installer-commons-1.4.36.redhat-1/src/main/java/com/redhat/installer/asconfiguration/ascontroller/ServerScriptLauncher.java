package com.redhat.installer.asconfiguration.ascontroller;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.Debug;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;

import java.io.*;

/**
 * Created by fcanas on 3/25/14.
 */
public class ServerScriptLauncher {
    private static final int SPLASH_SCREEN_LENGTH = 11;
    private ProcessBuilder builder;
    private Process p;
    private BufferedReader reader;
    private BufferedWriter writer;
    private static AbstractUIProcessHandler mHandler;
    private AutomatedInstallData idata;

    ServerScriptLauncher(String... command) {
        builder = new ProcessBuilder(command);
        ProcessPanelHelper.adjustJbossHome(builder);
        mHandler = null;
    }

    /**
     * This one takes an explicit handler for output.
     *
     * @param handler
     * @param command
     */
    ServerScriptLauncher(AbstractUIProcessHandler handler, String... command) {
        idata = AutomatedInstallData.getInstance();
        builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        ProcessPanelHelper.adjustJbossHome(builder);
        ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("ProcessPanelHelper.jbosshome.adjusted"), builder.environment().get("JBOSS_HOME")), false);
        mHandler = handler;
    }

    /**
     * Used by the ServerScriptLauncher class to detect starting and error codes
     * from the server's output when starting.
     *
     * @param line A line from the server's start output
     * @return The specific code detected, or empty string if none.
     */
    public static String extractCode(String line) {
        String code = "";
        if (line.contains(ServerManager.CODE_START_OK)) {
            code = ServerManager.CODE_START_OK;
        } else if (line.contains(ServerManager.CODE_START_ERROR)) {
            code = ServerManager.CODE_START_ERROR;
        } else if (line.contains(ServerManager.CODE_START_OK_FULL_HA)
                && ServerManager.getConfig().equals(ServerManager.FULL_HA)) {
            code = ServerManager.CODE_START_OK_FULL_HA;
        }
        return code;
    }

    /**
     * Used by the ServerScriptLauncher class to detect if the server started with SSL enabled
     *
     * @param line A line from the server's start output
     * @return The specific code detected, or empty string if none.
     */
    public static String extractSSLCode(String line) {
        String code = "";
        if (line.contains(ServerManager.CODE_SSL_SERVER)) {
            code = ServerManager.CODE_SSL_SERVER;
        }
        return code;
    }

    /**
     * Used to detect the server's shutdown code.
     *
     * @param line
     * @return the shutdown code detected, or empty string.
     */
    public static String extractEndCode(String line) {
        String code = "";
        if (line.contains(ServerManager.CODE_STOP_OK)) {
            code = ServerManager.CODE_STOP_OK;
        }
        return code;
    }

    @SuppressWarnings("deprecation")
    public void runScript() {
        try {
            p = builder.start();
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            writer.newLine();
            writer.flush();
            Thread b = new Thread(new Runnable() {
                public void run() {
                    try {
                        boolean foundCode = false;
                        boolean serverNotReady = true;
                        String line;
                        int counter = 0;

                        while ((line = reader.readLine()) != null) {
                            // Check for readiness codes here
                            String code = extractCode(line);

                            if (!code.isEmpty() && !foundCode) {
                                ServerManager.setServerCode(code);
                                ProcessPanelHelper.printToPanel(mHandler, String.format(
                                        idata.langpack.getString("ProcessPanelHelper.servercode.detected"), code), false);
                                serverNotReady = false;
                                foundCode = true;
                            }

                            String sslCode = extractSSLCode(line);

                            if (!sslCode.isEmpty()) {
                                ServerManager.setSslEnabled(true);
                                ServerManager.setURL(ServerManager.DEFAULT_SSL_SERVER_URL);
                            }

                            String endCode = extractEndCode(line);

                            if (!endCode.isEmpty()) {
                                ServerManager.setServerDown();
                                serverNotReady = false;
                            }

                            if (line.lastIndexOf('\033') > -1) {
                                line = line.substring(
                                        line.lastIndexOf('\033'),
                                        line.length());
                                line = line.substring(
                                        line.indexOf('m') + 1,
                                        line.length());
                            }

                            /**
                             * Print to GUI/Console only until server is
                             * ready, then stop outputting but continue
                             * swallowing.
                             **/
                            if (serverNotReady) {
                                    if (counter > SPLASH_SCREEN_LENGTH && line.isEmpty()) {
                                      // do nothing
                                        continue;
                                    } else {
                                        ProcessPanelHelper.printToPanel(mHandler, line, false);
                                    }
                            }
                            counter++;
                        }
                    } catch (IOException e) {
                        Debug.log(e.getMessage());
                    }
                }
            });
            b.setDaemon(true);
            b.start();
            b.join(5000);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

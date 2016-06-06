package com.redhat.installer.installation.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.OsVersion;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * @author dcheung@redhat.com, dmondega@redhat.com, thauser@redhat.com,
 *         aszczucz@redhat.com, jtripath@redhat.com
 */
public class ProcessPanelHelper {
    private static final String UNZIP_NATIVES = "unzip-natives";

    private static AbstractUIProcessHandler mHandler;


    public static void run(AbstractUIProcessHandler handler, String[] args) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        mHandler = handler;

        ArgumentParser parser = new ArgumentParser();
        parser.parse(args);


        if (parser.hasProperty(UNZIP_NATIVES)) {
            final String installPath = idata.getInstallPath();
            final File installPathFile = new File(installPath);
            final String nativesUnzipPath = installPath + "/" + idata.getVariable("eap.native.parent.path");
            final File nativesUnzipFile = new File(nativesUnzipPath);

            // Check for a native unzip utility (available on *nix usually)
            boolean hasUnzip;
            try {
                Runtime.getRuntime().exec("unzip");
                hasUnzip = true;
            } catch (Exception e) {
                hasUnzip = false;
            }

            // Check for link utility (available on *nix almost always)
            boolean hasLink;
            try {
                Runtime.getRuntime().exec("ln");
                hasLink = true;
            } catch (Exception e) {
                hasLink = false;
            }

            // Check for native CP utility (available on *nix almost universally)
            boolean hasCp;
            try {
                Runtime.getRuntime().exec("cp");
                hasCp = true;
            } catch (Exception e) {
                hasCp = false;
            }

            // Filter for files with a .zip extension
            FilenameFilter zipFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".zip");
                }
            };

            // For saving the combined lines of the SHA256SUM files from multiple zips.
            List<String> sha256sums = new ArrayList<String>();

            // Process zips in izpack install path
            File[] list = new File(installPath).listFiles(zipFilter);
            for (File sourceZipPath : list) {

                // Use unzip if available, except on Solaris, as its unzip is buggy
                if (hasUnzip && !OsVersion.IS_SUNOS && !OsVersion.IS_WINDOWS) {
                    try {
                        Process p = new ProcessBuilder("unzip", "-o", sourceZipPath.toString(), "-d", installPath).start();
                        if (p.waitFor() != 0) {
                            throw new RuntimeException("Native unzip utility exited abnormally for " + sourceZipPath);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Couldn't exec native unzip utility", e);
                    } catch (InterruptedException e) {
                        return;
                    }

                    if (!sourceZipPath.delete()) {
                        System.err.println("Couldn't delete temporary zip " + sourceZipPath);
                    }

                    sha256sums.addAll(getSHA256sums());

                    // Use Java's unzip functionality, plus symlink processing
                } else {
                    ZipFile zipFile = null;
                    try {
                        zipFile = new ZipFile(sourceZipPath);

                        // Extract all files
                        Enumeration<? extends ZipEntry> entries = zipFile.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();

                            // Create directory tree leading to the destination path
                            File destinationPath = new File(installPath, entry.getName());
                            destinationPath.getParentFile().mkdirs();

                            // Entry is a directory
                            if (entry.isDirectory()) {
                                // This is required to handle any empty directories in the zip
                                // Create the last directory in destinationPath
                                destinationPath.mkdir();

                                // Entry is an actual file
                            } else {
                                // Copy entire stream for the entry to disk
                                FileOutputStream fos = null;
                                FileChannel outputChannel = null;
                                try {
                                    fos = new FileOutputStream(destinationPath);
                                    outputChannel = fos.getChannel();

                                    ReadableByteChannel inputChannel = Channels.newChannel(zipFile.getInputStream(entry));

                                    outputChannel.transferFrom(inputChannel, 0L, Long.MAX_VALUE);
                                } catch (IOException e) {
                                    throw new RuntimeException("Can't write destination file " + destinationPath, e);
                                } finally {
                                    if (fos != null) {
                                        try {
                                            fos.close();
                                        } catch (IOException e) {
                                        }
                                    }
                                    if (outputChannel != null) {
                                        try {
                                            outputChannel.close();
                                        } catch (IOException e) {
                                        }
                                    }
                                    // Closing the zip entry's input stream will close the whole zip file, so don't close it here.
                                }

                                // Process symlinks if applicable (Method is STORED, and contents are a single line, and are a path)
                                if (hasLink && entry.getMethod() == ZipEntry.STORED) {
                                    // Get just one line of text in the file, and check there is only one line of text in the file.
                                    BufferedReader reader = null;
                                    String firstLine;
                                    String secondLine;
                                    try {
                                        reader = new BufferedReader(new FileReader(destinationPath));
                                        firstLine = reader.readLine();
                                        secondLine = reader.readLine();
                                    } catch (FileNotFoundException e) {
                                        throw new RuntimeException("File written from zip does not exist " + destinationPath, e);
                                    } catch (IOException e) {
                                        throw new RuntimeException("File written from zip cannot be read " + destinationPath, e);
                                    } finally {
                                        if (reader != null) {
                                            try {
                                                reader.close();
                                            } catch (IOException e) {
                                            }
                                        }
                                    }

                                    // Check if there is only one line
                                    if (secondLine == null) {
                                        // Check if the first line is a valid path
                                        File symlinkTarget = new File(firstLine);
                                        boolean validPath;
                                        try {
                                            symlinkTarget.getCanonicalPath();
                                            validPath = true;
                                        } catch (IOException e) {
                                            validPath = false;
                                        }

                                        if (validPath) {
                                            // Create a symlink to replace the one-line file
                                            try {
                                                Process p = new ProcessBuilder("ln", "-s", "-f", symlinkTarget.toString(), destinationPath.toString()).start();
                                                if (p.waitFor() != 0) {
                                                    throw new RuntimeException("Link utility exited abnormally when converting " + destinationPath);
                                                }
                                            } catch (IOException e) {
                                                throw new RuntimeException("Couldn't exec link utility", e);
                                            } catch (InterruptedException e) {
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        sha256sums.addAll(getSHA256sums());
                    } catch (IOException e) {
                        throw new RuntimeException("Can't extract zip file " + sourceZipPath, e);
                    } finally {
                        try {
                            if (zipFile != null) {
                                zipFile.close();
                            }
                        } catch (IOException e) {
                        }
                        sourceZipPath.delete();
                    }
                }
            }
            if (!sha256sums.isEmpty()) {
                writeSHA256sums(sha256sums);
            }
            // we're done. with the new changes to SUBPATH, we need to move all contents into the INSTALL_PATH
            // we also have to use the native 'cp' function on *nix machines, since java <= 6 cannot deal with symlinks.
            // if no cp utility exists on the target machine, we are kind of stuck.
            try {
                File[] nativeFiles = nativesUnzipFile.listFiles();
                if (nativeFiles != null) {
                    for (File f : nativeFiles) {
                        if (f.isDirectory()) {
                            if (!OsVersion.IS_WINDOWS && hasCp) {
                                Process cp = new ProcessBuilder("cp", "-rP", f.getCanonicalPath(), installPathFile.getCanonicalPath()).start();
                                cp.waitFor();
                                ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("CommandRunner.success") + " cp -rP " + f.getCanonicalPath() + " " + installPathFile.getCanonicalPath(), false);
                            } else {
                                FileUtils.copyDirectoryToDirectory(f, installPathFile);
                            }
                        } else {
                            // This should typically just be the amalgamated SHA256SUM
                            if (!OsVersion.IS_WINDOWS && hasCp) {
                                Process cp = new ProcessBuilder("cp", f.getCanonicalPath(), installPathFile.getCanonicalPath()).start();
                                cp.waitFor();
                                ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("CommandRunner.success") + " cp " + f.getCanonicalPath() + " " + installPathFile.getCanonicalPath(), false);
                            } else {
                                FileUtils.copyFileToDirectory(f, installPathFile, false);
                            }
                        }
                    }
                    // after the move, nuke the nativesUnzipFile path.
                    FileUtils.deleteDirectory(nativesUnzipFile);
                }
            } catch (IOException ioe) {
                throw new RuntimeException("Can't move natives from: " + nativesUnzipFile.getAbsolutePath() + " to appropriate location " + installPathFile.getAbsolutePath(), ioe);
            } catch (InterruptedException e) {
                throw new RuntimeException("Copy process failed:", e);
            }
            handler.logOutput(idata.langpack.getString("postinstall.processpanel.unpacking.complete"), false);
        }
    }


    /**
     * Helper method that writes all of the gathered SHA256SUMs into a single file. Delimited by a simple line feed
     *
     * @param sums
     * @throws Exception
     */
    private static void writeSHA256sums(List<String> sums) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();

        File sumFile = new File(idata.getInstallPath() + "/" + idata.getVariable("eap.native.parent.path") + "/SHA256SUM");

        // Current file is that of the latest zip, we need to replace it
        sumFile.delete();

        BufferedWriter br = null;
        try {
            br = new BufferedWriter(new FileWriter(sumFile));
            for (String sum : sums) {
                // Newline is constant (not decided by OS) so as to pass QE file diff checks
                br.write(sum + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot write master zip SHA256SUM file to " + sumFile, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Helper method used during native unzipping to amalgamate the SHA256SUM files that the zips contain.
     * If this is not done, the zips will overwrite eachother, and we'll be left with only the file contents of the
     * one that wins that race.
     *
     * @return List of SHA256SUM file lines
     */
    private static List<String> getSHA256sums() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();

        File sumFile = new File(idata.getInstallPath() + "/" + idata.getVariable("eap.native.parent.path") + "/SHA256SUM");

        List<String> retval = new ArrayList<String>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(sumFile));
            String line;
            while ((line = br.readLine()) != null) {
                retval.add(line);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }

        return retval;
    }


    /**
     * This method simply sets the given ProcessBuilder's environment variable JBOSS_HOME to the current installation. This resolves
     * https://bugzilla.redhat.com/show_bug.cgi?id=1017054
     *
     * @param builder
     */

    // TODO: is there a better place for this method?
    public static void adjustJbossHome(ProcessBuilder builder) {
        Map<String, String> env = builder.environment();
        //env.put("JBOSS_HOME", AutomatedInstallData.getInstance().getInstallPath()+File.separator+AutomatedInstallData.getInstance().getVariable("INSTALL_SUBPATH"));
        String installPath = AutomatedInstallData.getInstance().getInstallPath();
        if (installPath.endsWith(File.separator)) {
            // remove file separator because it mucks with EAP somehow
            installPath = installPath.substring(0, installPath.length() - 1);
        }
        env.put("JBOSS_HOME", installPath);
    }

    /**
     * Helper method to print text to the panel. This method will log the output to the logfile in an appropriate manner.
     *
     * @param handler
     * @param message
     */

    public static void printToPanel(AbstractUIProcessHandler handler, String message, boolean error) {
        BufferedWriter log;
        String ignoreMsg = "Press any key to continue";
        try {
            log = getLogWriter();
            if (!message.contains(ignoreMsg)) {
                handler.logOutput(message, error);
            }

            log.write(message);
            log.newLine();
            log.flush();
            log.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void printToLog(String message) {
        BufferedWriter log = getLogWriter();
        try {
            log.write(message);
            log.newLine();
            log.flush();
            log.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printExceptionToLog(StackTraceElement[] ex){
        BufferedWriter log = getLogWriter();
        try {
            for (StackTraceElement e : ex){
                log.write(e.toString());
                log.newLine();
            }
            log.flush();
            log.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static BufferedWriter getLogWriter() {
        BufferedWriter log = null;
        try {
            AutomatedInstallData idata = AutomatedInstallData.getInstance();
            log = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(idata.getInstallPath() + File.separator + idata.getVariable("installation.logfile"), true)));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return log;
    }

}

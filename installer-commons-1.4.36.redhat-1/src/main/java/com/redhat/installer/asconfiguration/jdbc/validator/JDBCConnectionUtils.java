package com.redhat.installer.asconfiguration.jdbc.validator;

import com.izforge.izpack.installer.AutomatedInstallData;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.sql.Connection;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * This validator will attempt to use the given JDBC driver jar to connect to the database the user has specified.
 * If the jar containing the remote class is a remote path, it will simply return true and trust the user.
 *
 * @author thauser
 */
public class JDBCConnectionUtils {

    // Excluded URI digits
    private static final String excluded = "<>\"{}|\\^`";
    // convenient hex digits String
    private static final String HEX_DIGITS = "0123456789abcdef";

    // these are guaranteed to exist by http://docs.oracle.com/javase/7/docs/api/java/net/URL.html
    private static List<String> supportedProtocols = new ArrayList<String>();

    static {
        supportedProtocols.add("http");
        supportedProtocols.add("https");
        supportedProtocols.add("ftp");
        supportedProtocols.add("file");
        supportedProtocols.add("jar");
    }

    /**
     * This method actually requires an array of String[]s, it casts the members of the parameter array
     * into String anyway.
     * TODO: refactor / redesign this to expect an array of Strings instead of Objects
     *
     * @param jars
     * @return
     */
    public static URL[] convertToUrlArray(Object[] jars) {
        URL[] jarUrls = new URL[jars.length];
        for (int i = 0; i < jarUrls.length; i++) {
            try {
                String jar = (String) jars[i];

                jar = escapeDisallowedChars(jar);

                URI jarUri = new URI(jar);

                if (supportedProtocols.contains(jarUri.getScheme())) {
                    jarUrls[i] = new URI(jar).toURL();
                } else {
                    // unsupported protocol; prepend "file:///" and retry, since the only logical choice here is to be a local file.
                    // (user input either A:\some\dir or /some/unix/dir), since scheme is null
                    jarUrls[i] = new URI("file:///" + jar).toURL();
                }
                //System.out.println("URL added:" + jarUrls[i].toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return jarUrls;
    }

    /**
     * Attempts to return a database connection. If a String is returned, the attempted connection
     * has failed and the String is the error; If the return is a java.sql.Connection, the connection was successful.
     *
     * @param driver   Driver instance to connect with
     * @param username username for the connection
     * @param password password for the connection
     * @param url      connection url
     * @return
     */
    public static Object getDatabaseConnection(Driver driver, String username, String password, String url) {
        // we got here, so it was successful.
        Properties dbInfo = new Properties();
        dbInfo.put("user", username);
        dbInfo.put("password", password);
        Connection conn = null;

        try {
            conn = driver.connect(url, dbInfo);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            String error =
                    AutomatedInstallData.getInstance().langpack.getString("JBossJDBCDriverSetupPanel.connection.error")
                            + "\n"
                            + e;
            //e.getClass().getName());
            return error;
        }
        //java.lang.UnsupportedOperationException:
        //SQLException
        return conn;
    }

    /**
     * Attempts to read the idata for paths added to idata through an IzPack DynamicComponentsPanel.serialize(prefix) call.
     * Useful for any class that needs to get the list of jars after they are added to idata.
     *
     * @return
     */
    public static List<String> readIdataForJarPaths(String variablePrefix) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        List<String> jarPathsList = new ArrayList<String>();
        String dashedVariablePrefix = variablePrefix + "-";
        String variableSuffix = "-path";
        int counter = 1; // start at 1 because of DynamicComponentsPanel
        while (true) {
            String path = idata.getVariable(dashedVariablePrefix + counter + variableSuffix);
            if (path == null) {
                break;
            }
            jarPathsList.add(path);
            counter++;
        }
        return jarPathsList;
    }


    /**
     * Returns an int based upon the result of various checks:<br/>
     * 0 : all is well <br/>
     * 1 : given path doesn't exist / is a directory<br/>
     * 2 : given path is not a zip<br/>
     * 3 : given path is an empty zip<br/>
     * 4 : given remote path is not accessible by the installer<br/>
     * 5 : Exception
     *
     * @param jar
     * @return
     */
    public static int verifyJarPath(String jar) {
        if (jar.equals("http://") || jar.equals("ftp://")) {
            return 4;
        } else if (jar.startsWith("http://")) {
            try {
                HttpURLConnection.setFollowRedirects(true);
                HttpURLConnection connection = (HttpURLConnection) new URL(jar).openConnection();
                connection.setRequestMethod("HEAD");
                int resp = connection.getResponseCode();
                if (resp != HttpURLConnection.HTTP_OK) {
                    return 4;
                }
            } catch (Exception e) {

                return 4;
            }
        } else if (jar.startsWith("ftp://")) {
            try {
                URL url = new URL(jar);
                InputStream check = url.openStream();
            } catch (Exception ex) {
                return 4;
            }
        } else {
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(jar);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                // we reached here and no entries? not a zip file.
                if (!entries.hasMoreElements()) {
                    // close the file
                    try {
                        zipFile.close();
                    } catch (IOException ioe) {
                        // more catastrophe
                        ioe.printStackTrace();
                    }
                    return 3;
                }
            } catch (FileNotFoundException fnf) {
                // file doesn't exist.
                return 1;
            } catch (ZipException ze) {
                // file is not a zip
                return 2;
            } catch (IOException ioe) {
                // catastrophe
                ioe.printStackTrace();
                return 5;
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return 0;
    }


    /**
     * Finds a class name in a given set of urls, which can contain local or remote jars<br/>
     * returns null if the class is not found, or the Class object which was loaded.
     * TODO: enhance entire class to be able to use JDBC 4 compliant jars to load the class, which would
     * detach us from needing to explicitly match classnames.
     */
    public static Class<?> findDriverClass(String driverClassName, URL... jarUrls) {

        if (driverClassName == null) return null;
        URLClassLoader loader = null;
        Class<?> driverClass = null;

        try {
            // closing this causes CNFE's. May have to look into how to close a CL properly
            loader = URLClassLoader.newInstance(jarUrls);
            driverClass = loader.loadClass(driverClassName);
            //loader.close();
        } catch (NoClassDefFoundError ncdfe) {
            // jar corruption? patchwork jar?
        } catch (ClassNotFoundException cnfe) {
            //cnfe.printStackTrace();
        } catch (SecurityException se) {
            //exception.printStackTrace
        } catch (Exception e) {
            e.printStackTrace();
        }
        return driverClass;
    }

    private static String utf8 = "UTF-8";


    public static String escapeDisallowedChars(String s) {
        StringBuffer buf = null;
        int len = s.length();
        int done = 0;
        for (; ; ) {
            int i = done;
            for (; ; ) {
                if (i == len) {
                    if (done == 0)
                        return s;
                    break;
                }
                if (isExcluded(s.charAt(i)))
                    break;
                i++;
            }
            if (buf == null)
                buf = new StringBuffer();
            if (i > done) {
                buf.append(s.substring(done, i));
                done = i;
            }
            if (i == len)
                break;
            for (i++; i < len && isExcluded(s.charAt(i)); i++)
                ;
            String tem = s.substring(done, i);
            byte[] bytes;
            try {
                bytes = tem.getBytes(utf8);
            } catch (UnsupportedEncodingException e) {
                utf8 = "UTF8";
                try {
                    bytes = tem.getBytes(utf8);
                } catch (UnsupportedEncodingException e2) {
                    // Give up
                    return s;
                }
            }
            for (int j = 0; j < bytes.length; j++) {
                // for UTF-8, chars are expressed with 8 bytes. Using the bit-shifts below, we can find the appropriate character
                // for the two 4-byte parts of the character code
                buf.append('%');
                buf.append(HEX_DIGITS.charAt((bytes[j] & 0xFF) >> 4));
                buf.append(HEX_DIGITS.charAt(bytes[j] & 0xF));
            }
            done = i;
        }
        return buf.toString();
    }

    private static boolean isExcluded(char c) {
        // if the character isn't part of the special "exclusions" array, or isn't in the printable ASCII range
        return c <= 0x20 || c >= 0x7F || excluded.indexOf(c) >= 0;
    }
}

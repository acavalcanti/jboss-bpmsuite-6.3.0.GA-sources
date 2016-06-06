package com.redhat.installer.layering.util;

/**
 * Created by aabulawi on 10/03/15.
 */
public class PlatformUtil {

    public static boolean isWindows(){
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

}

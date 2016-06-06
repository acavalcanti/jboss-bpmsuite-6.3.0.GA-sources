package com.redhat.installer.ports.utils;

/**
 * Created by thauser on 3/10/15.
 */
public class PortUtils {

    /**
     * Gets integer port value from a String.
     * @param propertyAndPort a String that could be in the form "9999" OR "${default.value:9999}
     * @return
     */
    public static int getPort(String propertyAndPort){
        int portIntValue = 0;
        if (propertyAndPort.startsWith("${") && propertyAndPort.contains(":") && propertyAndPort.endsWith("}")) {
            portIntValue = Integer.parseInt(propertyAndPort.substring(propertyAndPort.indexOf(':')+1,propertyAndPort.indexOf('}')));
        } else {
            portIntValue = Integer.parseInt(propertyAndPort);
        }
        return portIntValue;
    }
}

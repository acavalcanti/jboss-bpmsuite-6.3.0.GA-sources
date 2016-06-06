package com.redhat.installer.ports.utils;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.Debug;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

/**
 * Created by eunderhi on 24/06/15.
 * Utility class to read in ports from each
 * server configuration xml
 */
public class PortReader {

    public static int getManagementPort(String path) {
        Document document;

        try {
            document = loadXMLFile(path);
        } catch (IOException e) {
            Debug.trace(e);
            return 0;
        }
        if (path.contains("domain")) {
            return getDomainManagementPort(document);
        }

        return getStandaloneManagementPort(document);
    }

    private static Document loadXMLFile(String path) throws IOException {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        File file = new File(idata.getInstallPath(), path);
        Document document = Jsoup.parse(file, null);
        return document;
    }

    /**
     * Standalone and domain xml files are parsed differently
     * because for some reason port offsets are applied
     * directly to port numbers for domain server instead of
     * the offset being a separate value.
     */
    private static int getStandaloneManagementPort(Document document) {
        int port;
        int offset;
        Element offsetElement = document.select("socket-binding-group").get(0);
        String offsetStr = offsetElement.attr("port-offset");
        offset = PortUtils.getPort(offsetStr);

        Elements portElements = document.select("socket-binding");
        String portStr = null;
        for (Element element : portElements) {
            if (element.hasAttr("port")) {
                portStr = element.attr("port");
                break;
            }
        }
        port = PortUtils.getPort(portStr);
        return offset + port;
    }

    private static int getDomainManagementPort(Document document) {
        Elements managementElements = document.select("native-interface");
        for (Element element : managementElements){
            for (Element child : element.getElementsByTag("socket")){
                if (child.hasAttr("port")) {
                    return PortUtils.getPort(child.attr("port"));
                }
            }
        }
        return 9999;
    }
}

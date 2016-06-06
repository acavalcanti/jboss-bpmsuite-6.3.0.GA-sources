package org.jboss.brmsbpmsuite.patching.integrationtests.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StreamGobbler extends Thread {

    private static Logger logger = LoggerFactory.getLogger(StreamGobbler.class);

    private final InputStream is;
    private final String type;

    public StreamGobbler(final InputStream is, final String type) {
        this.is = is;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            final InputStreamReader isr = new InputStreamReader(is);
            final BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                logger.info(type + "> " + line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

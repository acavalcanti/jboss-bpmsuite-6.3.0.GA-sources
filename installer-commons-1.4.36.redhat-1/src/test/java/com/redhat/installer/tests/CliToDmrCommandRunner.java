package com.redhat.installer.tests;

import org.jboss.as.cli.*;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Base class for testing the ASManagementClient
 * Created by fcanas on 3/13/14.
 */
public class CliToDmrCommandRunner {
    static final String VIEW_STANDALONE_PROPERTIES = "/core-service=platform-mbean/type=runtime:read-attribute(name=system-properties)";
    static CommandContext ctx;
    ModelControllerClient client;

    @BeforeClass
    public static void initClient() {

        try {
            ctx = CommandContextFactory.getInstance().newCommandContext();
        } catch(CliInitializationException e) {
            throw new IllegalStateException("Failed to initialize CLI context", e);
        }
    }

    @Before
    public void connect() {
        try {
            // connect to the server ascontroller
            ctx.connectController();

            // execute commands and operations
            //ctx.handle(":take-snapshot");
            //ctx.handle("deploy myapp.ear");
        } catch (CommandLineException e) {
            // the operation or the command has failed
        }
    }

    @After
    public void disconnect() {
        // terminate the session and
        // close the connection to the ascontroller
        ctx.terminateSession();
    }

    @Test
    public void test() {
        ModelNode deployRequest;
        try {
            deployRequest = ctx.buildRequest(VIEW_STANDALONE_PROPERTIES);

            ModelControllerClient client = ctx.getModelControllerClient();
            if(client != null) {
                    deployRequest = client.execute(deployRequest);
                    System.out.println(deployRequest.toJSONString(false));
            } else {
                // the client is not available, meaning the connection to the ascontroller
                // has not been established, which means ctx.connectController(...)
                // or ctx.handle("connect") haven't been executed before
            }
        } catch (CommandFormatException e) {
            // there was a problem building a DMR request
        } catch (IOException e) {
        // client failed to execute the request
        }
    }
}

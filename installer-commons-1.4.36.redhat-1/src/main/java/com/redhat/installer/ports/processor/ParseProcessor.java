package com.redhat.installer.ports.processor;

import java.util.Arrays;
import java.util.List;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Processor;

public class ParseProcessor implements Processor
{

    public String process(ProcessingClient client)
    {
        StringBuilder tempFormat = new StringBuilder("${");
        if ((client.getNumFields() == 2) && (client.getFieldContents(0).equals("") == false)) {
            tempFormat.append(client.getFieldContents(0));
            tempFormat.append(":");
            tempFormat.append(client.getFieldContents(1));
            tempFormat.append("}");
            return tempFormat.toString();
        } else {
            return client.getFieldContents(1);
        }
    }
}

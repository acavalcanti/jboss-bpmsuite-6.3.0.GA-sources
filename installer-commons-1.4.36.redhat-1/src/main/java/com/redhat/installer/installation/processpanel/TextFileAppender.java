package com.redhat.installer.installation.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Appends text onto the end of a given file. the text appended is contained
 * within the ProcessPanel.spec.xml. If no --text is supplied, every argument from args[1] onward is considered
 * a line of text that will be appended onto the file in succession.
 *
 * @author thauser
 *
 */
public class TextFileAppender {
	private static final String FILE = "file";
	private static final String TEXT = "text";
    private static final String IF = "||IF||";
    private static final String NOT = "||NOT||";

	private static AutomatedInstallData idata;
	private static AbstractUIProcessHandler mHandler;

	public static void run(AbstractUIProcessHandler handler, String[] args) throws Exception {
        idata = AutomatedInstallData.getInstance();
        mHandler = handler;
        ArgumentParser parser = new ArgumentParser();
        parser.parse(args);
        List<String> filePaths = parser.getListProperty(FILE);

        for (String appendTargerPath : filePaths) {
            File appendTarget = new File(appendTargerPath);
            appendTarget.getParentFile().mkdirs();
            PrintWriter pw = new PrintWriter(new FileWriter(appendTarget, true));
            if (parser.hasProperty(TEXT)) {
                pw.println();
                pw.println(parser.getStringProperty(TEXT));
            } else {
                for (int i = 1; i < args.length; i++) {
                    String line = checkCondition(args[i]);
                    if (line != null) {
                        pw.println(line);
                    }
                }
            }
            pw.close();
        }
    }

    //If line to be appended has condition "IF", "NOT", evaluate, and return string to be appended if condition holds true.
    //Otherwise return null.
    //If no condition is specified return the line to be appended
    private static String checkCondition(String line)
    {
        String[] words = line.split(" ");
        if (words[0].equals(IF))
        {
            if (words[1].equals("true") || words[1].equals("true"))
            {
                String[] actualLine = Arrays.copyOfRange(words, 2, words.length);
                return StringUtils.join(actualLine, " ");
            }
            else
            {
                return null;
            }
        }
        else if (words[0].equals(NOT))
        {
            if (words[1].equals("false") || words[1].equals("false"))
            {
                String[] actualLine = Arrays.copyOfRange(words, 2, words.length);
                return StringUtils.join(actualLine, " ");
            }
            else
            {
                return null;
            }
        }
        else
        {
            return line;
        }
    }
}

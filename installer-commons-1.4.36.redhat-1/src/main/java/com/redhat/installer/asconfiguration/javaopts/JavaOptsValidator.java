package com.redhat.installer.asconfiguration.javaopts;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aabulawi on 14/04/15.
 */
public class JavaOptsValidator implements Validator {

    private boolean success;
    @Override
    public boolean validate(ProcessingClient client) {

        String input = client.getText();


        if (input.isEmpty()) {
            return true;
        }

        Pattern badArgsPattern = Pattern.compile("\\s*-version\\s+|\\s*-X\\s+|\\s*-D\\s+|\\s+[^-]+\\s+|^\\s*[^-]+\\s+|suspend=y");
        Matcher m = badArgsPattern.matcher(input);
        if (m.find())
            return false;

        String[] javaopts =  input.split(" ");
        String pathToJava = System.getProperty("java.home");
        String[] command = new String[1+javaopts.length];
        int counter = 0;
        command[counter] = pathToJava+"/bin/java";
        for (String option : javaopts){
            counter++;
            command[counter] = option;
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process p = null;
        BufferedReader reader = null;
        try {
            p = processBuilder.start();
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            success = true;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Error")) {
                    success = false;
                    break;
                }
            }
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }
}

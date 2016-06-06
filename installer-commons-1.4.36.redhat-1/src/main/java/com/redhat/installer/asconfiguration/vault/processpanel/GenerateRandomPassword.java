package com.redhat.installer.asconfiguration.vault.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import org.apache.commons.lang.StringUtils;

import java.util.Random;



/**
 * Generates a random password which contains at least one number, one letter, and one symbol.
 * Saves the variable to whatever parameter was passed in
 * 
 * @author thauser
 *
 */
public class GenerateRandomPassword {

    private static String validAlphas = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static String validNumbers = "0123456789";
    private static String validSymbols = "!@#$%^&*?";
    // the variable to save the password in
    private static final String VARIABLE = "variable";

	public static void run (AbstractUIProcessHandler handler, String [] args){
		AutomatedInstallData idata = AutomatedInstallData.getInstance();
		ArgumentParser parser = new ArgumentParser();
		parser.parse(args);

        if (!parser.hasProperty(VARIABLE)){
            return;
        } else {
            String varName = parser.getStringProperty(VARIABLE);
            idata.setVariable(varName, addRandomChars("", 25));
        }
	}

    /**
     * Method uses int -> char conversions to generate a random password
     * must contain at least one char from all three valid chars arrays
     * Made accessible for other classes if necessary
     * @param base
     * @return
     */
    public static String addRandomChars(String base, int numRands){
        String finished = base;
        Random rand = new Random();

        while (numRands > 0) {
            int charChoice = rand.nextInt(3);
            switch (charChoice) {
                case 0:
                    // add an alpha
                    finished = addRandomChar(finished, validAlphas);
                    break;
                case 1:
                    finished = addRandomChar(finished, validNumbers);
                    break;
                case 2:
                    finished = addRandomChar(finished, validSymbols);
                    break;
            }
            numRands--;
        }

        // ensure the conditions are met.
        if (!StringUtils.containsAny(finished, validAlphas)){
            finished = addRandomChar(finished, validAlphas);
        }

        if (!StringUtils.containsAny(finished, validNumbers)){
            finished = addRandomChar(finished, validNumbers);
        }

        if (!StringUtils.containsAny(finished, validSymbols)){
            finished = addRandomChar(finished, validSymbols);
        }
        return finished;
    }

    private static String addRandomChar(String base, String validChars){
        String finished = base;
        Random rand = new Random();
        int fob = rand.nextInt(2);

        if (fob == 1){
            int randIndex = rand.nextInt(validChars.length());
            finished = validChars.charAt(randIndex) + finished;
        } else {
            int randIndex = rand.nextInt(validChars.length());
            finished += validChars.charAt(randIndex);
        }
        return finished;
    }

    public String getValidAlphas(){
        return validAlphas;
    }

    public String getValidNumbers(){
        return validNumbers;
    }

    public String getValidSymbols(){
        return validSymbols;
    }
}

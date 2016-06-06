package com.redhat.installer.installation.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.VariableSubstitutor;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * ArgumentParser parses arguments of the following format:
 *      --key=value
 *
 * Anything else will be ignored
 */
public class ArgumentParser {
    private static final String ARGS_OPTION = "--";
    private static final String EQUAL_SIGN = "=";
    private static final int ARGS_OPTION_LENGTH = ARGS_OPTION.length();
    private final PropertiesConfiguration property;
    {
        property = new PropertiesConfiguration();
    }
    public void parse(final String[] args) {
        VariableSubstitutor vs = new VariableSubstitutor(AutomatedInstallData.getInstance().getVariables());
        try {
            for (String argument : args) {
                if (argument.startsWith(ARGS_OPTION) && argument.contains(EQUAL_SIGN)) {
                    String subbedArg = vs.substitute(argument.substring(ARGS_OPTION_LENGTH).replaceAll("\\\\", "\\\\\\\\"));
                    property.load(new StringReader(subbedArg));
                }
            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses all arguments through a deep substitute rather than a simple substitute; this allows more flexibility in the ProcessPanel and matters especially on paths
     * in Windows environments
     * @param args
     */
    public void deepParse(final String[] args){
        VariableSubstitutor vs = new VariableSubstitutor(AutomatedInstallData.getInstance().getVariables());
        try {
            for (String argument : args) {
                if (argument.startsWith(ARGS_OPTION) && argument.contains(EQUAL_SIGN)) {
                    String subbedArg = vs.deepSubstitute(argument.substring(ARGS_OPTION_LENGTH).replaceAll("\\\\", "\\\\\\\\"), "java");
                    property.load(new StringReader(subbedArg));
                }
            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve a single property with the given key
     * @param key the key to look for
     * @return
     */
    public String getStringProperty(final String key) {
        return property.getString(key);
    }

    /**
     * Retrieve a single property with the given key, falling back on the given defaultValue if no such property exists
     * @param key
     * @param defaultValue
     * @return
     */
    public String getStringProperty(final String key, final String defaultValue) {
        return property.getString(key, defaultValue);
    }

    /**
     * Returns if the map has a value with the given key
     * @param key
     * @return
     */
    public boolean hasProperty(final String key) {
        return property.containsKey(key);
    }

    /**
     * Returns an ArrayList of properties which have the given key.
     * @param key the key of the property to look for
     * @return
     */
    public List<String> getListProperty(final String key) {
        List<String> retval = new ArrayList<String>();
        List<Object> os = property.getList(key);
        for (Object o: os)
            retval.add((String)o);
        return retval;
    }

    /**
     * Returns an ArrayList of properties which have the given key, and if none exist, returns the defaultValue
     * @param key the key of the property to look for
     * @param defaultValue the value to return if there are no properties with the given key
     * @return
     */
    public List<String> getListProperty(final String key, final List<String> defaultValue) {
        if (getListProperty(key).isEmpty()){
            return defaultValue;
        } else {
            return getListProperty(key);
        }
    }

    /**
     * Reads the property with the given key and returns if the text is "true"
     * @param key the key of the property to look for
     * @return
     */
    public boolean propertyIsTrue(final String key) {
        return (hasProperty(key) && getStringProperty(key).equalsIgnoreCase("true"));
    }

    /**
     * Sets the underlying PropertiesConfiguration's list delimiter to the given character
     * @param delim the character to use for list delimiting
     */
    public void setListDelimiter(char delim){
        property.setListDelimiter(delim);
    }
}
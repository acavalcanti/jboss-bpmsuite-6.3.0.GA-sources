package com.redhat.installer.framework.mock;

import com.izforge.izpack.panels.ProcessingClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that can be used to test Validators designed to run in UserInputPanel, using the ProcessingClient interface.
 * Content can be added and removed inside test cases easily.
 * Created by thauser on 2/3/14.
 */
public class MockProcessingClient implements ProcessingClient {

    ArrayList<String> fields = new ArrayList<String>();
    Map<String, String> params = new HashMap<String, String>();

    @Override
    public int getNumFields() {
        return fields.size();
    }

    @Override
    public String getFieldContents(int index) {
        if (index > getNumFields() || index < 0){
            return "index out of bounds";
        } else {
            return fields.get(index);
        }
    }

    // unused, but required in the interface
    @Override
    public String getText() {
        return fields.get(0);
    }

    @Override
    public boolean hasParams() {
        return !params.isEmpty();

    }

    @Override
    public Map<String, String> getValidatorParams() {
        return params;
    }

    /**
     * Adds the given string to the mock field list
     * @param fieldContent
     */
    public void addToFields(String fieldContent){
        fields.add(fieldContent);
    }

    /**
     * Adds the given key, value pair to the params map.
     * @param key
     * @param value
     */
    public void addToParams(String key, String value) {
        params.put(key, value);
    }

    /**
     * Clears all of the field contents
     */
    public void clear(){
        fields.clear();
    }


}

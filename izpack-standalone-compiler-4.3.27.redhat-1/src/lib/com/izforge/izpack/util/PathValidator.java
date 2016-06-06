package com.izforge.izpack.util;

import java.io.File;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;


/**
 * This class represents a simple validator to test if a file exists.
 * 
 */
public class PathValidator implements Validator
{

    /**
     * PathValidator
     * Validates the given path
     *
     * @param client the client object using the services of this validator.
     * @return <code>true</code> if the validation passes, otherwise <code>false</code>.
     */
    public boolean validate(ProcessingClient client)
    {
        
        String value = client.getFieldContents(0);

        if ((value == null) || (value.length() == 0))
        {
            return false;
        }
        return pathMatches(value);
    }
    
    
    private boolean pathMatches(String path)
    {
        if (path != null)
        { 
            File file = new File(path);
            if ( file.isDirectory() )
            {
                return true;
            }
        }
        return false;
    }
}

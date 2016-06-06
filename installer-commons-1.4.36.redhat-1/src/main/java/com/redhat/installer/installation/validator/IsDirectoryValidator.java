package com.redhat.installer.installation.validator;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;
import com.izforge.izpack.util.IoHelper;

import java.io.File;

/**
 * Responsible for ensuring that the given path input is an folder, and not a file.
 * Created by fcanas on 4/17/14.
 */
public class IsDirectoryValidator implements Validator {
    public boolean validate(ProcessingClient client)
    {
        String path = client.getFieldContents(0);
        File file = new File(path);
        if (file.exists() && !file.isDirectory())
        {
            return false;
        }
        return true;
    }
}


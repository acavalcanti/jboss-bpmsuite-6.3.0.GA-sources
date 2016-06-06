package com.redhat.installer.installation.validator;

import java.io.File;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;
import com.izforge.izpack.util.IoHelper;

public class WritablePathValidator implements Validator
{

    public boolean validate(ProcessingClient client) 
    {        
        String path = client.getFieldContents(0);
        File existParent = IoHelper.existingParent(new File(path));

        if (existParent == null || !existParent.canWrite())
        {
            return false;
        }
        return true;
    }
}



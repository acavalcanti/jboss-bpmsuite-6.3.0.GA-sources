package com.redhat.installer.installation.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

import java.io.File;
import java.util.ArrayList;

public class DirectoryValidator  implements Validator, DataValidator
{
	AutomatedInstallData idata;
	String error;
    String message;

    /** Chracters that installer or its products can't handle */
    public static final String [] invalidCharacters = {"  ", "?", "%", ":"};

    /** Invalid Windows Characters
     *  http://msdn.microsoft.com/en-us/library/aa365247.aspx
     *  Forwardslash slash not included because installer will end up creating directory with backslash
     *  These should never change, its official
     *  NOTE: We choose double backslash to be invalid rather than just backslash, because we are checking against paths
     *        A normal backslash would just represent another folder
     */
    public static final String [] invalidWindows = {"<", ">", ":", "\"", "/", "\\\\", "|", "?", "*", "\\ "};

    /** Invalid Unix Chracters
     *  NULL: Not allowed marks end of file name
     *  "// : Cannot have a filename named slash
     *  ";" : Do not want semi-colon as it indicates end of command in bash, would have to escape for this to work
     *  "\" : If you are escaping paths in your directory there is bound to be trouble, let Java do its thing
     */
    public static final String [] invalidUnix = {"\\0", "//", ";", "\\", "\""};

    public static boolean validate(File dir)
    {
        if (!checkDirectory(dir.getAbsolutePath()))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    //Validator for UserInputSpec
	public boolean validate(ProcessingClient client)
	{
		idata = AutomatedInstallData.getInstance();
		int numFields = client.getNumFields();
		
        for (int i = 0; i < numFields; i++)
        {
            if (!checkDirectory(client.getFieldContents(i)))
    		{
            	return false;
    		}
        } 
        return true;
	}

    //Validator for target panel
	public Status validateData(AutomatedInstallData idata)
	{
		String path = idata.getVariable("INSTALL_PATH");

        String invalidChars = getInvalidCharacters();
		if (!checkDirectory(path))
		{
            setError(String.format(idata.langpack.getString("TargetPanel.invalid"), invalidChars));
            message = idata.langpack.getString(error);
			return Status.ERROR;
		}
		return Status.OK;
	}
	
	private static boolean checkDirectory(String path)
	{
        String filteredPath = path;

        if(System.getProperty("os.name").toLowerCase().contains("window"))
        {
            if (path.length() <= 2) return false;            //Windows root folder must start with drive
            filteredPath = path.substring(2, path.length()); //Remove starting drive
            if (!isValidString(filteredPath,invalidWindows)) return false;
        }
        else
        {
            if (!isValidString(filteredPath,invalidUnix)) return false;
        }

        for (String character : invalidCharacters)
        {
            if (filteredPath.contains(character)) return false;
        }

        return true;
	}

    private static boolean isValidString(String input, String[] filter)
    {
        for (String character : filter){
            if (input.contains(character)) return false;
        }
        return true;
    }

    public static String getInvalidCharacters()
    {
        String invalidChars= "";
        ArrayList<String> invalidList = new ArrayList();
        for (String installerChar : invalidCharacters)
        {
            invalidList.add(installerChar);
        }
        if(System.getProperty("os.name").toLowerCase().contains("window"))
        {
            for (String winChar : invalidWindows)
            {
                if(!invalidList.contains(winChar))
                {
                    invalidList.add(winChar);
                }
            }
        }
        else
        {
            for (String unixChar : invalidUnix)
            {
                if(!invalidList.contains(unixChar))
                {
                    invalidList.add(unixChar);
                }
            }
        }

        for (String invalidChar : invalidList)
        {
            invalidChars += ", \""+invalidChar+"\"";
        }
        return invalidChars.substring(1);
    }
	public void setError(String e)
	{
		this.error= e;
	}
	public String getErrorMessageId() {
		// TODO Auto-generated method stub
		return error;
	}

	public String getWarningMessageId() {
		// TODO Auto-generated method stub
		return error;
	}

	public boolean getDefaultAnswer() {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public String getFormattedMessage() {
        return message;
	}
}

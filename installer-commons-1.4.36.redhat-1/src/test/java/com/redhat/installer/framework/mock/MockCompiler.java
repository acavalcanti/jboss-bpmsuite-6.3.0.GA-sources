package com.redhat.installer.framework.mock;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.IXMLParser;
import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.rules.RulesEngine;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.IllegalFormatException;
import java.util.Iterator;


/** Utility class to initialize the state of the installer.
 * TODO: Maybe try using methods directly from the CompilerConfig class.
 * - Generate Rules Engine
 * - Initialize Conditions
 * - Initialize Variables
 */
public class MockCompiler
{
    protected static void initVariables(String pathToVariables, AutomatedInstallData idata)
    {
        IXMLElement data = getXMLTree(pathToVariables);
        Iterator<IXMLElement> iter = data.getChildrenNamed("variable").iterator();
        while (iter.hasNext())
        {
            IXMLElement var = iter.next();
            String name = requireAttribute(var, "name");
            String value = requireAttribute(var, "value");
            if (idata.getVariables().containsValue(name)) System.out.println("WARNING: Duplicate Variable: " + name + "has been found");
            idata.setVariable(name, value);
            //System.out.println("Variable " + name + "has been set to " + value);
        }
    }

    protected static String requireAttribute(IXMLElement element, String attribute) throws IllegalFormatException
    {
        String value = element.getAttribute(attribute);
        if (value == null)
        {
            throw new IllegalStateException("<" + element.getName() + "> requires attribute " + attribute);
        }
        return value;
    }

    protected static void initConditions(String pathToConditions, AutomatedInstallData idata)
    {
        RulesEngine rules = genRulesEngine(pathToConditions, idata);
        idata.setRules(rules);
    }

    protected static RulesEngine genRulesEngine(String path, AutomatedInstallData idata)
    {
        IXMLElement data = getXMLTree(path);
        RulesEngine rules = new RulesEngine(data, idata);
        return rules;
    }

    protected static void initLangpacks(String pathToLangpacks, AutomatedInstallData idata)
    {
        IXMLElement data = getXMLTree(pathToLangpacks);
        Iterator<IXMLElement> iter = data.getChildrenNamed("langpack").iterator();
    }

    protected static IXMLElement getXMLTree(String path)
    {
        try
        {
            IXMLParser parser = new XMLParser();
            File file = new File(path).getAbsoluteFile();
            IXMLElement data  = parser.parse(new FileInputStream(path),file.getAbsolutePath());
            return data;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}

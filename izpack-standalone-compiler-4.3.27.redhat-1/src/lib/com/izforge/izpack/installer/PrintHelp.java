package com.izforge.izpack.installer;

import com.izforge.izpack.LocaleDatabase;

import java.io.InputStream;

/**
 * Prints IzPack and optional installer help to stdout
 *
 * IzPack help is in langpack as installer.usage
 * Installer help is in langpack as installer.usage.append
 */
public class PrintHelp
{
    private LocaleDatabase langpack;

    private static String DEFAULT_LANG = "eng";

    public PrintHelp() {
        this(DEFAULT_LANG);
    }

    public PrintHelp(String langcode) {
        try {
            InputStream izpackLangPack = getClass().getResourceAsStream("/langpacks/" + langcode + ".xml");
            langpack = new LocaleDatabase(izpackLangPack);
            InputStream customLangPack = getClass().getResourceAsStream("/res/CustomLangpack.xml_" + langcode);
            langpack.add(customLangPack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printHelp() {
        printToConsole(langpack.getString("installer.usage"));
        String append = langpack.getString("installer.usage.append");
        if (!append.equals("installer.usage.append")) {
            System.out.println();
            printToConsole(append);
        }

    }

    private void printToConsole(String toConsole) {
        String[] lines = toConsole.split("\\\\n");
        for (String line : lines) {
            System.out.println(line);
        }
    }
}

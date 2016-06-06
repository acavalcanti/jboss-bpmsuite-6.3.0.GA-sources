package com.redhat.installer.layering.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.VariableSubstitutor;
import com.redhat.installer.layering.PreExistingConfigurationConstants;
import com.redhat.installer.layering.action.PreExistingSetter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SetVaultDefaults extends PreExistingSetter {
    /**
     * if the user defined multiple different vaults in different descriptors, this method will have trouble.
     *
     * @param xml
     * @param doc
     */
    @Override
    protected void setDefaults(String xml, Document doc) {
        if (!doc.select("vault").isEmpty()) {
            idata.setVariable("vault.preexisting", "true");
            idata.setVariable(xml + ".vault.preexisting", "true");
            idata.setAttribute("system-properties", doc.select("system-properties > property"));

            // all of the vault options must exist, or the standalone.xml is malformed
            idata.setVariable("vault.keystoreloc", doc.select("vault > vault-option[name=KEYSTORE_URL]").first().attr("value"));
            idata.setVariable("vault.alias", doc.select("vault > vault-option[name=KEYSTORE_ALIAS]").first().attr("value"));
            idata.setVariable("vault.salt", doc.select("vault > vault-option[name=SALT]").first().attr("value"));
            idata.setVariable("vault.itercount", doc.select("vault > vault-option[name=ITERATION_COUNT]").first().attr("value"));
            idata.setVariable("vault.encrdir", doc.select("vault > vault-option[name=ENC_FILE_DIR]").first().attr("value"));

        }
    }
    @Override
    protected void resetDefaults() {
        idata.setVariable("vault.keystoreloc", idata.getVariable("vault.keystoreloc.default"));
        idata.setVariable("vault.encrdir", idata.getVariable("vault.encrdir.default"));
        idata.setVariable("vault.preexisting", "false");
    }
}

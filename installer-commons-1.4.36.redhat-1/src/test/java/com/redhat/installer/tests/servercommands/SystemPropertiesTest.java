package com.redhat.installer.tests.servercommands;

import com.redhat.installer.asconfiguration.processpanel.postinstallation.SystemProperties;
import com.redhat.installer.framework.testers.PostinstallTester;
import com.redhat.installer.tests.TestUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.nodes.Attributes;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by aabulawi on 30/07/15.
 */
public class SystemPropertiesTest extends PostinstallTester {

    private final String PROPERTY = "property";
    private final String NAME = "name";
    private final String VALUE = "value";

    @Test
    public void correctlyAddSystemProperty() throws Exception {

        Elements elements = new Elements();
        elements.add(createPropertyTag("oranges", "apples"));

        idata.setAttribute("system-properties", elements);
        SystemProperties.run(mockAbstractUIProcessHandler, new String[] {});
        Elements result = TestUtils.getXMLTagsFromConfig(idata.getInstallPath() + TestUtils.STANDALONE_CONFIG_DIR + "standalone.xml", "system-properties > property");

        checkEquality(elements, result);

    }

    private void checkEquality(Elements expected, Elements generated){
        assertEquals(expected.size(), generated.size());
        for (int i=0; i < generated.size(); i++){
            assertEquals(expected.get(i).attr(NAME), generated.get(i).attr(NAME));
            assertEquals(expected.get(i).attr(VALUE), generated.get(i).attr(VALUE));
        }
    }

    private Element createPropertyTag(String name, String value){
        Attributes atrributes = new Attributes();
        atrributes.put(NAME, name);
        atrributes.put(VALUE, value);
        Tag tag = Tag.valueOf(PROPERTY);
        return new Element(tag, "", atrributes);
    }

}

package com.redhat.installer.tests.installation.action;

import com.izforge.izpack.Pack;
import com.izforge.izpack.util.OsConstraint;
import com.redhat.installer.installation.action.QuickstartsPackUpdate;
import com.redhat.installer.framework.testers.PanelActionTester;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by thauser on 9/3/14.
 */
public class QuickstartsPackUpdateTest extends PanelActionTester {

    @BeforeClass
    public static void beforeAll(){
        Pack test = new Pack("test", "","",new ArrayList<OsConstraint>(),new ArrayList<String>(),false,false,false,"",false);
        idata.availablePacks.add(test);
        idata.availablePacksMap.put("application-platform.quickstarts", test);
    }

    @Before
    public void before(){
        panelAction = new QuickstartsPackUpdate();
    }

    @Test
    public void testAddQuickstarts(){
        idata.setVariable("installQuickStarts", TRUE);
        panelAction.executeAction(idata, handler);
        assertTrue(idata.selectedPacks.contains(idata.getPackage("application-platform.quickstarts")));
    }

    @Test
    public void testRemoveQuickstarts(){
        idata.setVariable("installQuickStarts", FALSE);
        panelAction.executeAction(idata, handler);
        assertFalse(idata.selectedPacks.contains(idata.getPackage("application-platform.quickstarts")));
    }
}

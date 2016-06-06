package com.redhat.installer.tests.installation.maven.processpanel;

import com.redhat.installer.framework.testers.ProcessPanelTester;
import com.redhat.installer.installation.maven.processpanel.MavenSettingsTransformer;
import com.redhat.installer.tests.TestUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.redhat.installer.framework.mock.MockFileBuilder.makeNewFileFromStrings;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by thauser on 2/4/14.
 */
public class MavenSettingsTransformerTest extends ProcessPanelTester {

    // these should / could be refactored into a more general area, for use elsewhere perhaps? would probably
    // be useful to rewrite the MavenSettingsTransformer using JSoup
    private static final String REDHAT_NAME = "redhat-techpreview-all-repository";
    private static final String newProfileQuery = "profiles > profile:has(id:matchesOwn("+REDHAT_NAME+"))";
    private static final String otherProfilesQuery = "profiles > profile > id:not(id:matchesOwn("+REDHAT_NAME+"))";
    private static final String activeProfileQuery = "activeProfiles > activeProfile:matchesOwn("+REDHAT_NAME+")";
    private static final String otherActiveProfilesQuery = "profiles > profile > id:not(id:matchesOwn("+REDHAT_NAME+"))";
    private static final String newRepoQuery = "profile > repositories > repository:has(id:matchesOwn("+ REDHAT_NAME +")";
    private static final String newPluginRepoQuery = "profile > pluginRepositories > pluginRepository:has(id:contains("+ REDHAT_NAME +"))";
    private static final String REPO_URL = "http://maven.repository.redhat.com/techpreview/all/";
    private static final String REPO_NAME = "Red Hat Tech Preview repository \\(all\\)";
    private static final String REPO_LAYOUT = "default";
    private static final String REPO_UPDATE_POLICY = "never";
    private static final String SNAPSHOT_ENABLED = "false";
    private static final String RELEASE_ENABLED = "true";
    private static final String repoUrlQuery = "url:matchesOwn("+REPO_URL+")";
    private static final String repoIdQuery = "id:matchesOwn("+REDHAT_NAME+")";
    private static final String repoNameQuery = "name:matchesOwn("+REPO_NAME+")";
    private static final String repoLayoutQuery = "layout:matchesOwn("+REPO_LAYOUT+")";
    private static final String repoUpdatePolicyQuery = "updatePolicy:matchesOwn("+REPO_UPDATE_POLICY+")";
    private static final String repoReleaseQuery = "releases > enabled:matchesOwn("+RELEASE_ENABLED+")";
    private static final String repoSnapshotQuery = "snapshots > enabled:matchesOwn("+SNAPSHOT_ENABLED+")";

    MavenSettingsTransformer mst;

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "MavenRepoCheckPanel.xslt.default.failure",
                "MavenRepoCheckPanel.xslt.default.success");
        mst = new MavenSettingsTransformer();
    }

    @After
    public void tearDown() throws Exception {
        mst = null;
    }

    @Test
    public void testDefaultCreation() throws Exception{
        String settingsPath = tempFolder.getRoot().getAbsolutePath() + File.separator + TestUtils.mockSettingsPath;
        mst.run(handler, putArgs(settingsPath, REDHAT_NAME, "fsw"));
        assertTrue(isNewSettingsXml(settingsPath));
    }

    @Test
    public void testExistingSettingsNoIdNoURL() throws Exception {
        File settings = makeNewFileFromStrings(tempFolder, "<settings>", "</settings>");
        mst.run(handler, putArgs(settings.getAbsolutePath(), REDHAT_NAME, "fsw"));
        assertTrue(isValidSettingsXml(settings.getAbsolutePath()));
    }

    @Test
    public void testExistingSettingsMatchingIdNoUrl() throws Exception {
        File settings = makeNewFileFromStrings(tempFolder,"<settings>",
                                                                 "<profiles>",
                                                                 "<profile>",
                                                                 "<id>"+REDHAT_NAME+"</id>",
                                                                 "</profile>",
                                                                 "</profiles>",
                                                                 "</settings>");
        mst.run(handler, putArgs(settings.getAbsolutePath(), REDHAT_NAME, "fsw"));
        assertTrue(isValidSettingsXml(settings.getAbsolutePath()));
    }

    @Test
    public void testExistingSettingsMatchingUrlsNoActive() throws Exception {
        File settings = makeNewFileFromStrings(tempFolder, "<settings>",
                                                                 "<profiles>",
                                                                 "<profile>",
                                                                 "<id>otherprofile</id>",
                                                                 "<repositories>",
                                                                 "<repository>",
                                                                 "<url>"+REPO_URL+"</url>",
                                                                 "</repository>",
                                                                 "</repositories>",
                                                                 "<pluginRepositories>",
                                                                 "<pluginRepository>",
                                                                 "<url>"+REPO_URL+"</url>",
                                                                 "</pluginRepository>",
                                                                 "</pluginRepositories>",
                                                                 "</profile>",
                                                                 "</profiles>",
                                                                 "</settings>");
        mst.run(handler, putArgs(settings.getAbsolutePath(), REDHAT_NAME, "fsw"));
        assertTrue(isValidSettingsXml(settings.getAbsolutePath()));
    }

    // Interesting discovery: the mavensettingstransformer will add the <? ?> line if it doesn't exist, even
    // though it is not technically required.
    @Test
    public void testExistingSettingsMatchingUrlsActive() throws Exception {
        File settings = makeNewFileFromStrings(tempFolder, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
                "<settings>",
                "<profiles>",
                "<profile>",
                "<id>otherprofile</id>",
                "<repositories>",
                "<repository>",
                "<url>"+REPO_URL+"</url>",
                "</repository>",
                "</repositories>",
                "<pluginRepositories>",
                "<pluginRepository>",
                "<url>"+REPO_URL+"</url>",
                "</pluginRepository>",
                "</pluginRepositories>",
                "</profile>",
                "</profiles>",
                "<activeProfiles>",
                "<activeProfile>otherprofile</activeProfile>",
                "</activeProfiles>",
                "</settings>");
        long oldChecksum = FileUtils.checksumCRC32(settings);
        mst.run(handler, putArgs(settings.getAbsolutePath(),REDHAT_NAME, "fsw"));
        long newChecksum = FileUtils.checksumCRC32(settings);
        assertEquals(oldChecksum, newChecksum);
    }

    private boolean isValidSettingsXml(String mockSettingsPath) throws Exception{
        File settings = new File(mockSettingsPath);
        Document doc = Jsoup.parse(settings, "UTF-8", "");
        Elements newProfileElement = doc.select(newProfileQuery);
        boolean activeProfile =!doc.select(activeProfileQuery).isEmpty();

        return (isCorrectProfileElement(newProfileElement) && activeProfile);
    }

    /**
     * Evaluates if the given settings.xml is newly generated (no other profiles / additions except the ones performed by
     * MavenSettingsTransformerTest)
     * @param mockSettingsPath
     * @return
     * @throws Exception
     */
    private boolean isNewSettingsXml(String mockSettingsPath) throws Exception{
        File settings = new File(mockSettingsPath);
        Document doc = Jsoup.parse(settings, "UTF-8", "");
        // make sure this is present also
        Elements newProfileElement = doc.select(newProfileQuery);
        boolean newProfile = !newProfileElement.isEmpty();
        boolean otherProfiles = !doc.select(otherProfilesQuery).isEmpty();
        boolean activeProfile = !doc.select(activeProfileQuery).isEmpty();
        boolean otherActiveProfiles = !doc.select(otherActiveProfilesQuery).isEmpty();

        // if we have no new profile or no new active profile, or no new repo / pluginRepo, OR we DO have otherProfiles or other activeProfiles, this
        // is not a newly created settings.xml
        if (!newProfile || !activeProfile || otherProfiles || otherActiveProfiles){
            return false;
        }

        // look into the profile subtree and make sure the correct elements exist with the correct values
       return isCorrectProfileElement(newProfileElement);
    }

    /**
     * Given a list of elements, confirm that they are a correct Repo representation
     * @param newProfile
     * @return
     */
    private boolean isCorrectProfileElement(Elements newProfile){

        Elements newRepoElements = newProfile.select(newRepoQuery);
        boolean newRepo = !newRepoElements.isEmpty();        // get our newly added plugin repo (1 element):
        Elements newPluginRepoElements = newProfile.select(newPluginRepoQuery);
        boolean newPluginRepo = !newPluginRepoElements.isEmpty();

        if (!isCorrectRepoElement(newRepoElements) || !isCorrectRepoElement(newPluginRepoElements)){
            return false;
        } else {
            return true;
        }
    }

    private boolean isCorrectRepoElement(Elements newRepo) {
        boolean urlCorrect = newRepo.select(repoUrlQuery).size() == 1;
        boolean nameCorrect = newRepo.select(repoNameQuery).size() == 1;
        boolean idCorrect = newRepo.select(repoIdQuery).size() == 1;
        boolean layoutCorrect = newRepo.select(repoLayoutQuery).size() == 1;
        boolean updateCorrect = newRepo.select(repoUpdatePolicyQuery).size() == 2;
        boolean releaseCorrect = newRepo.select(repoReleaseQuery).size() == 1;
        boolean snapshotCorrect = newRepo.select(repoSnapshotQuery).size() == 1;
        return urlCorrect && nameCorrect && idCorrect && layoutCorrect && updateCorrect && releaseCorrect && snapshotCorrect;
    }

    private String[] putArgs(String file, String profile, String installer){
        return new String[]{"--settings-file="+file,"--profile="+profile,"--installer="+installer};

    }

    @Override
    public void testProcessPanelInstantiation() {

    }
}

package com.redhat.installer.installation.maven.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.constants.GeneralConstants;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;

/**
 * @author dcheung@redhat.com, dmondega@redhat.com
 */

/**
 * Add necessary profile to maven's settings.xml.
 * If the settings.xml file does not exist, we will create one.
 * The settings.xml does not have to be named "settings.xml"
 *
 * Steps taken to choose weather or not to add a profile.
 * 1. Check active profiles, if no active profiles create required profile.
 * 2. From the active profiles check that the repository url is what we need, if not create required profile
 * 3. From the active profiles check that the plugin repository url is what we need, if not create required profile
 */
public class MavenSettingsTransformer implements GeneralConstants
{
    private static ArgumentParser parser;
	private static AutomatedInstallData idata;
    private static AbstractUIProcessHandler mHandler;

    //XML Tags
    private static final String ACTIVE_PROFILES = "activeProfiles";
    private static final String ACTIVE_PROFILE = "activeProfile";
    private static final String PROFILE = "profile";
    private static final String PROFILES = "profiles";
    private static final String REPOSITORIES = "repositories";
    private static final String REPOSITORY = "repository";
    private static final String PLUGIN_REPOSITORIES = "pluginRepositories";
    private static final String PLUGIN_REPOSITORY = "pluginRepository";
    private static final String ID = "id";
    private static final String URL = "url";
    private static final String SETTINGS = "settings";
    private static final String NAME = "name";
    private static final String LAYOUT = "layout";
    private static final String RELEASES = "releases";
    private static final String UPDATE_POLICY = "updatePolicy";
    private static final String SNAPSHOTS = "snapshots";
    private static final String ENABLED = "enabled";
    private static final String LOCAL_REPOSTIROY = "localRepository";

    // Arguments
    private static final String SETTINGS_FILE = "settings-file";
    private static final String INSTALLER = "installer";
    private static final String PROFILE_ARG = "profile";


    /**
     * Modifies a maven settings.xml file, or generates one if it does not already exist.
     * Result of the settings.xml file should have a profile, that will be marked as an <activeProfile>
     * When modifying an existing settings.xml file a backup will be created.
     *
     * @param handler
     * @param args --settings-file, --installer, --profile
     * @throws Exception
     */
	public static void run(AbstractUIProcessHandler handler, String[] args) throws Exception
    {
		mHandler = handler;
        parser = new ArgumentParser();
		idata = AutomatedInstallData.getInstance();
        parser.parse(args);

        String settingsPath = parser.getStringProperty(SETTINGS_FILE);
        String installer = parser.getStringProperty(INSTALLER);
        String profileName = parser.getStringProperty(PROFILE_ARG);

		File settingsFile = new File(settingsPath);
		if (!settingsFile.exists())
		{
			createDefaultSettingsXml(settingsPath);
		}
        else
        {
            FileUtils.copyFile(settingsFile, new File(settingsFile.getPath()+".jboss_backup"));
		}

		readAndModifySettingsXml(settingsPath, installer, profileName);
	}


    /**
     * Add required profile to setting.xml file
     *
     * @param fileName path to a maven settings.xml location
     * @param installer specify which installer is running this method
     * @param profileName profile name to be added to the settings.xml file
     */
	private static void readAndModifySettingsXml(String fileName, String installer, String profileName)
    {
		File settingsFile = new File(fileName);

		try 
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbFactory.newDocumentBuilder();
			Document doc = builder.parse(settingsFile);

            ArrayList<Map<String, String>> profileData = new ArrayList<Map<String, String>>();
            ArrayList<String> existingIds = new ArrayList<String>();
            ArrayList<String> missingUrls = new ArrayList<String>();

            /**
             * The whole snippet below should be parameterized through ProcessPanel args, this kind of branching based upon variables
             * in the installer is hard to use. for later releases
             */
            String url = "";
            if (installer.equals(EAP))
            {
                url = idata.getVariable("MAVEN_REPO_PATH");
                missingUrls.add(url);
                profileData.add(genMap(
                        profileName, profileName+"-repository", profileName+"-plugin-repository", "JBoss GA Tech Preview Maven Repository",
                        "JBoss 6 Maven Plugin Repository", idata.getVariable("MAVEN_REPO_PATH"), "never", "default"));
            }
            else
            {
                url = "http://maven.repository.redhat.com/techpreview/all/";
                missingUrls.add(url);
                profileData.add(genMap(
                        profileName, profileName, profileName, "Red Hat Tech Preview repository (all)",
                        "Red Hat Tech Preview repository (all)", "http://maven.repository.redhat.com/techpreview/all/", "never", "default"));
            }

            // Find the profile required to add based on url
            missingUrls = findMissingUrls(doc, missingUrls);

            if (missingUrls.isEmpty())
            {
                // there are no missing urls in the settings.xml, ie, it already contains profiles with the repo we're trying to add
                ProcessPanelHelper.printToPanel(mHandler,
                        String.format(idata.langpack.getString("MavenRepoCheckPanel.settings.urls.exist"), settingsFile, url),
                        false);
                return;
            }

            NodeList nodeList = doc.getElementsByTagName(ID);
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                Element e = (Element) nodeList.item(i);
                existingIds.add(e.getTextContent());
            }

            Element root =  (Element) doc.getElementsByTagName(SETTINGS).item(0);
            Element profiles = (Element) doc.getElementsByTagName(PROFILES).item(0);
            Element actives = (Element) doc.getElementsByTagName(ACTIVE_PROFILES).item(0);

            if (profiles == null)
            {
                profiles = (Element) doc.createElement(PROFILES);
                root.appendChild(profiles);
            }
            if (actives  == null)
            {
                actives  = (Element) doc.createElement(ACTIVE_PROFILES);
                root.appendChild(actives);
            }

            String profileId = "";
            for (Map<String, String> entry : profileData)
            {
                if (!missingUrls.contains(entry.get("url")))
                {
                    continue;
                }
                // Use a unique name if the profile name already exists
                if (existingIds.contains(entry.get("profileId")))
                {
                    entry.put("profileId", entry.get("profileId") + "-" + System.currentTimeMillis());
                }

                profileId = entry.get("profileId");
                Element newProfile = generateProfile(
                        doc,
                        profileId,
                        entry.get("repoId"),
                        entry.get("pluginId"),
                        entry.get("repoName"),
                        entry.get("pluginName"),
                        entry.get("url"),
                        entry.get("updatePolicy"),
                        entry.get("layout"));

                profiles.appendChild(newProfile);
                Element newActive = doc.createElement("activeProfile");
                newActive.setTextContent(entry.get("profileId"));
                actives.appendChild(newActive);
            }
            writeToFile(doc, settingsFile);
            ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("MavenSettingsTransformer.profile.creation"),profileId, settingsFile), false);
		}
        catch (Exception e)
        {
			e.printStackTrace();
		}

	}

    /**
     * Helper method to generate a profile for the settings.xml file
     *
     * @param doc Document to generate your xml file
     * @param profileId ID of the associated profile
     * @param repositoryId ID of the associated repository
     * @param pluginId ID of the associated plugin
     * @param repositoryName name of the associated repository
     * @param pluginName name of the associated plugin
     * @param repositoryUrl url of the associated repository
     * @param updatePolicy profiles update policy
     * @param layout
     * @return profile element to be added to the xml
     */
	private static Element generateProfile(Document doc, String profileId,
			String repositoryId, String pluginId, String repositoryName,
			String pluginName, String repositoryUrl, String updatePolicy,
			String layout) {

		Element newProfile = doc.createElement(PROFILE);
		Element id = doc.createElement(ID);
		id.setTextContent(profileId);

		Element repos = doc.createElement(REPOSITORIES);
		Element repo = doc.createElement(REPOSITORY);
		Element repoId = doc.createElement(ID);
		repoId.setTextContent(repositoryId);
		Element repoName = doc.createElement(NAME);
		repoName.setTextContent(repositoryName);
		Element repoUrl = doc.createElement(URL);
		repoUrl.setTextContent(repositoryUrl);
		Element repoLayout = doc.createElement(LAYOUT);
		repoLayout.setTextContent(layout);

		Element repoReleases = doc.createElement(RELEASES);
		Element repoRelEnabled = doc.createElement(ENABLED);
		repoRelEnabled.setTextContent("true");
		Element releasePolicy = doc.createElement(UPDATE_POLICY);
		releasePolicy.setTextContent(updatePolicy);

		Element repoSnapshots = doc.createElement(SNAPSHOTS);
		Element repoSnapEnabled = doc.createElement(ENABLED);
		repoSnapEnabled.setTextContent("false");
		Element snapshotPolicy = doc.createElement(UPDATE_POLICY);
		snapshotPolicy.setTextContent(updatePolicy);

		repoSnapshots.appendChild(repoSnapEnabled);
		repoSnapshots.appendChild(snapshotPolicy);

		repoReleases.appendChild(repoRelEnabled);
		repoReleases.appendChild(releasePolicy);

		repo.appendChild(repoId);
		repo.appendChild(repoName);
		repo.appendChild(repoUrl);
		repo.appendChild(repoLayout);
		repo.appendChild(repoReleases);
		repo.appendChild(repoSnapshots);
		// append repo to repos element
		repos.appendChild(repo);
		// repositories element done

		Element pluginRepos = doc.createElement(PLUGIN_REPOSITORIES);
		// plugin repo
		Element pluginRepo = doc.createElement(PLUGIN_REPOSITORY);
		Element pluginRepoId = doc.createElement(ID);
		pluginRepoId.setTextContent(pluginId);
		Element pluginRepoName = doc.createElement(NAME);
		pluginRepoName.setTextContent(pluginName);
		Element pluginRepoUrl = doc.createElement(URL);
		pluginRepoUrl.setTextContent(repositoryUrl);

		Element pluginLayout = doc.createElement(LAYOUT);
		pluginLayout.setTextContent(layout);

		Element pluginRepoReleases = doc.createElement(RELEASES);
		Element pluginRepoRelEnabled = doc.createElement(ENABLED);
		pluginRepoRelEnabled.setTextContent("true");

		Element pluginReleasePolicy = doc.createElement(UPDATE_POLICY);
		pluginReleasePolicy.setTextContent(updatePolicy);

		Element pluginRepoSnapshots = doc.createElement(SNAPSHOTS);
		Element pluginRepoSnapEnabled = doc.createElement(ENABLED);
		pluginRepoSnapEnabled.setTextContent("false");
		Element pluginSnapshotPolicy = doc.createElement(UPDATE_POLICY);
		pluginSnapshotPolicy.setTextContent(updatePolicy);

		// organize everything
		pluginRepoSnapshots.appendChild(pluginRepoSnapEnabled);
		pluginRepoSnapshots.appendChild(pluginSnapshotPolicy); // policied being added

		pluginRepoReleases.appendChild(pluginRepoRelEnabled);
		pluginRepoReleases.appendChild(pluginReleasePolicy); // Policies being added

		pluginRepo.appendChild(pluginRepoId);
		pluginRepo.appendChild(pluginRepoName);
		pluginRepo.appendChild(pluginRepoUrl);
		pluginRepo.appendChild(pluginLayout);
		pluginRepo.appendChild(pluginRepoReleases);
		pluginRepo.appendChild(pluginRepoSnapshots);
		pluginRepos.appendChild(pluginRepo);

		// construction!
		newProfile.appendChild(id);
		newProfile.appendChild(repos);
		newProfile.appendChild(pluginRepos);

		return newProfile;
	}

    /**
     * Makes a backup of the File located at path specified. The backup will be in
     * the same directory with the extension ".jboss_backup" added on to it.
     *
     * @param pathToFile
     * @throws Exception
     */
	private static void makeBackup(String pathToFile) {
        File settings = new File(pathToFile);
		File backup = new File(settings.getPath() + ".jboss_backup");
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(settings);
            out = new FileOutputStream(backup);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a Default settings xml. 
     * Only used if the provided location is non-existent
     *
     * @param settingsPath location to where settings path should be placed.
     */
	private static void createDefaultSettingsXml(String settingsPath)
    {
		File defaultSettings = new File(settingsPath);
		defaultSettings.getParentFile().mkdirs();
		boolean successful = true;

		try
        {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root settings element
			Document doc = docBuilder.newDocument();
			Element root = doc.createElement(SETTINGS);
			Attr xmlns = doc.createAttribute("xmlns");
			xmlns.setValue("http://maven.apache.org/SETTINGS/1.0.0");
			Attr xmlnsXsi = doc.createAttribute("xmlns:xsi");
			xmlnsXsi.setValue("http://www.w3.org/2001/XMLSchema-instance");
			Attr xsiLoc = doc.createAttribute("xsi:schemaLocation");
			xsiLoc.setValue("http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd");
			root.setAttributeNode(xmlns);
			root.setAttributeNode(xmlnsXsi);
			root.setAttributeNode(xsiLoc);
			doc.appendChild(root);

			// child localRepository element
			Element localRepo = doc.createElement(LOCAL_REPOSTIROY);
			root.appendChild(localRepo);


			// child profiles
			Element profiles = doc.createElement(PROFILES);
			root.appendChild(profiles);

			// child active profiles
			Element activeProfiles = doc.createElement(ACTIVE_PROFILES);
			root.appendChild(activeProfiles);

			// done construction; now write the file
			writeToFile(doc, defaultSettings);

		}
        catch (Exception e)
        {
            successful = false;
			e.printStackTrace();
		}

		if (!successful)
		{
			ProcessPanelHelper.printToPanel(mHandler,
					idata.langpack.getString("MavenRepoCheckPanel.xslt.default.failure"),
					true);
		} 
		else 
		{
			ProcessPanelHelper.printToPanel(mHandler,
					String.format(idata.langpack.getString("MavenRepoCheckPanel.xslt.default.success"),settingsPath),
					false);
		}
	}


    /**
     * Writes a DOM document to a File
     *
     * @param doc Dom document generated
     * @param file File to write to
     */
	private static void writeToFile(Document doc, File file)
    {
		BufferedWriter writeOut = null;
		try
        {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", 4);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);

			StreamResult result = new StreamResult(new StringWriter());
			transformer.transform(source, result);

			String outputString = result.getWriter().toString();

			writeOut = new BufferedWriter(new FileWriter(file));
			writeOut.write(outputString);
		}
        catch (Exception e)
        {
			e.printStackTrace();
		} finally {
            try {
                writeOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

    /**
     * Helper method to generate hash map for the profileData method.
     * Order of the data does matter.
     *
     * @param data Input the number of required fields based on your keys array.
     *             Order does matter.
     *             profileId, repoId, pluginId, repoName, pluginName, url, updatePolicy, layout
     * @return Hash map of your data
     */
    private static HashMap<String, String> genMap(String... data)
    {
        HashMap<String, String> dataMap = new HashMap<String, String>();
        String [] keys =  {"profileId", "repoId", "pluginId", "repoName", "pluginName", "url", "updatePolicy", "layout"};

        if (data.length != keys.length)
        {
            throw new InputMismatchException("Number of arguments don't match the length of the key array");
        }
        for (int i = 0; i < keys.length; i++)
        {
            dataMap.put(keys[i], data[i]);
        }
        return dataMap;
    }

    /**
     * Find the missing urls based on the profiles you want to add.
     *
     * @param doc  Your xml document
     * @param urls Urls to look for
     * @return List of urls that are missing from the settings.xml file
     */
    private static ArrayList<String> findMissingUrls(Document doc, ArrayList<String> urls)
    {
        //Get all active profiles
        ArrayList<String> activeProfiles = new ArrayList<String>();
        NodeList activeProfileList = doc.getElementsByTagName(ACTIVE_PROFILES);

        for (int i = 0; i < activeProfileList.getLength(); i++)
        {
            Element p = (Element) activeProfileList.item(i);
            NodeList activeList = p.getElementsByTagName(ACTIVE_PROFILE);

            for (int j = 0; j < activeList.getLength(); j++)
            {
                Element a = (Element) activeList.item(j);
                activeProfiles.add(a.getTextContent());
            }
        }

        //Get all profiles
        NodeList profileList = doc.getElementsByTagName(PROFILE);
        for (int i = 0; i < profileList.getLength(); i++)
        {
            String url = "";
            Element p = (Element) profileList.item(i);
            String profile = p.getElementsByTagName(ID).item(0).getTextContent();


            NodeList reposList = p.getElementsByTagName(REPOSITORIES);
            Element repoElement = (Element)reposList.item(0);
            NodeList repoList = (repoElement != null) ? repoElement.getElementsByTagName(REPOSITORY) : null;

            //Save the repo url if its a url you are looking for
            if (repoList != null)
            {
                for (int j = 0; j < repoList.getLength(); j++)
                {
                    Element repoElem = (Element) repoList.item(j);
                    String repo = repoElem.getElementsByTagName(URL).item(0).getTextContent();
                    if(urls.contains(repo))
                    {
                        url = repo;
                    }
                }
            }

            NodeList pluginRepoList = p.getElementsByTagName(PLUGIN_REPOSITORIES);
            Element pluginRepoElement = ((Element)pluginRepoList.item(0));
            NodeList pluginList = (pluginRepoElement != null) ? pluginRepoElement.getElementsByTagName(PLUGIN_REPOSITORY) : null;

            //If the plugin repository contains the same url of interest remove url of interest from the urls List
            //It's possible for p.getElementsByTagName(...) to return null, so we can't immediately do .item(0) on it
            if (pluginList != null)
            {
                for (int j = 0; j < pluginList.getLength(); j++)
                {
                    Element pluginElem = (Element) pluginList.item(j);
                    String pluginRepo = pluginElem.getElementsByTagName(URL).item(0).getTextContent();
                    if(url.equals(pluginRepo) && activeProfiles.contains(profile))
                    {
                        urls.remove(url);
                    }
                }
            }
        }
        return urls;
    }
}

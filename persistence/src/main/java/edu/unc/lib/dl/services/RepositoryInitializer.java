/**
 * Copyright 2008 The University of North Carolina at Chapel Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.unc.lib.dl.services;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import edu.unc.lib.dl.fedora.FedoraException;
import edu.unc.lib.dl.fedora.ManagementClient;
import edu.unc.lib.dl.fedora.ManagementClient.Format;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.fedora.ServiceException;
import edu.unc.lib.dl.ingest.IngestException;
import edu.unc.lib.dl.util.ContentModelHelper;
import edu.unc.lib.dl.util.FileUtils;
import edu.unc.lib.dl.util.PremisEventLogger;
import edu.unc.lib.dl.util.TripleStoreQueryService;
import edu.unc.lib.dl.xml.FOXMLJDOMUtil;
import edu.unc.lib.dl.xml.FOXMLJDOMUtil.ObjectProperty;

public class RepositoryInitializer implements Runnable {

	private static final Log log = LogFactory.getLog(RepositoryInitializer.class);
	private String autoinitialize = null;
	DigitalObjectManagerImpl digitalObjectManager = null;

	private MailNotifier mailNotifier = null;

	public MailNotifier getMailNotifier() {
		return mailNotifier;
	}

	public void setMailNotifier(MailNotifier mailNotifier) {
		this.mailNotifier = mailNotifier;
	}

	private FolderManager folderManager = null;
	private Map<String, String> initialAdministrators = null;
	private File initialBatchIngestDir = null;

	private ManagementClient managementClient = null;

	private TripleStoreQueryService tripleStoreQueryService = null;

	public String getAutoinitialize() {
		return autoinitialize;
	}

	public DigitalObjectManagerImpl getDigitalObjectManager() {
		return digitalObjectManager;
	}

	public FolderManager getFolderManager() {
		return folderManager;
	}

	public Map<String, String> getInitialAdministrators() {
		return initialAdministrators;
	}

	public ManagementClient getManagementClient() {
		return managementClient;
	}

	public TripleStoreQueryService getTripleStoreQueryService() {
		return tripleStoreQueryService;
	}

	/**
	 * This is a callback method for Spring bean initialization. It initializes the repository if necessary.
	 */
	public void init() {
		if ("yes".equals(this.getAutoinitialize().toLowerCase())) {
			new Thread(this, "RepositoryAutoInitializer").start();
		}
	}

	@Override
	public void run() {
		try {
			if (this.digitalObjectManager == null || this.folderManager == null || this.initialBatchIngestDir == null
					|| this.initialAdministrators == null || this.managementClient == null
					|| this.tripleStoreQueryService == null) {
				throw new Error("RepositoryInitializer is missing dependencies and cannot run as configured.");
			}

			log.warn("Waiting on Fedora startup to initialize repository, polling..");
			try {
				this.managementClient.pollForObject(ContentModelHelper.Fedora_PID.FEDORA_OBJECT.getPID(), 30, 600);
			} catch (InterruptedException e1) {
				throw new Error("RepositoryInitializer was interrupted.", e1);
			}

			try {
				this.managementClient.getObjectXML(ContentModelHelper.Administrative_PID.REPOSITORY.getPID());
				log.warn("Found an existing repository root, auto-initialization aborted.");
				return;
			} catch (Exception e) {
				log.warn("No existing repository root found, auto-initializing.");
			}

			ingestRepositoryManagementSoftwareAgent();

			ingestContentModelsServices();

			ingestRepositoryFolder();

			// we are going to use the DOM now
			this.getDigitalObjectManager().setAvailable(true, "available");

			ingestAdminFolders();

			this.digitalObjectManager.setAvailable(true);
			log.info("Repository was initialized");
		} catch (ServiceException e) {
			log.fatal("Fedora service misconfigured or unavailable during initialization", e);
		} catch (RuntimeException e) {
			log.fatal("Could not initialize repository, unexpected exception", e);
		} catch (Error e) {
			log.fatal("Could not initialize repository, unexpected exception", e);
		}
	}

	/**
	 *
	 */
	private void ingestRepositoryManagementSoftwareAgent() {
		log.info("Ingesting repository software agent..");
		ContentModelHelper.Administrative_PID repo = ContentModelHelper.Administrative_PID.REPOSITORY_MANAGEMENT_SOFTWARE;
		PID pid = repo.getPID();
		Document doc = FOXMLJDOMUtil.makeFOXMLDocument(pid.getURI());
		FOXMLJDOMUtil.setProperty(doc, ObjectProperty.label, repo.name());
		try {
			this.managementClient.ingest(doc, Format.FOXML_1_1, "initial ingest of repository software agent record");
			this.managementClient.addLiteralStatement(pid, ContentModelHelper.CDRProperty.slug.getURI().toString(),
					"reposoftware", null);
			this.managementClient.addObjectRelationship(pid, ContentModelHelper.FedoraProperty.hasModel.getURI()
					.toString(), ContentModelHelper.Model.SOFTWAREAGENT.getPID());
		} catch (FedoraException e) {
			throw new Error("Cannot create repository software agent", e);
		}
	}

	/**
	 * Creates the initial set of folder for the repository.
	 */
	private void ingestAdminFolders() {
		log.info("Adding Admin Containers...");
		try {
			this.getFolderManager().createPath("/Collections",
					ContentModelHelper.Administrative_PID.ADMINISTRATOR_GROUP.getPID().getURI(),
					ContentModelHelper.Administrative_PID.REPOSITORY_MANAGEMENT_SOFTWARE.getPID().getURI());
		} catch (IngestException e) {
			log.error("Cannot create administrative folders", e);
		}
	}

	/**
	 * @param repoSoftware
	 * @throws FedoraException
	 */
	private void ingestRepositoryFolder() {
		PID root = this.getTripleStoreQueryService().fetchByRepositoryPath("/");
		if (root != null) {
			this.digitalObjectManager.setAvailable(true);
			log.warn("Repository object already exists (previously initialized).  You can disable RepositoryInitializer.autoinitialize in Spring config.");
			return;
		}

		log.info("Creating REPOSITORY Container..");
		// create REPOSITORY folder
		String slug = "REPOSITORY";
		PID pid = ContentModelHelper.Administrative_PID.REPOSITORY.getPID();
		Document foxml = FOXMLJDOMUtil.makeFOXMLDocument(pid.getPid());
		FOXMLJDOMUtil.setProperty(foxml, FOXMLJDOMUtil.ObjectProperty.label, "Repository Folder");

		PremisEventLogger logger = new PremisEventLogger(
				ContentModelHelper.Administrative_PID.REPOSITORY_MANAGEMENT_SOFTWARE.getPID().getURI());
		logger.logEvent(PremisEventLogger.Type.CREATION, "Created repository root container.", pid);

		// upload the MD_EVENTS datastream
		Document MD_EVENTS = new Document(logger.getObjectEvents(pid));
		String eventsLoc = this.getManagementClient().upload(MD_EVENTS);
		Element eventsEl = FOXMLJDOMUtil.makeLocatorDatastream("MD_EVENTS", "M", eventsLoc, "text/xml", "URL",
				"PREMIS Events Metadata", false, null);
		foxml.getRootElement().addContent(eventsEl);

		try {
			PID realpid = this.getManagementClient().ingest(foxml, ManagementClient.Format.FOXML_1_1,
					"initializing repository");
			this.getManagementClient().addLiteralStatement(realpid,
					ContentModelHelper.CDRProperty.slug.getURI().toString(), slug, null);
			this.getManagementClient().addResourceStatement(realpid,
					ContentModelHelper.FedoraProperty.hasModel.getURI().toString(),
					ContentModelHelper.Model.CONTAINER.getURI().toString());
		} catch (Exception e) {
			throw new Error("Cannot create repository folder.", e);
		}
	}

	/**
	 * loads every XML file in foxml-objects, purges their pids and ingests them.
	 * 
	 * @throws FedoraException
	 */
	private void ingestContentModelsServices() {
		log.info("Ingesting content models and services..");
		try {
			File tmpDir = File.createTempFile("RepositoryInitializerBatch", "");
			tmpDir.delete();
			tmpDir.mkdir();
			FileUtils.copyFolder(getInitialBatchIngestDir(), tmpDir);
			getDigitalObjectManager().ingestBatchNow(tmpDir);
		} catch (Exception e) {
			log.error("Cannot copy initial content models and services ingest batch.", e);
		}
	}

	public void setAutoinitialize(String autoinitialize) {
		this.autoinitialize = autoinitialize;
	}

	public void setDigitalObjectManager(DigitalObjectManagerImpl digitalObjectManager) {
		this.digitalObjectManager = digitalObjectManager;
	}

	public void setFolderManager(FolderManager folderManager) {
		this.folderManager = folderManager;
	}

	public void setInitialAdministrators(Map<String, String> initialAdministrators) {
		this.initialAdministrators = initialAdministrators;
	}

	public void setManagementClient(ManagementClient managementClient) {
		this.managementClient = managementClient;
	}

	public void setTripleStoreQueryService(TripleStoreQueryService tripleStoreQueryService) {
		this.tripleStoreQueryService = tripleStoreQueryService;
	}

	public File getInitialBatchIngestDir() {
		return initialBatchIngestDir;
	}

	public void setInitialBatchIngestDir(File initialBatchIngestDir) {
		this.initialBatchIngestDir = initialBatchIngestDir;
	}

}

package edu.unc.lib.workers;

import static edu.unc.lib.dl.util.BagConstants.DESCRIPTION_DIR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.MessageFormat;
import java.util.UUID;

import net.greghaines.jesque.Job;
import net.greghaines.jesque.client.Client;

import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.rdf.model.Model;

import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.util.BagConstants;
import edu.unc.lib.dl.util.PremisEventLogger;
import edu.unc.lib.dl.util.PremisEventLogger.Type;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.transformer.impl.UpdateCompleter;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

/**
 * Constructed with bag directory and deposit ID.
 * Facilitates event logging with standard success/failure states.
 * @author count0
 *
 */
public abstract class AbstractBagJob implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(AbstractBagJob.class);
	public static final String DEPOSIT_QUEUE = "Deposit";
	private File bagDirectory;
	private PID depositPID;
	private String jobUUID;
	public String getJobUUID() {
		return jobUUID;
	}

	public void setJobUUID(String uuid) {
		this.jobUUID = uuid;
	}

	private JobStatusFactory jobStatusFactory;
	
	public JobStatusFactory getJobStatusFactory() {
		return jobStatusFactory;
	}

	public void setJobStatusFactory(JobStatusFactory jobStatusFactory) {
		this.jobStatusFactory = jobStatusFactory;
	}

	private DepositStatusFactory depositStatusFactory;
	public DepositStatusFactory getDepositStatusFactory() {
		return depositStatusFactory;
	}

	public void setBagStatusFactory(DepositStatusFactory depositStatusFactory) {
		this.depositStatusFactory = depositStatusFactory;
	}

	private BagFactory bagFactory = new BagFactory();
	public BagFactory getBagFactory() {
		return bagFactory;
	}
	
	public File getDescriptionDir() {
		return new File(getBagDirectory(), DESCRIPTION_DIR);
	}

	@Autowired
	Client jesqueClient = null;
	public Client getJesqueClient() {
		return jesqueClient;
	}

	public void setJesqueClient(Client jesqueClient) {
		this.jesqueClient = jesqueClient;
	}
	
	private String defaultNextJob = null;
	public String getDefaultNextJob() {
		return defaultNextJob;
	}

	public void setDefaultNextJob(String defaultNextJob) {
		this.defaultNextJob = defaultNextJob;
	}
	
	public void enqueueDefaultNextJob() {
		if(this.defaultNextJob != null) {
			Job job = new Job(this.defaultNextJob, UUID.randomUUID().toString(), getBagDirectory().getAbsolutePath(), this.getDepositPID().getURI());
			jesqueClient.enqueue(DEPOSIT_QUEUE, job);
		}
	}
	
	public void enqueueJob(String jobName) {
		if(jobName != null) {
			Job job = new Job(jobName, UUID.randomUUID().toString(), getBagDirectory().getAbsolutePath(), this.getDepositPID().getURI());
			jesqueClient.enqueue(DEPOSIT_QUEUE, job);
		}
	}

	private PremisEventLogger eventLog = new PremisEventLogger(this.getClass().getName());
	private File eventsFile;

	public AbstractBagJob(String uuid, String bagDirectory, String depositId) {
		log.debug("Bag job created: {} {}", bagDirectory, depositId);
		this.jobUUID = uuid;
		this.bagDirectory = new File(bagDirectory);
		this.eventsFile = new File(bagDirectory, BagConstants.EVENTS_FILE);
		this.depositPID = new PID(depositId);
	}
	
	public AbstractBagJob() {}
	
	public PID getDepositPID() {
		return depositPID;
	}

	public void setDepositPID(PID depositPID) {
		this.depositPID = depositPID;
	}

	public File getBagDirectory() {
		return bagDirectory;
	}

	public void setBagDirectory(File bagDirectory) {
		this.bagDirectory = bagDirectory;
	}

	public PremisEventLogger getEventLog() {
		return eventLog;
	}
	
	public void recordDepositEvent(Type type, String messageformat, Object... args) {
		String message = MessageFormat.format(messageformat, args);
		log.debug("event recorded: {}", message);
		Element event = getEventLog().logEvent(type, message, this.getDepositPID());
		appendDepositEvent(event);
	}
	
	public void failJob(Type type, String message, String details) {
		log.debug("failed deposit: {}", message);
		Element event = getEventLog().logEvent(type, message, this.getDepositPID());
		event = PremisEventLogger.addDetailedOutcome(event, "failed", details, null);
		appendDepositEvent(event);
		throw new JobFailedException(message);
	}
	
	public void failJob(Throwable throwable, Type type, String messageformat, Object... args) {
		String message = MessageFormat.format(messageformat, args);
		log.debug("failed deposit: {}", message);
		Element event = getEventLog().logException(message, throwable);
		event = PremisEventLogger.addLinkingAgentIdentifier(event, "SIP Processing Job", this.getClass().getName(), "Software");
		appendDepositEvent(event);
		throw new JobFailedException(message, throwable);
	}
	
	protected void appendDepositEvent(Element event) {
			File file = new File(bagDirectory, BagConstants.EVENTS_FILE);
	        FileLock lock = null;
	        FileOutputStream out = null;
	        try {
	        	file.createNewFile();
	        	@SuppressWarnings("resource")
				FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
	        	// Get an exclusive lock on the whole file
	            lock = channel.lock();
			    out = new FileOutputStream(file, true);
			    out.write("\n".getBytes());
			    new XMLOutputter(Format.getPrettyFormat()).output(event, out);
			    out.close();
	        } catch(IOException e) {
	        	throw new Error(e);
	        } finally {
	        	IOUtils.closeQuietly(out);
	            try {
					lock.release();
				} catch (IOException e) {
					throw new Error(e);
				}
	        }
	}
	
	protected Bag loadBag() {
		Bag bag = getBagFactory().createBag(getBagDirectory());
		return bag;
	}

	protected void saveBag(gov.loc.repository.bagit.Bag bag) {
		if(eventsFile.exists())	bag.addFileAsTag(eventsFile);
		// TODO serialize object-level events streams
		bag = bag.makeComplete(new UpdateCompleter(getBagFactory()));
		try {
			FileSystemWriter writer = new FileSystemWriter(getBagFactory());
			writer.setTagFilesOnly(true);
			writer.write(bag, getBagDirectory());
			bag.close();
		} catch (IOException e) {
			failJob(e, Type.NORMALIZATION, "Unable to write to deposit bag");
		}
	}

	protected void saveModel(Model model, String tagfilepath) {
		File arrangementFile = new File(this.getBagDirectory(), tagfilepath);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(arrangementFile);
			model.write(fos, "N-TRIPLE");
		} catch(IOException e) {
			throw new Error("Cannot open file "+arrangementFile, e);
		} finally {
			try {
				fos.close();
			} catch (IOException ignored) {}
		}
	}
}
package edu.upenn.cis455.crawler;

/*TODO: 
 * 		- Adjust the key for page storage to remove protocol and domain info
 * 		- Store page meta instead as a secondary database with same key as page data 
 */

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class DatabaseWrapper {

	private Environment db_env;
	private Database documents;

	public DatabaseWrapper(String db_location) throws Exception {
		// Modify to directory name, if necessary
		if (db_location.endsWith("/"))
			db_location.concat("/");

		// Setup Environment
		File file = new File(db_location);
		file.mkdir();
		if (!file.exists() || !file.isDirectory()) {
			if (!file.mkdir())
				throw new IllegalArgumentException(
						"Invalid directory name specified.");
		}
		EnvironmentConfig env_config = new EnvironmentConfig();
		env_config.setAllowCreate(true);
		db_env = new Environment(file, env_config);
		setupDatabases();
	}

	public void setupDatabases() {
		DatabaseConfig config = new DatabaseConfig(), config_dup = new DatabaseConfig();

		config.setAllowCreate(true);
		config_dup.setAllowCreate(true);
		config_dup.setSortedDuplicates(true);

		documents = db_env.openDatabase(null, "documents", config_dup);
	}

	public boolean addDocument(Document doc) throws Exception {
		return ((documents.put(null, stringToDbEntry(doc.getUrl()),
				docToDBEntry(doc))).equals(OperationStatus.SUCCESS));
	}

	// convert a doc to a DatabaseEntry
	private DatabaseEntry docToDBEntry(Document doc) {
		StringBuffer buff = new StringBuffer();

		for (String str : doc.getOutboundLinks()) {
			buff.append(str);
			buff.append(" ");
		}

		String url = doc.getUrl();
		String outboundLinks = buff.toString();
		String size = String.valueOf(doc.getSize());
		String page = doc.getPage();

		buff = new StringBuffer();

		buff.append(url);
		buff.append("<>");
		buff.append(size);
		buff.append("<>");

		buff.append(outboundLinks);
		buff.append("<>");

		buff.append(page);

		return stringToDbEntry(buff.toString());
	}

	public Document getDocument(String url) {
		DatabaseEntry data = new DatabaseEntry();
		OperationStatus status = documents.get(null, stringToDbEntry(url),
				data, LockMode.DEFAULT);
		if (!status.equals(OperationStatus.SUCCESS))
			return null;
		return dbEntryToDoc(data);
	}

	public LinkedList<Document> getAllDocuments() {
		LinkedList<Document> docs = new LinkedList<Document>();
		Cursor cursor = null;
		try {
			cursor = documents.openCursor(null, null);

			DatabaseEntry foundKey = new DatabaseEntry();
			DatabaseEntry foundData = new DatabaseEntry();

			while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				docs.add(dbEntryToDoc(foundData));
			}
		} catch (DatabaseException de) {
			System.err.println("Error accessing database." + de);
		} finally {
			cursor.close();
		}

		return docs;
	}

	private Document dbEntryToDoc(DatabaseEntry entry) {
		LinkedList<String> outboundLinks = new LinkedList<String>();
		String[] arr = dbEntryToString(entry).split("<>", 4);

		outboundLinks.addAll(Arrays.asList(arr[2].split(" ")));

		Document doc = new Document();
		doc.setOutboundLinks(outboundLinks);
		doc.setPage(arr[3]);
		doc.setSize(Integer.parseInt(arr[1]));
		doc.setUrl(arr[0]);

		return doc;
	}

	// USER HANDLING METHODS

	private DatabaseEntry stringToDbEntry(String str) {
		byte[] b = null;
		try {
			b = str.getBytes("UTF-8");
			return new DatabaseEntry(b);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	private String dbEntryToString(DatabaseEntry entry) {
		byte[] b = entry.getData();

		try {
			return new String(b, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// This should be unreachable
		return null;
	}

	void sync() {
		try {
			db_env.sync();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void close() throws Exception {
		this.documents.close();
		db_env.close();
	}
}

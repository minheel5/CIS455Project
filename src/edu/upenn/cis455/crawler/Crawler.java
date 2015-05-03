package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;



public class Crawler implements Runnable {
	
	private static final Logger logger = Logger.getLogger(Crawler.class.getName());
	
	private static DatabaseWrapper db;
	
	private int pageLimit;
	private int searchedPageNum;
	
	public Set<String> seenURLs;
	public Set<byte[]> seenDigests;
	
	public LinkedList<String> searchURLs;
	public LinkedList<Document> docs;
	
	private CrawlerMaster master;
	

	public Crawler(CrawlerMaster master, int pageLimit, Set<byte[]> seenDigests, Set<String> seenURLs, String seed){
		this.seenURLs = seenURLs;
		this.searchURLs = new LinkedList<String>();
		searchURLs.add(seed);
		this.seenDigests = seenDigests;
		this.pageLimit = pageLimit;
		this.searchedPageNum = 0;
		this.docs = new LinkedList<Document>();
		this.master = master;
		//if(db != null){
			try {
				db = new DatabaseWrapper("database");
			} catch (Exception e) {
				e.printStackTrace();
			}
		//}
		
	}
	
	public void run(){
		Timer timer = new Timer();
		timer.schedule(new runCrawler(), 0, 1000);
		
	}
	
	class runCrawler extends TimerTask {
		public void run() {
			if(searchedPageNum < pageLimit && !searchURLs.isEmpty()){
				while(!searchURLs.isEmpty() && searchedPageNum < pageLimit){
					String url = searchURLs.removeFirst();
					processURL(url);
				}
			}
			else if (searchedPageNum >= pageLimit){
				//check modified 
				
			}
				
		}
	}
	
	private void processURL(String url) {
		//logger.info("processing link: " + url);
		if(seenURLs.contains(url)) return; //check last modified?
		seenURLs.add(url);
		
		URL link;
		try{
			link = new URL(url);
		} catch(MalformedURLException e){
			e.printStackTrace();
			return;
		}
		
		if(!validateLink(link)) return;
		
		//process User-Agent: cis455crawler
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) link.openConnection();
			connection.setRequestProperty("User-Agent", "cis455crawler");
			connection.setRequestMethod("HEAD");
			connection.connect();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
			
		int fileLength = connection.getContentLength();
		connection.disconnect();
		
		BufferedReader br;
		try{
			br = new BufferedReader(new InputStreamReader(link.openStream()));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		String line = "";
		try {
			while((line = br.readLine()) != null){
				sb.append(line);
			}
			line = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String file = sb.toString();
		//logger.info("retrieved file");
		
		byte[] digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(file.getBytes());
			digest = md.digest();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(hasSeenDigest(digest)) return;
		
		seenDigests.add(digest);
		LinkedList<String>links = extractLinks(file, url);
		//logger.debug("sending " + links.size() + " links to master");
		master.sendLinks(links);
		
		Document doc = new Document();
		doc.setPage(file);
		doc.setUrl(url);
		doc.setSize(fileLength);
		doc.setOutboundLinks(links);
		
		
		try {
			//logger.debug("starting to add db entry");
			db.addDocument(doc);
			logger.info(searchedPageNum + " doc added to db");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//logger.debug("done");
		
		//logger.debug("doc created");
		//logger.debug("url: " + doc.getUrl());
		//logger.debug("page: " + doc.getPage());
		
		docs.add(doc);
		searchedPageNum++;
		//logger.debug("searched: " + searchedPageNum);

	}
	
	private LinkedList<String> extractLinks(String file, String url){
		//logger.info("extracting links");
		LinkedList<String> links = new LinkedList<String>();
		String path;
		if (url.indexOf("/", url.indexOf("://") + "://".length()) > -1) path = url;
		else path = url + "/";
		
		URI uri = URI.create(path);
		URI altUri = path.endsWith("/") ? null : URI.create(path + "/");
		
		String[] fragments = file.split("</[aA]\\s*>");
		for (String fragment : fragments) {
			if (fragment.matches("(?i).*<a\\b.*href=\"([^\"]*)\".*>.*")) {
				String link = fragment.replaceFirst("(?i).*<a\\b", "").replaceFirst(">.*", "")
					.replaceFirst("(?i).*href=\"([^\"]*)\".*", "$1").trim();
				if (link.length() != 0 && !link.startsWith("#") && 
					!link.matches("(?i)^javascript:.*") && !link.matches("(?i)^mailto:.*")) {
					try {
						link = link.replaceAll("\\s", "%20").replaceAll("&amp;", "&");
						URI linkUri = new URI(link);
						URI resolvedUri = uri.resolve(linkUri);
						links.add(resolvedUri.toString());
						if (altUri != null && !linkUri.isAbsolute()) {
							resolvedUri = altUri.resolve(linkUri);
							links.add(resolvedUri.toString());
						}
					} catch (URISyntaxException e) { 
						//e.printStackTrace();
					}	
				} 	
			}
		}
    	return links;
	}
	
	private boolean hasSeenDigest(byte[] digest){
		for(byte[] d : seenDigests)
			if(Arrays.equals(d, digest))
				return true;
		return false;
	}
	
	private boolean validateLink(URL link){
		//logger.info("validating url: " + link.toString());
		String protocol = link.getProtocol();
		if(!protocol.equalsIgnoreCase("http") && !protocol.equalsIgnoreCase("https")) return false;
		
		//check robots.txt
		String host = link.getHost();
		String robotURL = link.getProtocol() + "://" + host + "/robots.txt";
		URL robotLink;
		try { 
			robotLink = new URL(robotURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		    return false;
		}

		String robotLines = "";
		try {
		    InputStream robotStream = robotLink.openStream();

		    //read robots.txt
		    byte[] byets = new byte[1000];
		    int numBytesRead = robotStream.read(byets);
		    while(numBytesRead != -1) {
		    	robotLines += new String(byets, 0, numBytesRead);
		    	numBytesRead = robotStream.read(byets);
		    	
		    }
		    robotStream.close();
		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}

		String filename = link.getFile();
		int index = 0;
		while ((index = robotLines.indexOf("Disallow", index)) != -1) {
		    index += "Disallow".length();
		    String invalidPath = robotLines.substring(index);
		    StringTokenizer st = new StringTokenizer(invalidPath);
		    if (!st.hasMoreTokens()) break;
		   
		    String badString = st.nextToken();
		    if (filename.indexOf(badString) == 0)
		    	return false;
		}
		return true;
	}
	
	public void assignLinks(LinkedList<String> urls){
		//synchronized(searchURLS)
		searchURLs.addAll(urls);
		
	}
}

package edu.upenn.cis455.crawler;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class CrawlerMaster implements Runnable{
	
	private static final Logger logger = Logger.getLogger(Crawler.class.getName());

	private static int numCrawlers = 1;
	private static int numPages = 200;
	public Set<byte[]> seenDigests;
	public Set<String> seenURLs;
	public LinkedList<String> linksToShuffle;
	static Crawler[] crawlers;
	
	public CrawlerMaster(Set<byte[]> seenDigests, Set<String> seenURLs){
		this.seenDigests = new HashSet<byte[]>();
		this.seenURLs = new HashSet<String>();
		this.linksToShuffle = new LinkedList<String>();
		crawlers = new Crawler[numCrawlers];
	}
	
	@Override
	public void run() {
		Timer timer = new Timer();
		timer.schedule(new shuffleLinks(), 0, 20000);
		
		//updateModifiedFiles
		
	}

	public void sendLinks(LinkedList<String> links){
		linksToShuffle.addAll(links);
	}
	
	class shuffleLinks extends TimerTask {
		public void run() {
			//logger.debug("Starting to distribute " + linksToShuffle.size() + " links");
			BigInteger twoPow160 = BigInteger.ZERO.setBit(160);
			BigInteger max160Bits = twoPow160.subtract(BigInteger.ONE);
			BigInteger section = max160Bits.divide(BigInteger.valueOf(numCrawlers));
			
			LinkedList<String>[] links = new LinkedList[numCrawlers];
			for(int i = 0; i < numCrawlers; i++) links[i] = new LinkedList<String>();
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA1");
				while(!linksToShuffle.isEmpty()){
					String link = linksToShuffle.removeFirst();
					byte[] hashed = md.digest(link.getBytes());
					BigInteger bHashed = new BigInteger(1, hashed);
					int which = bHashed.divide(section).intValue();
					links[which].add(link);
				}
				
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i = 0; i < numCrawlers; i++){
				if(links[i].size() != 0){
					//logger.info("adding " + links[i].size() + " links to crawler " + i);
					crawlers[i].assignLinks(links[i]);
				}
			}
			links = null;
				
		}
	}
	
	public static void main(String args[]){
		
		Set<byte[]> seenDigests = new HashSet<byte[]>();
		Set<String> seenURLs = new HashSet<String>();
		
		CrawlerMaster cm = new CrawlerMaster(seenDigests, seenURLs);
		Thread master = new Thread(cm);
		master.start();
		
		LinkedList<String> seeds = new LinkedList<String>();
		seeds.add("http://www.cnn.com");
		seeds.add("http://www.upenn.edu");
		seeds.add("http://www.nytimes.com");
		seeds.add("http://www.cnet.com");
		
		for(int i = 0; i < numCrawlers; i++){
			//logger.info("creating crawler" + i);
			Crawler c = new Crawler(cm, numPages, seenDigests, seenURLs, seeds.removeFirst());
			Thread crawler = new Thread(c);
			crawlers[i] = c;
			crawler.start();
		}
		
		
	}
	
	
}

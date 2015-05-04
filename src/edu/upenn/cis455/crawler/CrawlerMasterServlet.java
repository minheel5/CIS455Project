package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class CrawlerMasterServlet extends HttpServlet  {

	//private static final Logger logger = Logger.getLogger(CrawlerMasterServlet.class.getName());

	private static final long serialVersionUID = 4568808849713803401L;
	
	private static int numCrawlers = 4;
	//private static int numPages = 200;
	//public Set<byte[]> seenDigests;
	//public Set<String> seenURLs;
	public LinkedList<String> linksToShuffle;
	LinkedList<CrawlerInfo> crawlers;

	@Override
	public void init() {
		this.linksToShuffle = new LinkedList<String>();
		this.crawlers = new LinkedList<CrawlerInfo>();
		
		Timer timer = new Timer();
		timer.schedule(new shuffleLinks(), 0, 20000);

		// updateModifiedFiles

	}
	
	class CrawlerInfo{
		String adds;
		int port;
		public CrawlerInfo(String adds, int port){
			this.adds = adds;
			this.port = port;
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		if (request.getRequestURI().endsWith("/send")) {
			String first = request.getParameter("first");
			if (first.equals("true")){
				String addr = request.getRemoteAddr();
				int port = request.getRemotePort();
				crawlers.add(new CrawlerInfo(addr,port));
			}
			String links = request.getParameter("links");
			linksToShuffle.addAll(Arrays.asList(links.split(" ")));
			
		}
	}

	class shuffleLinks extends TimerTask {
		public void run() {
			BigInteger twoPow160 = BigInteger.ZERO.setBit(160);
			BigInteger max160Bits = twoPow160.subtract(BigInteger.ONE);
			BigInteger section = max160Bits.divide(BigInteger
					.valueOf(numCrawlers));

			String[] links = new String[numCrawlers];
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA1");
				while (!linksToShuffle.isEmpty()) {
					String link = linksToShuffle.removeFirst();
					byte[] hashed = md.digest(link.getBytes());
					BigInteger bHashed = new BigInteger(1, hashed);
					int which = bHashed.divide(section).intValue();
					links[which] += " " + link;
				}

			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < numCrawlers; i++) {
				if (links[i].length() != 0) {
					//send to crawlerServlet
					try {
						CrawlerInfo c = crawlers.get(i);
						Socket socket = new Socket(c.adds, c.port);
						PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
						String params = "links=" + links[i];
						
				        String message = "POST /crawler/distribute HTTP/1.0\r\n";
				        message += "Content-Type: application/x-www-form-urlencoded \r\n";
				        message += "Content-Length: " + params.length() + "\r\n\r\n";
				        message += params;
				        out.println(message);
				        out.flush();
				        socket.close();
					}catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	/*
	public static void main(String args[]) {

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

		for (int i = 0; i < numCrawlers; i++) {
			// logger.info("creating crawler" + i);
			Crawler c = new Crawler(cm, numPages, seenDigests, seenURLs,
					seeds.removeFirst());
			Thread crawler = new Thread(c);
			crawlers[i] = c;
			crawler.start();
		}

	}
	*/

}

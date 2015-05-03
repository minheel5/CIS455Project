package edu.upenn.cis455.crawler;

import java.util.LinkedList;

import org.apache.log4j.Logger;


public class DBTest {
	private static final Logger logger = Logger.getLogger(DBTest.class.getName());
	
	public static void main(String args[])
	{ 
		try {
			DatabaseWrapper db = new DatabaseWrapper("database");
			LinkedList<edu.upenn.cis455.crawler.Document> docs = db.getAllDocuments();
			for (Document doc : docs){
				logger.info(doc.getUrl());
				logger.info(doc.getSize());
				logger.info(doc.getPage());
				logger.info("*****************************************************************");
				for(String link : doc.getOutboundLinks()){
					logger.info(link);
				}
				logger.info("_______________________________________________________________");
			}
			db.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}

package crawler;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class Crawler implements Runnable {
	
	public Set<String> seenURLs;
	
	public LinkedList<String> searchURLs;
	public LinkedList<Document> docs;
	

	public Crawler(){

	}
	
	public void run(){
		
	}

	//given an URL, create/store Document object to docs 
	public void storeDocument(String Url) {

	}


	//extract all links in a page 
    private List<String> extractLinks(String url, String page) {
    	List<String> links = new LinkedList<String>();
		return links;
    	
    }

	//check robots.txt 
	private boolean isValid(URL url){
		return false;
		
	}
    
	//send links to process
	public void sendLinks(List<String> links) {
		
	}
	
	public void addLinks(List<String> urls) {
		for(String u : urls)
			searchURLs.add(u);
	}
}

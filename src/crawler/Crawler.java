package crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;


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

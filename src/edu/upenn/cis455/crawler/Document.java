package edu.upenn.cis455.crawler;

import java.util.LinkedList;

public class Document {
	private String page;
	private String url;
	private int size;
	private LinkedList<String> outboundLinks;
	
	public String getPage() {
		return page;
	}
	
	public void setPage(String page) {
		this.page = page;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public LinkedList<String> getOutboundLinks(){
		return outboundLinks;
	}
	
	public void setOutboundLinks(LinkedList<String> outboundLinks){
		this.outboundLinks = outboundLinks;
	}
	
}

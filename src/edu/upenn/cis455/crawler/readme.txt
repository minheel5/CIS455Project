1) Min Hee Lee, lemin@seas.upenn.edu

2) Features implemented
 - Mercator-style crawler with 4 CrawlerServlets and CrawlerMasterServlet that communicate through HTTP
 - Each crawler takes seed URLs to start, validates URLs (check for robots.txt restrictions, if a url is already seen, and etc), downloads/parses HTML documents, extract links on a page, and stores the crawled documents in Berkeley DB  
 - CrawlerMasterServlet receives extracted links from CrawlerServlets, periodically shuffles and re-distributes them to the crawlers.   
 - CrawlerServlet also receives get requests and sends back its locally stored crawled pages (for pageRank and indexer).

3) Extra credit
 - Added support for digests to detect that the same doc has been retrieved under different URLs. I used MessageDigest to hash the content and check if the content has been seen before.

4) Source Files 
CrawlerServlet.java
DatabaseWrapper.java
DBTest.java
Document.java
CrawlerMasterServlet.java

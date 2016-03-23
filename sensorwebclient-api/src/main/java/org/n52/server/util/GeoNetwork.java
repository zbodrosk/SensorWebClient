package org.n52.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.n52.oxf.util.web.HttpClientException;
import org.n52.oxf.util.web.SimpleHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GeoNetwork {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GeoNetwork.class);
	
	public static Set<String> getGeoNetworkSosLinks(String serverUrl) {
		
		LOGGER.info("<<<<<< START getGeoNetworkSosLinks()");
		
		Set<String> sosSet = new HashSet<String>();
		SimpleHttpClient simpleClient = new SimpleHttpClient();
		try {
			HttpResponse response = simpleClient.executeGet(serverUrl + "/srv/eng/xml.search");
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity responseEntity = response.getEntity();
	            InputStream is = responseEntity.getContent();
	            
	            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        	Document doc = dBuilder.parse(is);

	        	NodeList nList = doc.getElementsByTagName("id");
	        	
	        	for (int i=0; i<nList.getLength(); i++) {
	        		Node nNode = nList.item(i);
	        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//	        			
	        			response = simpleClient.executeGet(serverUrl + "/srv/eng/xml.relation?id=" + nNode.getTextContent());
//	        			response = simpleClient.executeGet(serverUrl + "/srv/eng/xml.metadata.get?id=" + nNode.getTextContent());
	        			if (response.getStatusLine().getStatusCode() == 200) {
	        				responseEntity = response.getEntity();
	        				InputStream is1 = responseEntity.getContent();
	        	            
	        	            DocumentBuilderFactory dbFactory1 = DocumentBuilderFactory.newInstance();
	        	        	DocumentBuilder dBuilder1 = dbFactory1.newDocumentBuilder();
	        	        	Document doc1 = dBuilder1.parse(is1);

	        	        	NodeList nList1 = doc1.getElementsByTagName("url");
//	        	        	NodeList nList1 = doc1.getElementsByTagName("gmd:URL");
	        	        	
	        	        	for (int j=0; j<nList1.getLength(); j++) {
	        	        		Node nNode1 = nList1.item(j);
	        	        		if (nNode1.getNodeType() == Node.ELEMENT_NODE) {
	        	        			if (!nNode1.getTextContent().contains("?")) {
	        	        				boolean isAdded = sosSet.add(nNode1.getTextContent());
	        	        				if (isAdded)
	        	        					LOGGER.info("URL added to set: " + nNode1.getTextContent() + ", MedatadaID: " + nNode.getTextContent());
	        	        			}
	        	        		}
	        	        	}
	        			}
	        		}
	        	}
			}
		} catch (HttpClientException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LOGGER.info(">>>>>> END getGeoNetworkSosLinks()");
		
		return sosSet;
	}

}

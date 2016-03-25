package org.n52.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
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
import org.w3c.dom.Element;
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
	
	public static HashMap<String, String> validateSOSLinks(Set<String> sosSet){
		LOGGER.info("SOS URLs validation started");
		HashMap<String, String> vSosSet = new HashMap<String, String>();
		
		///test!!!
		sosSet.add("http://getit.lter-europe.net/observations/sos");
		///
		 
		if(sosSet.size() > 0){
			for(String url: sosSet){
				LOGGER.info("!#!#!# URL "+ url);
//				if(url.equals("http://sdf.ndbc.noaa.gov/sos/server.php") || url.equals("http://getit.lter-europe.net/observations/sos")){
				String sosVersion = null;
				SimpleHttpClient simpleClient = new SimpleHttpClient();
				try{
					HttpResponse response = simpleClient.executeGet(url+"?REQUEST=GetCapabilities&SERVICE=SOS");
					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity responseEntity = response.getEntity();
			            InputStream is = responseEntity.getContent();
			            
			            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			        	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			        	Document doc = dBuilder.parse(is);
//			        	Document doc = dBuilder.parse(new URL(url+"?REQUEST=GetCapabilities&SERVICE=SOS").openStream());

			        	NodeList nlParameter = doc.getElementsByTagName("ows:Parameter");
			        	if(nlParameter.getLength() > 0){
			        		Set<String> allowedValues = new HashSet<String>();
			        		for(int i = 0; i < nlParameter.getLength(); i++){
//				        		LOGGER.info("nn: "+ nlParameter.item(i).getNodeName());
				        		Node nParameter = nlParameter.item(i);
				        		if(nParameter.getNodeType() == Node.ELEMENT_NODE){
				        			Element el = (Element) nParameter;
//				        			LOGGER.info("name: "+ el.getAttribute("name"));
				        			if(el.getAttribute("name").equals("version") || el.getAttribute("name").equals("AcceptVersions")){
				        				NodeList nlValue = el.getElementsByTagName("ows:Value");
				        				if(nlValue.getLength() > 0){
				        					for(int j = 0; j < nlValue.getLength(); j++){
				        						String version = nlValue.item(j).getTextContent();
//				        						LOGGER.info("Version: "+ version);
				        						if(version.equals("1.0.0") || version.equals("2.0.0")){
				        							allowedValues.add(version);
				        						}
				        					}
				        				}
				        			}
				        		}
			        		}
        					if(allowedValues.size() > 0){
        						if(allowedValues.contains("2.0.0")){
        							sosVersion = "2.0.0";
//        							LOGGER.info("sosVersion = 2.0.0!!!");
        							vSosSet.put(url, "2.0.0");
        						}else if(allowedValues.contains("1.0.0")){
        							sosVersion = "1.0.0";
//        							LOGGER.info("sosVersion = 1.0.0!!!");
        							vSosSet.put(url, "1.0.0");
        						}	
        					}
			        	}
					}else{
						LOGGER.info("Response !200: "+ response.getStatusLine().getStatusCode() + " " + url);
					}
				} catch (HttpClientException e) {
//					e.printStackTrace();
					LOGGER.warn("HTTPClientException: "+ url);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					LOGGER.warn("IllegalStateException: "+ url);
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					LOGGER.warn("IOException: "+ url);
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					LOGGER.warn("ParserConfigurationException: "+ url);
				} catch (SAXException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					LOGGER.warn("SAXException: "+ url);
				}
//			}
				
			}
		}
		
		LOGGER.info("SOS URLs validation ended");
		return vSosSet;
	}

}

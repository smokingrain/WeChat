package com.xk.utils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;

public class XMLUtils {
	
	public static Element createElement(String name){
		return DocumentHelper.createDocument().addElement(name);
	}
	
	public static Document fromText(String text){
		try {
			return DocumentHelper.parseText(text);
		} catch (DocumentException e) {
			return null;
		}
	}
	
	public static boolean isMessage(Document doc){
		return null!=doc&&"message".equals(doc.getRootElement().getName());
	}
	
	public static boolean isPresence(Document doc){
		return null!=doc&&"presence".equals(doc.getRootElement().getName());
	}
	
	public static boolean isIQ(Document doc){
		return null!=doc&&"iq".equals(doc.getRootElement().getName());
	}
	
	public static void main(String[] args){
//		String xml="<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"   xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">  </urlset> ";
		String xml="<sttttream:stream xmlns=\"jabber:client\" xmlns:sttttream=\"http://etherx.jabber.org/streams\" version=\"1.0\" to=\"dji.com\"/>";
		Document doc=fromText(xml);
		Element root=doc.getRootElement();
		Namespace ns=root.getNamespace();
		System.out.println(ns);
	}
}

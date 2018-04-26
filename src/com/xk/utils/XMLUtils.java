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
	}
}

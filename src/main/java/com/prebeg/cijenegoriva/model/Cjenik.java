package com.prebeg.cijenegoriva.model;

import java.io.File;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Cjenik {
	
	@XmlElement(name="gorivo")
	private List<Gorivo> goriva = new LinkedList<Gorivo> ();
	
	public void sort ()  {
		Collections.sort(goriva);
	}
	
	private void addGorivo(Gorivo gorivo)  {
		goriva.add(gorivo);
	}
	
	public void addGoriva(List<Gorivo> newGoriva) {
		goriva.addAll(newGoriva);
	}
	
	public List<Gorivo> getGoriva() {
		return goriva;
	}
	
	public void remove (Gorivo gorivo) {
		goriva.remove(gorivo);
	}
	
	public String toString () {
		
		String str = new String();
		StringWriter sw = new StringWriter();
		
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Cjenik.class);
			 Marshaller m = context.createMarshaller();
			 m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			 m.marshal(this, sw);
			 
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sw.toString();
	}
}

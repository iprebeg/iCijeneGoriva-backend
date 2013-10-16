package com.prebeg.cijenegoriva.data;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.springframework.stereotype.Component;

import com.prebeg.cijenegoriva.model.Cjenik;

@Component
public class CachedCjenik {
	
	private String xmlFileName = "/tmp/cijenegoriva.xml";
	
	private boolean cacheFileExpired (File file) {
		
		long lastModified = file.lastModified();
		long now = System.currentTimeMillis();
		long validFor = 1000 * 60 * 60 * 6; // 6h 
		//long validFor = 1000; // 1 sec
		if ((now - lastModified) > validFor) 
			return true;
		
		return false;
	}
	
	public Cjenik getCjenik() {
		
		JAXBContext context;
		Cjenik cjenik = null;
		
		try {
			
			File xmlFile = new File(xmlFileName);
			
			if (cacheFileExpired(xmlFile))
				return null;
			
			
			context = JAXBContext.newInstance(Cjenik.class);
			Unmarshaller um = context.createUnmarshaller();
			
			cjenik = (Cjenik) um.unmarshal(new File(xmlFileName));
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return cjenik;
	}
	
	public void saveCjenik(Cjenik cjenik) {
		
		System.out.println("marshaling....");
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Cjenik.class);
			 Marshaller m = context.createMarshaller();
			 m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			 m.marshal(cjenik, new File(xmlFileName));
			 
			 //m.marshal(cjenik, System.out);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
}

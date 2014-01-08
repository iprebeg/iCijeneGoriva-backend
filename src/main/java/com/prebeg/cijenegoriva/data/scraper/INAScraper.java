package com.prebeg.cijenegoriva.data.scraper;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.prebeg.cijenegoriva.model.Gorivo;
import com.prebeg.cijenegoriva.notification.NotificationService;

@Component
public class INAScraper implements Scraper {
	

	@Resource
	NotificationService notificationService;
	
	private String baseurl = "http://www.ina.hr/default.aspx?id=203";
	
	private void resolveAutocesta (Gorivo gorivo) {
		if (gorivo.getNaziv().contains("autocesta")) {
			gorivo.setAutocesta("YES");
			gorivo.setNaziv(gorivo.getNaziv().replaceFirst("na benzinskim postajama na autocestama \\*", ""));
		} else 
			gorivo.setAutocesta("NO");
	}
	
	private void resolveCategory (Gorivo gorivo) {
		if (gorivo.getNaziv().contains("ulje") || 
			gorivo.getNaziv().contains("Plavi")	
		) {
			gorivo.setKategorija("Lozulje");
		}
		else 
		if (gorivo.getNaziv().contains("plin")) {
			gorivo.setKategorija("Autoplin");
		}
		else 
		if (gorivo.getNaziv().contains("diesel") ||
			gorivo.getNaziv().contains("DIESEL") ||
			gorivo.getNaziv().contains("DIZEL") ||
			gorivo.getNaziv().contains("Dizel") ||
			gorivo.getNaziv().contains("dizel") 	
		) { 
			gorivo.setKategorija("Dizel");
		}
		else 
		if (gorivo.getNaziv().contains("Eurosuper") ||
			gorivo.getNaziv().contains("super") ||
			gorivo.getNaziv().contains("SUPER")	
		) {
			if (gorivo.getNaziv().contains("98") ||
				gorivo.getNaziv().contains("100") 
			) 
				gorivo.setKategorija("Super98");
			else 
				gorivo.setKategorija("Super95"); 
		}
		else
			gorivo.setKategorija("sigh blah"); 

	}

	public List<Gorivo> scrape(WebClient wc) {
		
		try {
			List<Gorivo> inaGoriva = new LinkedList<Gorivo>();
			
			System.out.print("Scraping INA   ");
			
			final HtmlPage page = wc.getPage(baseurl);
					
	
			//System.out.println(page.asXml());
	
			String datumText = ((HtmlDivision)page.getByXPath( "//div[@class='pageBody']").get(0)).asText();
	
			Pattern p = Pattern.compile("Datum: (.*) ");
			Matcher m = p.matcher(datumText);
	
			String datum = "";
			if (m.find()) {
			    datum = m.group(1) + ".";
			}
		
	
			final HtmlTable table = (HtmlTable)page.getElementsByTagName("table").item(10); 
			
			//System.out.println("##############################\n"+ table.asXml());
			
			Gorivo currGorivo = null;
			for (final HtmlTableRow row : table.getRows()) {
				
				// first element is shit, so init gorivo and skip it
				if (currGorivo == null) {
					currGorivo = new Gorivo();
					continue;
				} else {
					currGorivo = new Gorivo();
				}
				
				
			
				currGorivo.setNaziv(row.getCells().get(0).asText());
				currGorivo.setCijena(row.getCells().get(2).asText());
				currGorivo.setDistributer("INA");
				resolveCategory (currGorivo);
				resolveAutocesta (currGorivo);
				currGorivo.setDatum(datum);
				
				
				/*
			    System.out.println("Found row");
			    
			    for (final HtmlTableCell cell : row.getCells()) {
			        System.out.println("   Found cell: " + cell.asText());
			    }
			    */
			    
			    
			    inaGoriva.add(currGorivo);
			}
			
			System.out.println ("[OK]");
			
			return inaGoriva;
		} catch (Exception e) {
			System.out.println("[FAIL]");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			if (notificationService != null) {
				notificationService.sendEmail("[CijeneGoriva] Exception occured", sw.toString());
			} else {
				//System.out.println("FUCK, NOTIFICATION IS NULL");
			}
		}
		return null;
	}
}

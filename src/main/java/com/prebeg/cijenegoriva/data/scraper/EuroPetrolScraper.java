package com.prebeg.cijenegoriva.data.scraper;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.prebeg.cijenegoriva.model.Gorivo;
import com.prebeg.cijenegoriva.notification.NotificationService;

@Component
public class EuroPetrolScraper implements Scraper {
	

	@Resource
	NotificationService notificationService;
	
	private String baseurl = "http://www.europetrol.hr/";
	
	private void resolveAutocesta (Gorivo gorivo) {
		if (gorivo.getNaziv().contains("autocesta")) {
			gorivo.setAutocesta("YES");
			gorivo.setNaziv(gorivo.getNaziv().replaceFirst("na benzinskim postajama na autocestama \\*", "(autocesta)"));
		} else 
			gorivo.setAutocesta("NO");
	}
	
	private void resolveCategory (Gorivo gorivo) {
		if (gorivo.getNaziv().contains("ulje") || 
			gorivo.getNaziv().contains("LUEL") || 
			gorivo.getNaziv().contains("LU-EL")	
		) {
			gorivo.setKategorija("Lozulje");
		}
		else 
		if (gorivo.getNaziv().contains("plin") || 
			gorivo.getNaziv().contains("LPG")
		) {
			gorivo.setKategorija("Autoplin");
		}
		else 
		if (gorivo.getNaziv().contains("diesel") ||
			gorivo.getNaziv().contains("DG") ||
			gorivo.getNaziv().contains("DIZEL") ||
			gorivo.getNaziv().contains("Dizel") ||
			gorivo.getNaziv().contains("dizel") 	
		) { 
			gorivo.setKategorija("Dizel");
		}
		else 
		if (gorivo.getNaziv().contains("98") ||
			gorivo.getNaziv().contains("100")) { 
				gorivo.setKategorija("Super98");
		}else if (gorivo.getNaziv().contains("95"))
		{
				gorivo.setKategorija("Super95");
		}
		else
			gorivo.setKategorija("sigh blah"); 

	}

	public List<Gorivo> scrape(WebClient wc) {
		
		try {
		
			List<Gorivo> euroPetrolGoriva = new LinkedList<Gorivo>();
			
			System.out.print("Scraping EUROPETROL   ");
			
			final HtmlPage page = wc.getPage(baseurl);
			
			//System.out.println("##############################\n"+ page.asXml());
	
			final HtmlTable table = (HtmlTable)page.getElementsByTagName("table").item(22); 
			
			//System.out.println("##############################\n"+ table.asXml());
			
			List<HtmlTableRow> rowFuels = table.getRows();
			
			// datum nije naveden, stavi danas jebiga
			String datum = "Nepoznat";	
			Calendar calendar = Calendar.getInstance();
		    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy.");        
		    datum = dateFormat.format(calendar.getTime());
			
			
			String autocesta = "NO";
			
			Gorivo currGorivo = null;
			
			for (final HtmlTableRow row : rowFuels) {
				
				if (currGorivo == null) {
					currGorivo = new Gorivo();
					continue;
				} else {
					currGorivo = new Gorivo();
				}
				
				currGorivo.setDistributer("EuroPetrol");
			
				currGorivo.setNaziv(row.getCell(0).asText().trim());
				resolveCategory (currGorivo);
				
				currGorivo.setCijena(row.getCell(1).asText().trim());
				
				currGorivo.setDatum(datum);
				currGorivo.setAutocesta(autocesta);
			
			    euroPetrolGoriva.add(currGorivo);
			}
		
			System.out.println("[OK]");
			
			return euroPetrolGoriva;
		} catch (Exception e) {
			System.out.println("[FAIL]");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace();
			if (notificationService != null) {
				notificationService.sendEmail("[CijeneGoriva] Exception occured", sw.toString());
			} else {
				//System.out.println("FUCK, NOTIFICATION IS NULL");
			}
		}
		
		return null;
	}
}

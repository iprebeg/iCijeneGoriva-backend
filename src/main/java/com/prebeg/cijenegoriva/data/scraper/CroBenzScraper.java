package com.prebeg.cijenegoriva.data.scraper;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.prebeg.cijenegoriva.model.Gorivo;
import com.prebeg.cijenegoriva.notification.NotificationService;

@Component
public class CroBenzScraper implements Scraper {
	
	@Resource
	NotificationService notificationService;
	
	private String baseurl = "http://www.crobenz.hr/crobenz_malpro/";
	
	private void resolveAutocesta (Gorivo gorivo) {
		if (gorivo.getNaziv().contains("autocesta")) {
			gorivo.setAutocesta("YES");
			gorivo.setNaziv(gorivo.getNaziv().replaceFirst("na benzinskim postajama na autocestama \\*", "(autocesta)"));
		} else 
			gorivo.setAutocesta("NO");
	}
	
	private void resolveCategory (Gorivo gorivo) {
		if (gorivo.getNaziv().contains("ulje") || 
			gorivo.getNaziv().contains("LUEL")	
		) {
			gorivo.setKategorija("Lozulje");
		}
		else 
		if (gorivo.getNaziv().contains("plin")) {
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
		if (gorivo.getNaziv().contains("EURO") ||
			gorivo.getNaziv().contains("Super") ||
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
		
			List<Gorivo> crobenzGoriva = new LinkedList<Gorivo>();
		
			System.out.print("Scraping CROBENZ   ");
			
			final HtmlPage page = wc.getPage(baseurl);
			
			final String pageAsXml = page.asXml();
			
			//System.out.println("##############################\n"+ pageAsXml);
			//System.out.println("##############################\n");
	
			final HtmlTable table = (HtmlTable)page.getElementsByTagName("table").item(0); 
			
			//System.out.println("##############################\n"+ table.asXml());
			
			Gorivo currGorivo = null;
			
			HtmlTableRow rowNames = table.getRow(0);
			HtmlTableRow rowPrices = table.getRow(1);
			
			String datum = rowPrices.getCell(0).asText();
			String autocesta = "NO";
			
			for (final HtmlTableCell cell : rowNames.getCells()) {
				
				currGorivo = new Gorivo();
			
				currGorivo.setNaziv(cell.asText().trim());
				currGorivo.setDistributer("CROBENZ");
				resolveCategory (currGorivo);
				
				currGorivo.setDatum(datum);
				currGorivo.setAutocesta(autocesta);
			
			    crobenzGoriva.add(currGorivo);
			}
			
			currGorivo = null;
			
			for (final HtmlTableCell cell : rowPrices.getCells()) {
				
				currGorivo = crobenzGoriva.get(rowPrices.getCells().indexOf(cell));
				currGorivo.setCijena(cell.asText());
			}
			
			crobenzGoriva.remove(0);
			
			System.out.println("[OK]");
			
			return crobenzGoriva;
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

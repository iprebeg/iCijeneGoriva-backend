package com.prebeg.cijenegoriva.data.scraper;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.prebeg.cijenegoriva.model.Gorivo;
import com.prebeg.cijenegoriva.notification.NotificationService;

@Component
public class TifonScraper {
	

	@Resource
	NotificationService notificationService;
	
	private String baseurl = "http://www.tifon.hr/default.aspx?id=15";
	private String baseurlAutocesta = "http://www.tifon.hr/default.aspx?id=81";
	
	private String stripCijena(String cijena)  {
		return cijena.replace(" ", "").replace(".", ",");
	}
	
	private String stripDatum(String datum) {
		return datum.replace("Cijene goriva važeće od", "").replace("Cijene goriva na benzinskim postajama na autocestama važeće od", "").replace(" ", "");
	}
	
	private void resolveAutocesta (Gorivo gorivo) {
		if (gorivo.getNaziv().contains("autocesta")) {
			gorivo.setAutocesta("YES");
			gorivo.setNaziv(gorivo.getNaziv().replaceFirst("na benzinskim postajama na autocestama \\*", "(autocesta)"));
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
		if (gorivo.getNaziv().contains("super") ||
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
			System.out.print("Scraping Tifon  ");
			
			List<Gorivo> tifonGoriva = new LinkedList<Gorivo>();
			final List<HtmlTable> tables = new LinkedList<HtmlTable>();
			
			final HtmlPage page = wc.getPage(baseurl);
			final HtmlPage pageAutocesta = wc.getPage(baseurlAutocesta);
			
			
			//System.out.print(page.asXml());
			
			tables.add((HtmlTable)pageAutocesta.getElementsByTagName("table").item(1));
			tables.add((HtmlTable)page.getElementsByTagName("table").item(1));
	
			for (HtmlTable table : tables) {
			
				//System.out.println("##############################\n"+ table.asXml());
			
				String datum = ((HtmlTableRow)table.getRows().get(0)).asText();
					
				String autocesta = null;
				if (datum.contains("autocestama"))
					autocesta = "YES";
				else
					autocesta = "NO";
	
				datum = stripDatum(datum);
			
				Gorivo currGorivo = null;
				
				//System.out.println("there is rows:" + table.getRows().size());
				
				for (final HtmlTableRow row : table.getRows()) {
			
					if (row == table.getRows().get(0) || row == table.getRows().get(1))
						continue;
					
					//System.out.println("##############################\n"+ row.asXml());
					currGorivo = new Gorivo();
					
					currGorivo.setNaziv(row.getCells().get(0).asText());
					currGorivo.setCijena(stripCijena(row.getCells().get(1).asText()));
					currGorivo.setDistributer("Tifon");
					currGorivo.setAutocesta(autocesta);
					currGorivo.setDatum(datum);
					resolveCategory (currGorivo);
					
					tifonGoriva.add(currGorivo);
				
				}
					
			}
			
			System.out.println("[OK]");
			
			return tifonGoriva;
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

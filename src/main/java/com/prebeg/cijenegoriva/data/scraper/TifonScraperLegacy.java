package com.prebeg.cijenegoriva.data.scraper;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

@Component
public class TifonScraperLegacy {
	
	private String baseurl = "http://www.tifon.hr/default.asp?ru=152&amp;akcija=";
	
	private String stripCijena(String cijena)  {
		return cijena.replace(" ", "");
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

	public List<Gorivo> scrape(WebClient wc) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		
		List<Gorivo> tifonGoriva = new LinkedList<Gorivo>();
		
		System.out.print("Scraping Tifon  ");
		
		final HtmlPage page = wc.getPage(baseurl);
		
		final String pageAsXml = page.asXml();
		
		final HtmlElement elementi = (HtmlElement)page.getElementsByTagName("elementi").item(0); 
		
		final List<HtmlTable> tables = new LinkedList<HtmlTable>();
		
		tables.add((HtmlTable)elementi.getElementsByTagName("table").item(0));
		tables.add((HtmlTable)elementi.getElementsByTagName("table").item(2));

		for (HtmlTable outtable : tables) {
		
			//System.out.println("##############################\n"+ outtable.asXml());
			
			
			String datum = ((HtmlTableRow)outtable.getRows().get(0)).asText();
			
			
			String autocesta = null;
			if (datum.contains("autocestama"))
				autocesta = "YES";
			else
				autocesta = "NO";

			datum = stripDatum(datum);
			
			//System.out.println("############DATUM ##\n"+ datum);

			HtmlTable table = (HtmlTable)outtable.getElementsByTagName("table").item(0);
			//System.out.println("##############################\n"+ table.asXml());

			
			
			
			Gorivo currGorivo = null;
			
			
			
			for (final HtmlTableRow row : table.getRows()) {
		
				if (row.getCells().size() == 0 || row.getCells().get(0).asText().equals("Naziv goriva"))
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
	}
}

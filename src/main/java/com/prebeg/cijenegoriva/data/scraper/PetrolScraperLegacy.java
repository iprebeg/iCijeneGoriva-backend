package com.prebeg.cijenegoriva.data.scraper;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.prebeg.cijenegoriva.model.Gorivo;

@Component
public class PetrolScraperLegacy {
	
	private String baseurl = "http://www.petrol.hr/index.php?sv_path=98,104";
	
	private String stripCijena(String cijena)  {
		String c =  cijena.replace(" HRK / L", "");
		return c.substring(0, c.length() - 1);
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
		
		List<Gorivo> petrolGoriva = new LinkedList<Gorivo>();
		
		System.out.print("Scraping Petrol   ");

		
		final HtmlPage page = wc.getPage(baseurl);
		
		final String pageAsXml = page.asXml();
		
		//System.out.println("##############################\n"+ pageAsXml);
		//System.out.println("##############################\n");
		
		//List<> 
		List<HtmlDivision> divs = (List<HtmlDivision>) page.getByXPath( "//div[@class='listitem']");

		Gorivo currGorivo = null;
	
		for (HtmlDivision div : divs) {
			
			List<HtmlDivision> div_cijena = (List<HtmlDivision>) div.getByXPath( ".//div[@class='gibanje_cen']");
			
			if (div_cijena.size() == 0) {
				//System.out.println(">>>>>");
				continue;
			}
			
			currGorivo = new Gorivo();
			
			currGorivo.setNaziv(((HtmlElement)div.getByXPath( ".//h3").get(0)).getTextContent().replace("*", ""));
			currGorivo.setCijena(stripCijena(((HtmlDivision)div.getByXPath( ".//div[@class='gibanje_cen_vrednost']").get(0)).getTextContent()));
			currGorivo.setDatum(((HtmlSpan)div.getByXPath( ".//span[@class='gibanje_cen_datum']").get(0)).getTextContent() + ".");			
			currGorivo.setDistributer("Petrol");
			
			resolveCategory (currGorivo);
			resolveAutocesta (currGorivo);
			
		    petrolGoriva.add(currGorivo);
		}
		
		System.out.println("[OK]");
		
		return petrolGoriva;
	}
}

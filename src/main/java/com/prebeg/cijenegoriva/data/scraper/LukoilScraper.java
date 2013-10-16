package com.prebeg.cijenegoriva.data.scraper;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.prebeg.cijenegoriva.model.Gorivo;
import com.prebeg.cijenegoriva.notification.NotificationService;

@Component
public class LukoilScraper {
	

	@Resource
	NotificationService notificationService;
	
	private String baseurl = "http://www.lukoil.hr";
	
	private String stripCijena(String cijena)  {
		return cijena.replace("|", "").replace("kn", "").replace(" ", "");
	}
	
	private String stripDatum(String datum) {
		//Pattern p = Pattern.compile("^[a-zA-Z]+([0-9]+).*");
		Pattern p = Pattern.compile("od (\\d{2}\\.\\d{2}\\.\\d{4}\\.)");
		
		Matcher m = p.matcher(datum);

		String parsedDate = "";
		if (m.find()) {
		    //System.out.println("********* NOVI DATUM **********:" + m.group(1));
			parsedDate = m.group(1);
		}
		
		//return datum.replace("Cjenik vrijedi od", "").replace(" ", "").trim();
		
		return parsedDate;
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
			gorivo.getNaziv().contains("ULJE")	
		) {
			gorivo.setKategorija("Lozulje");
		}
		else 
		if (gorivo.getNaziv().contains("PLIN")) {
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
			gorivo.setKategorija(""); 

	}

	public List<Gorivo> scrape(WebClient wc) {
		
		try {
			List<Gorivo> lukoilGoriva = new LinkedList<Gorivo>();
			
			System.out.print("Scraping Lukoil  ");
			
			final HtmlPage page = wc.getPage(baseurl);
			
			final String pageAsXml = page.asXml();
			
			//System.out.println("##############################\n"+ pageAsXml);
			//System.out.println("##############################\n");

			List<HtmlDivision> kartaContDivs = (List<HtmlDivision>) page.getByXPath( "//div[@class='gorivo-karta-cont']");
			HtmlDivision kartaDiv = kartaContDivs.get(0);
			Iterable<HtmlElement> childs = kartaDiv.getChildElements();
			Iterator<HtmlElement> chit = childs.iterator();
			
			// skip anchor 
			chit.next();
			chit.next();
			
			String d1 = chit.next().asText();
			String d2 = chit.next().asText();
			
			//System.out.println("d1:" + d1);
			//System.out.println("d2:" + d2);
			
			String gorivaDatum = null;
			String autoplinDatum = null;
			
			if (d1.contains("Autoplin")) {
				autoplinDatum = d1;
				gorivaDatum = d2;
			} else {
				gorivaDatum = d1;
				autoplinDatum = d2;
			}
			
			//System.out.println("G:" + gorivaDatum);
			//System.out.println("A:" + autoplinDatum);
			
			gorivaDatum = stripDatum(gorivaDatum);
			autoplinDatum = stripDatum(autoplinDatum);
			
			//System.out.println("Gs:" + gorivaDatum);
			//System.out.println("As:" + autoplinDatum);
			
			List<HtmlDivision> cijenedivs = (List<HtmlDivision>) page.getByXPath( "//div[@class='gorivo-item-box-cijena']");
			List<HtmlDivision> nazividivs = (List<HtmlDivision>) page.getByXPath( "//div[@class='gorivo-item-box-naziv']");
			
			Iterator<HtmlDivision> cijeneIt = cijenedivs.iterator();
			Iterator<HtmlDivision> naziviIt = nazividivs.iterator();
			
			Gorivo currGorivo = null;
			while (cijeneIt.hasNext() && naziviIt.hasNext()) 
			{
				String cijena = cijeneIt.next().asText();
				String naziv = naziviIt.next().asText();
				
				//System.out.println("name:" + naziv);
				//System.out.println("price:" + cijena);
				
				currGorivo = new Gorivo();
				
				currGorivo.setNaziv(naziv);
				currGorivo.setCijena(stripCijena(cijena));
				currGorivo.setDistributer("Lukoil");
				resolveCategory (currGorivo);
				resolveAutocesta (currGorivo);
				if (currGorivo.getKategorija().contains("Autoplin"))
					currGorivo.setDatum(autoplinDatum);
				else
					currGorivo.setDatum(gorivaDatum);
			    lukoilGoriva.add(currGorivo);
			}
			
			System.out.println("[OK]");
			
			return lukoilGoriva;
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
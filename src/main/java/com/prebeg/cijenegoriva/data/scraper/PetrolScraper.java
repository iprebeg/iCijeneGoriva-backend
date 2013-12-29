package com.prebeg.cijenegoriva.data.scraper;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;
import com.prebeg.cijenegoriva.model.Gorivo;
import com.prebeg.cijenegoriva.notification.NotificationService;

@Component
public class PetrolScraper {

	@Resource
	NotificationService notificationService;
	
	private String baseurl = "http://www.petrol.hr/";
	
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
		if (gorivo.getNaziv().contains("plin") ||
			gorivo.getNaziv().contains("LPG")
				) {
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
		if (gorivo.getNaziv().contains("98") ||
			gorivo.getNaziv().contains("100") 
			) { 
				gorivo.setKategorija("Super98");
		}
		else if (gorivo.getNaziv().contains("95")) { 
				gorivo.setKategorija("Super95"); 
		}
		else
			gorivo.setKategorija("sigh blah"); 

	}

	public List<Gorivo> scrape(WebClient wc) {
		
		try {
			List<Gorivo> petrolGoriva = new LinkedList<Gorivo>();
			
			System.out.print("Scraping Petrol ");
	
			
			final HtmlPage page = wc.getPage(baseurl);
			
			/*
			System.out.println("##############################\n"+ page.asXml());
			System.out.println("############################## END PAGE\n");
			*/	
		
			//System.out.println("##############################" + ((HtmlElement)page.getByXPath( ".//em[@class='date']//em[@class='placeholder']").get(0)).getTextContent());
			
			//System.out.println("##############################" + ((HtmlElement)page.getByXPath( ".//div[@class='date']//em[@class='placeholder']").get(0)).getTextContent());
			
			
			
			String dateString = ((HtmlElement)page.getByXPath( ".//div[@class='date']//em[@class='placeholder']").get(0)).getTextContent();
			//System.out.println("DATESTRING:" + dateString);
			if (dateString.contains(","))
				dateString = dateString.split(",")[1];
			//System.out.println("DATESTRING:" + dateString);
			Date date = null;
			// 04 Oct 2011 
			// 12 6. 2012.
			
			List<SimpleDateFormat> sdfs = new LinkedList<SimpleDateFormat>();
			sdfs.add(new SimpleDateFormat("dd M yyyy"));
			sdfs.add(new SimpleDateFormat("dd MM yyyy"));
			sdfs.add(new SimpleDateFormat("dd MMM yyyy"));
			sdfs.add(new SimpleDateFormat("dd M. yyyy"));
			sdfs.add(new SimpleDateFormat("dd MM. yyyy"));
			sdfs.add(new SimpleDateFormat("dd MMM. yyyy"));
			sdfs.add(new SimpleDateFormat("d M. yyyy"));
			sdfs.add(new SimpleDateFormat("d MM. yyyy"));
			sdfs.add(new SimpleDateFormat("d MMM. yyyy"));
			sdfs.add(new SimpleDateFormat("d.M.yyyy"));
			sdfs.add(new SimpleDateFormat("dd.M.yyyy"));
			sdfs.add(new SimpleDateFormat("d.MM.yyyy"));
			sdfs.add(new SimpleDateFormat("dd.MM.yyyy"));
			
			for (SimpleDateFormat sdf : sdfs) 
			{
				if (date != null)
					break;
				
				try {
					//System.out.println("trying :" + sdf.toString());
					date = sdf.parse(dateString);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
				}
			}
			
			if (date == null)
				return null;
			
			SimpleDateFormat outsdf = new SimpleDateFormat("dd.MM.yyyy.");
			String datum = outsdf.format(date);
			
			final HtmlUnorderedList list = (HtmlUnorderedList)page.getElementsByTagName("ul").item(13);
	
			/*
			System.out.println("##############################\n"+ list.asXml());
			System.out.println("##############################  END LIST\n");
			*/
			Gorivo currGorivo = null;
			
			
		
			for (HtmlElement e : list.getElementsByTagName("li")) {
				
				HtmlListItem li = (HtmlListItem)e;
				
				/*
				System.out.println("##############################\n"+ li.asXml());
				System.out.println("##############################\n");
				*/
			
				currGorivo = new Gorivo();
				
				currGorivo.setNaziv(((HtmlElement)li.getByXPath( ".//span").get(0)).getTextContent().replace("*", ""));
				currGorivo.setCijena(stripCijena(((HtmlElement)li.getByXPath( ".//strong").get(0)).getTextContent()));
				currGorivo.setDistributer("Petrol");
				currGorivo.setDatum(datum);	
				
				resolveCategory (currGorivo);
				resolveAutocesta (currGorivo);
				
			    petrolGoriva.add(currGorivo);
				
			    /*
				currGorivo.setNaziv(((HtmlElement)li.getByXPath( ".//span").get(0)).getTextContent().replace("*", ""));
				currGorivo.setCijena(stripCijena(((HtmlDivision)li.getByXPath( ".//strong").get(0)).getTextContent()));
				currGorivo.setDatum(((HtmlSpan)li.getByXPath( ".//span[@class='gibanje_cen_datum']").get(0)).getTextContent() + ".");			
				currGorivo.setDistributer("Petrol");
				 */
			}
			
			System.out.println("[OK]");
			
			return petrolGoriva;
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

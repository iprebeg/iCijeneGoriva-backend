package com.prebeg.cijenegoriva.data.scraper;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.prebeg.cijenegoriva.model.Gorivo;
import com.prebeg.cijenegoriva.notification.NotificationService;

import java.util.Date;
import java.text.SimpleDateFormat;

@Component
public class CroduxScraper {
	
	@Resource
	NotificationService notificationService;
	
	private String baseurl = "http://crodux-derivati.hr/cijene-goriva/";
	
	private void resolveAutocesta (Gorivo gorivo) {
		if (gorivo.getNaziv().contains("autocesta")) {
			gorivo.setAutocesta("YES");
			gorivo.setNaziv(gorivo.getNaziv().replaceFirst("na benzinskim postajama na autocestama \\*", "(autocesta)"));
		} else 
			gorivo.setAutocesta("NO");
	}
	
	private void resolveCategory (Gorivo gorivo) {
		if (gorivo.getNaziv().contains("ulje") || 
			gorivo.getNaziv().contains("ULJE") ||	
			gorivo.getNaziv().contains("LUEL")	
		) {
			gorivo.setKategorija("Lozulje");
		}
		else 
		if (gorivo.getNaziv().contains("plin") ||
        gorivo.getNaziv().contains("PLIN")
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

  private List<Gorivo> process(String[] header, String[] bpLine, boolean autocesta, String datum)
  {
      //System.out.println("LINE:" + bpLine[0]);
			List<Gorivo> croduxGoriva = new LinkedList<Gorivo>();
      for (int i = 1; i < header.length; i++)
      {
        if (bpLine[i] != null && !bpLine[i].equals("/"))
        {
          Gorivo gorivo = new Gorivo();
          gorivo.setNaziv(header[i]);
          gorivo.setCijena(bpLine[i].replaceAll("\\.",","));
          gorivo.setDistributer("CRODUX");
      	  if (autocesta)
            gorivo.setAutocesta("YES");
				  else
            gorivo.setAutocesta("NO");
	        resolveCategory(gorivo);

          gorivo.setDatum(datum);
      
          croduxGoriva.add(gorivo);
        }
      }
      return croduxGoriva;
  }

	public List<Gorivo> scrape(WebClient wc) {
		
		try {
		
			List<Gorivo> croduxGoriva = new LinkedList<Gorivo>();
		
			System.out.print("Scraping CRODUX   ");
			
			final HtmlPage page = wc.getPage(baseurl);
			
			final String pageAsXml = page.asXml();
			
			//System.out.println("##############################\n"+ pageAsXml);
			//System.out.println("##############################\n");
	
      String[] header = new String[7];

      header[0] = ((HtmlDivision)page.getByXPath( "//div[@class='name header']").get(0)).asText();
      header[1] = ((HtmlDivision)page.getByXPath( "//div[@class='column1 header']").get(0)).asText();
      header[2] = ((HtmlDivision)page.getByXPath( "//div[@class='column2 header']").get(0)).asText();
      header[3] = ((HtmlDivision)page.getByXPath( "//div[@class='column3 header']").get(0)).asText();
      header[4] = ((HtmlDivision)page.getByXPath( "//div[@class='column4 header']").get(0)).asText();
      header[5] = ((HtmlDivision)page.getByXPath( "//div[@class='column5 header']").get(0)).asText();
      header[6] = ((HtmlDivision)page.getByXPath( "//div[@class='column6 header']").get(0)).asText();

      for (String h : header)
      {
        //System.out.println("h: " + h);
      }

      List<String[]> prices = new ArrayList<String[]>();
    
      int idx = 0;
      for (Object e : page.getByXPath( "//div[@class='name']"))
      {
        prices.add(new String[7]);
        prices.get(idx++)[0] = ((HtmlDivision)e).asText();
        //System.out.println("e: " + ((HtmlDivision)e).asText());
      }

      idx = 0;
      for (Object e : page.getByXPath( "//div[@class='column1']"))
      {
        prices.get(idx++)[1] = ((HtmlDivision)e).asText();
      }

      idx = 0;
      for (Object e : page.getByXPath( "//div[@class='column2']"))
      {
        prices.get(idx++)[2] = ((HtmlDivision)e).asText();
      }

      idx = 0;
      for (Object e : page.getByXPath( "//div[@class='column3']"))
      {
        prices.get(idx++)[3] = ((HtmlDivision)e).asText();
      }

      idx = 0;
      for (Object e : page.getByXPath( "//div[@class='column4']"))
      {
        prices.get(idx++)[4] = ((HtmlDivision)e).asText();
      }

      idx = 0;
      for (Object e : page.getByXPath( "//div[@class='column5']"))
      {
        prices.get(idx++)[5] = ((HtmlDivision)e).asText();
      }
      
      idx = 0;
      for (Object e : page.getByXPath( "//div[@class='column6']"))
      {
        prices.get(idx++)[6] = ((HtmlDivision)e).asText();
      }

      /*
      for (int i = 0; i < 7; i++)
      {
        System.out.print(header[i] + " ");
      }
      System.out.println();
      for (String[] ll : prices)
      {
        for (int i = 0; i < 7; i++)
        {
          System.out.print(ll[i] + " ");
        }
        System.out.println();
      }
      */

      Date date = new Date();
      SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy.");
      String datum = formatter.format(date);

      for (String[] ll : prices)
      {
        String name = ll[0];
        if (name.equals("OSIJEK DIVALTOVA"))
        {
          croduxGoriva.addAll(process(header,ll,false,datum)); 
        }
        else if (name.equals("NADIN JUG"))
        {
          croduxGoriva.addAll(process(header,ll,true,datum)); 
        }
        else if (name.equals("NOVSKA - LUEL"))
        {
          croduxGoriva.addAll(process(header,ll,false,datum)); 
        }
      }
			
			//System.out.println("##############################\n"+ table.asXml());
			
			System.out.println("[OK]");
			
			return croduxGoriva;
		} catch (Exception e) {
      e.printStackTrace();
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

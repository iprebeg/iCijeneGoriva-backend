package com.prebeg.cijenegoriva.data.scraper;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.util.Date;
import java.text.SimpleDateFormat;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.prebeg.cijenegoriva.model.Gorivo;
import com.prebeg.cijenegoriva.notification.NotificationService;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

@Component
public class TifonScraper implements Scraper {
	

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

  private List<Gorivo> process(String[] header, String[] bpLine, boolean autocesta)
  {
    List<Gorivo> goriva = new LinkedList<Gorivo>();
    for (int i = 1; i < header.length; i++)
    {
      if (bpLine[i] != null && !bpLine[i].equals("-"))
      {
        Gorivo gorivo = new Gorivo();
        gorivo.setNaziv(header[i]);
        gorivo.setCijena(bpLine[i]);
        gorivo.setDistributer("Tifon");
      	if (autocesta)
          gorivo.setAutocesta("YES");
				else
          gorivo.setAutocesta("NO");
	      resolveCategory(gorivo);

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy.");
        String formattedDate = formatter.format(date);
        gorivo.setDatum(formattedDate);
      
        goriva.add(gorivo);
      }
    }
    return goriva; 
  }
  public List<Gorivo> scrape(WebClient wc)
  {
    try {

			System.out.print("Scraping Tifon  ");
      
      String baseurl = "http://www.tifon.hr/default.aspx?id=166";
      final HtmlPage page = wc.getPage(baseurl);

      final HtmlDivision pDiv = (HtmlDivision)page.getByXPath( "//div[@class='pageContent']").get(0);
      //System.out.println("*" + pDiv.asXml());

      final HtmlAnchor pAnc = (HtmlAnchor)page.getByXPath( "//div[@class='pageContent']/a").get(0);
      //System.out.println("*" + pAnc.asXml());


      String pdfUrl = pAnc.getHrefAttribute();

      
      //System.out.println("url:" + pdfUrl);

      //String pdfUrl = "http://www.tifon.hr/UserDocsImages/Cjenik/CIJENE_GORIVA_24122013.pdf";
    
      List<Gorivo> tifonGoriva = scrapePDF(pdfUrl);

  		System.out.println("[OK]");
			
			return tifonGoriva;

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

  private List<Gorivo> scrapePDF(String pdfUrl)
  {
    List<Gorivo> goriva = new LinkedList<Gorivo>();
    try
    {
      PDDocument doc = new PDDocument();
      doc = PDDocument.load(new URL(pdfUrl));
      PDFTextStripper textStripper = new PDFTextStripper();  
      String str = textStripper.getText(doc);  

      //System.out.println("******\n[" + str + "]\n******\n");

      String[] all = str.split("\n");


      List<String> nl = new ArrayList<String>();
      String backlog = "";

      for (String line : all)
      {
        if (line.startsWith("BP ") || line.startsWith("EURO") || line.startsWith("LPG") ) 
        {
          // reset 
          if (!backlog.isEmpty())
          {
            nl.add(backlog);
          }
          
          backlog = line;

        }
        else
        {
          //backlog += " " + line;
          backlog += line;
        }
        //System.out.println("line:" + line);
      }

      if (!backlog.isEmpty())
      {
        nl.add(backlog);
      }
      

      int headerlen = 1;
      for (String line : nl)
      {
        //System.out.println("nline:" + line);
        if (!line.startsWith("BP "))
        {
          headerlen++;
        }
      }

      String[] header = new String[headerlen];
      header[0] = "Benzinska postaja";
      for (int i = 1; i < headerlen; i++)
      {
        header[i] = nl.get(i-1);
      }

      for (String h : header)
      {
        //System.out.println("h: " + h);
      }

      for (String line : nl)
      {
        if (line.startsWith("BP ZAPRE"))
        {
          line = line.replaceAll("BP ", ""); 
          List<Gorivo> tmp = process(header, line.split("\\s+"), false);
          if (tmp != null) goriva.addAll(tmp);
        }
        else if (line.startsWith("BP RAVNA GORA"))
        {
          line = line.replaceAll("BP RAVNA ", ""); 
          List<Gorivo> tmp = process(header, line.split("\\s+"), true);
          if (tmp != null) goriva.addAll(tmp);
        }
        else
        {
          continue;
        }
      }

      if( doc != null )
        doc.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return goriva;
  }

	public List<Gorivo> __scrape(WebClient wc) {
		
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

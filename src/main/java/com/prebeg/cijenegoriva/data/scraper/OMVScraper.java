package com.prebeg.cijenegoriva.data.scraper;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.prebeg.cijenegoriva.model.Gorivo;
import com.prebeg.cijenegoriva.notification.NotificationService;

@Component
public class OMVScraper {
	

	@Resource
	NotificationService notificationService;
	
	private String baseurl = "http://www.omv.hr/portal/01/hr/!ut/p/c5/04_SB8K8xLLM9MSSzPy8xBz9CP0os3hfA0sPN89Qo1BHE38z18Bgb1c_AwgAykeiyBsZeJt6hzkawuTx6w7OKNL388jPTdUvyI0oBwBvK_5n/dl3/d3/L0lJSklrQSEhL3dMTUFCa0FFak1nIS80Qm40UklBd1FBISEvNl9NMDlIRklVMlVBNE82RVFTNkJPMDAwMDAwMC83X00wOUhGSVUyVUE0TzZFUVM3UFIxMDAwMDAwL0hvbWU!/";
	
	private String stripCijena(String cijena)  {
		if (cijena.length() == 0)
			return cijena;
		String c = cijena.replace("HRK", "").replace(" ", "");
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
			gorivo.getNaziv().contains("LUEL")	
		) {
			gorivo.setKategorija("Lozulje");
		}
		else 
		if (gorivo.getNaziv().contains("plin")) {
			gorivo.setKategorija("Autoplin");
		}
		else 
		if (gorivo.getNaziv().contains("Eurodiesel") ||
			gorivo.getNaziv().contains("DG") ||
			gorivo.getNaziv().contains("DIZEL") ||
			gorivo.getNaziv().contains("Dizel") ||
			gorivo.getNaziv().contains("dizel") 	
		) { 
			gorivo.setKategorija("Dizel");
		}
		else 
		if (gorivo.getNaziv().contains("Eurosuper") ||
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
	
	private String cijenaGOCR(HtmlImage image, Gorivo currGorivo) {
		
		
		Process p = null;
		String ret = "";
		String chunk = "";
		
		String path = "/tmp/";
		
		//String gocrBin = "/home/ivor/bin/gocr";
		String gocrBin = "/home/prebegco/bin/gocr";
		
		String orgFN = path + currGorivo.getNaziv().replace(" ", "") + ".gif";
		String negFN = path + currGorivo.getNaziv().replace(" ", "") + "negative.gif";
		String pnmFN = path + currGorivo.getNaziv().replace(" ", "") + "negative.pnm";
		
		/*
		String orgFN = path + "org" + ".gif";
		String negFN = path + "neg" + ".gif";
		String pnmFN = path + "pnm" + ".pnm";
		*/
		File priceImageFile = new File(orgFN);
		String line = "";
		BufferedReader input = null;
		BufferedReader error = null; 
		
		try {
			
			
			image.saveAs(priceImageFile);
		
			
			
			line = "convert -negate " + orgFN + " " + negFN;
			//System.out.println("running:" + line);
			p = Runtime.getRuntime().exec(line);
			
			
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((chunk = input.readLine()) != null) {
				//System.out.println("STD:" + chunk);
				ret += chunk;
			}
			
			error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((chunk = error.readLine()) != null) {
				System.err.println("ERR:"+ chunk);
			
			}
			
			line = "convert  " + negFN + " " + pnmFN;
			//System.out.println("running:" + line);
			p = Runtime.getRuntime().exec(line);
			
			
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((chunk = input.readLine()) != null) {
				//System.out.println("STD:" + chunk);
				ret += chunk;
			}
			
			error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((chunk = error.readLine()) != null) {
				System.err.println("ERR:"+ chunk);
			
			}
			
			line = gocrBin + " " + pnmFN;
			//System.out.println("running:" + line);
			p = Runtime.getRuntime().exec(line);
			
			
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((chunk = input.readLine()) != null) {
				//System.out.println("STD:" + chunk);
				ret += chunk;
			}
			
			error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((chunk = error.readLine()) != null) {
				System.err.println("ERR:"+ chunk);
			
			}		
		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println("RETURNING:" + ret);
		
		return ret;

	}

	public List<Gorivo> scrape(WebClient wc) {
		
		for (int i = 0; i < 1; i++) {
		try {
			
			System.out.print("Scraping OMV   ");

			List<Gorivo> omvGoriva = new LinkedList<Gorivo>();
			
			wc.setJavaScriptEnabled(true);
			wc.setCssEnabled(true);
			
			// IN YOUR FACE DIRTY OIL MOTHERFUCKERS
			wc.setAjaxController(new NicelyResynchronizingAjaxController());
			
			// get first page
			final HtmlPage page = wc.getPage(baseurl);
			final String pageAsXml = page.asXml();
			
			// get iframe window
			final List<FrameWindow> window = page.getFrames();
			final HtmlPage lokatorPage = (HtmlPage) window.get(0).getEnclosedPage();
			
			//System.out.println("####### LOKATOR ###########\n"+ lokatorPage.asXml());
			//System.out.println("##############################\n");
			
			// get input form
			//HtmlForm form = lokatorPage.getFormByName("filling_station_form");
			HtmlInput form = lokatorPage.getHtmlElementById("LOC");
			//System.out.println("#######FORM ###########\n"+ form.asXml());
			//System.out.println("##############################\n");
			
			
			// set Mjesto
			//HtmlInput input = lokatorPage.getHtmlElementById("filling_station_form:i_c_google_ity");
			form.setValueAttribute("Zagreb");
			
			
			//System.out.println("##############################\n"+ input.asXml());
			//System.out.println("##############################\n");
			
			
			//System.out.println("####### FILLED PAGE ###########\n"+ lokatorPage.asXml());
			//System.out.println("##############################\n");
			
			// OK OUR FORM SHOULD BE FILLED ENOUGH BY NOW. LET'S GO CLICKING
			
			HtmlDivision wggDiv = lokatorPage.getHtmlElementById("wggNdStartDiv");
			
			// gimme button and click to get poiPage
			HtmlAnchor poiButton = (HtmlAnchor)wggDiv.getElementsByTagName("a").get(0);// ("filling_station_form:poiupperbutton");
			HtmlPage poiPage = poiButton.click();
			
			//System.out.println("####### POI PAGE ###########\n"+ poiPage.asXml());
			//System.out.println("##############################\n");
			
			HtmlDivision geocodeDiv = poiPage.getHtmlElementById("geocodeDiv");
			
			//System.out.println("####### GEOCODE DIV ###########\n"+ geocodeDiv.asXml());
			//System.out.println("##############################\n");
			
			HtmlSelect select = (HtmlSelect)geocodeDiv.getElementById("selGeocode");
			
			//System.out.println("####### SELECT ###########\n"+ select.asXml());
			//System.out.println("##############################\n");
			
			HtmlOption option = select.getOption(0);
			select.setSelectedAttribute(option, true);
			
			// get POI form
			HtmlAnchor daljeButton = (HtmlAnchor)geocodeDiv.getElementsByTagName("a").get(0);// ("filling_station_form:poiupperbutton");
			HtmlPage pageRes = daljeButton.click();
			
			//System.out.println("####### PAGE RES ###########\n"+ pageRes.asXml());
			//System.out.println("##############################\n");
			
			//System.out.println(pageRes.getByXPath( ".//div[@title='Otvorite popis sa svim pronađenim rezultatima']").size());
			
			System.out.println("sleep");
			Thread.sleep(5000);
			System.out.println("done sleep");
			
			System.out.println(pageRes.getByXPath( ".//a[@class='MapPushpinBase']").size());
			
			HtmlAnchor benga = (HtmlAnchor)pageRes.getByXPath( ".//a[@class='MapPushpinBase']").get(0);
			HtmlPage pageRes2 = benga.click();
			
			System.out.println("sleep");
			Thread.sleep(5000);
			System.out.println("done sleep");
			
			System.out.println("####### PAGE2 RES ###########\n"+ pageRes2.asXml());
			System.out.println("##############################\n");
			
			//pageRes = null;
			/*
			HtmlDivision clickDiv = (HtmlDivision)pageRes.getByXPath( ".//div[@title='Otvorite popis sa svim pronađenim rezultatima']").get(0);
			
			System.out.println("####### CLICK DIV ###########\n"+ clickDiv.asXml());
			System.out.println("##############################\n");
			
			HtmlPage pageRes2 = clickDiv.click();
			System.out.println("sleep");
			Thread.sleep(5000);
			System.out.println("done sleep");
			
			System.out.println("####### PAGE2 RES ###########\n"+ pageRes2.asXml());
			System.out.println("##############################\n");
			
			HtmlDivision resultDiv = (HtmlDivision)pageRes2.getElementById("resultDiv");
			System.out.println(" RES DIV ########################\n"+ resultDiv.asXml());
			System.out.println("##############################\n");
			*/
			HtmlForm poiForm = null;
			
			List<HtmlInput> poisearchButtons = poiForm.getInputsByName("fillingstationsearchform:poisearchlowerbutton2");
			
			for (HtmlInput button : poisearchButtons) {
				//System.out.println(" POI SEARCH BUTTONS ########################\n"+ button.asXml());
				//System.out.println("##############################\n");
			}
			
			
		 
		    HtmlInput poisearchButton = poiForm.getInputByName("fillingstationsearchform:poisearchlowerbutton2");
		 	//HtmlInput poisearchButton = poiForm.getInputByName("fillingstationsearchform:prev_top");
	
		 	HtmlPage resultsPage = poisearchButton.click();
		
			//System.out.println("####### RES PAGE ###########\n"+ resultsPage.asXml());
			//System.out.println("##############################\n");
			
	
			List <HtmlAnchor> anchors = resultsPage.getAnchors();
			
			for (HtmlAnchor a : anchors) {
				//System.out.println(" Anchors ########################\n"+ a.asXml());
				//System.out.println("##############################\n");
			}
			
			HtmlAnchor detaljiAnchor = resultsPage.getAnchorByText("Detalji");
			HtmlPage detaljiPage = detaljiAnchor.click();
			
			/*
			System.out.println("####### DETALJI PAGE ###########\n"+ detaljiPage.asXml());
			System.out.println("##############################\n");
			*/
			
			
			HtmlTable priceTable = (HtmlTable)detaljiPage.getElementById("priceListForm:priceListTable");
			
			// Done with this shiat
			
			
			/*
			System.out.println("####### TABLICA CIJENA ###########\n"+ priceTable.asXml());
			System.out.println("##############################\n");
			*/
			
			List<HtmlTableCell> nameCellsAll  = priceTable.getRows().get(0).getCells();
			List<HtmlTableCell> priceCellsAll = priceTable.getRows().get(1).getCells();
			
			Gorivo currGorivo = null;
			String datum = priceCellsAll.get(0).asText().split(" ")[0] + ".";
			
			List<HtmlTableCell> nameCells  = nameCellsAll.subList(1, nameCellsAll.size() );;
			List<HtmlTableCell> priceCells = priceCellsAll.subList(1, priceCellsAll.size() );
			
			
			for (final HtmlTableCell cell : nameCells) {
		        
				//System.out.println("   Found cell: " + cell.asText());
		        
				currGorivo = new Gorivo();
				
				currGorivo.setAutocesta("NO");
				currGorivo.setDatum(datum);
				currGorivo.setDistributer("OMV");
				currGorivo.setNaziv(cell.asText());
				resolveCategory(currGorivo);
				
				omvGoriva.add(currGorivo);
		    }
			
			    
			for (final HtmlTableCell cell : priceCells) {
		        
				//System.out.println("   Found cell: " + cell.asXml());
		        
			
				currGorivo = omvGoriva.get(priceCells.indexOf(cell));
				
				
				HtmlImage priceImage = (HtmlImage) cell.getElementsByTagName("img").get(0);
				
			
				String cijena = cijenaGOCR(priceImage, currGorivo);
				
				
				currGorivo.setCijena(stripCijena(cijena));
				
		    }    
			
			wc.setJavaScriptEnabled(false);
			wc.setCssEnabled(false);
			System.out.println("[OK]");
			return omvGoriva;

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
		}
		
		wc.setJavaScriptEnabled(false);
		wc.setCssEnabled(false);
		
		return null;
	}
}

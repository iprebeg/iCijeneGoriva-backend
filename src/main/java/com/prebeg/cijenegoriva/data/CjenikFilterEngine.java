package com.prebeg.cijenegoriva.data;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.prebeg.cijenegoriva.model.Cjenik;
import com.prebeg.cijenegoriva.model.Gorivo;

@Component
public class CjenikFilterEngine {
	
	String kategorijaFilter = "All";
	String distributerFilter = "All";

	public void filter (Cjenik cjenik, String kategorijaFilter, String distributeriFilter, String autocestaFilter ) {
		
		List<Gorivo> toRemove = new LinkedList<Gorivo>();
		
		String[] distributeriArray = distributeriFilter.split(",");
		List<String> distributeri = Arrays.asList(distributeriArray);

		
		// find candidates to remove, by category
		for (Gorivo gorivo : cjenik.getGoriva()) {
			if (!gorivo.getKategorija().equals(kategorijaFilter))
					toRemove.add(gorivo);
		}
		
		// remove 
		for (Gorivo gorivo : toRemove) {
			cjenik.remove(gorivo);
		}
		
		
		// remove distributers
		for (Gorivo gorivo : cjenik.getGoriva()) {			
			if (!distributeri.contains(gorivo.getDistributer())) {
				toRemove.add(gorivo);
			}
		}	
		
		// remove 
		for (Gorivo gorivo : toRemove) {
			cjenik.remove(gorivo);
		}

		// remove autocesta
		if (autocestaFilter.equals("NE")) {

			for (Gorivo gorivo : cjenik.getGoriva()) {			
				if (gorivo.getAutocesta().equals("YES")) {
					toRemove.add(gorivo);
				}
			}
		}
		
		// remove 
		for (Gorivo gorivo : toRemove) {
			cjenik.remove(gorivo);
		}

	}
	
	
	
}

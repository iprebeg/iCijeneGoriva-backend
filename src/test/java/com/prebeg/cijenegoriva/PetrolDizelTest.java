package com.prebeg.cijenegoriva;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.prebeg.cijenegoriva.model.Cjenik;
import com.prebeg.cijenegoriva.model.Gorivo;

@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@WebAppConfiguration
@ContextConfiguration( locations = {"file:src/main/resources/META-INF/spring/applicationContext.xml", "file:src/main/webapp/WEB-INF/spring/webmvc-config.xml"})
public class PetrolDizelTest extends TestCase {

	@Autowired WebApplicationContext wac; 
    @Autowired MockHttpSession session;
    @Autowired MockHttpServletRequest request;
    
    private MockMvc mockMvc;
    
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        //System.out.println("build web app context " + wac);
    }
    
    @Test
    public void test_01_getPetrolDizel() throws Exception 
    {
    	String kategorija = "Dizel";
    	String distributeri = "Petrol";
    	String autocesta = "DA";
    	
    	String url = String.format("/cjenik?kategorija=%s&distributeri=%s&Autocesta=%s", kategorija, distributeri, autocesta);
    	
    	System.out.println("URL=" + url);
    	MvcResult res = this.mockMvc.perform(get(url).session(session))
    		.andExpect(status().is(200))
    		.andReturn();
    
    	int status = res.getResponse().getStatus();
    	String body = res.getResponse().getContentAsString();
    	
    	System.out.println("STATUS:" + status);
    	System.out.println("BODY:" + body);
    	
    	JAXBContext ctx = JAXBContext.newInstance(Cjenik.class);
    	Unmarshaller unm = ctx.createUnmarshaller();
    	Cjenik cjenik = (Cjenik) unm.unmarshal(new StringReader(body));
    	
    	assertNotNull(cjenik);
    	assertNotNull(cjenik.getGoriva());
    	assertTrue(!cjenik.getGoriva().isEmpty());
    	//assertTrue(cjenik.getGoriva().size() > 1);
    	
    	for (Gorivo g : cjenik.getGoriva())
    	{
    		assertNotNull(g);
    	
    		assertNotNull(g.getAutocesta());
    		assertNotNull(g.getCijena());
    		assertNotNull(g.getDatum());
    		assertNotNull(g.getDistributer());
    		assertNotNull(g.getKategorija());
    		assertNotNull(g.getNaziv());
    		
    		assertTrue(!g.getAutocesta().isEmpty());
    		assertTrue(!g.getCijena().isEmpty());
    		assertTrue(!g.getDatum().isEmpty());
    		assertTrue(!g.getDistributer().isEmpty());
    		assertTrue(!g.getKategorija().isEmpty());
    		assertTrue(!g.getNaziv().isEmpty());
    	}
    	
    	System.out.println("SIZE:" + cjenik.getGoriva().size());
    }
}

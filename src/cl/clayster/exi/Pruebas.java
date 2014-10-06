package cl.clayster.exi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

import org.dom4j.DocumentException;
import org.jivesoftware.smack.XMPPException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.siemens.ct.exi.FidelityOptions;
import com.siemens.ct.exi.GrammarFactory;
import com.siemens.ct.exi.api.sax.EXIResult;
import com.siemens.ct.exi.api.sax.EXISource;
import com.siemens.ct.exi.exceptions.EXIException;
import com.siemens.ct.exi.grammars.Grammars;


class Pruebas{ 
	
	public static void main(String[] args) throws Exception{
		
		//config();
		swbDOM();
	}
		
	private static void config() throws Exception{
		EXIBufferedInputStream bis;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		EXISetupConfiguration config = new EXISetupConfiguration();
		config.setAlignment(EXISetupConfiguration.ALIGN_COMPRESSION);
		config.setSessionWideBuffers(true);
		//config.setFidelityOptions(FidelityOptions.createStrict());
		//config.setStrict(true);
		config.setFidelityOptions(FidelityOptions.createAll());
		
		int t = 20;
		EXIProcessor p = new EXIProcessor(config);
		String xml0 = "<presence from=\"javier.placencio@clayster.cl/Spark 2.6.3\" id=\"V77Wj-7\" to=\"javier@clayster.cl/Smack\"><status>En línea</status><priority>1</priority></presence>";
		System.out.println(xml0.length());
		for(int i = 0 ; i < t ; i++){
			byte[] exi = p.encodeToByteArray(xml0);
			System.out.println(exi.length + "-" + EXIUtils.bytesToHex(exi));
			baos.write(exi);
		}
		
		bis = new EXIBufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));
		System.out.println("av: " + bis.available());
		while(true){
			try{
				bis.mark(baos.size());
				String xml = p.decode(bis);
System.err.println("decoded(" + xml.length() + "): " + xml);
System.out.println("av: " + bis.available());
System.out.println("pos: " + bis.getPos());
			} catch (TransformerException | EXIException | SAXException e) {
				
				break;
			}
		}
	}
	
	
	private static void swb() throws Exception{
		
		EXISetupConfiguration exiFactory = new EXISetupConfiguration();
		EXIResult exiResult;
	    SAXSource exiSource;
	    Transformer transformer;
		XMLReader exiReader, xmlReader;
		boolean swb = true;
		
		swb = exiFactory.isSessionWideBuffers();
        String xsdLocation = exiFactory.getCanonicalSchemaLocation();
        
        if(xsdLocation != null && new File(xsdLocation).isFile()){
        	try{
            	GrammarFactory grammarFactory = GrammarFactory.newInstance();
                Grammars g = grammarFactory.createGrammars(xsdLocation, new SchemaResolver());
                g.setSchemaId(exiFactory.getSchemaId());
                exiFactory.setGrammars(g);
                
                TransformerFactory tf = TransformerFactory.newInstance();
    			transformer = tf.newTransformer();
    			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    			
    			exiResult = new EXIResult(exiFactory);
    	        xmlReader = XMLReaderFactory.createXMLReader();
    	        xmlReader.setContentHandler(exiResult.getHandler());
    			
    			exiSource = new EXISource(exiFactory);
    	        exiReader = exiSource.getXMLReader();
            } catch (IOException | TransformerConfigurationException | SAXException e){
                e.printStackTrace();
                throw new EXIException("Error while creating Grammars.");
            }
        }
        else{
        	String message = "Invalid Canonical Schema file location: " + xsdLocation;
        	throw new EXIException(message);
        }
        
        String xml = "<presence from=\"javier.placencio@clayster.cl/Spark 2.6.3\" id=\"V77Wj-7\" to=\"javier@clayster.cl/Smack\"><status>En línea</status><priority>1</priority></presence>";
        System.out.println("xml(" + xml.length() + "): " + xml);
		
        exiResult = new EXIResult(exiFactory);
        xmlReader.setContentHandler(exiResult.getHandler());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	exiResult.setOutputStream(baos);
        
        for(int i=0 ; i<5 ; i++){
        	baos.flush();
        	exiResult.setOutputStream(baos);
        	InputSource is = new InputSource(new StringReader(xml));
            xmlReader.parse(is);
            
            byte[] ba = baos.toByteArray();
            System.out.println("EXI(" + ba.length + "): " + EXIUtils.bytesToHex(ba));
        }
	}
	
	private static void swbDOM() throws Exception{
		String xml = "<presence from=\"javier.placencio@clayster.cl/Spark 2.6.3\" id=\"V77Wj-7\" to=\"javier@clayster.cl/Smack\"><status>En línea</status><priority>1</priority></presence>";
		System.out.println("xml(" + xml.length() + "): " + xml);
		
		EXISetupConfiguration exiConfig = new EXISetupConfiguration();
		exiConfig.setSessionWideBuffers(true);
		EXIDOMProcessor edm = new EXIDOMProcessor(exiConfig);

		for(int i=0; i<5 ; i++){
			byte ba[] = edm.encodeToByteArray(xml);
			System.out.println("EXI(" + ba.length + "): " + EXIUtils.bytesToHex(ba));
		}
	}
		
		
	private static void multiple(){
		/*
		bis = new EXIBufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));
		System.out.println("av: " + bis.available());
		while(true){
			try{
				bis.mark(baos.size());
				String xml = p.decode(bis);
System.err.println("decoded(" + xml.length() + "): " + xml);
System.out.println("av: " + bis.available());
System.out.println("pos: " + bis.getPos());
			} catch (TransformerException | EXIException | SAXException e) {
				
				break;
			}
		}
		
		boolean a = true;
		if(a)return;
		
		/*
		byte[] ba = ("1234567890qwertyuiopasdfghjklzxcvbnm"
				   + "098765432poiuytrewqñlkjhgfdsamnbvcxz").getBytes();
		byte[] r = new byte[ba.length];
		ByteArrayInputStream is = new ByteArrayInputStream(ba);
		
		
		is.read(r, 0, ba.length/2);
System.out.println("first half: " + new String(r));
		r = new byte[ba.length];
		if(is.available() > 0){
			is.read(r, 0, ba.length/2);
		}
		else{
			System.err.println("end of the stream");
		}
System.out.println("second half: " + new String(r));
		if(is.available() > 0){
			is.read(r, 0, ba.length/2);
		}
		else{
			System.err.println("end of the stream");
		}
		*/
	}
	
}




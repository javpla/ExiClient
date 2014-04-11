package cl.clayster.exi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.siemens.ct.exi.GrammarFactory;
import com.siemens.ct.exi.api.sax.EXIResult;
import com.siemens.ct.exi.api.sax.EXISource;
import com.siemens.ct.exi.exceptions.EXIException;
import com.siemens.ct.exi.grammars.Grammars;

public class EXIProcessor extends EXIBaseProcessor{
	
	/**
	 * Constructs an EXI Processor using <b>xsdLocation</b> as the Canonical Schema and the respective parameters in exiConfig for its configuration.
	 * @param xsdLocation        location of the Canonical schema file
	 * @param exiConfig        EXISetupConfiguration instance with the necessary EXI options
	 * @throws EXIException
	 */
	public EXIProcessor(EXISetupConfiguration exiConfig) throws EXIException{
		if(exiConfig == null)        exiConfig = new EXISetupConfiguration();
        // create factory and EXI grammar for given schema
        exiFactory = exiConfig;
        String xsdLocation = exiConfig.getCanonicalSchemaLocation();
        
        if(xsdLocation != null && new File(xsdLocation).isFile()){
        	try{
            	GrammarFactory grammarFactory = GrammarFactory.newInstance();
                Grammars g = grammarFactory.createGrammars(xsdLocation, new SchemaResolver());
                g.setSchemaId(exiConfig.getSchemaId());
                exiFactory.setGrammars(g);
            } catch (IOException e){
                e.printStackTrace();
                throw new EXIException("Error while creating Grammars.");
            }
        }
        else{
        	String message = "Invalid Canonical Schema file location: " + xsdLocation;
        	throw new EXIException(message);
        }
	}
	        
	        
	
	/** FUNCIONES DEFINITIVAS Y PARA XSD VARIABLES **/
	@Override
	protected byte[] encodeToByteArray(String xml) throws IOException, EXIException, SAXException, TransformerException{
	        // encoding
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        exiResult = new EXIResult(exiFactory);                
	        exiResult.setOutputStream(baos);
	        
	        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
	        xmlReader.setContentHandler(exiResult.getHandler());
	        xmlReader.parse(new InputSource(new StringReader(xml)));
	        return baos.toByteArray();
	}
	
	@Override
	protected String decodeByteArray(byte[] exiBytes) throws IOException, EXIException, TransformerException{
	        // decoding                
	        exiSource = new EXISource(exiFactory);
	        XMLReader exiReader = exiSource.getXMLReader();
	
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();                
	        
	        InputStream exiIS = new ByteArrayInputStream(exiBytes);
	        exiSource = new SAXSource(new InputSource(exiIS));
	        exiSource.setXMLReader(exiReader);
	
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        transformer.transform(exiSource, new StreamResult(baos));
	        
	        String xml = baos.toString("UTF-8");
	        return xml.substring(xml.indexOf('>') + 1);
	}
	
	protected String decode(InputStream exiIS) throws IOException, EXIException, SAXException, TransformerException{                
        // decoding
        exiSource = new EXISource(exiFactory);
        XMLReader exiReader = exiSource.getXMLReader();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        
        exiSource = new SAXSource(new InputSource(exiIS));
        exiSource.setXMLReader(exiReader);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(exiSource, new StreamResult(baos));                
        
        String xml = baos.toString("UTF-8");
        return xml.substring(xml.indexOf('>') + 1);
    }
	
	protected ByteArrayInputStream decodeToStream(InputStream exiIS) throws IOException, EXIException, SAXException, TransformerException{                
        // decoding
        exiSource = new EXISource(exiFactory);
        XMLReader exiReader = exiSource.getXMLReader();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        
        exiSource = new SAXSource(new InputSource(exiIS));
        exiSource.setXMLReader(exiReader);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(exiSource, new StreamResult(baos));
       
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
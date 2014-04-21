package cl.clayster.exi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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
import com.siemens.ct.exi.util.FragmentUtilities;
import com.siemens.ct.exi.util.SkipRootElementXMLReader;

public class EXIProcessor extends EXIBaseProcessor{
	
	protected Transformer transformer;
	protected XMLReader exiReader, xmlReader;
	
	/**
	 * Constructs an EXI Processor using <b>xsdLocation</b> as the Canonical Schema and the respective parameters in exiConfig for its configuration.
	 * If exiConfig is null, default values and the default canonical schema will be used.
	 * @param exiConfig        EXISetupConfiguration instance with the necessary EXI options.
	 * @throws EXIException
	 */
	public EXIProcessor(EXISetupConfiguration exiConfig) throws EXIException{
		if(exiConfig == null){
			exiConfig = new EXISetupConfiguration();
		}
        // create factory and EXI grammar for given schema
        exiFactory = exiConfig;
        String xsdLocation = exiConfig.getCanonicalSchemaLocation();
        
        if(xsdLocation != null && new File(xsdLocation).isFile()){
        	try{
            	GrammarFactory grammarFactory = GrammarFactory.newInstance();
                Grammars g = grammarFactory.createGrammars(xsdLocation, new SchemaResolver());
                g.setSchemaId(exiConfig.getSchemaId());
                exiFactory.setGrammars(g);
                
                TransformerFactory tf = TransformerFactory.newInstance();
    			transformer = tf.newTransformer();
    			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    			
    			exiResult = new EXIResult(exiFactory);
    	        xmlReader = XMLReaderFactory.createXMLReader();
    	        if (exiFactory.isFragment()) {
    				xmlReader = new SkipRootElementXMLReader(xmlReader);
    			}
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
	}
	        
	        
	
	/** FUNCIONES DEFINITIVAS Y PARA XSD VARIABLES **/
	@SuppressWarnings("resource")
	@Override
	protected byte[] encodeToByteArray(String xml) throws IOException, EXIException, SAXException, TransformerException{
	        // encoding
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();         
	        exiResult.setOutputStream(baos);
	        
	        InputSource is = new InputSource(new StringReader(xml));

	        if (exiFactory.isFragment()) {
	        	InputStream xmlInput = new ByteArrayInputStream(xml.getBytes());
				xmlInput = FragmentUtilities.getSurroundingRootInputStream(xmlInput);
				is = new InputSource(xmlInput);
			}
	        xmlReader.parse(is);
	        return baos.toByteArray();
	}
	
	@Override
	protected String decodeByteArray(byte[] exiBytes) throws IOException, EXIException, TransformerException{
	        // decoding                
	        InputStream exiIS = new ByteArrayInputStream(exiBytes);
	        exiSource = new SAXSource(new InputSource(exiIS));
	        exiSource.setXMLReader(exiReader);
	
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        transformer.transform(exiSource, new StreamResult(baos));
	        
	        return baos.toString();
	}
	
	protected String decode(InputStream exiIS) throws IOException, EXIException, SAXException, TransformerException{                
        // decoding        
        exiSource = new SAXSource(new InputSource(exiIS));
        exiSource.setXMLReader(exiReader);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(exiSource, new StreamResult(baos));                
        
        return baos.toString();
    }
}
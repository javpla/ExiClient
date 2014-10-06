package cl.clayster.exi;

import java.io.BufferedInputStream;
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

/**
 * This class containes several methods and fields to be used for encoding and decoding using the EXI compression method.
 * @author Javier Placencio
 *
 */
class EXIProcessor extends EXIBaseProcessor{
	
	protected EXISetupConfiguration exiFactory;
	protected EXIResult exiResult;
    protected SAXSource exiSource;
    protected Transformer transformer;
	protected XMLReader exiReader, xmlReader;
	private boolean swb = false;
	
	private int cn = 0;
	private float ct = 0;
	private float cr = 0;
	
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
		swb = exiConfig.isSessionWideBuffers();
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
	@Override
	protected byte[] encodeToByteArray(String xml) throws IOException, EXIException, SAXException, TransformerException{
		if(!swb){
	        exiResult = new EXIResult(exiFactory);
	        xmlReader.setContentHandler(exiResult.getHandler());
		}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();         
        exiResult.setOutputStream(baos);
        
        InputSource is = new InputSource(new StringReader(xml));

        xmlReader.parse(is);
        
        byte[] ba = baos.toByteArray();

System.err.println("EXI.COMPRESSING(" + xml.getBytes().length + "): " + xml);
System.err.println("EXI.ENCODED(" + ba.length + "):" + EXIUtils.bytesToHex(ba));
		cr = (float) (float)ba.length * 100 / (float)xml.getBytes().length;
System.err.println("EXI.COMPRESSION RATE = " + cr + "%");
		cn++;
		ct += cr;
		
System.out.println("EXI.AVRG COMPRESSION = " + (float)(ct / cn) + "%");
        
        return ba;
	}
	
	@Override
	protected String decodeByteArray(byte[] exiBytes) throws IOException, EXIException, TransformerException{
		if(!swb){
			exiSource = new EXISource(exiFactory);
	        exiReader = exiSource.getXMLReader();
		}
        InputStream exiIS = new ByteArrayInputStream(exiBytes);
        exiSource = new SAXSource(new InputSource(exiIS));
        exiSource.setXMLReader(exiReader);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(exiSource, new StreamResult(baos));
        
        return baos.toString("UTF-8");
	}
	
	@Override
	protected String decode(BufferedInputStream exiIS) throws IOException, EXIException, SAXException, TransformerException{
		/*
		EXIBufferedInputStream ebis = (EXIBufferedInputStream) exiIS;
		int i = ebis.getPos(); 
		*/
		
		if(!swb){
			exiSource = new EXISource(exiFactory);
	        exiReader = exiSource.getXMLReader();
		}
        exiSource = new SAXSource(new InputSource(exiIS));
        exiSource.setXMLReader(exiReader);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(exiSource, new StreamResult(baos));                
        
        String xml = baos.toString("UTF-8");

        /*
        int f = ebis.getPos();
        int dif = f - i;
        
        if(dif > 10){
        	System.err.println("EXI.DECODING " + f + "-" + i + " = " + dif);
        	System.err.println("EXI.DECOMPRESSED(" + xml.getBytes().length + "): " + xml);
        	System.err.println("EXI.DECOMPRESSION RATE = " + (float)((float)xml.getBytes().length*100/(float)dif) + "%");
        	dn++;
        	dt += (float)((float)xml.getBytes().length*100/(float)dif);
        }
        else{
        	System.err.println("EXI.DECOMPRESSED-UNKNOWN RATE(" + xml.getBytes().length + "): " + xml);
        }
        System.out.println("EXI.AVRG DECOMPRESSION = " + (float)(dt / dn) + "%");
        */
        return xml;
    }
}
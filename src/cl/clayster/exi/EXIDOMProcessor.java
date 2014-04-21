package cl.clayster.exi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.siemens.ct.exi.EXIBodyEncoder;
import com.siemens.ct.exi.EXIFactory;
import com.siemens.ct.exi.GrammarFactory;
import com.siemens.ct.exi.api.dom.DOMBuilder;
import com.siemens.ct.exi.api.dom.DOMWriter;
import com.siemens.ct.exi.api.dom.DocumentFragmentBuilder;
import com.siemens.ct.exi.exceptions.EXIException;
import com.siemens.ct.exi.grammars.Grammars;
import com.siemens.ct.exi.util.NoEntityResolver;

public class EXIDOMProcessor{
	
	protected DOMWriter enc;
	protected EXIFactory exiFactory;
	
	/**
	 * Constructs an EXI Processor using <b>xsdLocation</b> as the Canonical Schema and the respective parameters in exiConfig for its configuration.
	 * @param xsdLocation        location of the Canonical schema file
	 * @param exiConfig        EXISetupConfiguration instance with the necessary EXI options
	 * @throws EXIException
	 */
	public EXIDOMProcessor(EXISetupConfiguration exiConfig) throws EXIException{
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
        enc = new DOMWriter(exiFactory);
	}
	        
	        
	
	/* FUNCIONES DEFINITIVAS Y PARA XSD VARIABLES */ 
	
	protected byte[] encodeToByteArray(String xml) throws ParserConfigurationException, SAXException, IOException, EXIException{
		InputStream xmlInput = new ByteArrayInputStream(xml.getBytes());
		Node doc;
		if (exiFactory.isFragment()) {
			DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
			dfactory.setNamespaceAware(true);

			DocumentBuilder documentBuilder = dfactory.newDocumentBuilder();
			DocumentFragmentBuilder dfb = new DocumentFragmentBuilder(
					documentBuilder);

			DocumentFragment docFragment = dfb.parse(xmlInput);

			doc = docFragment;
		} else {
			DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
			dfactory.setNamespaceAware(true);
			DocumentBuilder documentBuilder = dfactory.newDocumentBuilder();
			documentBuilder.setEntityResolver(new NoEntityResolver());
			documentBuilder.setErrorHandler(null);
			doc = documentBuilder.parse(xmlInput);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		enc.setOutput(baos);
		enc.encode(doc);
		
		return baos.toByteArray();
	}
	
	protected String decodeByteArray(byte[] exiBytes) throws EXIException, ParserConfigurationException, TransformerException{
		InputStream is = new ByteArrayInputStream(exiBytes);
		DOMBuilder domBuilder = new DOMBuilder(exiFactory);
		
		Node doc;
		if (exiFactory.isFragment()) {
			doc = domBuilder.parseFragment(is);
		} else {
			doc = domBuilder.parse(is);
		}
		// create string from xml tree
		StringWriter sw = new StringWriter();
		
		// set up a transformer
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		// output options
		trans.setOutputProperty(OutputKeys.METHOD, "xml");
		// due to fragments
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		// remaining keys
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		// create string from xml tree
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(doc);
		trans.transform(source, result);

		return sw.toString();
	}
	
	static byte[] encodeExiBody(String xml) throws EXIException, IOException{
		EXISetupConfiguration ef = new EXISetupConfiguration();
		String xsdLocation = EXIUtils.defaultCanonicalSchemaLocation;
        
        if(xsdLocation != null && new File(xsdLocation).isFile()){
        	try{
            	GrammarFactory grammarFactory = GrammarFactory.newInstance();
                Grammars g = grammarFactory.createGrammars(xsdLocation, new SchemaResolver());
                g.setSchemaId(ef.getSchemaId());
                ef.setGrammars(g);
            } catch (IOException e){
                e.printStackTrace();
                throw new EXIException("Error while creating Grammars.");
            }
        }
        else{
        	String message = "Invalid Canonical Schema file location: " + xsdLocation;
        	throw new EXIException(message);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EXIBodyEncoder exiBody = ef.createEXIBodyEncoder();
        exiBody.setOutputStream(baos);
        // TODO:!!
        return baos.toByteArray();
	}
}
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

import com.siemens.ct.exi.CodingMode;
import com.siemens.ct.exi.EXIFactory;
import com.siemens.ct.exi.FidelityOptions;
import com.siemens.ct.exi.GrammarFactory;
import com.siemens.ct.exi.api.sax.EXIResult;
import com.siemens.ct.exi.api.sax.EXISource;
import com.siemens.ct.exi.exceptions.EXIException;
import com.siemens.ct.exi.grammars.Grammars;
import com.siemens.ct.exi.helpers.DefaultEXIFactory;

public class EXIProcessor {
	
	static EXIFactory exiFactory;
	static EXIResult exiResult;
	static SAXSource exiSource;
	
	private static final CodingMode schemalessCodingMode = CodingMode.COMPRESSION;
	private static final FidelityOptions schemalessFidelityOptions = FidelityOptions.createAll();
	private static final boolean schemalessIsFragmet = false;
	
	public EXIProcessor(String xsdLocation) throws EXIException{
		// create default factory and EXI grammar for schema
		exiFactory = DefaultEXIFactory.newInstance();
		exiFactory.setFidelityOptions(FidelityOptions.createAll());
		exiFactory.setCodingMode(CodingMode.BIT_PACKED);
		
		if(xsdLocation != null && new File(xsdLocation).isFile()){
			GrammarFactory grammarFactory = GrammarFactory.newInstance();
			Grammars g = grammarFactory.createGrammars(xsdLocation);
			exiFactory.setGrammars(g);
		}
		else{
			System.out.println("Invalid Schema file location. Encoding schema-less.");
		}
	}
	
	/**
	 * Encodes an XML String into an EXI byte array using no schema files.
	 * 
	 * @param xml the String to be encoded
	 * @return a byte array containing the encoded bytes
	 * @throws IOException
	 * @throws EXIException
	 * @throws SAXException
	 */
	public static byte[] encodeSchemaless(String xml) throws IOException, EXIException, SAXException{
		ByteArrayOutputStream osEXI = new ByteArrayOutputStream();
		// start encoding process
		EXIFactory factory = DefaultEXIFactory.newInstance();
		factory.setCodingMode(schemalessCodingMode);
		factory.setFidelityOptions(schemalessFidelityOptions);
		factory.setFragment(schemalessIsFragmet);
		
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		EXIResult exiResult = new EXIResult(factory);
		
		exiResult.setOutputStream(osEXI);
		xmlReader.setContentHandler(exiResult.getHandler());
		xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);	// ignorar DTD externos
		
		xmlReader.parse(new InputSource(new StringReader(xml)));
		
System.out.println("(" + xml.length() + "->" + osEXI.toByteArray().length + ")");
		return osEXI.toByteArray();
	}
	
	/**
	 * Decodes an EXI byte array using no schema files.
	 * 
	 * @param exi the EXI stanza to be decoded
	 * @return a String containing the decoded XML
	 * @throws IOException
	 * @throws EXIException
	 * @throws SAXException
	 */
	public static String decodeSchemaless(byte[] exi) throws TransformerException, EXIException{
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		
		EXIFactory factory = DefaultEXIFactory.newInstance();
		factory.setCodingMode(schemalessCodingMode);
		factory.setFidelityOptions(schemalessFidelityOptions);
		factory.setFragment(schemalessIsFragmet);
		
		SAXSource exiSource = new SAXSource(new InputSource(new ByteArrayInputStream(exi)));
		exiSource.setXMLReader(factory.createEXIReader());

		ByteArrayOutputStream xmlDecoded = new ByteArrayOutputStream();
		transformer.transform(exiSource, new StreamResult(xmlDecoded));

		return xmlDecoded.toString();
	}
	
	/**
	 * Uses distinguishing bits (10) to recognize EXI stanzas.
	 * 
	 * @param b the first byte of the EXI stanza to be evaluated
	 * @return <b>true</b> if the byte starts with distinguishing bits, <b>false</b> otherwise
	 */
	public static boolean isEXI(byte b){
		byte distinguishingBits = -128;
		byte aux = (byte) (b & distinguishingBits);
		return aux == distinguishingBits;
	}
	
	/** FUNCIONES DEFINITIVAS Y PARA XSD VARIABLES **/
	
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
	
	protected String decode(byte[] exiBytes) throws IOException, EXIException, SAXException, TransformerException{
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
		return baos.toString();
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
		return baos.toString();
	}
}

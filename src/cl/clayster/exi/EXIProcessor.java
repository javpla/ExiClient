package cl.clayster.exi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
	
	public EXIProcessor(String xsdLocation) throws EXIException{
		// create default factory and EXI grammar for schema
		exiFactory = DefaultEXIFactory.newInstance();
		exiFactory.setFidelityOptions(FidelityOptions.createAll());
		exiFactory.setCodingMode(CodingMode.BIT_PACKED);
		GrammarFactory grammarFactory = GrammarFactory.newInstance();
		Grammars g = grammarFactory.createGrammars(xsdLocation);
		exiFactory.setGrammars(g);
	}
	
	public EXIProcessor() {}
	
	public static byte[] encodeSchemaless(String xml) throws IOException, EXIException, SAXException{
		ByteArrayOutputStream osEXI = new ByteArrayOutputStream();
		// start encoding process
		EXIFactory factory = DefaultEXIFactory.newInstance();
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();

		EXIResult exiResult = new EXIResult(factory);
		exiResult.setOutputStream(osEXI);
		xmlReader.setContentHandler(exiResult.getHandler());

		xmlReader.parse(new InputSource(new StringReader(xml)));
		
		return osEXI.toByteArray();
	}
	/*
	protected static String decode(String exi) throws IOException, EXIException, SAXException, TransformerException{		
		// create default factory and EXI grammar for schema
		EXIFactory exiFactory = DefaultEXIFactory.newInstance();
		exiFactory.setFidelityOptions(FidelityOptions.createAll());
		GrammarFactory grammarFactory = GrammarFactory.newInstance();
		Grammars g = grammarFactory.createGrammars(xsdLocation);
		exiFactory.setGrammars(g);
		
		// decoding		
		SAXSource exiSource = new EXISource(exiFactory);
		XMLReader exiReader = exiSource.getXMLReader();
	
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();

		byte[] exiBytes = exi.getBytes(EXIProcessor.CHARSET);		
		
		InputStream exiIS = new ByteArrayInputStream(exiBytes);
		exiSource = new SAXSource(new InputSource(exiIS));
		exiSource.setXMLReader(exiReader);
	
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		transformer.transform(exiSource, new StreamResult(baos));		
		return baos.toString();
	}
	
	protected static String decode(InputStream exiIS) throws IOException, EXIException, SAXException, TransformerException{		
		// create default factory and EXI grammar for schema
		EXIFactory exiFactory = DefaultEXIFactory.newInstance();
		exiFactory.setFidelityOptions(FidelityOptions.createAll());
		GrammarFactory grammarFactory = GrammarFactory.newInstance();
		Grammars g = grammarFactory.createGrammars(xsdLocation);
		exiFactory.setGrammars(g);
		
		// decoding		
		SAXSource exiSource = new EXISource(exiFactory);
		XMLReader exiReader = exiSource.getXMLReader();
	
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		
		exiSource = new SAXSource(new InputSource(exiIS));
		exiSource.setXMLReader(exiReader);
	
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		transformer.transform(exiSource, new StreamResult(baos));		
		return baos.toString();
	}
	*/
	
	public static boolean isEXI(byte b){
		byte distinguishingBits = -128;
		byte aux = (byte) (b & distinguishingBits);
		return aux == distinguishingBits;
	}
	
	/** FUNCIONES DEFINITIVAS Y PARA XSD VARIABLES **/
	
	protected byte[] encodeByteArray(String xml) throws IOException, EXIException, SAXException, TransformerException{
		// encoding
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		exiResult = new EXIResult(exiFactory);		
		exiResult.setOutputStream(baos);
		
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(exiResult.getHandler());
		xmlReader.parse(new InputSource(new StringReader(xml)));
		return baos.toByteArray();
	}
	
	// TODO: TRABAJAR CON BYTES!!!! 
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
	
	protected String decode(String exi) throws IOException, EXIException, SAXException, TransformerException{
		// decoding		
		exiSource = new EXISource(exiFactory);
		XMLReader exiReader = exiSource.getXMLReader();
	
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();		
		
		byte[] exiBytes = exi.getBytes();
		
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

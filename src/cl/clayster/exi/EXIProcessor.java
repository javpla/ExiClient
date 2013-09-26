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

import com.siemens.ct.exi.EXIFactory;
import com.siemens.ct.exi.FidelityOptions;
import com.siemens.ct.exi.GrammarFactory;
import com.siemens.ct.exi.api.sax.EXIResult;
import com.siemens.ct.exi.api.sax.EXISource;
import com.siemens.ct.exi.exceptions.EXIException;
import com.siemens.ct.exi.grammars.Grammars;
import com.siemens.ct.exi.helpers.DefaultEXIFactory;

public class EXIProcessor {
	
	public static final String xsdLocation = "C:\\Users\\Javier\\Documents\\UTFSM\\Memoria - XMPP\\Softwares\\Exifficient\\bundle\\sample-data\\jabber-client.xsd";
	public static final String CHARSET = "ISO-8859-1";
	
	protected static String encode(String xml) throws IOException, EXIException, SAXException, TransformerException{
		// create default factory and EXI grammar for schema
		EXIFactory exiFactory = DefaultEXIFactory.newInstance();
		exiFactory.setFidelityOptions(FidelityOptions.createAll());
		GrammarFactory grammarFactory = GrammarFactory.newInstance();
		Grammars g = grammarFactory.createGrammars(xsdLocation);
		exiFactory.setGrammars(g);
		
		// encoding
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		EXIResult exiResult = new EXIResult(exiFactory);		
		exiResult.setOutputStream(baos);
		
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(exiResult.getHandler());
		xmlReader.parse(new InputSource(new StringReader(xml)));
		return new String(baos.toByteArray(), EXIProcessor.CHARSET);
	}

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
	
	public static boolean isEXI(byte b){
		byte distinguishingBits = -128;
		byte aux = (byte) (b & distinguishingBits);
		return aux == distinguishingBits;
	}
}

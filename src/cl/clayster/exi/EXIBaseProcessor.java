package cl.clayster.exi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.siemens.ct.exi.Constants;
import com.siemens.ct.exi.EXIFactory;
import com.siemens.ct.exi.EncodingOptions;
import com.siemens.ct.exi.FidelityOptions;
import com.siemens.ct.exi.api.sax.EXIResult;
import com.siemens.ct.exi.api.sax.SAXDecoder;
import com.siemens.ct.exi.exceptions.EXIException;

public class EXIBaseProcessor {
        
    protected EXIFactory exiFactory;
    protected EXIResult exiResult;
    protected SAXSource exiSource;
    
    /**
     * Constructs an EXI Processor using <b>xsdLocation</b> as the Canonical Schema and <b>default values</b> for its configuration.
     * @param xsdLocation
     * @throws EXIException
     */
    public EXIBaseProcessor(){}
        
    public static byte[] encodeEXIBody(String xml) throws EXIException, IOException, SAXException{
            byte[] exi = encodeSchemaless(xml, false);
            // With default options, the header is only 1 byte long: Distinguishing bits (10), Presence of EXI Options (0) and EXI Version (00000)
        System.arraycopy(exi, 1, exi, 0, exi.length - 1);
        return exi;
    }
    
    /**
     * Decodes an EXI body byte array using no schema files.
     * 
     * @param exi the EXI stanza to be decoded
     * @return a String containing the decoded XML
     * @throws IOException
     * @throws EXIException
     * @throws UnsupportedEncodingException 
     * @throws SAXException
     */
    public static String decodeExiBodySchemaless(byte[] exi) throws TransformerException, EXIException, UnsupportedEncodingException{
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        
        EXIFactory factory = new EXISetupConfiguration();
        
        SAXSource exiSource = new SAXSource(new InputSource(new ByteArrayInputStream(exi)));
        SAXDecoder saxDecoder = (SAXDecoder) factory.createEXIReader();
        try {
                saxDecoder.setFeature(Constants.W3C_EXI_FEATURE_BODY_ONLY, Boolean.TRUE);
        } catch (SAXNotRecognizedException e) {
                e.printStackTrace();
        } catch (SAXNotSupportedException e) {
                e.printStackTrace();
        }
        exiSource.setXMLReader(saxDecoder);

        ByteArrayOutputStream xmlDecoded = new ByteArrayOutputStream();
        transformer.transform(exiSource, new StreamResult(xmlDecoded));

        String xml = xmlDecoded.toString("UTF-8");
        return xml.substring(xml.indexOf('>') + 1);
    }
    

    /**
     * Encodes an XML String into an EXI byte array using no schema files and default {@link EncodingOptions} and {@link FidelityOptions}.
     * 
     * @param xml the String to be encoded
     * @return a byte array containing the encoded bytes
     * @param cookie if the encoding should include EXI Cookie or not
     * @throws IOException
     * @throws EXIException
     * @throws SAXException
     */
    public static byte[] encodeSchemaless(String xml, boolean cookie) throws IOException, EXIException, SAXException{
        ByteArrayOutputStream osEXI = new ByteArrayOutputStream();
        // start encoding process
        EXIFactory factory = new EXISetupConfiguration();
        
        if(cookie)        factory.getEncodingOptions().setOption(EncodingOptions.INCLUDE_COOKIE);
        
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        EXIResult exiResult = new EXIResult(factory);
        
        exiResult.setOutputStream(osEXI);
        xmlReader.setContentHandler(exiResult.getHandler());
        xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);        // ignorar DTD externos
        
        xmlReader.parse(new InputSource(new StringReader(xml)));
        
        return osEXI.toByteArray();
    }
    
    /**
     * Encodes an XML String into an EXI byte array using no schema files.
     * 
     * @param xml the String to be encoded
     * @param eo Encoding Options (if null, default will be used)
     * @param fo Fidelity Options (if null, default will be used)
     * @return a byte array containing the encoded bytes
     * @throws IOException
     * @throws EXIException
     * @throws SAXException
     */
    public static byte[] encodeSchemaless(String xml, EncodingOptions eo, FidelityOptions fo) throws IOException, EXIException, SAXException{
        ByteArrayOutputStream osEXI = new ByteArrayOutputStream();
        // start encoding process
        EXIFactory factory = new EXISetupConfiguration();
        // EXI configurations setup
        if(eo != null){
                factory.setEncodingOptions(eo);
        }
        if(fo != null){
        	factory.setFidelityOptions(fo);
        	factory.getFidelityOptions().setFidelity(FidelityOptions.FEATURE_PREFIX, true);
        }
        
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        EXIResult exiResult = new EXIResult(factory);
        
        exiResult.setOutputStream(osEXI);
        xmlReader.setContentHandler(exiResult.getHandler());
        xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);        // ignorar DTD externos
        
        xmlReader.parse(new InputSource(new StringReader(xml)));
        
        return osEXI.toByteArray();
    }
    
    /**
     * Decodes an EXI byte array using no schema files.
     * 
     * @param exi the EXI stanza to be decoded
     * @return a String containing the decoded XML
     * @throws IOException
     * @throws EXIException
     * @throws UnsupportedEncodingException 
     * @throws SAXException
     */
    public static String decodeSchemaless(byte[] exi) throws TransformerException, EXIException, UnsupportedEncodingException{
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        
        EXIFactory factory = new EXISetupConfiguration();
        SAXSource exiSource = new SAXSource(new InputSource(new ByteArrayInputStream(exi)));
        exiSource.setXMLReader(factory.createEXIReader());

        ByteArrayOutputStream xmlDecoded = new ByteArrayOutputStream();
        transformer.transform(exiSource, new StreamResult(xmlDecoded));

        String xml = xmlDecoded.toString("UTF-8");
        return xml.substring(xml.indexOf('>') + 1);
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
    
    public static boolean hasEXICookie(byte[] bba){
		byte[] ba = new byte[4];
        System.arraycopy(bba, 0, ba, 0, bba.length >= 4 ? 4 : bba.length);
		return "$EXI".equals(new String(ba));
	}
    
    /** FUNCIONES DEFINITIVAS Y PARA XSD VARIABLES **/
    
    protected byte[] encodeToByteArray(String xml) throws IOException, EXIException, SAXException, TransformerException{
        return encodeSchemaless(xml, false);
    }
    
    protected String decodeByteArray(byte[] exiBytes) throws IOException, EXIException, TransformerException{
        return decodeSchemaless(exiBytes);
    }
}
package cl.clayster.exi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.xerces.impl.dv.util.Base64;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.util.ObservableReader;
import org.jivesoftware.smack.util.ObservableWriter;
import org.xml.sax.SAXException;

import com.siemens.ct.exi.exceptions.EXIException;

/**
 * Defines necessary methods to establish an EXI connection over XMPP according to XEP-0322
 * 
 * @author Javier Placencio
 *
 */
public class EXIXMPPConnection extends XMPPConnection{
	
	public int schemaDownloads = 0;
	private boolean quickSetup = false;
	
	public EXIXMPPConnection(ConnectionConfiguration config) {
		super(config);
	}
	
	public boolean getQuickSetup(){
		return quickSetup;
	}
	
	/**
	 * Defines the reader and writer which are capable of sending both normal XMPP messages and EXI messages depending on which one is enabled.
	 */
	@Override
	public void initReaderAndWriter() throws XMPPException {
		EXIProcessor exiProcessor = null;
		try {
			exiProcessor = new EXIProcessor(EXIUtils.canonicalSchemaLocation);
		} catch (EXIException e1) {
			e1.printStackTrace();
		}
		if(exiProcessor == null){
			super.initReaderAndWriter();
			throw new XMPPException("Unable to create EXI processor. Continuing using normal XMPP only.");
		}
		try {
            if (compressionHandler == null) {
            	
                //reader = new EXIReader(new InputStreamReader(socket.getInputStream(), EXIProcessor.CHARSET), exiProcessor);
                //writer = new EXIWriter(new OutputStreamWriter(socket.getOutputStream(), EXIProcessor.CHARSET), exiProcessor);
            	reader = new EXIReader(socket.getInputStream(), exiProcessor);
                writer = new EXIWriter(socket.getOutputStream(), exiProcessor);
            }
            else {
                try {
                    OutputStream os = compressionHandler.getOutputStream(socket.getOutputStream());
                    //writer = new EXIWriter(new OutputStreamWriter(os, EXIProcessor.CHARSET), exiProcessor);
                    writer = new EXIWriter(os, exiProcessor);

                    InputStream is = compressionHandler.getInputStream(socket.getInputStream());
                    //reader = new EXIReader(new InputStreamReader(is, EXIProcessor.CHARSET), exiProcessor);
                    reader = new EXIReader(is, exiProcessor);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    compressionHandler = null;
                    //reader = new EXIReader(new InputStreamReader(socket.getInputStream(), EXIProcessor.CHARSET), exiProcessor);
                    reader = new EXIReader(socket.getInputStream(), exiProcessor);
                    //writer = new EXIWriter(new OutputStreamWriter(socket.getOutputStream(), EXIProcessor.CHARSET), exiProcessor);
                    writer = new EXIWriter(socket.getOutputStream(), exiProcessor);
                }
            }
        }
        catch (IOException ioe) {
            throw new XMPPException(
                    "EXI_XMPPError establishing connection with server.",
                    new XMPPError(XMPPError.Condition.remote_server_error,
                            "EXI_XMPPError establishing connection with server."),
                    ioe);
        }
		
        // If debugging is enabled, we open a window and write out all network traffic.
        initDebugger();
	}
	
	
	/**
	 * Turns on the EXI connection to start encoding and decoding EXI messages. Connection parameters should be already negotiated with the server.
	 * 
	 * @param enable true to enable EXI messages (false to disable)
	 * @throws IOException 
	 */
	public void enableEXI(boolean enable) throws IOException{
		if(reader instanceof ObservableReader && writer instanceof ObservableWriter){
			((EXIReader) ((ObservableReader) reader).wrappedReader).setEXI(enable);
			((EXIWriter) ((ObservableWriter) writer).wrappedWriter).setEXI(enable);
		}
		else if(reader instanceof EXIReader && writer instanceof EXIWriter){
			((EXIReader) reader).setEXI(enable);
			((EXIWriter) writer).setEXI(enable);
			System.out.println("sdada");
		}	
		else System.err.println("No se ha activado EXI");
		
		/*
		writer.write("<exi:streamStart from='client@im.example.com'"
				+ " to='im.example.com'"
				+ " version='1.0'"
				+ " xml:lang='en'"
				+ " xmlns:exi='http://jabber.org/protocol/compress/exi'>"
				+ " <exi:xmlns prefix='' namespace='jabber:client'/>"
				+ " <exi:xmlns prefix='streams' namespace='http://etherx.jabber.org/streams'/>"
				+ " <exi:xmlns prefix='exi' namespace='http://jabber.org/protocol/compress/exi'/>"
				+ " </exi:streamStart>");
		writer.flush();
		*/
	}
	
	/**
	 * Sends an EXI setup proposition to the server (assuming that the server supports EXI compression).
	 * Reads the schemas stanzas file, removes unnecessary attributes (which need to be present) and generates the Setup stanza to inform about the schemas needed.
	 * @throws IOException If there are problems reading the schemas stanzas file
	 * @throws DocumentException 
	 * @parameter quickSetup true if quick configurations are to be proposed, false otherwise
	 */
	public void proposeEXICompression(boolean quickSetup) throws IOException, DocumentException{
		String setupStanza = "";
		this.quickSetup = quickSetup;
		if(quickSetup){
			String configId = EXIUtils.readFile(EXIUtils.configurationIdLocation);
			if(configId != null){
				setupStanza = "<setup xmlns='http://jabber.org/protocol/compress/exi' configurationId='" + configId + "'/>";
			}
		}
		else{
			Element setupElement = DocumentHelper.parseText(EXIUtils.readFile(EXIUtils.schemasFileLocation)).getRootElement();
	 		Element auxSchema;
	        for (@SuppressWarnings("unchecked") Iterator<Element> i = setupElement.elementIterator("schema"); i.hasNext();) {
	        	auxSchema = i.next();
	        	auxSchema.remove(auxSchema.attribute("url"));
	        	auxSchema.remove(auxSchema.attribute("schemaLocation"));
	        }
	        setupStanza = setupElement.asXML();
		}
        writer.write(setupStanza);
	    writer.flush();
	}

	public void startExiCompression() throws IOException {
		writer.write("<compress xmlns=\'http://jabber.org/protocol/compress\'><method>exi</method></compress>");
	    writer.flush();
	}

	private void uploadMissingSchemas(List<String> missingSchemas) throws IOException, DocumentException, EXIException, SAXException, TransformerException, NoSuchAlgorithmException {
		String encodedBytes, schemaLocation = null;
		String canonicalSchemaString = EXIUtils.readFile(EXIUtils.canonicalSchemaLocation);
		Element canonicalSchemaElement = DocumentHelper.parseText(canonicalSchemaString).getRootElement();
		Element auxElement;
		for(String ms : missingSchemas){
			for (@SuppressWarnings("unchecked") Iterator<Element> i = canonicalSchemaElement.elementIterator("import"); i.hasNext();) {
				auxElement = i.next();
				if(auxElement.attributeValue("namespace").equals(ms)){
					schemaLocation = auxElement.attributeValue("schemaLocation");
					break;
				}
			}
			if(schemaLocation == null){
				System.err.println("error: no se ha encontrado el archivo: " + ms);
				return;
			}
			
			String contentType = "Text", content = Base64.encode(Files.readAllBytes(Paths.get(schemaLocation)));
			
			encodedBytes = "<uploadSchema xmlns='http://jabber.org/protocol/compress/exi' contentType='" + contentType + "'>"
					.concat(content)
					.concat("</uploadSchema>");
		
		writer.write(encodedBytes);
		writer.flush();
		}
	}
	
	private void uploadCompressedMissingSchemas(List<String> missingSchemas) throws IOException, DocumentException, EXIException, SAXException, TransformerException, NoSuchAlgorithmException {
		String encodedBytes, schemaLocation = null;
		String canonicalSchemaString = EXIUtils.readFile(EXIUtils.canonicalSchemaLocation);
		Element canonicalSchemaElement = DocumentHelper.parseText(canonicalSchemaString).getRootElement();
		Element auxElement;
		for(String ms : missingSchemas){
			for (@SuppressWarnings("unchecked") Iterator<Element> i = canonicalSchemaElement.elementIterator("import"); i.hasNext();) {
				auxElement = i.next();
				if(auxElement.attributeValue("namespace").equals(ms)){
					schemaLocation = auxElement.attributeValue("schemaLocation");
					break;
				}
			}
			if(schemaLocation == null){
				System.err.println("error: no se ha encontrado el archivo: " + ms);
				return;
			}
			
			String content = Base64.encode(Files.readAllBytes(Paths.get(schemaLocation)));
			MessageDigest md = MessageDigest.getInstance("MD5");
	    	File file = new File(schemaLocation);
			String md5Hash = EXIUtils.bytesToHex(md.digest(Files.readAllBytes(file.toPath())));
			String archivo = new String(Files.readAllBytes(file.toPath()));
			String contentType = "ExiBody' md5Hash='" + md5Hash + "' bytes='" + file.length() + "'";
			//TODO: arreglar la siguiente linea, pide archivos pero es schemaless!!
			content = EXIProcessor.encodeSchemaless(archivo);
			
			encodedBytes = "<uploadSchema xmlns='http://jabber.org/protocol/compress/exi' contentType='" + contentType + "'>"
					.concat(content)
					.concat("</uploadSchema>");
		
		writer.write(encodedBytes);
		writer.flush();
		}
	}
	
	private void downloadSchemas(List<String> missingSchemas) throws IOException, NoSuchAlgorithmException, DocumentException, EXIException, SAXException, TransformerException{
		String msg = "", url = "";
		Element schemasElement;
		try {
			schemasElement = DocumentHelper.parseText(EXIUtils.readFile(EXIUtils.schemasFileLocation)).getRootElement();
		} catch (DocumentException e) {
			System.err.println("error: no se ha encontrado el archivo: " + EXIUtils.schemasFileLocation);
			return;
		}
		Element auxElement;
		for(String ms : missingSchemas){
			url = "";
			for (@SuppressWarnings("unchecked") Iterator<Element> i = schemasElement.elementIterator("schema"); i.hasNext();) {
				auxElement = i.next();
				if(auxElement.attributeValue("ns").equals(ms)){
					url = auxElement.attributeValue("url");
					break;
				}
			}
			if(!url.equals("")){
				msg = "<downloadSchema xmlns='http://jabber.org/protocol/compress/exi' url='" + url + "'/>";
				writer.write(msg);
				writer.flush();
				schemaDownloads++;
			}
			else{
				System.err.println("No url for " + ms + ". Trying to upload schema as binary file.");
				List<String> l = new ArrayList<String>();
				l.add(ms);
				uploadMissingSchemas(l);
			}
		}	
	}

	/**
	 * Send schemas that are missing in the server.
	 * 
	 * @param missingSchemas a list containing all schemas missing in the server
	 * @param opt how missing schemas will be sent to the server. Options are as follows
	 * <br> 1 - upload schema as EXI body
	 * <br> 2 - upload schema as EXI document
	 * <br> 3 - send a url for the server to download the schema by itself  
	 * <br> x - anything else to upload schema as a binary file
	 * @throws TransformerException 
	 * @throws SAXException 
	 * @throws EXIException 
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	public void sendMissingSchemas(List<String> missingSchemas, int opt) 
			throws NoSuchAlgorithmException, IOException, DocumentException, EXIException, SAXException, TransformerException {
		switch(opt){
			case 1:
				uploadCompressedMissingSchemas(missingSchemas);
				break;
			case 2:
				// TODO
				uploadCompressedMissingSchemas(missingSchemas);
				break;
			case 3:
				downloadSchemas(missingSchemas);
				break;
			default:	
				uploadMissingSchemas(missingSchemas);
				break;
		}
	}

	public void saveConfigId(String configId) {
		if(configId != null && !quickSetup){
			try {
				EXIUtils.writeFile(EXIUtils.configurationIdLocation, configId);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
	
}

package cl.clayster.exi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.apache.xerces.impl.dv.util.Base64;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
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
	
	public static final int USE_AVAILABLE = -1;
	public static final int UPLOAD_BINARY = 0;
	public static final int UPLOAD_EXI_DOCUMENT = 1;
	public static final int UPLOAD_EXI_BODY = 2;
	public static final int UPLOAD_URL = 3;
	
	protected boolean usingEXI = false;
	protected EXISetupConfiguration exiConfig;
	protected EXIBaseProcessor exiProcessor;
	
	protected int schemaDownloadsCounter = 0;
	protected boolean sentMissingSchemas = false;
	
	List<String> missingSchemas = new ArrayList<String>(0);
	HashMap<String, String> sentURL = new HashMap<String, String>(0);
	
	private int uploadSchemaOption = USE_AVAILABLE;
	
	private List<EXIEventListener> compressionStartedListeners =  new ArrayList<EXIEventListener>(0);
	
	/**
	 * This constructor uses the given <code>EXISetupConfiguration</code> to negotiate EXI compression while logging in.
	 * By default,  compression will be enabled unless <b>exiConfig</b> is null. 
	 * All schemas within the <i>schema</i> folder will be used.
	 * @param config configurations to connect to the server
	 * @param exiConfig EXI parameters to be used. If null, default EXI/XMPP parameters will be used.
	 */
	public EXIXMPPConnection(ConnectionConfiguration config, EXISetupConfiguration exiConfig) {
		super(config);
		
		config.setCompressionEnabled(true);
		//config.setCompressionEnabled(exiConfig != null); // to invoke useComprssion(EXISEtupConfiguration) afterwards TODO:¿?
		if(exiConfig == null)	exiConfig = new EXISetupConfiguration();
		this.exiConfig = exiConfig;
		return;
	}
	
	/**
	 * Sets new EXI compression configurations for this connection and start EXI negotiation with the server.
	 * This method is to be used when EXI compression has not been started while logging in to the server.
	 * @param exiConfig 
	 * @return true if stream compression negotiation was successful. false if exi was already being used.
	 */
	protected boolean useCompression(EXISetupConfiguration exiConfig){
		this.exiConfig = exiConfig;
		return useCompression();
	}
	
	@Override
	protected boolean useCompression() {
		if(!compressionMethods.contains("exi")){
			System.err.println("The server does not support EXI compression.");
			return false;
		}
		if(isUsingCompression()){
			return false;
		}
		
		try {
			EXIUtils.generateSchemasFile();
			setCanonicalSchema();
		} catch (NoSuchAlgorithmException | IOException | DocumentException e) {
			e.printStackTrace();
			return false;
		}
		
		// maybe use quick setup
		if(!proposeEXICompressionQuickSetup()){
			proposeEXICompression();
		}
		
		synchronized (this) {
            try {
                this.wait(SmackConfiguration.getPacketReplyTimeout() * 5);
            }
            catch (InterruptedException e) {
            }
        }
        return isUsingCompression();
	}
	
	public int getUploadSchemaOption(){
		return uploadSchemaOption;
	}
	
	public void setUploadSchemaOption(int option){
		if(option < -1 || option > 3)	option = UPLOAD_BINARY;
		uploadSchemaOption = option;
	}


	/**
	 * Uses quick configurations setup in order to skip the handshake. Only if the configured setup already has been used and exists. 
	 * @return	<b>true</b> if there is a previous configuration available, <b>false</b> otherwise 
	 */
	public boolean proposeEXICompressionQuickSetup(){
		if(!exiConfig.exists()){
			return false;
		}
		String setupStanza = "<setup xmlns='http://jabber.org/protocol/compress/exi' configurationId='" + exiConfig.getConfigutarionId() + "'/>";
		try {
			send(setupStanza);
			return true;
		} catch (IOException e) {
			System.err.println("Error while writing <setup> stanza: " + e.getMessage());
		}
		return false;
	}
	
	/**
	 * Sends an EXI setup proposition to the server (is called only if the compression is supported)
	 */
	public boolean proposeEXICompression(){		
		try {
			String setupStanza = parseSetupStanza();
	        send(setupStanza);
			return true;
		} catch (DocumentException e) {
			System.err.println("Unable to propose EXI compression. " + e.getMessage());
			return false;
		} catch (IOException e) {
			System.err.println("Error while writing <setup> stanza: " + e.getMessage());
			return false;
		}
	}
	
	protected String parseSetupStanza() throws DocumentException{
		Element auxSchema;
		Element canonicalSchema = DocumentHelper.parseText(EXIUtils.readFile(exiConfig.getCanonicalSchemaLocation())).getRootElement();
		List<String> namespaces = new ArrayList<String>();
		
		for(@SuppressWarnings("unchecked") Iterator<Element> i = canonicalSchema.elementIterator("import"); i.hasNext();) {
			auxSchema = i.next();
			namespaces.add(auxSchema.attributeValue("namespace"));
		}
		
		Element setupElement = DocumentHelper.parseText(EXIUtils.readFile(EXIUtils.schemasFileLocation)).getRootElement();
		// add compression parameters
		setupElement.addAttribute("version", "1");
		setupElement.addAttribute("alignment", exiConfig.getAlignmentString());
		setupElement.addAttribute("strict", String.valueOf(exiConfig.getFidelityOptions().isStrict()));
		setupElement.addAttribute("blockSize", String.valueOf(exiConfig.getBlockSize()));
		setupElement.addAttribute("valueMaxLength", String.valueOf(exiConfig.getValueMaxLength()));
		setupElement.addAttribute("valuePartitionCapacity", String.valueOf(exiConfig.getValuePartitionCapacity()));
        for(@SuppressWarnings("unchecked") Iterator<Element> i = setupElement.elementIterator("schema"); i.hasNext();) {
        	auxSchema = i.next();
        	if(!namespaces.contains(auxSchema.attributeValue("ns"))){
				setupElement.remove(auxSchema);
        		continue;
        	}
        	
        	auxSchema.remove(auxSchema.attribute("url"));
        	auxSchema.remove(auxSchema.attribute("schemaLocation"));
        }
        return setupElement.asXML();
	}
	
	protected void send(String message) throws IOException{
		writer.write(message);
		writer.flush();
	}
	
	protected void createEXIProcessor(String configId){
		exiConfig.setConfigurationId(configId);
		EXIUtils.saveExiConfig(exiConfig);
		try {
			exiProcessor = new EXIProcessor(exiConfig);
		} catch (EXIException e) {
			System.err.println("Unable to create EXI Processor." + e.getMessage());
			EXIUtils.saveExiConfig(null);
		}
	}
	
	public void requestEXICompression(String schemaId) {
		createEXIProcessor(schemaId);
		requestStreamCompression("exi");
	};
	
	public void startStreamCompression1() throws Exception{
		startStreamCompression();
	}
	
	@Override
	protected void startStreamCompression() throws Exception{
		serverAckdCompression = true;
		// Very important function set the EXI Processor to the EXIWriter and EXIReader!!
		setEXIProcessor();
		// enable EXIProcessor and send start stream tag
		openEXIStream();
		
		// Notify that compression is being used
        synchronized (this) {
            this.notify();
        }
	}
	

	/**
	 * Send schemas that are missing in the server. Then waits one second and retries configuration with a new <setup> stanza.
	 * 
	 * @param missingSchemas a list containing all schemas missing in the server
	 * @param opt how missing schemas will be sent to the server. Options are as follows
	 * <br> 0 - upload schema as a binary file
	 * <br> 1 - upload schema as EXI document
	 * <br> 2 - upload schema as EXI body
	 * <br> 3 - send a url for the server to download the schema by itself  
	 * <br> -1 - create a new canonical schema ignoring missing schemas (default)
	 * @throws TransformerException 
	 * @throws SAXException 
	 * @throws EXIException 
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * @throws XMLStreamException 
	 */
	public void sendMissingSchemas(List<String> missingSchemas, int opt) 
			throws NoSuchAlgorithmException, IOException, DocumentException, EXIException, SAXException, TransformerException, XMLStreamException {
		sentMissingSchemas = true;
		switch(opt){
			case UPLOAD_EXI_DOCUMENT: // upload compressed EXI document
				uploadCompressedMissingSchemas(missingSchemas, false);
				break;
			case UPLOAD_EXI_BODY: // upload compressed EXI body
				uploadCompressedMissingSchemas(missingSchemas, true);
				break;
			case UPLOAD_URL:	// send URL and download on server 
				downloadSchemas(missingSchemas);
				break;
			case UPLOAD_BINARY: // upload binary file
				uploadMissingSchemas(missingSchemas);
				break;
			case USE_AVAILABLE:
			default:
				this.missingSchemas = missingSchemas;
				setCanonicalSchema();
				break;
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		proposeEXICompression();
	}
	
	/**
	 * Creates a canonical schema and adds its schema ID to the current connection's EXISetupConfiguration. 
	 * The new canonical schema excludes missing schemas if existent.
	 */
	public void setCanonicalSchema() {
		try {
			exiConfig.setSchemaId(EXIUtils.generateCanonicalSchema(missingSchemas));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isUsingCompression(){
		return this.usingEXI;
	}

	/**
	 * Defines the reader and writer which are capable of sending both normal XMPP messages and EXI messages depending on which one is enabled.
	 */
	@Override
	protected void initReaderAndWriter() throws XMPPException {
		try {
        	reader = new EXIReader(socket.getInputStream());
            writer = new EXIWriter(socket.getOutputStream());
        }
        catch (IOException ioe) {
            throw new XMPPException(
                    "EXI_XMPPError establishing connection with server.",
                    new XMPPError(XMPPError.Condition.remote_server_error,
                            "EXI_XMPPError establishing connection with server."),
                    ioe);
        }
	}
	
	/**
	 * Sets the current exiProcessor in this class as the current EXIProcessor in EXIWriter and EXIReader 
	 */
	private void setEXIProcessor(){
		if(reader instanceof ObservableReader && writer instanceof ObservableWriter){
			((EXIReader) ((ObservableReader) reader).wrappedReader).setExiProcessor(exiProcessor);
			((EXIWriter) ((ObservableWriter) writer).wrappedWriter).setExiProcessor(exiProcessor);
		}
		else if(reader instanceof EXIReader && writer instanceof EXIWriter){
			((EXIReader) reader).setExiProcessor(exiProcessor);
			((EXIWriter) writer).setExiProcessor(exiProcessor);
		}
		else System.err.println("Unable to create EXI Processor: Instances of reader and writer are not treated. (EXIXMPPConnection.setEXIProcessor)");
	}

	/**
	 * Turns on the EXI connection to start encoding and decoding EXI messages. Connection parameters should be already negotiated with the server.
	 * 
	 * @throws IOException 
	 */
	protected void enableEXI(){
		if(reader instanceof ObservableReader && writer instanceof ObservableWriter){
			((EXIReader) ((ObservableReader) reader).wrappedReader).setEXI(true);
			((EXIWriter) ((ObservableWriter) writer).wrappedWriter).setEXI(true);
		}
		else if(reader instanceof EXIReader && writer instanceof EXIWriter){
			((EXIReader) reader).setEXI(true);
			((EXIWriter) writer).setEXI(true);
		}	
		else {
			System.err.println("Unable to create EXI Processor: Instances of reader and writer are not treated. (EXIXMPPConnection.enableEXI)");
			return;
		}
		this.usingEXI = true;
		if(!compressionStartedListeners.isEmpty()){
			for(EXIEventListener eel : compressionStartedListeners){
				eel.compressionStarted();
			}
		}
	}
	
	protected void openEXIStream() throws IOException{
		enableEXI();
		String exiOpen = "<exi:open from='"
				+ getUser()
	 			+ "' to='"
	 			+ getHost()
	 			+ "' version='1.0'"
	 			+ " xml:lang='en'"
	 			+ " xmlns:exi='http://jabber.org/protocol/compress/exi'>"
	 			+ "<exi:xmlns prefix='' namespace='jabber:client'/>"
	 			+ "<exi:xmlns prefix='streams' namespace='http://etherx.jabber.org/streams'/>"
	 			+ "<exi:xmlns prefix='exi' namespace='http://jabber.org/protocol/compress/exi'/>"
	 			+ "</exi:open>";
		send(exiOpen);
	}
	
	

	private void uploadMissingSchemas(List<String> missingSchemas) throws IOException, DocumentException, EXIException, SAXException, TransformerException, NoSuchAlgorithmException {
		String xml, schemaLocation = null;
		String schemasFileContent = EXIUtils.readFile(EXIUtils.schemasFileLocation);
		Element schemasFileElement = DocumentHelper.parseText(schemasFileContent).getRootElement();
		Element auxElement;
		for(String ms : missingSchemas){
			for (@SuppressWarnings("unchecked") Iterator<Element> i = schemasFileElement.elementIterator("schema"); i.hasNext();) {
				auxElement = i.next();
				if(ms.equals(auxElement.attributeValue("ns"))){
					schemaLocation = auxElement.attributeValue("schemaLocation");
					break;
				}
			}
			if(schemaLocation == null){
				System.err.println("error: no se ha encontrado el archivo para " + ms);
				return;
			}
			
			String contentType = "Text", content = Base64.encode(Files.readAllBytes(Paths.get(schemaLocation)));
			
			xml = "<uploadSchema xmlns='http://jabber.org/protocol/compress/exi' contentType='" + contentType + "'>"
					.concat(content)
					.concat("</uploadSchema>");
		
		send(xml);
		}
	}
	
	private void uploadCompressedMissingSchemas(List<String> missingSchemas, boolean exiBody) throws IOException, DocumentException, EXIException, SAXException, TransformerException, NoSuchAlgorithmException, XMLStreamException {
		String schemaLocation = null;
		String schemasFileContent = EXIUtils.readFile(EXIUtils.schemasFileLocation);
		Element schemasFileElement = DocumentHelper.parseText(schemasFileContent).getRootElement();
		Element auxElement;
		
		byte[] ba, xmlStart, xmlEnd = "</uploadSchema>".getBytes();
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		for(String ms : missingSchemas){
			for (@SuppressWarnings("unchecked") Iterator<Element> i = schemasFileElement.elementIterator("schema"); i.hasNext();) {
				auxElement = i.next();
				if(ms.equals(auxElement.attributeValue("ns"))){
					schemaLocation = auxElement.attributeValue("schemaLocation");
					break;
				}
			}
			if(schemaLocation == null){
				System.err.println("error: no se ha encontrado el archivo para " + ms);
				return;
			}
			
			MessageDigest md = MessageDigest.getInstance("MD5");
	    	File file = new File(schemaLocation);
			String md5Hash = EXIUtils.bytesToHex(md.digest(Files.readAllBytes(file.toPath())));
			String archivo = new String(Files.readAllBytes(file.toPath()));
			String contentType = "ExiDocument";

			byte[] content = new byte[]{};
			if(exiBody){
				contentType = "ExiBody";
				content = EXIProcessor.encodeEXIBody(archivo);
			}
			else{
				content = EXIProcessor.encodeSchemaless(archivo, false);
			}
			
			xmlStart = ("<uploadSchema xmlns='http://jabber.org/protocol/compress/exi'"
                    + " contentType='" + contentType + "' md5Hash='" + md5Hash + "' bytes='" + file.length() + "'>").getBytes();                    
			
			ba = new byte[xmlStart.length + content.length + xmlEnd.length];
			System.arraycopy(xmlStart, 0, ba, 0, xmlStart.length);
			System.arraycopy(content, 0, ba, xmlStart.length, content.length);
			System.arraycopy(xmlEnd, 0, ba, xmlStart.length + content.length, xmlEnd.length);
			
			bos.write(ba);
			bos.flush();
		}
	}
	
	private void downloadSchemas(List<String> missingSchemas) throws IOException, NoSuchAlgorithmException, DocumentException, EXIException, SAXException, TransformerException{
		String msg = "", url = "";
		Element schemasElement;
		List<String> noURL = new ArrayList<String>();
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
				send(msg);
				schemaDownloadsCounter++;
				sentURL.put(url, ms);
			}
			else{
				System.err.println("No url for " + ms + ". Trying to upload schema as binary file.");
				noURL.add(ms);
			}
		}
		this.missingSchemas = noURL;
		setCanonicalSchema();
	}
	
	/**
	 * Used to reduce and get the count of schemas being downloaded on the server.
	 * @return Amount of schemas left to be downloaded. When 0 is returned, then the server has downloaded all schemas.
	 */
	public int schemaDownloaded(){
		return --this.schemaDownloadsCounter;
	}
	
	public boolean missingSchemasSent(){
		return this.sentMissingSchemas;
	}
	
	/**
	 * Adds an EXIEventListener to the connection. Needs to be called after connection has been done.
	 * @param listener
	 */
	public void addEXIEventListener(EXIEventListener listener){
		addCompressionStartedListener(listener);
		addReadListener(listener);
		addWriteListener(listener);
	}
	
	public boolean removeEXIEventListener(EXIEventListener listener){
		return (removeCompressionStartedListener(listener) && removeReadListener(listener) && removeWriteListener(listener));
	}
	
	public void addCompressionStartedListener(EXIEventListener listener){
		compressionStartedListeners.add(listener);
	}
	
	public boolean removeCompressionStartedListener(EXIEventListener listener){
		return compressionStartedListeners.remove(listener);
	}	
	
	public void addReadListener(EXIEventListener listener){
		if(reader instanceof ObservableReader){
			((EXIReader) ((ObservableReader) reader).wrappedReader).addReadListener(listener);
		}
		else if(reader instanceof EXIReader){
			((EXIReader) reader).addReadListener(listener);
		}
		else System.err.println("Unable to add EXIReadListener: An instance of wrapped reader is not treated.");
	}
	
	public boolean removeReadListener(EXIEventListener listener){
		if(reader instanceof ObservableReader){
			return ((EXIReader) ((ObservableReader) reader).wrappedReader).removeReadListener(listener);
		}
		else if(reader instanceof EXIReader){
			return ((EXIReader) reader).removeReadListener(listener);
		}
		else {
			System.err.println("Unable to add EXIReadListener: An instance of wrapped reader is not treated.");
			return false;
		}
		
	}
	
	public void addWriteListener(EXIEventListener listener){
		if(writer instanceof ObservableWriter){
			((EXIWriter) ((ObservableWriter) writer).wrappedWriter).addWriteListener(listener);
		}
		else if(writer instanceof EXIWriter){
			((EXIWriter) writer).addWriteListener(listener);
		}
		else System.err.println("Unable to add EXIWriteListener: An instance of wrapped writer is not treated.");
	}
	
	public boolean removeWriteListener(EXIEventListener listener){
		if(writer instanceof ObservableWriter){
			return ((EXIWriter) ((ObservableWriter) writer).wrappedWriter).removeWriteListener(listener);
		}
		else if(writer instanceof EXIWriter){
			return ((EXIWriter) writer).removeWriteListener(listener);
		}
		else {
			System.err.println("Unable to add EXIWriteListener: An instance of wrapped writer is not treated.");
			return false;
		}
	}
	
	public boolean addMissingSchemaByURL(String URL){
		return missingSchemas.add(sentURL.get(URL));
	}
	
	public boolean removeMissingSchema(String ns){
		if(ns != null){
			return missingSchemas.remove(ns);
		}
		else return false;
	}
}

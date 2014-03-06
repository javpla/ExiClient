package cl.clayster.exi;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.smack.ConnectionConfiguration;

public class EXIXMPPAlternativeConnection extends EXIXMPPConnection {

	public EXIXMPPAlternativeConnection(ConnectionConfiguration config, EXISetupConfiguration exiConfig) {
		super(config, exiConfig);
		this.exiConfig = exiConfig;
	}
	
	public void startAlternativeBinding() throws IOException{
		exiProcessor = new EXIBaseProcessor();
		try {
			startStreamCompression();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void openEXIStream() throws IOException{
		enableEXI(true);
		String exiStreamStart = "<?xml version=\"1.0\"?>"
				+ "<exi:streamStart xmlns:exi='http://jabber.org/protocol/compress/exi' version=\"1.0\" to=\""
				+ getHost()
				+ "\" xml:lang=\"en\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" >"
				+ "<exi:xmlns prefix=\"stream\" namespace=\"http://etherx.jabber.org/streams\" />"
				+ "<exi:xmlns prefix=\"\" namespace=\"jabber:client\" />"
				+ "<exi:xmlns prefix=\"xml\" namespace=\"http://www.w3.org/XML/1998/namespace\" />"
				+ "</exi:streamStart>";
		writer.write(exiStreamStart);
		writer.flush();
	}
	
	public void negotiateConfigurations()	{
		//if(exiConfig == null)	return; // continue using default EXI configurations
		if(exiConfig == null){
			exiConfig = new EXISetupConfiguration(false);
		}
		try {
			EXIUtils.generateBoth(EXIUtils.schemasFolder, exiConfig);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		String setupStanza = "";
		try {
			Element setupElement = DocumentHelper.parseText(EXIUtils.readFile(EXIUtils.schemasFileLocation)).getRootElement();
	 		Element auxSchema;
	        for (@SuppressWarnings("unchecked") Iterator<Element> i = setupElement.elementIterator("schema"); i.hasNext();) {
	        	auxSchema = i.next();
	        	auxSchema.remove(auxSchema.attribute("url"));
	        	auxSchema.remove(auxSchema.attribute("schemaLocation"));
	        }
	        setupStanza = setupElement.asXML();
	        writer.write(setupStanza);
			writer.flush();
			return;
		} catch (DocumentException e) {
			System.err.println("Unable to propose EXI compression. " + e.getMessage());
			return;
		} catch (IOException e) {
			System.err.println("Error while writing <setup> stanza: " + e.getMessage());
			return;
		}
	}
}

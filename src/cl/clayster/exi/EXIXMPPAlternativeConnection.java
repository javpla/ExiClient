package cl.clayster.exi;

import java.io.File;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;

public class EXIXMPPAlternativeConnection extends EXIXMPPConnection {

	public EXIXMPPAlternativeConnection(ConnectionConfiguration config, EXISetupConfiguration exiConfig) {
		super(config, exiConfig);
		this.exiConfig = exiConfig;
	}
	
	public EXIXMPPAlternativeConnection(ConnectionConfiguration config, EXISetupConfiguration exiConfig, File canonicalSchema) {
		super(config, exiConfig, canonicalSchema);
		this.exiConfig = exiConfig;
	}
	
	@Override
	protected void initReaderAndWriter() throws XMPPException {
		try {
        	reader = new EXIAltReader(socket.getInputStream());
            writer = new EXIWriter(socket.getOutputStream(), true);
		}
        catch (IOException ioe) {
            throw new XMPPException(
                    "EXI_XMPPError establishing connection with server.",
                    new XMPPError(XMPPError.Condition.remote_server_error,
                            "EXI_XMPPError establishing connection with server."),
                    ioe);
        }
	}
	
	public void startAlternativeBinding() throws IOException{
		try {
			exiProcessor = new EXIProcessor();
			startStreamCompression();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void restartEXIStream(String configId){
		EXIUtils.saveConfigId(configId);
		try {
			exiProcessor = new EXIProcessor(canonicalSchemaLocation, exiConfig);
			startStreamCompression();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void openEXIStream() throws IOException{
		String exiStreamStart = "<?xml version=\"1.0\"?>"
				+ "<exi:streamStart xmlns:exi='http://jabber.org/protocol/compress/exi' version=\"1.0\" to=\""
				+ getHost()
				+ "\" xml:lang=\"en\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" >"
				+ "<exi:xmlns prefix=\"stream\" namespace=\"http://etherx.jabber.org/streams\" />"
				+ "<exi:xmlns prefix=\"\" namespace=\"jabber:client\" />"
				+ "<exi:xmlns prefix=\"xml\" namespace=\"http://www.w3.org/XML/1998/namespace\" />"
				+ "</exi:streamStart>";
		((EXIWriter)writer).writeWithCookie(exiStreamStart);
		writer.flush();
	}
	
	@Override
	protected String parseSetupStanza() throws DocumentException{
		// TODO: ¿?
		if(exiConfig == null)	exiConfig = new EXISetupConfiguration();
		String setupStanza = super.parseSetupStanza();
		setupStanza = setupStanza.replaceAll("<setup", "<exi:setup").replaceAll("xmlns=", "xmlns:exi=")
				.replaceAll("<schema", "<exi:schema").replaceAll("</setup>", "</exi:setup>");
		return setupStanza;
	}
}
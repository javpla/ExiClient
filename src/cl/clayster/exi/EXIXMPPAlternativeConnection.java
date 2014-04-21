package cl.clayster.exi;

import java.io.IOException;

import org.dom4j.DocumentException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;

import com.siemens.ct.exi.EncodingOptions;
import com.siemens.ct.exi.exceptions.EXIException;

public class EXIXMPPAlternativeConnection extends EXIXMPPConnection {
	
	private boolean triedQuickSetup = false;
	
	public EXIXMPPAlternativeConnection(ConnectionConfiguration config, EXISetupConfiguration exiConfig) {
		super(config, exiConfig);
		setCanonicalSchema();
	}
	
	@Override
	protected void initReaderAndWriter() throws XMPPException {
		try {
        	reader = new EXIAltReader(socket.getInputStream());
            writer = new EXIWriter(socket.getOutputStream(), true);
            if(exiProcessor != null){
            	setEXIProcessor();
            }
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
			if(!triedQuickSetup){
				if(!tryQuickSetup()){
					exiProcessor = new EXIProcessor(null);	// create exi processor with default configurations
				}
				startStreamCompression();
			}
			else{
				openEXIStream();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void restartEXIStream(String configId){
		createEXIProcessor(configId);
		try {
			startStreamCompression();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void openEXIStream() throws IOException{
		String exiOpen = "<?xml version=\"1.0\"?>"
				+ "<exi:open xmlns:exi='http://jabber.org/protocol/compress/exi' version=\"1.0\" to=\""
				+ getHost()
				+ "\" xml:lang=\"en\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\">"
				+ "<exi:xmlns prefix=\"stream\" namespace=\"http://etherx.jabber.org/streams\"/>"
				+ "<exi:xmlns prefix=\"\" namespace=\"jabber:client\"/>"
				+ "<exi:xmlns prefix=\"xml\" namespace=\"http://www.w3.org/XML/1998/namespace\"/>"
				+ "</exi:open>";
		((EXIWriter)writer).writeWithCookie(exiOpen);
		writer.flush();
	}
	
	@Override
	protected String parseSetupStanza() throws DocumentException{
		String setupStanza = super.parseSetupStanza();
		setupStanza = setupStanza.replaceAll("<setup", "<exi:setup").replaceAll("xmlns=", "xmlns:exi=")
				.replaceAll("<schema", "<exi:schema").replaceAll("</setup>", "</exi:setup>");
		return setupStanza;
	}
	
	@Override
	protected boolean useCompression() {
		if(triedQuickSetup || exiConfig == new EXISetupConfiguration()){
			this.usingEXI = true;
			return true;
		}
		else{
			return super.useCompression();
		}
		
	}
	
	private boolean tryQuickSetup() throws EXIException{
		triedQuickSetup = true;
		if(exiConfig.exists()){
			exiConfig.getEncodingOptions().setOption(EncodingOptions.INCLUDE_OPTIONS);
			// TODO: schema id debe incluir c:
			exiConfig.getEncodingOptions().setOption(EncodingOptions.INCLUDE_SCHEMA_ID);
			exiProcessor = new EXIProcessor(exiConfig);
System.out.println("QUICK AB SETUP using: " + exiConfig);
			return true;
		}
		else{
			return false;
		}
	}
}
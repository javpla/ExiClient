package cl.clayster.exi;

import java.io.IOException;

import org.jivesoftware.smack.ConnectionConfiguration;

public class EXIXMPPAlternativeConnection extends EXIXMPPConnection {

	public EXIXMPPAlternativeConnection(ConnectionConfiguration config) {
		super(config);
	}
	
	public EXIXMPPAlternativeConnection(ConnectionConfiguration config, EXISetupConfiguration exiConfig) {
		super(config);
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
	
}

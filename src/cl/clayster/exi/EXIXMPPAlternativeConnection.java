package cl.clayster.exi;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;

/**
 * Defines necessary methods to establish an EXI connection over XMPP according to XEP-0322
 * 
 * @author Javier Placencio
 *
 */
public class EXIXMPPAlternativeConnection extends EXIXMPPConnection{
	
	public EXIXMPPAlternativeConnection(ConnectionConfiguration config) {
		super(config);
	}
	
	/**
	 * Defines the reader and writer which are capable of sending both normal XMPP messages and EXI messages depending on which one is enabled.
	 */
	@Override
	protected void initReaderAndWriter() throws XMPPException {
		super.initReaderAndWriter();
		enableEXI(true);
	}
	
}

package cl.clayster.exi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.util.ObservableReader;
import org.jivesoftware.smack.util.ObservableWriter;

/**
 * Defines necessary methods to establish an EXI connection over XMPP according to XEP-0322
 * 
 * @author Javier Placencio
 *
 */
public class EXIXMPPConnection extends XMPPConnection{
	
	public EXIXMPPConnection(ConnectionConfiguration config) {
		super(config);
	}
	
	/**
	 * Defines the reader and writer which are capable of sending both normal XMPP messages and EXI messages depending on which one is enabled.
	 */
	@Override
	public void initReaderAndWriter() throws XMPPException {
		try {
            if (compressionHandler == null) {
            	
                reader = new EXIReader(new InputStreamReader(socket.getInputStream(), EXIProcessor.CHARSET));
                writer = new EXIWriter(new OutputStreamWriter(socket.getOutputStream(), EXIProcessor.CHARSET));
            }
            else {
                try {
                    OutputStream os = compressionHandler.getOutputStream(socket.getOutputStream());
                    writer = new EXIWriter(new OutputStreamWriter(os, EXIProcessor.CHARSET));

                    InputStream is = compressionHandler.getInputStream(socket.getInputStream());
                    reader = new EXIReader(new InputStreamReader(is, EXIProcessor.CHARSET));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    compressionHandler = null;
                    reader = new EXIReader(new InputStreamReader(socket.getInputStream(), EXIProcessor.CHARSET));
                    writer = new EXIWriter(new OutputStreamWriter(socket.getOutputStream(), EXIProcessor.CHARSET));
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
	 */
	public void enableEXI(boolean enable){
		if(reader instanceof ObservableReader && writer instanceof ObservableWriter){
			((EXIReader) ((ObservableReader) reader).wrappedReader).setEXI(enable);
			((EXIWriter) ((ObservableWriter) writer).wrappedWriter).setEXI(enable);
		}
		if(reader instanceof EXIReader && writer instanceof EXIWriter){
			((EXIReader) reader).setEXI(enable);
			((EXIWriter) writer).setEXI(enable);
			System.out.println("sdada");
		}		
	}
	
	/**
	 * Starts negotiating an EXI connection with the server by sending a setup suggestion. Assumes that the server offers EXI compression method.
	 * @throws IOException
	 */
	public void proposeEXICompression() throws IOException{
		// TODO: hacer la negociacion
		writer.write(generarSetupStanza());
	    writer.flush();
	        
	}
	
	public String generarSetupStanza(){
		StringBuilder stream = new StringBuilder();
        stream.append("<setup xmlns=\'http://jabber.org/protocol/compress/exi\' version=\'1\' strict=\'true\' blockSize=\'1024\' valueMaxLength=\'32\' valuePartitionCapacity=\'100\'");
        stream.append("</setup>");
        return stream.toString();
	}
	
}

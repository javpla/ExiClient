package cl.clayster.exi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

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

import com.siemens.ct.exi.exceptions.EXIException;

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
            	
                reader = new EXIReader(new InputStreamReader(socket.getInputStream(), EXIProcessor.CHARSET), exiProcessor);
                writer = new EXIWriter(new OutputStreamWriter(socket.getOutputStream(), EXIProcessor.CHARSET), exiProcessor);
            }
            else {
                try {
                    OutputStream os = compressionHandler.getOutputStream(socket.getOutputStream());
                    writer = new EXIWriter(new OutputStreamWriter(os, EXIProcessor.CHARSET), exiProcessor);

                    InputStream is = compressionHandler.getInputStream(socket.getInputStream());
                    reader = new EXIReader(new InputStreamReader(is, EXIProcessor.CHARSET), exiProcessor);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    compressionHandler = null;
                    reader = new EXIReader(new InputStreamReader(socket.getInputStream(), EXIProcessor.CHARSET), exiProcessor);
                    writer = new EXIWriter(new OutputStreamWriter(socket.getOutputStream(), EXIProcessor.CHARSET), exiProcessor);
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
	 * Reads the schemas stanzas file and generates the Setup stanza to inform about the schemas needed.
	 * @throws IOException If there are problems reading the schemas stanzas file
	 */
	public void proposeEXICompression() throws IOException{
	    String schemas = EXIUtils.readFile(EXIUtils.schemasFileLocation);
        
        writer.write(schemas);
	    writer.flush();
	}

	public void startExiCompression() throws IOException {
		writer.write("<compress xmlns=\'http://jabber.org/protocol/compress\'><method>exi</method></compress>");
	    writer.flush();
	}

	public void sendMissingSchemas(List<String> missingSchemas) throws IOException, DocumentException {
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
			
			encodedBytes = "<uploadSchema xmlns='http://jabber.org/protocol/compress/exi' contentType='Text'>"
					.concat(Base64.encode(EXIUtils.readFile(schemaLocation).getBytes()))
					.concat("</uploadSchema>");
		
		writer.write(encodedBytes);
		writer.flush();
		}
	}
	
	
	
}

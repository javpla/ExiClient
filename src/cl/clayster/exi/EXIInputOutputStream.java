package cl.clayster.exi;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.TransformerException;

import org.jivesoftware.smack.compression.XMPPInputOutputStream;
import org.xml.sax.SAXException;

import com.siemens.ct.exi.exceptions.EXIException;


public class EXIInputOutputStream extends XMPPInputOutputStream {
	
	private byte[] anterior;
	
	public boolean supported;
	private EXIProcessor ep;

	public EXIInputOutputStream(){
		compressionMethod = "exi";
		
		supported = false;
		anterior = new byte[8000];
	}

	@Override
	public boolean isSupported() {
		return supported;
	}

	@Override
	public InputStream getInputStream(InputStream inputStream) throws Exception {
		InputStream is;
		try {
			is = ep.decodeToStream(inputStream);
		} catch(EXIException | SAXException | TransformerException e){
			inputStream.read(anterior);
			return null;
		}
		anterior = new byte[8000];
		return is;
	}

	@Override
	public OutputStream getOutputStream(OutputStream outputStream)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
    
}

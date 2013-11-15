package cl.clayster.exi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.siemens.ct.exi.exceptions.EXIException;

public class EXIReader extends BufferedReader {
	 
	private boolean exi = false;
	private EXIProcessor exiProcessor;
	private BufferedInputStream is;
	private ByteBuffer buf;
	
    public EXIReader(InputStream in, EXIProcessor exiProcessor) throws UnsupportedEncodingException {
    	super(new InputStreamReader(in, EXIProcessor.CHARSET));
    	is = new BufferedInputStream(in);
    	this.exiProcessor = exiProcessor;
    }
    
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
System.out.println("received: " + new String(cbuf));
    	buf = ByteBuffer.allocate(len);
    	if(exi && EXIProcessor.isEXI((byte) cbuf[0])){
    		is.read(buf.array(), off, len);
    		String exi = new String(cbuf);
    		try {
    			exi = exiProcessor.decode(buf.array());
System.out.println("decoded: " + exi);    			
    			cbuf = exi.toCharArray();
    		} catch (EXIException | SAXException | TransformerException e) {
    			e.printStackTrace();
    		}
    	}
    	return super.read(cbuf, off, len);
    }
    
    boolean isEXI() {
		return exi;
	}


	void setEXI(boolean usarEXI) {
		this.exi = usarEXI;
	}


	@Override
    public int read(char[] cbuf) throws IOException {
    	return super.read(cbuf);
    }
    
    @Override
    public int read() throws IOException {
    	return super.read();
    }
    
    @Override
    public int read(CharBuffer target) throws IOException {
    	return super.read(target);
    }

}

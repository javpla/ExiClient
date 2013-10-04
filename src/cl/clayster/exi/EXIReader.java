package cl.clayster.exi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.siemens.ct.exi.exceptions.EXIException;

public class EXIReader extends BufferedReader {
	 
	private boolean exi = false;
	private EXIProcessor exiProcessor;
	
    public EXIReader(Reader wrappedReader, EXIProcessor exiProcessor) {
    	super(wrappedReader);
    	
    	this.exiProcessor = exiProcessor;
    }
    
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
    	if(exi && EXIProcessor.isEXI((byte) cbuf[off])){
    		String exi = new String(cbuf);
    		try {
    			exi = exiProcessor.decode(exi.substring(off, off + len));
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

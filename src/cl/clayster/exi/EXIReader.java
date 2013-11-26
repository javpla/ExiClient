package cl.clayster.exi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.siemens.ct.exi.exceptions.EXIException;

public class EXIReader extends BufferedReader {
	 
	private boolean exi = false;
	private EXIProcessor exiProcessor;
	private int leido = 0;
	
	private BufferedInputStream is;
	private byte[] ba;
	
	private byte[] anterior;
	
    public EXIReader(InputStream in, EXIProcessor exiProcessor) throws UnsupportedEncodingException {
    	super(new InputStreamReader(in, "UTF-8"));
    	this.exiProcessor = exiProcessor;
    	this.is = new BufferedInputStream(in);
    }
    
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
    	
    	synchronized (lock) {
    		if(!exi){
    			return super.read(cbuf, off, len);
    		}
    		ba = new byte[len];
    		leido = is.read(ba, 0, len);
System.out.println("Recibido(" + leido + "): " + new String(ba));
	    	if(exi){
	    		if(anterior != null || EXIProcessor.isEXI(ba[0])){
System.out.println("EXI(" + leido + "): " + EXIUtils.bytesToHex(ba));
	    			if(leido <= 3){
	    				anterior = new byte[leido];
	    				System.arraycopy(ba, 0, anterior, 0, leido);
System.out.println("bytes guardados: " + EXIUtils.bytesToHex(anterior));
						return leido;
	    			}
	    			else if(anterior != null){
		    			System.arraycopy(ba, 0, ba, anterior.length, ba.length - anterior.length);
		    			System.arraycopy(anterior, 0, ba, 0, anterior.length);
		    		}
			    	try {
			    		//System.arraycopy(exiProcessor.decode(ba).toCharArray(), 0, cbuf, off, leido);
			    		cbuf = exiProcessor.decode(ba).toCharArray();
System.out.println("decoded XML(" + (leido + anterior.length) + "): " + new String(cbuf));
						anterior = null;
						ba = null;
			    		return leido;
			    	} catch (EXIException | SAXException | TransformerException e) {
			    		e.printStackTrace();
			    	}
				}
	    	}
	    	System.arraycopy(new String(ba).toCharArray(), 0, cbuf, off, leido);
System.out.println("XML(" + leido + "): " + new String(cbuf));
			return leido;
	    }
    	/*
    	leido = super.read(cbuf, off, len);
    	if(leido <= 0)	return leido;
    	if(exi && EXIProcessor.isEXI((byte) cbuf[off])){
    		if(leido <= 1){
    			anterior = new char[leido];
System.out.println("bytes guardados: " + EXIUtils.bytesToHex(Charset.forName("UTF-8").encode(CharBuffer.wrap(anterior)).array()));
    			System.arraycopy(cbuf, off, anterior, 0, leido);
    			return leido;
    		}
            try {
            		byte[] bytes = new String(cbuf).substring(off - anterior.length, off + len).getBytes();
System.out.println("bytes a decodificar: " + EXIUtils.bytesToHex(bytes));
                    cbuf = exiProcessor.decode(bytes).toCharArray();
                    anterior = null;
            } catch (EXIException | SAXException | TransformerException e) {
                    e.printStackTrace();
            }
    	}
    	return leido;
    	/**/
    }
    
    
    boolean isEXI() {
		return exi;
	}


	void setEXI(boolean usarEXI) {
		this.exi = usarEXI;
	}

}

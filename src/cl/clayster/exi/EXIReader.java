package cl.clayster.exi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import com.siemens.ct.exi.exceptions.EXIException;

public class EXIReader extends BufferedReader {
	 
	private boolean exi = false;
	private EXIBaseProcessor ep;
	private int leido = 0;
	
	private BufferedInputStream is;
	private byte[] ba;
	
	private byte[] anterior;	// bytes recibidos anteriormente (mensaje EXI incompleto)
	
	public EXIReader(InputStream in) throws UnsupportedEncodingException {
    	super(new InputStreamReader(in, "UTF-8"));
    	this.is = new BufferedInputStream(in);
    }
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
    	
    	synchronized (lock) {
    		if(!exi){
    			leido = super.read(cbuf, off, len);
System.err.println("RECIBIDO (" + leido + "): " + new String(cbuf, off, len));			
    			return leido;
    		}
    		ba = new byte[len];
    		leido = is.read(ba, 0, len);
    		if(leido == -1)	return leido;
System.err.println("Recibido(" + leido + "): " + EXIUtils.bytesToHex(ba));
	    	if(exi && (EXIProcessor.isEXI(ba[0]) || EXIProcessor.hasEXICookie(ba) || anterior != null)){
System.err.println("EXI(" + (leido + (anterior != null ? anterior.length : 0)) + "): " + EXIUtils.bytesToHex(ba));
    			if(anterior != null){	// agregar lo guardado anteriormente a lo leido ahora
	    			System.arraycopy(ba, 0, ba, anterior.length, ba.length - anterior.length);
	    			System.arraycopy(anterior, 0, ba, 0, anterior.length);
	    		}
		    	try {
		    		//TODO: sacar substring con cte!
			    	char[] cbuf2 = ep.decodeByteArray(ba).substring(38).toCharArray();
			    	leido = cbuf2.length;
			    	System.arraycopy(cbuf2,0, cbuf, off, leido);
			    	String xml = new String(cbuf2);
System.err.println("decoded XML(" + (leido) + "): " + xml);
					anterior = null;
					ba = null;
					return leido;
		    	} catch (EXIException | TransformerException e) {
		    		// EXI incompleto, los bytes leidos se guardan en anterior
		    		anterior = new byte[leido];
    				System.arraycopy(ba, 0, anterior, 0, leido);
System.err.println("bytes guardados: " + EXIUtils.bytesToHex(anterior));
					return leido;
		    	}
			}
	    	System.arraycopy(new String(ba).toCharArray(), 0, cbuf, off, leido);
	    	
System.err.println("XML(" + leido + "): " + new String(cbuf));
			return leido;
	    }
    }
    
    
    boolean isEXI() {
		return exi;
	}


	void setEXI(boolean usarEXI) {
		synchronized (lock) {
			this.exi = usarEXI;
		}
	}
	
	void setExiProcessor(EXIBaseProcessor ep){
		this.ep = ep;
	}

}

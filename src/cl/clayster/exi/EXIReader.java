package cl.clayster.exi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import com.siemens.ct.exi.exceptions.EXIException;

public class EXIReader extends BufferedReader {
	 
	private boolean exi = false;
	private EXIBaseProcessor ep;
	private int leido = 0;
	
	private BufferedInputStream is;
	private byte[] ba;
	
	private byte[] anterior;	// bytes recibidos anteriormente (mensaje EXI incompleto)
	
	private List<EXIEventListener> readListeners = new ArrayList<EXIEventListener>(0); 
	
	public EXIReader(InputStream in) throws UnsupportedEncodingException {
    	super(new InputStreamReader(in, "UTF-8"));
    	this.is = new BufferedInputStream(in);
    }
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
    	
    	synchronized (lock) {
    		if(!exi){
    			leido = super.read(cbuf, off, len);			
    			return leido;
    		}
    		byte[] dest = new byte[len];
    		leido = is.read(dest, 0, len);
    		ba = new byte[leido];
    		System.arraycopy(dest, 0, ba, 0, leido);
    		if(leido == -1)	return leido;
	    	if(exi && (EXIProcessor.isEXI(ba[0]) || EXIProcessor.hasEXICookie(ba) || anterior != null)){
    			if(anterior != null){	// agregar lo guardado anteriormente a lo leido ahora
	    			System.arraycopy(ba, 0, ba, anterior.length, ba.length - anterior.length);
	    			System.arraycopy(anterior, 0, ba, 0, anterior.length);
	    		}
		    	try {
		    		String xml1 = ep.decodeByteArray(ba);
			    	char[] cbuf2 = xml1.toCharArray();
			    	leido = cbuf2.length;
			    	System.arraycopy(cbuf2,0, cbuf, off, leido);
			    	String xml = new String(cbuf2);
			    	
					if(!readListeners.isEmpty()){
						for(EXIEventListener eel : readListeners){
							eel.packetDecoded(xml, ba);
						}
					}
					
					anterior = null;
					ba = null;
					return leido;
		    	} catch (EXIException | TransformerException e) {
		    		// EXI incompleto, los bytes leidos se guardan en anterior
		    		anterior = new byte[leido];
    				System.arraycopy(ba, 0, anterior, 0, leido);
					return leido;
		    	}
			}
	    	System.arraycopy(new String(ba).toCharArray(), 0, cbuf, off, leido);
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
	
	void addReadListener(EXIEventListener listener){
		readListeners.add(listener);
	}
	
	boolean removeReadListener(EXIEventListener listener){
		return readListeners.remove(listener);
	}

}

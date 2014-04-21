package cl.clayster.exi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import com.siemens.ct.exi.exceptions.EXIException;

public class EXIAltReader extends EXIReader {
	 
	private boolean exi = false;
	private EXIBaseProcessor ep;
	private int leido = 0;
	
	private BufferedInputStream is;
	private byte[] ba;
	
	private byte[] anterior;	// bytes recibidos anteriormente (mensaje EXI incompleto)
	
	private List<EXIEventListener> readListeners = new ArrayList<EXIEventListener>(0); 
	
	public EXIAltReader(InputStream in) throws UnsupportedEncodingException {
    	super(in);
    	this.is = new BufferedInputStream(in);
    	this.exi = true;
    }
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
    	
    	synchronized (lock) {
    		byte[] dest = new byte[len];
    		leido = is.read(dest, 0, len);
    		if(leido == -1)	return leido;
    		
    		ba = new byte[leido];
    		System.arraycopy(dest, 0, ba, 0, leido);
System.err.println("exi: " + EXIUtils.bytesToHex(ba));
	    	if(exi && (EXIProcessor.isEXI(ba[0]) || EXIProcessor.hasEXICookie(ba) || anterior != null)){
    			if(anterior != null){	// agregar lo guardado anteriormente a lo leido ahora
	    			System.arraycopy(ba, 0, ba, anterior.length, ba.length - anterior.length);
	    			System.arraycopy(anterior, 0, ba, 0, anterior.length);
	    		}
		    	try {
		    		String xml = ep.decodeByteArray(ba)
		    				.replaceAll("\r", "")
		    				.replaceAll("\n", "");
		    		if(xml.startsWith("<exi:open")){
		    			String from = EXIUtils.getAttributeValue(xml, "from");
		    			String id = EXIUtils.getAttributeValue(xml, "id");
		    			xml = "<stream:stream xmlns:stream=\"http://etherx.jabber.org/streams\" xmlns=\"jabber:client\" from=\"" + from + "\""
		    					+ " id=\"" + id + "\" xml:lang=\"en\" version=\"1.0\">";
		    		}
		    		else if(xml.startsWith("<streamEnd")){
		    			xml = "</stream:stream>";
		    		}
			    	char[] cbuf2 = xml.toCharArray();
			    	leido = cbuf2.length;
			    	System.arraycopy(cbuf2, 0, cbuf, off, leido);

System.err.println("xml: " + xml);

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
		synchronized(lock){
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

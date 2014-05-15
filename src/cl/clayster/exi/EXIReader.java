package cl.clayster.exi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box.Filler;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.siemens.ct.exi.exceptions.EXIException;

public class EXIReader extends BufferedReader {
	 
	private boolean exi = false;
	private EXIBaseProcessor ep;
	private int leido = 0;
	
	private BufferedInputStream bis;
	
	private List<EXIEventListener> readListeners = new ArrayList<EXIEventListener>(0); 
	
	public EXIReader(InputStream in) throws UnsupportedEncodingException {
    	super(new InputStreamReader(in, "UTF-8"));
    	this.bis = new BufferedInputStream(in);
    }
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
    	
    	synchronized (lock) {
    		if(!exi){
				leido = super.read(cbuf, off, len);
System.err.println("received: " + new String(cbuf, off, leido));
    			return leido;
    		}
    		
    		while(!ready()){
    			
    		}
			while(true){
				try{
					bis.mark(len);
					String xml = ep.decode(bis);
System.err.println("decoded: " + xml);
					char[] cbuf2 = xml.toCharArray();
					leido = cbuf2.length;
					System.arraycopy(cbuf2, 0, cbuf, off, leido);
					return leido;
				} catch (TransformerException e){
					bis.reset();
				} catch (EXIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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

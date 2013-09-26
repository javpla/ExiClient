package cl.clayster.exi;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.siemens.ct.exi.exceptions.EXIException;

public class EXIWriter extends BufferedWriter {
	
	private boolean exi = false;

	public EXIWriter(Writer wrappedWriter) {
		super(wrappedWriter);
	}

	@Override
    public void write(String xml , int off, int len) throws IOException {
    	if(!exi || xml.contains("stream:stream")){
    		super.write(xml, off, len);
			return;
    	}
    	
    	String exi = null;
    	try {
			exi = EXIProcessor.encode(xml);
    	}catch (SAXException | EXIException | TransformerException e){
    		e.printStackTrace();
			super.write(xml, off, len);
			return;
    	}
    	System.out.println("msj a codificar: " + xml);
        super.write(exi, off, exi.length());
        
        // send messages separately (do not wait for the queue to send them)
        super.flush();   	        
    }
	
	@Override
	public void write(String xml) throws IOException {
		write(xml, 0, xml.length());
	}

	boolean isEXI() {
		return exi;
	}

	void setEXI(boolean usarEXI) {
		this.exi = usarEXI;
	}

}

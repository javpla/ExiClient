package cl.clayster.exi;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.siemens.ct.exi.exceptions.EXIException;

public class EXIWriter extends BufferedWriter {

	private boolean exi = false;
	private EXIProcessor exiProcessor;
	private BufferedOutputStream os;

	public EXIWriter(OutputStream out, EXIProcessor exiProcessor) throws UnsupportedEncodingException {
		super(new OutputStreamWriter(out, "UTF-8"));
		os = new BufferedOutputStream(out);
		this.exiProcessor = exiProcessor;
	}

	@Override
    public void write(String xml , int off, int len) throws IOException {
    	if(!exi || xml.contains("stream:stream")){
    		super.write(xml, off, len);
			return;
    	}
    	
    	byte[] exi = null;
    	try {
			exi = exiProcessor.encodeByteArray(xml);
    	}catch (SAXException | EXIException | TransformerException e){
    		e.printStackTrace();
			super.write(xml, off, len);
			return;
    	}
System.out.println("XML a codificar(" + xml.length() + "): " + xml);
System.out.println("EXI a enviar(" + exi.length + "): " + new String(exi));
        
    	os.write(exi, off, exi.length);
    	os.flush();
	}
	
		boolean isEXI() {
		return exi;
	}

	void setEXI(boolean usarEXI) {
		this.exi = usarEXI;
	}

	
}

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
	private EXIBaseProcessor exiProcessor;
	private BufferedOutputStream os;

	public EXIWriter(OutputStream out) throws UnsupportedEncodingException {
		super(new OutputStreamWriter(out, "UTF-8"));
		os = new BufferedOutputStream(out);
	}
	
	@Override
    public void write(String xml , int off, int len) throws IOException {
System.out.println("XML(" + xml.length() + "): " + xml);
    	if(!exi){
    		super.write(xml, off, len);
			return;
    	}
    	try {
        	byte[] exi = exiProcessor.encodeToByteArray(xml);
System.out.println("Enviando EXI(" + xml.length() + " => " + exi.length + "): " + EXIUtils.bytesToHex(exi));
        	os.write(exi, off, exi.length);
        	os.flush();
    	}catch (SAXException | EXIException | TransformerException e){
    		e.printStackTrace();
			super.write(xml, off, len);
			return;
    	}
	}
	
	public boolean isEXI() {
		return exi;
	}

	public void setEXI(boolean usarEXI) {
		this.exi = usarEXI;
	}
	
	void setExiProcessor(EXIBaseProcessor exiProcessor){
		this.exiProcessor = exiProcessor;
	}
}

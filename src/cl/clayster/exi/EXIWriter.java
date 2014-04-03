package cl.clayster.exi;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.siemens.ct.exi.exceptions.EXIException;

public class EXIWriter extends BufferedWriter {

	private boolean exi = false;
	private EXIBaseProcessor ep;
	private BufferedOutputStream os;
	
	private List<EXIEventListener> writeListeners = new ArrayList<EXIEventListener>(0);

	public EXIWriter(OutputStream out) throws UnsupportedEncodingException {
		super(new OutputStreamWriter(out, "UTF-8"));
		os = new BufferedOutputStream(out);
	}
	
	public EXIWriter(OutputStream out, boolean exi) throws UnsupportedEncodingException {
		super(new OutputStreamWriter(out, "UTF-8"));
		os = new BufferedOutputStream(out);
		this.exi = exi;
	}
	
	@Override
    public void write(String xml , int off, int len) throws IOException {
    	if(!exi){
    		super.write(xml, off, len);
			return;
    	}
    	try {
        	byte[] exi = ep.encodeToByteArray(xml);
        	
System.out.println("xml: " + xml);
System.out.println("exi: " + EXIUtils.bytesToHex(exi));
			if(!writeListeners.isEmpty()){
				for(EXIEventListener eel : writeListeners){
					eel.packetEncoded(xml, exi);
				}
			}
			
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
		this.ep = exiProcessor;
	}
	
	void addWriteListener(EXIEventListener listener){
		writeListeners.add(listener);
	}
	
	boolean removeWriteListener(EXIEventListener listener){
		return writeListeners.remove(listener);
	}

	public void writeWithCookie(String xml) throws IOException {
		try {
        	byte[] exi = ep.encodeToByteArray(xml);
        	byte[] c = "$EXI".getBytes();
        	byte[] aux = new byte[exi.length + c.length]; 
			System.arraycopy(exi, 0, aux, c.length, exi.length);
			System.arraycopy(c, 0, aux, 0, c.length);
			exi = aux;

System.out.println("xml: " + xml);
System.out.println("exi: " + EXIUtils.bytesToHex(exi));
			if(!writeListeners.isEmpty()){
				for(EXIEventListener eel : writeListeners){
					eel.packetEncoded(xml, exi);
				}
			}
        	os.write(exi, 0, exi.length);
        	os.flush();
    	}catch (SAXException | EXIException | TransformerException e){
    		e.printStackTrace();
			super.write(xml, 0, xml.length());
			return;
    	}
	}
}

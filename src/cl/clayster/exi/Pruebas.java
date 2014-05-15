package cl.clayster.exi;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.dom4j.DocumentException;
import org.jivesoftware.smack.XMPPException;
import org.xml.sax.SAXException;

import com.siemens.ct.exi.exceptions.EXIException;


public class Pruebas{ 
	
	public static void main(String[] args) throws XMPPException, IOException, DocumentException, NoSuchAlgorithmException, EXIException, SAXException, TransformerException, ParserConfigurationException{
		
		EXISetupConfiguration config = new EXISetupConfiguration();
		config.setSessionWideBuffers(true);
		int t = 10000;
		EXIProcessor p = new EXIProcessor(config);
		for(int i = 0 ; i < t ; i++){
			String xml0 = "<message id='Sk5JM-19' to='exi2@exi.clayster.cl/Spark 2.6.3' from='exi1@exi.clayster.cl/Smack'><body>"
					+ "estas palabras son el mensaje que contiene muchas palabras con palabras y palabras y palabras lalalala palabras</body></message>";
			byte[] exi = p.encodeToByteArray(xml0);
			System.out.println("encoded (" + exi.length + "): " + EXIUtils.bytesToHex(exi));
			
			String xml1 = p.decodeByteArray(exi);
			System.out.println("decoded (" + xml1.length() + "): " + xml1);
		}
		boolean a = true;
		if(a)return;
		
		/*
		byte[] ba = ("1234567890qwertyuiopasdfghjklzxcvbnm"
				   + "098765432poiuytrewqñlkjhgfdsamnbvcxz").getBytes();
		byte[] r = new byte[ba.length];
		ByteArrayInputStream is = new ByteArrayInputStream(ba);
		
		
		is.read(r, 0, ba.length/2);
System.out.println("first half: " + new String(r));
		r = new byte[ba.length];
		if(is.available() > 0){
			is.read(r, 0, ba.length/2);
		}
		else{
			System.err.println("end of the stream");
		}
System.out.println("second half: " + new String(r));
		if(is.available() > 0){
			is.read(r, 0, ba.length/2);
		}
		else{
			System.err.println("end of the stream");
		}
		*/
		
	}
	
}




package cl.clayster.exi;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.dom4j.DocumentException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;

import cl.clayster.packet.QueryElement;

import com.siemens.ct.exi.CodingMode;
import com.siemens.ct.exi.exceptions.UnsupportedOption;


public class Pruebas{ 
	
	public static void main(String[] args) throws XMPPException, IOException, UnsupportedOption, DocumentException, NoSuchAlgorithmException{
		EXISetupConfiguration exiPre = new EXISetupConfiguration();
		exiPre.setCodingMode(CodingMode.PRE_COMPRESSION);
		exiPre.setStrict(true);
		
		
	}
	
}




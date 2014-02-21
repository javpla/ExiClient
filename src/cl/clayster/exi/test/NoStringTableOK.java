package cl.clayster.exi.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIXMPPConnection;

import com.siemens.ct.exi.CodingMode;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NoStringTableOK {
	static EXIXMPPConnection connection;
	
	@Before
	public void connectar() {		
		ConnectionConfiguration config = new ConnectionConfiguration(TestUtils.SERVER);
		connection = new EXIXMPPConnection(config);
		try {
			connection.connect();
			connection.login(TestUtils.USER, TestUtils.PASSWORD);
		} catch (XMPPException e) {
			fail(e.getMessage());
		}
	}
	
	@After
	public void desconectar() {		
		if(connection.isConnected())	connection.disconnect();
	}
	
	@Test
	public void aNoStringTable(){
		EXISetupConfiguration config = new EXISetupConfiguration();
		config.setValueMaxLength(0);
		
		connection.proposeEXICompression(config);
		// wait for the negotiation to take place before continuing with the rest of the tests
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("default configuration but NO string table");
		connection.sendPacket(msg);
		
		assertTrue(connection.isConnected());
	}
	
	
	public void bNoStringTableCompressionAndBlocksize(){
		EXISetupConfiguration config = new EXISetupConfiguration();
		config.setAlignment(CodingMode.COMPRESSION);
		config.setBlockSize(2048);
		config.setValueMaxLength(0);
		
		connection.proposeEXICompression(config);
		// wait for the negotiation to take place before continuing with the rest of the tests
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("NO string table y custom setup (compression y blocksize=2048)");
		connection.sendPacket(msg);
		
		assertTrue(connection.isConnected());
	}
	
	@AfterClass
	public static void disconnect() {		
		if(connection.isConnected())	connection.disconnect();
	}

}

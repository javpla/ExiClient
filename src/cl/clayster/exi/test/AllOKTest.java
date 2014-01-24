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
public class AllOKTest {
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
	public void aQuickSetupSin(){
		connection.setConfigId(null);
		boolean quickSetup = connection.proposeEXICompressionQuickSetup();
		
		// wait for the negotiation to take place before continuing with the rest of the tests
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("quick Setup Sin");
		connection.sendPacket(msg);
		
		assertTrue(!quickSetup && connection.isConnected());
	}
	
	@Test
	public void bDefaultSetup(){
		connection.proposeEXICompression();
		// wait for the negotiation to take place before continuing with the rest of the tests
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("default setup");
		connection.sendPacket(msg);
		
		assertTrue(connection.isConnected());
	}
	
	@Test
	public void cQuickSetupCon(){
		boolean quickSetup = connection.proposeEXICompressionQuickSetup();
		
		// wait for the negotiation to take place before continuing with the rest of the tests
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("quick setup Con");
		connection.sendPacket(msg);
		
		assertTrue(quickSetup && connection.isConnected());
	}
	
	@Test
	public void dCustomSetup(){
		EXISetupConfiguration config = new EXISetupConfiguration();
		config.setAlignment(CodingMode.COMPRESSION);
		config.setBlockSize(2048);
		
		connection.proposeEXICompression(config);
		// wait for the negotiation to take place before continuing with the rest of the tests
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("custom setup (compression y blocksize=2048)");
		connection.sendPacket(msg);
		
		assertTrue(connection.isConnected());
	}
	
	@AfterClass
	public static void disconnect() {		
		if(connection.isConnected())	connection.disconnect();
	}

}

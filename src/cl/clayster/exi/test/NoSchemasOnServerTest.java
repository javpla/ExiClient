package cl.clayster.exi.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIXMPPConnection;

import com.siemens.ct.exi.CodingMode;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NoSchemasOnServerTest {
	static EXIXMPPConnection connection;
	
	@Before
	public void eliminarSchemas(){
		File folder = new File(TestUtils.OPENFIRE_BASE + TestUtils.RES_FOLDER);
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles != null){
            File file;
			for (int i = 0; i < listOfFiles.length; i++) {
	        	file = listOfFiles[i];
	        	if (!file.getName().endsWith(".dtd")) {
	        		file.delete();
	        	}
			}
        }
	}
	
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
		TestUtils.deleteFolder("C:/Users/Javier/workspace/Personales/openfire/target/openfire/plugins/exi/res/exiSchemas");
	}
	
	@Test
	public void aQuickSetupSin(){
		connection.setConfigId(null);
		boolean quickSetup = connection.proposeEXICompressionQuickSetup();
		
		// wait for the negotiation to take place before continuing with the rest of the test (sending a message and disconnecting)
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("quick Setup sin configId (no EXI)");
		connection.sendPacket(msg);
		
		assertTrue(!quickSetup);
		assertTrue(connection.isConnected());
		assertTrue(!connection.isUsingEXI());
	}
	
	@Test
	public void bDefaultSetup(){
		boolean propose = connection.proposeEXICompression();
		// wait for the negotiation to take place before continuing with the rest of the test (sending a message and disconnecting)
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("default setup");
		connection.sendPacket(msg);
		
		assertTrue(propose);
		assertTrue(connection.isConnected());
		assertTrue(connection.isUsingEXI());
	}
	
	@Test
	public void cQuickSetupCon(){
		boolean quickSetup = connection.proposeEXICompressionQuickSetup();
		
		// wait for the negotiation to take place before continuing with the rest of the test (sending a message and disconnecting)
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("quick setup con configId (si se usa EXI)");
		connection.sendPacket(msg);
		
		assertTrue(quickSetup);
		assertTrue(connection.isConnected());
		assertTrue(connection.isUsingEXI());
	}
	
	@Test
	public void dCustomSetup(){
		EXISetupConfiguration config = new EXISetupConfiguration();
		config.setAlignment(CodingMode.COMPRESSION);
		config.setBlockSize(2048);
		
		boolean propose = connection.proposeEXICompression(config);
		// wait for the negotiation to take place before continuing with the rest of the test (sending a message and disconnecting)
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("custom setup (compression y blocksize=2048)");
		connection.sendPacket(msg);
		
		assertTrue(propose);
		assertTrue(connection.isConnected());
		assertTrue(connection.isUsingEXI());
	}
	/*
	@AfterClass
	public static void disconnect() {		
		if(connection.isConnected())	connection.disconnect();
		// eliminar archivos
		File folder = new File(TestUtils.OPENFIRE_BASE + TestUtils.RES_FOLDER);
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles != null){
            File file;
			for (int i = 0; i < listOfFiles.length; i++) {
	        	file = listOfFiles[i];
	        	if (!file.getName().endsWith(".dtd")) {
	        		file.delete();
	        	}
			}
        }
	}
/**/
}

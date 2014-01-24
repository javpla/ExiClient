package cl.clayster.exi.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import cl.clayster.exi.EXIXMPPConnection;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UploadSchemasTest {
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
	public void aUploadBinary(){
		connection.setUploadSchemaOpt(EXIXMPPConnection.UPLOAD_BINARY);
		boolean propose = connection.proposeEXICompression();
		// wait for the negotiation to take place before continuing with the rest of the test (sending a message and disconnecting)
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("Uploaded binary files");
		connection.sendPacket(msg);
		
		assertTrue(propose);
		assertTrue(connection.isConnected());
		assertTrue(connection.isUsingEXI());
	}
	
	@Test
	public void bUploadExiBody(){
		connection.setUploadSchemaOpt(EXIXMPPConnection.UPLOAD_EXI_BODY);
		boolean propose = connection.proposeEXICompression();
		// wait for the negotiation to take place before continuing with the rest of the test (sending a message and disconnecting)
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("Uploaded EXI Body compressed files");
		connection.sendPacket(msg);
		
		assertTrue(propose);
		assertTrue(connection.isConnected());
		assertTrue(connection.isUsingEXI());
	}
	
	@Test
	public void cUploadExiDocument(){
		connection.setUploadSchemaOpt(EXIXMPPConnection.UPLOAD_EXI_DOCUMENT);
		boolean propose = connection.proposeEXICompression();
		// wait for the negotiation to take place before continuing with the rest of the test (sending a message and disconnecting)
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("Uploaded EXI compressed files");
		connection.sendPacket(msg);
		
		assertTrue(propose);
		assertTrue(connection.isConnected());
		assertTrue(connection.isUsingEXI());
	}
	
	@Test
	public void dUploadURL(){
		connection.setUploadSchemaOpt(EXIXMPPConnection.UPLOAD_URL);
		boolean propose = connection.proposeEXICompression();
		// wait for the negotiation to take place before continuing with the rest of the test (sending a message and disconnecting)
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(TestUtils.CONTACT);
		msg.setBody("Downloaded schemas from server (URL was sent by the client)");
		connection.sendPacket(msg);
		
		assertTrue(propose);
		assertTrue(connection.isConnected());
		assertTrue(connection.isUsingEXI());
	}
	
	
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

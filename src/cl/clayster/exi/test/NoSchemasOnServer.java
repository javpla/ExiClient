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

import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIXMPPConnection;

import com.siemens.ct.exi.CodingMode;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NoSchemasOnServer {
	
	static final String servidor = "exi.clayster.cl";
	static final String contacto = "javier@exi.clayster.cl";	// usuario al cual se le envían mensajes
	static final String usuario = "exiuser";
	static final String password = "exiuser";
	static final String openfireBase = "C:/Users/Javier/workspace/Personales/openfire/target/openfire";
	static final String resFolder = "/plugins/exi/res";
	static EXIXMPPConnection connection;
	
	
	@Before
	public void eliminarSchemas(){
		File folder = new File(openfireBase + resFolder);
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
		ConnectionConfiguration config = new ConnectionConfiguration(servidor);
		connection = new EXIXMPPConnection(config);
		try {
			connection.connect();
			connection.login(usuario, password);
		} catch (XMPPException e) {
			fail(e.getMessage());
		}
	}
	
	@After
	public void desconectar() {		
		if(connection.isConnected())	connection.disconnect();
		
		File folder = new File("C:/Users/Javier/workspace/Personales/openfire/target/openfire/plugins/exi/res/exiSchemas");
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles != null)
        	for (int i = 0; i < listOfFiles.length; i++) {
        		listOfFiles[i].delete();
        	}
		folder.delete();
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
		Message msg = new Message(contacto);
		msg.setBody("quick Setup Sin");
		connection.sendPacket(msg);
		
		assertTrue(!quickSetup && connection.isConnected());
	}
	
	@Test
	public void bDefaultSetup(){
		boolean propose = connection.proposeEXICompression();
		// wait for the negotiation to take place before continuing with the rest of the tests
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(contacto);
		msg.setBody("default setup");
		connection.sendPacket(msg);
		
		assertTrue(propose && connection.isConnected());
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
		Message msg = new Message(contacto);
		msg.setBody("quick setup Con");
		connection.sendPacket(msg);
		
		assertTrue(quickSetup && connection.isConnected());
	}
	
	@Test
	public void dCustomSetup(){
		EXISetupConfiguration config = new EXISetupConfiguration();
		config.setAlignment(CodingMode.COMPRESSION);
		config.setBlockSize(2048);
		
		boolean propose = connection.proposeEXICompression(config);
		// wait for the negotiation to take place before continuing with the rest of the tests
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(contacto);
		msg.setBody("custom setup (compression y blocksize=2048)");
		connection.sendPacket(msg);
		
		assertTrue(propose && connection.isConnected());
	}
	
	@AfterClass
	public static void disconnect() {		
		if(connection.isConnected())	connection.disconnect();
		// eliminar archivos
		File folder = new File(openfireBase + resFolder);
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

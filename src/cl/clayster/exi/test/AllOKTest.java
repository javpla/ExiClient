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
	
	static final String servidor = "exi.clayster.cl";
	static final String contacto = "javier@exi.clayster.cl";	// usuario al cual se le envían mensajes
	static final String usuario = "exiuser";
	static final String password = "exiuser";
	static EXIXMPPConnection connection;
	
/*	
	@BeforeClass
	public static void copiarSchemas(){
		File folder = new File("./res/all_schemas");
        File[] listOfFiles = folder.listFiles();
        File file;
        Path source, aux;
		for (int i = 0; i < listOfFiles.length; i++) {
        	file = listOfFiles[i];
        	
        	source = file.toPath();
        	aux = new File("./res").toPath();
        	if (file.isFile() && !file.getName().endsWith("canonicalSchema.xsd")) {
        		try {
        			Files.copy(source, aux.resolve(source.getFileName()));
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
		}
	}
	
	@AfterClass
	public static void eliminarSchemas(){
		File folder = new File("./res");
        File[] listOfFiles = folder.listFiles();
        File file;
		for (int i = 0; i < listOfFiles.length; i++) {
        	file = listOfFiles[i];
        	if (file.isFile()) {
        		file.delete();
        	}
		}
	}
*/

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
		connection.proposeEXICompression();
		// wait for the negotiation to take place before continuing with the rest of the tests
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(contacto);
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
		
		connection.proposeEXICompression(config);
		// wait for the negotiation to take place before continuing with the rest of the tests
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message(contacto);
		msg.setBody("custom setup (compression y blocksize=2048)");
		connection.sendPacket(msg);
		
		assertTrue(connection.isConnected());
	}
	
	@AfterClass
	public static void disconnect() {		
		if(connection.isConnected())	connection.disconnect();
	}

}

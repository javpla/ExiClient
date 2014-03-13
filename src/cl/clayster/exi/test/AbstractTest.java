package cl.clayster.exi.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIXMPPConnection;

public abstract class AbstractTest {
	
	final static String SERVER = "exi.clayster.cl";
	final static String USERNAME1 = "exiuser"; 
	final static String USERNAME2 = "javier";	
	final static String OPENFIRE_BASE = "C:/Users/Javier/workspace/Personales/openfire/target/openfire";
	final static String CLASSES_FOLDER = "/plugins/exi/classes";
	
	protected ConnectionConfiguration config1 = new ConnectionConfiguration(SERVER);
	protected ConnectionConfiguration config2 = new ConnectionConfiguration(SERVER);
	protected EXIXMPPConnection client1, client2;
	protected String message;
	
	private String received = new String();
	
	
	public AbstractTest(boolean compression1, EXISetupConfiguration exiConfig1,
			boolean compression2, EXISetupConfiguration exiConfig2, String message){
		this.config1.setCompressionEnabled(compression1);
		this.config2.setCompressionEnabled(compression2);
		
	    this.client1 = new EXIXMPPConnection(config1, exiConfig1);
	    this.client2 = new EXIXMPPConnection(config2, exiConfig2);;
	    this.message = message;
	}
		
	private void connect() {
		try {
			client1.connect();
			client1.login(AbstractTest.USERNAME1, AbstractTest.USERNAME1);
			client2.connect();
			client2.login(AbstractTest.USERNAME2, AbstractTest.USERNAME2);
		} catch (XMPPException e) {
			fail(e.getMessage());
		}
		
		// wait for the negotiation to take place before continuing with the rest of the tests
		int count = 0;
		while(client1.isUsingCompression() ^ config1.isCompressionEnabled()){
			try {
				Thread.sleep(++count * 1000);
				if(count > 5)	fail("timeout while negotiating EXI (client1)");
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
		}
		
		count = 0;
		while(client2.isUsingCompression() ^ config2.isCompressionEnabled()){
			try {
				Thread.sleep(++count * 1000);
				if(count > 5)	fail("timeout while negotiating EXI (client2)");
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
		}
	}
	
	
	private void disconnect() {		
		if(client1.isConnected())	client1.disconnect();
		if(client2.isConnected())	client2.disconnect();
	}
	
	/**
	 * Connects to the server and send a message from one client to the other using EXI compression. 
	 * Disconnects afterwards.
	 */
	protected void testSimpleMessage(){
		connect();	// connect and wait until compression negotiation is ready
		
		PacketListener packetListener = new PacketListener() {	// stores a received message's body into String "received"
			@Override
			public void processPacket(Packet packet) {
				received = ((Message) packet).getBody();
			}
		};
		PacketFilter messageFilter = new PacketFilter() {	// only lets Message packets pass
			@Override
			public boolean accept(Packet packet) {
				return (packet instanceof Message);
			}
		};
		client2.addPacketListener(packetListener, messageFilter);	// add a packet listener to read the incoming message
		
		Message msg = new Message(USERNAME2 + '@' + SERVER);
		msg.setBody(message);
		client1.sendPacket(msg);
		
		try {
			Element sentElement = DocumentHelper.parseText(msg.toXML()).getRootElement();
		} catch (DocumentException e1) {
			fail("DocumentException: " + e1.getMessage());
		}
		
		// wait for the message to be processed on the receiving client
		int count = 0;
		while(received.equals(new String())){
			try {
				Thread.sleep(++count * 1000);
				if(count > 4)	fail("timeout while receiving the message: " + msg);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
		}
		
		if(received.equalsIgnoreCase(message)){
			assertTrue(received.equalsIgnoreCase(message));
		}
		else{
			System.out.println("received: " + received);
			fail("received: " + received);
		}
		assertTrue(client1.isConnected() && client2.isConnected());
		
		disconnect();
	}
	
	// TODO: 
	// xml en canonical form
	// comparar elementos (localnames y namespaces, no prefijos)
	// orden de att puede ser diferente
	
	// fixed xml (iq, message. Sacar de xep 0323, 0325)
	
	
	
	
	//******************************************** Util methods for testings ********************************************// 
	
	/**
	 * Deletes a file or a folder and all its content
	 * @param folderLocation
	 */
	public static void deleteFolder(String folderLocation){
		File file = new File(folderLocation);
        File[] listOfFiles = file.listFiles();
        if(listOfFiles != null)	// then it is a folder
        	for (int i = 0; i < listOfFiles.length; i++) {
        		deleteFolder(listOfFiles[i].getAbsolutePath());
        	}
        if(!file.getName().endsWith(".dtd") && !file.getName().endsWith("classes"))	file.delete();
	}
	
	/**
	 * Removes all the content of the <i>"classes"</i> directory on the server's EXI plugin 
	 * leaving it as if it was newly created (containing only two DTD files).
	 * @param folderLocation
	 */
	public void clearClassesFolder(){
		deleteFolder(OPENFIRE_BASE + CLASSES_FOLDER);
	}
}

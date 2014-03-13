package cl.clayster.exi.test;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;

import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIXMPPConnection;

public abstract class AbstractTest extends DocumentAbstractTest{
	
	final static String SERVER = "exi.clayster.cl";
	final static String USERNAME1 = "exiuser"; 
	final static String USERNAME2 = "javier";	
	final static String OPENFIRE_BASE = "C:/Users/Javier/workspace/Personales/openfire/target/openfire";
	final static String CLASSES_FOLDER = "/plugins/exi/classes";
	
	protected ConnectionConfiguration config1 = new ConnectionConfiguration(SERVER);
	protected ConnectionConfiguration config2 = new ConnectionConfiguration(SERVER);
	protected EXIXMPPConnection client1, client2;
	protected String info;
	
	private Packet received = null;
	
	private IQ iq;
	private Message message;
	
	PacketListener packetListener = new PacketListener() {	// stores a received message's body into String "received"
		@Override
		public void processPacket(Packet packet) {
			received = packet;
		}
	};
	PacketFilter messageFilter = new PacketFilter() {	// only lets Message packets pass
		@Override
		public boolean accept(Packet packet) {
			return (packet instanceof Message);
		}
	};
	
	public AbstractTest(boolean compression1, EXISetupConfiguration exiConfig1,
			boolean compression2, EXISetupConfiguration exiConfig2, String info){
		this.config1.setCompressionEnabled(compression1);
		this.config2.setCompressionEnabled(compression2);
		
	    this.client1 = new EXIXMPPConnection(config1, exiConfig1);
	    this.client2 = new EXIXMPPConnection(config2, exiConfig2);;
	    this.info = info;
	    
	    setPackets();
	}
	
	private void setPackets(){
		iq = new IQ() {
			@Override
			public String getChildElementXML() {
				return "<req xmlns='urn:xmpp:iot:sensordata' seqnr='4' all='true' when='2013-03-07T19:00:00'/>";
			}
		};
		iq.setType(IQ.Type.GET);
		//iq.setFrom(from);
		iq.setTo(USERNAME2 + '@' + SERVER);
		iq.setProperty("id", "S0004");
		
		message = new Message(USERNAME2 + '@' + SERVER);
		message.addExtension(new PacketExtension() {
			@Override
			public String toXML() {
				return "<started xmlns='urn:xmpp:iot:sensordata' seqnr='4'/>";
			}
			@Override public String getNamespace() {return null;}
			@Override public String getElementName() {return null;}
		});
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
		
		client2.addEXIEventListener(new EXIPacketLogger(false));
		client2.addPacketListener(packetListener, messageFilter);	// add a packet listener to read the incoming message
		
		client1.sendPacket(message);
		
		// wait for the message to be processed on the receiving client
		int count = 0;
		while(received == null){
			try {
				Thread.sleep(++count * 1000);
				if(count > 4)	fail("timeout while receiving the message: " + message.toXML());
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
		}
		
		assertEquivalentXML(message.toXML(), received.toXML());
		
		// test connectivity
		assertTrue("Clients are still connected", client1.isConnected() && client2.isConnected());
		if(config1.isCompressionEnabled())	assertTrue("Client1 is still using compression", client1.isUsingCompression());
		if(config2.isCompressionEnabled())	assertTrue("Client2 is still using compression", client2.isUsingCompression());
		
		disconnect();
	}
	
	/**
	 * Compares two XML stanzas to see if they are equivalent (they have the same attributes and values, but maybe in different order). 
	 * Based on dom4j's AbstractTest, ignores <i>from</i> attribute since it is added by the server 
	 * @param xml1
	 * @param xml2
	 */
	private void assertEquivalentXML(String xml1, String xml2){
		Document doc1 = null, doc2 = null;
		try {
			doc1 = DocumentHelper.parseText(xml1);
			doc2 = DocumentHelper.parseText(xml2);
			assertDocumentsEqual(doc1, doc2);
		} catch (DocumentException e1) {
			fail("DocumentException: " + e1.getMessage());
		} catch (Exception e) {
			fail("Exception: " + e.getMessage());
		}
	}
	
	// TODO: 
	// xml en canonical form
	// comparar elementos (localnames y namespaces, no prefijos)
	// orden de att puede ser diferente
	
	// fixed xml (iq, message. Sacar de xep 0323, 0325)
	
	
	
	
	/******************************************** Util methods for testings ********************************************/ 
	
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

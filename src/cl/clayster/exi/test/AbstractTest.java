package cl.clayster.exi.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.ProviderManager;
import org.junit.After;

import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIXMPPConnection;
import cl.clayster.packet.Done;
import cl.clayster.packet.Failure;
import cl.clayster.packet.Fields;
import cl.clayster.packet.Node;
import cl.clayster.packet.Numeric;
import cl.clayster.packet.Started;
import cl.clayster.packet.StringElement;
import cl.clayster.packet.Timestamp;

public abstract class AbstractTest extends DocumentAbstractTest{
	
	final static String SERVER = "exi.clayster.cl";
	final static String USERNAME1 = "exi1"; 
	final static String USERNAME2 = "exi2";	
	final static String OPENFIRE_BASE = "C:/Users/Javier/workspace/Personales/openfire/target/openfire";
	final static String CLASSES_FOLDER = "/plugins/exi/classes";
	
	protected ConnectionConfiguration config1 = new ConnectionConfiguration(SERVER);
	protected ConnectionConfiguration config2 = new ConnectionConfiguration(SERVER);
	protected EXIXMPPConnection client1, client2;
	protected String testInfo;

	private Message m = null;
	private IQ iq = null;
	private BlockingQueue<Packet> sentMessages = new ArrayBlockingQueue<Packet>(500, true);
	private BlockingQueue<Packet> receivedMessages = new ArrayBlockingQueue<Packet>(500, true);
	private BlockingQueue<Packet> sentIQs = new ArrayBlockingQueue<Packet>(500, true);
	
	PacketListener messageListener = new PacketListener() {	
		@Override
		public void processPacket(Packet packet) {
			try {
				receivedMessages.put(packet);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
		}
	};
	PacketFilter messageFilter = new PacketFilter() {	// only lets Message packets pass
		@Override
		public boolean accept(Packet packet) {
			return (packet instanceof Message);
		}
	};
	
	PacketListener iqListener = new PacketListener() {	
		@Override
		public void processPacket(Packet packet) {			
			assertEquivalentXML(sentIQs.poll().toXML(), packet.toXML());
		}
	};
	PacketFilter iqFilter = new PacketFilter() {	// only lets Message packets pass
		@Override
		public boolean accept(Packet packet) {
			return (packet instanceof Message);
		}
	};
	
	PacketFilter openFilter = new PacketFilter() {	
		@Override
		public boolean accept(Packet packet) {
			return true;
		}
	};
	
	
	
	public AbstractTest(boolean compression1, EXISetupConfiguration exiConfig1,
			boolean compression2, EXISetupConfiguration exiConfig2, String info){
		ProviderManager.getInstance().addExtensionProvider("fields", "urn:xmpp:iot:sensordata", new Fields.Provider());
		ProviderManager.getInstance().addExtensionProvider("failure", "urn:xmpp:iot:sensordata", new Failure.Provider());
		ProviderManager.getInstance().addExtensionProvider("started", "urn:xmpp:iot:sensordata", new Started.Provider());
		ProviderManager.getInstance().addExtensionProvider("done", "urn:xmpp:iot:sensordata", new Done.Provider());
		
		this.config1.setCompressionEnabled(compression1);
		this.config2.setCompressionEnabled(compression2);
		
	    this.client1 = new EXIXMPPConnection(config1, exiConfig1);
	    
	    this.client2 = new EXIXMPPConnection(config2, exiConfig2);
	    this.testInfo = info;
	    
	    connect();
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
		try {
			while(client1.isUsingCompression() ^ config1.isCompressionEnabled()){
				Thread.sleep(1000);
				if(++count > 10){
					fail("timeout while negotiating EXI (client1)");
				}
			}
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		
		count = 0;
		try {
			while(client2.isUsingCompression() ^ config2.isCompressionEnabled()){
				Thread.sleep(1000);
				if(++count > 10){
					fail("timeout while negotiating EXI (client2)");
				}
			}
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}
	
	@After
	public void disconnect() {		
		if(client1.isConnected())	client1.disconnect();
		if(client2.isConnected())	client2.disconnect();
	}
	
	/**
	 * Compares two XML stanzas to see if they are equivalent (they have the same attributes and values, but maybe in different order). 
	 * Based on dom4j's AbstractTest, ignores <i>from</i> attribute since it is added by the server 
	 * @param xml1
	 * @param xml2
	 */
	public void assertEquivalentXML(String xml1, String xml2){
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
	
	/**
	 * Connects to the server and send a message from one client to the other using EXI compression. 
	 * Disconnects afterwards.
	 */
	protected void testMessages(){
		client2.addPacketListener(messageListener, messageFilter);
		
		//client2.addEXIEventListener(new EXIPacketLogger());
		
		for(PacketExtension pe : TestExtensions.msgExt){
			m = new Message(client2.getUser());
			m.setFrom(client1.getUser());
			m.addExtension(pe);
			client1.sendPacket(m);
			try {
				sentMessages.put(m);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
		}
		
		// wait for all messages to be received
		int count = 0;
		while(sentMessages.size() != receivedMessages.size()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
			if(++count > 5){
				fail("timeout");
			}
		}
				
		while(!sentMessages.isEmpty()){
			assertEquivalentXML(sentMessages.poll().toXML(), receivedMessages.poll().toXML());
		}
		
		// test connectivity
		assertTrue("Clients are still connected", client1.isConnected() && client2.isConnected());
		if(config1.isCompressionEnabled())	assertTrue("Client1 is still using compression", client1.isUsingCompression());
		if(config2.isCompressionEnabled())	assertTrue("Client2 is still using compression", client2.isUsingCompression());
	}
	
	protected void testSimpleMessage(int i){
		client2.addPacketListener(messageListener, messageFilter);
		//client2.addEXIEventListener(new EXIPacketLogger());
		
		Message m = new Message(client2.getUser());
		m.setFrom(client1.getUser());
		m.addExtension(TestExtensions.msgExt[i]);
		try {
			sentMessages.put(m);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		client1.sendPacket(m);
		
		// wait for the message to be received
		int count = 0;
		while(sentMessages.size() != receivedMessages.size()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
			if(++count > 5){
				fail("timeout");
			}
		}
		
		while(!sentMessages.isEmpty()){
			assertEquivalentXML(sentMessages.poll().toXML(), receivedMessages.poll().toXML());
		}
	}
	
	protected void testSimpleExtendedMessage(){
		
		client2.addPacketListener(messageListener, messageFilter);
		
		Message m = new Message(client2.getUser());
		m.setFrom(client1.getUser());
		
		List<Numeric> numerics = new ArrayList<Numeric>();
		numerics.add(new Numeric("Temperature", null, null, null, "true", "true", "23.4", "�C"));
		numerics.add(new Numeric("Runtime", null, null, "true", null, "true", "12345", "h"));
		
		Timestamp t = new Timestamp("2013-03-07T19:00:00", numerics, null);
		
		StringElement se = new StringElement("Device ID", "true", "true", "Device01");
		t.addString(se);
		
		Node n = new Node("Device01");
		n.addTimestamp(t);
		
		Fields f = new Fields("4", null);
		f.addNode(n);
		m.addExtension(f);
		client1.sendPacket(m);
		try {
			sentMessages.put(m);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}
	
	
	protected void testSimpleIQ(final int i){
		
		client2.addPacketListener(iqListener, iqFilter);
		client2.addEXIEventListener(new EXIPacketLogger());
		
		iq = new IQ() {
			@Override 
			public String getChildElementXML() {
				return TestExtensions.iqExt[i].toXML();
				}
		};
		String elementName = TestExtensions.iqExt[i].getElementName();
		if(elementName.equals("query") || elementName.equals("req") || elementName.equals("cancel")){
			iq.setType(Type.GET);
		}
		else if(elementName.equals("accepted") || elementName.equals("cancelled")){
			iq.setType(Type.RESULT);
		}
		else if(elementName.equals("rejected")){
			iq.setType(Type.ERROR);
		}
		iq.setTo(client2.getUser());
		iq.setFrom(client1.getUser());
		// iq.setProperty("id", "S0001");	TODO: is set by the server?
		client1.sendPacket(iq);
		try {
			sentIQs.put(iq);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}
	
	protected void testIQs(){
		client2.addPacketListener(iqListener, iqFilter);
		client2.addEXIEventListener(new EXIPacketLogger());
		
		for(final PacketExtension iqExt : TestExtensions.iqExt){
			iq = new IQ() {
				@Override 
				public String getChildElementXML() {
					return iqExt.toXML();
					}
			};
			String elementName = iqExt.getElementName();
			if(elementName.equals("query") || elementName.equals("req") || elementName.equals("cancel")){
				iq.setType(Type.GET);
			}
			else if(elementName.equals("accepted") || elementName.equals("cancelled")){
				iq.setType(Type.RESULT);
			}
			else if(elementName.equals("rejected")){
				iq.setType(Type.ERROR);
			}
			iq.setTo(client2.getUser());
			iq.setFrom(client1.getUser());
			client1.sendPacket(iq);
			try {
				sentIQs.put(iq);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
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

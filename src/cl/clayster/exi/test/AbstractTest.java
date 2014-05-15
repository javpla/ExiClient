package cl.clayster.exi.test;

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
import org.junit.Test;

import cl.clayster.exi.EXISetupConfiguration;
import cl.clayster.exi.EXIXMPPConnection;
import cl.clayster.packet.Accepted;
import cl.clayster.packet.Cancel;
import cl.clayster.packet.Cancelled;
import cl.clayster.packet.Done;
import cl.clayster.packet.Failure;
import cl.clayster.packet.Fields;
import cl.clayster.packet.Node;
import cl.clayster.packet.Numeric;
import cl.clayster.packet.Rejected;
import cl.clayster.packet.Req;
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
	
	protected int timeOut = 30;

	private Message m = null;
	private IQ iq = null;
	
	private BlockingQueue<Packet> sent = new ArrayBlockingQueue<Packet>(500, true);
	private BlockingQueue<Packet> received = new ArrayBlockingQueue<Packet>(500, true);
	
	PacketListener packetListener = new PacketListener() {	
		@Override
		public void processPacket(Packet packet) {
			try {
				received.put(packet);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
		}
	};
	PacketFilter testFilter = new PacketFilter() {
		@Override
		public boolean accept(Packet packet) {
			
			// We want to verify only packets sent by the test (no roster or jabber IQs)
			if(packet instanceof IQ){
				if(((IQ) packet).getType().equals(IQ.Type.ERROR)){
					return true;
				}
				else if(((IQ) packet).getChildElementXML().contains("urn:xmpp:iot:sensordata")){
					return true;					
				}
			}
			
			return packet instanceof Message;
		}
	};
	
	/**
	 * Adds urn:xmpp:iot:sensordata PacketExtension providers and initializes both clients.
	 * @param exiConfig1
	 * @param exiConfig2
	 * @param info
	 */
	public AbstractTest(EXISetupConfiguration exiConfig1, EXISetupConfiguration exiConfig2, String info){
		addExtensionProviders();
	    this.client1 = new EXIXMPPConnection(config1, exiConfig1);
	    this.client2 = new EXIXMPPConnection(config2, exiConfig2);
	    this.testInfo = info;
	    beforeConnect();
	    connect();
	}
	
	abstract void beforeConnect();
	
	protected void addExtensionProviders(){
		ProviderManager.getInstance().addExtensionProvider("fields", "urn:xmpp:iot:sensordata", new Fields.Provider());
		ProviderManager.getInstance().addExtensionProvider("failure", "urn:xmpp:iot:sensordata", new Failure.Provider());
		ProviderManager.getInstance().addExtensionProvider("started", "urn:xmpp:iot:sensordata", new Started.Provider());
		ProviderManager.getInstance().addExtensionProvider("done", "urn:xmpp:iot:sensordata", new Done.Provider());
		ProviderManager.getInstance().addExtensionProvider("rejected", "urn:xmpp:iot:sensordata", new Rejected.Provider());
		
		ProviderManager.getInstance().addIQProvider("req", "urn:xmpp:iot:sensordata", new Req.Provider());
		ProviderManager.getInstance().addIQProvider("cancel", "urn:xmpp:iot:sensordata", new Cancel.Provider());
		ProviderManager.getInstance().addIQProvider("accepted", "urn:xmpp:iot:sensordata", new Accepted.Provider());
		ProviderManager.getInstance().addIQProvider("cancelled", "urn:xmpp:iot:sensordata", new Cancelled.Provider());
	}
	
	public void connect() {
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
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
			if(++count > 10){
				fail("timeout while negotiating EXI (client1)");
			}
		}
		
		count = 0;
		while(client2.isUsingCompression() ^ config2.isCompressionEnabled()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
			if(++count > 10){
				fail("timeout while negotiating EXI (client2)");
			}
		}
		
		try {
			Thread.sleep(5000);
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
			System.out.println("xml1: " + xml1);
			System.out.println("xml2: " + xml2);
			fail("DocumentException: " + e1.getMessage());
		} catch (Exception e) {
			fail("Exception: " + e.getMessage());
		}
	}
	
	/**
	 * Connects to the server and send a message from one client to the other using EXI compression. 
	 * Disconnects afterwards.
	 */
	protected void sendMessages(){
		client2.addPacketListener(packetListener, testFilter);
		
		for(PacketExtension pe : TestExtensions.msgExt){
			m = new Message(client2.getUser());
			m.setFrom(client1.getUser());
			m.addExtension(pe);
			client1.sendPacket(m);
			try {
				sent.put(m);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
		}
	}
	
	protected void testSimpleMessage(int i){
		client2.addPacketListener(packetListener, testFilter);
		
		Message m = new Message(client2.getUser());
		m.setFrom(client1.getUser());
		m.addExtension(TestExtensions.msgExt[i]);
		try {
			sent.put(m);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		client1.sendPacket(m);
	}
	
	protected void testSimpleExtendedMessage(){
		
		client2.addPacketListener(packetListener, testFilter);
		
		Message m = new Message(client2.getUser());
		m.setFrom(client1.getUser());
		
		List<Numeric> numerics = new ArrayList<Numeric>();
		Numeric runtime = new Numeric();
		runtime.setName("Runtime");
		runtime.setStatus("true");
		runtime.setAutomaticReadout("true");
		runtime.setValue("12345");
		runtime.setUnit("h");
		numerics.add(runtime);
		numerics.add(new Numeric("Temperature", "23.4", "ºC", "true", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null));
		
		Timestamp t = new Timestamp("2013-03-07T19:00:00", numerics, null);
		
		StringElement se = new StringElement("Device ID", "true", "true", "Device01");
		t.addString(se);
		
		Node n = new Node("Device01");
		n.addTimestamp(t);
		
		Fields f = new Fields("4", null);
		f.addNode(n);
		m.addExtension(f);
		try {
			sent.put(m);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		client1.sendPacket(m);
	}
	
	
	protected void testSimpleIQ(final int i){
		
		client2.addPacketListener(packetListener, testFilter);
		
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
			sent.put(iq);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}
	
	protected void sendIQs(){
		
		client2.addPacketListener(packetListener, testFilter);
		
		for(final PacketExtension iqExt : TestExtensions.iqExt){
			IQ iq = new IQ() {
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
				sent.put(iq);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
		}
	}
	
	@Test
	public void testAll(){
		/*
		sendMessages();
		sendIQs();
		*/
		testSimpleMessage(0);
		waitAndTest();
	}
	
	/**
	 * Waits to receive the same amount of packets as has been sent.
	 */
	private void waitReception(){
		int count = 0;
		while(sent.size() != received.size()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
			if(++count > timeOut){
				StringBuilder buf = new StringBuilder();
				for(Packet p : received){
					buf.append(p.toXML() + '\n');
				}
				fail("Timeout. Messages received so far:\n" + buf.toString() 
						+ "\nSENT: " + sent.size()
						+ "\nRECV: " + received.size());
			}
		}
	}
	
	public void waitAndTest(){
		waitReception();
		
		while(!(sent.isEmpty() || received.isEmpty())){
			assertEquivalentXML(sent.poll().toXML(), received.poll().toXML());
		}
		
		// test connectivity
		assertTrue("Clients are still connected", client1.isConnected() && client2.isConnected());
		if(config1.isCompressionEnabled())	assertTrue("Client1 is still using compression", client1.isUsingCompression());
		if(config2.isCompressionEnabled())	assertTrue("Client2 is still using compression", client2.isUsingCompression());
	}
	
	

	
	// TODO: 
	// xml en canonical form
	// comparar elementos (localnames y namespaces, no prefijos)
	// orden de att puede ser diferente
	
	// fixed xml (iq, message. Sacar de xep 0323, 0325)
	
}

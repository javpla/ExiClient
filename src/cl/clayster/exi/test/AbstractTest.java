package cl.clayster.exi.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import cl.clayster.exi.EXIXMPPConnection;

public abstract class AbstractTest {
	
	final String SERVER = "exi.clayster.cl";
	final String CONTACT = "javier";	// usuario al cual se le envían mensajes
	final String USER = "exiuser";
	final static String OPENFIRE_BASE = "C:/Users/Javier/workspace/Personales/openfire/target/openfire";
	final static String CLASSES_FOLDER = "/plugins/exi/classes";
	
	protected ConnectionConfiguration config;
	protected EXIXMPPConnection user, contact;
	protected String message;
	
	private String recMsg = new String();
	private PacketListener packetListener = new PacketListener() {
		@Override
		public void processPacket(Packet packet) {
			recMsg = ((Message) packet).getBody();
		}
	};
	
	/**
	 * Deletes a file or a folder with all its content
	 * @param folderLocation
	 */
	public static void deleteFolder(String folderLocation){
		File folder = new File(folderLocation);
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles != null)
        	for (int i = 0; i < listOfFiles.length; i++) {
        		deleteFolder(listOfFiles[i].getAbsolutePath());
        	}
		folder.delete();
	}
	
	/**
	 * Removes all the content of the classes directory leaving it as if it was new (containing only two DTD files).
	 * @param folderLocation
	 */
	public static void clearClassesFolder(){
		File file = new File(OPENFIRE_BASE + CLASSES_FOLDER);
        File[] listOfFiles = file.listFiles();
        if(listOfFiles != null)
        	for (int i = 0; i < listOfFiles.length; i++) {
        		deleteFolder(listOfFiles[i].getAbsolutePath());
        	}
        if(!file.getName().endsWith(".dtd") && !file.getName().endsWith("/classes"))	file.delete();
	}
	
	/**
	 * Initializes:
	 * 	<li><b>clients</b> (user and contact) with their configurations</li>
	 * 	<li><b>message</b> related to the given configurations</li>
	 */
	private void setUp(){
		prepareTest();	// do whatever the implementing class needs before the test
		config = new ConnectionConfiguration(SERVER);	
		config.setCompressionEnabled(true);	// set EXI compression to be used
		setUser();
		setContact();
		setMessage();
		
		// add a packet listener to read the incoming message
		contact.addPacketListener(packetListener, new PacketFilter() {
			@Override
			public boolean accept(Packet packet) {
				return true;
			}
		});
	}
	
	/**
	 * May contain preparation code before the test is done. For example: delete schema files on the server.
	 */
	protected abstract void prepareTest();
	
	/**
	 * <b>MUST<b> initialize the <b>user</b> which will start an EXI communication with <b>contact</b>.
	 */
	protected abstract void setUser();
	
	/**
	 * <b>MUST<b> initialize the <b>contact</b>. which will start an EXI communication with <b>user</b>. 
	 */
	protected abstract void setContact();
	
	/**
	 * Sets the message to be sent. The content should be related to the configurations used.
	 */
	protected abstract void setMessage();
	
	
	private void connect() {
		try {
			user.connect();
			user.login(this.USER, this.USER);
			contact.connect();
			contact.login(this.CONTACT, this.CONTACT);
		} catch (XMPPException e) {
			fail(e.getMessage());
		}
	}
	
	
	private void disconnect() {		
		if(user.isConnected())	user.disconnect();
		if(contact.isConnected())	contact.disconnect();
	}
	
	/**
	 * Connects to the server and send a message from one client to the other using EXI compression. 
	 * Disconnects afterwards.
	 */
	protected void testCommunication(){
		setUp();
		
		connect();
		
		// wait for the negotiation to take place before continuing with the rest of the tests
		while(!user.isUsingCompression() || !contact.isUsingCompression()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Message msg = new Message(CONTACT + '@' + SERVER);
		msg.setBody(message);
		user.sendPacket(msg);
		
		assertTrue(user.isConnected() && user.isUsingCompression());
		
		disconnect();
	}
}

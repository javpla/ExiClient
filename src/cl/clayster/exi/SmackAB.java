package cl.clayster.exi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import com.siemens.ct.exi.CodingMode;


public class SmackAB implements MessageListener{
	static final String servidor = "exi.clayster.cl";
	static final String contacto = "javier@exi.clayster.cl";	// usuario al cual se le envían mensajes
	static final String usuario = "exiuser";
	static final String password = "exiuser";
	
	public static void main(String[] args) throws XMPPException, IOException{
		// visualize XMLs sent and received 
		System.setProperty("smack.debugEnabled", "true");
		XMPPConnection.DEBUG_ENABLED = true;
		
		//create a connection to localhost on a specific port and login
		ConnectionConfiguration config = new ConnectionConfiguration(servidor);
		
		config.setCompressionEnabled(true);
		
		EXIXMPPConnection connection = new EXIXMPPConnection(config);
		connection.connect();
		connection.login(usuario, password);
		
		// Start EXI Alternative Binding
		EXISetupConfiguration exiConfig = new EXISetupConfiguration();
		exiConfig.setAlignment(CodingMode.COMPRESSION);
		exiConfig.setBlockSize(2048);
		connection.setUploadSchemaOpt(EXIXMPPConnection.UPLOAD_BINARY);
		
		connection.proposeEXICompression(); 
		
		// chatmanager to interchange messages
		ChatManager chatmanager = connection.getChatManager();
		Chat newChat = chatmanager.createChat(contacto, showMsgThread);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String msg;
		while (!(msg = br.readLine()).equals("bye")) {
			if(msg.equalsIgnoreCase("u")){
				// Create a new presence. Pass in false to indicate we're unavailable.
				Presence presence = new Presence(Presence.Type.unavailable);
				presence.setStatus("Gone fishing");
				// Send the packet (assume we have a Connection instance called "con").
				connection.sendPacket(presence);
				continue;
			}
			if(msg.equalsIgnoreCase("a")){
				// Create a new presence. Pass in false to indicate we're unavailable.
				Presence presence = new Presence(Presence.Type.available);
				// Send the packet (assume we have a Connection instance called "con").
				connection.sendPacket(presence);
				continue;
			}
			if(msg.startsWith("iq")){
				IQ iq = new IQ() {
					
					@Override
					public String getChildElementXML() {
						//return "<query xmlns=\"jabber:iq:roster\"></query>";
						return "<req xmlns='urn:xmpp:sn' seqnr='1' momentary='true'/>";
					}
				};
				iq.setTo("gogonet1@jabber.se");
				iq.setFrom(usuario + "@" + servidor);
				iq.setType(IQ.Type.GET);
				connection.sendPacket(iq);
				continue;
			}
			else
				newChat.sendMessage(msg);
        }
		connection.disconnect();
		return;
	}
	
	// repeat like a parrot to every user starting a conversation (except from contacto, he has another messagelistener)
	final static MessageListener parrot = new MessageListener() {
			public void processMessage(Chat chat, Message message) {
		        // Send back the same text the other user sent us.
		        try {
					chat.sendMessage(message.getBody());
				} catch (XMPPException e) {
					e.printStackTrace();
				}
			}
	};
	
	final static MessageListener showMsgThread = new MessageListener() {
	    public void processMessage(Chat chat, Message message) {
	    	System.out.println("Message received (" + message.getThread() + "): " + message.toXML());
	    }
	};

	@Override
	public void processMessage(Chat chat, Message message) {}
	
}

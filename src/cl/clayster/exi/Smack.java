package cl.clayster.exi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;

import cl.clayster.exi.test.TestExtensions;
import cl.clayster.packet.Req;


class Smack implements MessageListener{
	
	
	static String servidor = "localhost";
	static String usuario = "javier";
	static String password = "javier";
	static String contacto = "javier@exi.clayster.cl/Spark 2.6.3";	// usuario al cual se le envÃ­an mensajes
	static boolean exi = true;
	/*
	static String servidor = "xmpp-exi.sust.se";
	static String contacto = "exiuser1@xmpp-exi.sust.se";	// usuario al cual se le envÃ­an mensajes
	static String usuario = "exiuser";
	static String password = "resuixe";
	static boolean exi = false;
	/**/
	
	public static void main(String[] args) throws XMPPException, IOException{
		
		//create a connection to localhost on a specific port and login
		ConnectionConfiguration config = new ConnectionConfiguration(servidor);
		config.setCompressionEnabled(true);
		config.setSecurityMode(SecurityMode.disabled);
	
		EXISetupConfiguration exiConfig = new EXISetupConfiguration();	// creates an EXISetupConfiguration with default parameters
		exiConfig.setAlignment(EXISetupConfiguration.ALIGN_COMPRESSION);	// choose alignment of compressed bits (this one chooses extra compression)
		EXIXMPPConnection connection = new EXIXMPPConnection(config, exiConfig);			
		// XMPPConnection connection = new EXIXMPPConnection(config, exiConfig);	// connection can be declared like this since it is an extension of XMPPConnection 
		
		connection.setUploadSchemaOption(EXIXMPPConnection.UPLOAD_BINARY); // chooses how to upload missing schemas to the server (by default they are excluded)
		
		// the rest is just like using a normal XMPPConnection from Smack 3.3.0
		connection.connect();
		connection.login(usuario, password);

		/*
		connection.addPacketListener(new PacketListener() {
			
			@Override
			public void processPacket(Packet packet) {
				System.err.println(packet.toXML());
			}
		}, new PacketFilter() {
			
			@Override
			public boolean accept(Packet packet) {
				return true;
			}
		});
		*/
		
		// get list of contacts (Roster)
		Roster roster = connection.getRoster();
		roster.addRosterListener(new RosterListener() {	// presence updates
		    // Ignored events public void entriesAdded(Collection<String> addresses) {}
		    public void entriesDeleted(Collection<String> addresses) {}
		    public void entriesUpdated(Collection<String> addresses) {}
		    public void presenceChanged(Presence presence) {
		    	// subscription type is relevant here (needs "both" to get the request and send the information)
		        System.out.println("Presence changed: " + presence.getFrom() + " " + presence);
		    }
			@Override
			public void entriesAdded(Collection<String> arg0) {}
		});
		if(roster.getEntryCount() > 0){
			System.out.println("Roster for " + connection.getUser() + ":");
			for(RosterEntry re : roster.getEntries()){
				System.out.println(re.getName() + " - " + re.getStatus());
			}
		}
		else{
			System.out.println("Roster for " + connection.getUser() + " empty!");
		}
		
		// chatmanager to interchange messages
		ChatManager chatmanager = connection.getChatManager();
		Chat newChat = chatmanager.createChat(contacto, showMsgBody);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String msg;
		while (!(msg = br.readLine()).equals("bye")) {
			if(msg.equals("R")){
				roster = connection.getRoster();
				if(roster.getEntryCount() > 0){
					System.out.println("Roster for " + connection.getUser() + ":");
					for(RosterEntry re : roster.getEntries()){
						System.out.println(re.getName() + " - " + re.getStatus());
					}
				}
				else{
					System.out.println("Roster for " + connection.getUser() + " empty!");
				}
			}
			else if(msg.equals("U")){
				// Create a new presence. Pass in false to indicate we're unavailable.
				Presence presence = new Presence(Presence.Type.unavailable);
				presence.setStatus("Gone fishing");
				// Send the packet (assume we have a Connection instance called "con").
				connection.sendPacket(presence);
				continue;
			}
			else if(msg.equals("A")){
				// Create a new presence. Pass in false to indicate we're unavailable.
				Presence presence = new Presence(Presence.Type.available);
				// Send the packet (assume we have a Connection instance called "con").
				connection.sendPacket(presence);
				continue;
			}
			else if(msg.startsWith("iq")){
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
			else if(msg.startsWith("T323")){
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
					iq.setTo(contacto);
					iq.setFrom(connection.getUser());
					connection.sendPacket(iq);
				}
				for(PacketExtension pe : TestExtensions.msgExt){
					Message m = new Message(contacto);
					m.setFrom(connection.getUser());
					m.addExtension(pe);
					connection.sendPacket(m);
				}
			}
			else if(msg.equals("sameMsg")){
				Message m = new Message(contacto);
				m.setFrom(connection.getUser());
				m.setBody("esto es para crear el mensaje que será enviado un millón de veces para probar que los mensajes se hacen más pequeños usando SWB.");
				for(int i = 0 ; i < 1000 ; i++){
					connection.sendPacket(m);
				}
			}
			else if(msg.startsWith("Ç")){
				for(int i = 0 ; i < 20 ; i++){
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
						iq.setTo(contacto);
						iq.setFrom(connection.getUser());
						connection.sendPacket(iq);
					}
					for(PacketExtension pe : TestExtensions.msgExt){
						Message m = new Message(contacto);
						m.setFrom(connection.getUser());
						m.addExtension(pe);
						connection.sendPacket(m);
					}
				}
			}
			else if(msg.startsWith("C=")){
				String c = msg.substring("C=".length());
				contacto = c;
				newChat = chatmanager.createChat(contacto, showMsgBody);
				System.out.println("Contacto updated: " + contacto);
			}
			else if(msg.startsWith("sd-fields")){
				Req r = new Req(contacto);
				r.addNodeId("ventilation");
				r.setMomentary("true");
				r.setSeqnr("1");
				r.addFieldName("OutdoorTemp");
				r.addFieldName("ExtractTemp");
				r.addFieldName("ExhaustTemp");
				r.addFieldName("SupplyTemp");
				r.addFieldName("Speed");
				connection.sendPacket(r);
			}
			else if(msg.startsWith("sd-each")){
				for(int i = 0 ; i < 10000 ; i++){
					Req r = new Req(contacto);
					r.setMomentary("true");
					r.setSeqnr("1");
					r.addFieldName("OutdoorTemp");
					connection.sendPacket(r);
					
					Req r1 = new Req(contacto);
					r1.setMomentary("true");
					r1.setSeqnr("1");
					r1.addFieldName("ExtractTemp");
					connection.sendPacket(r1);
					
					Req r2 = new Req(contacto);
					r2.setMomentary("true");
					r2.setSeqnr("1");
					r2.addFieldName("ExhaustTemp");
					connection.sendPacket(r2);
					
					Req r3 = new Req(contacto);
					r3.setMomentary("true");
					r3.setSeqnr("1");
					r3.addFieldName("SupplyTemp");
					connection.sendPacket(r3);
					
					Req r4 = new Req(contacto);
					r4.setMomentary("true");
					r4.setSeqnr("1");
					r4.addFieldName("Speed");
					connection.sendPacket(r4);
				}
			}
			else if(msg.startsWith("sd-all")){
			
				Req r = new Req();
				r.setTo(contacto);
				r.setAll("true");
				r.setSeqnr("1");
				connection.sendPacket(r);
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
	
	final static MessageListener showMsgBody = new MessageListener() {
	    public void processMessage(Chat chat, Message message) {
	    	if(message.getBody() != null){
	    		System.out.println("Message RCVD: " + message.getBody());
	    	}
	    	else{
	    		System.out.println("Message RCVD (no body): " + message.toXML());
	    	}
	    }
	};

	@Override
	public void processMessage(Chat chat, Message message) {
		// TODO Auto-generated method stub
	}
	
}


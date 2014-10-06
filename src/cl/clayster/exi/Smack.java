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
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;

import cl.clayster.exi.test.TestExtensions;


class Smack implements MessageListener{ 
	
	/*
	static String servidor = "localhost";
	static String usuario = "exiuser";
	static String password = "exiuser";
	static String contacto = "javier@exi.clayster.cl/Spark 2.6.3";	// usuario al cual se le env√≠an mensajes
	static boolean exi = true;
	/**/
	
	static String servidor = "clayster.cl";
	static String contacto = "demo.server@clayster.cl";	// usuario al cual se le env√≠an mensajes
	static String usuario = "javier";
	static String password = "javier";
	static boolean exi = false;
	/**/
	
	public static void main(String[] args) throws XMPPException, IOException{
		if(args != null && args.length != 0){
			if(args[0] != null)	servidor = args[0];
			if(args[1] != null)	contacto = args[1];
			if(args[2] != null)	usuario = args[2];
			if(args[3] != null)	password = args[3];
		}
		
		//create a connection to localhost on a specific port and login
		ConnectionConfiguration config = new ConnectionConfiguration(servidor);
		config.setCompressionEnabled(true);
		config.setSecurityMode(SecurityMode.disabled);
	
		XMPPConnection connection = new XMPPConnection(config);
		
		EXISetupConfiguration exiConfig = new EXISetupConfiguration();
		exiConfig.setAlignment(EXISetupConfiguration.ALIGN_COMPRESSION);
		connection = new EXIXMPPConnection(config, exiConfig);
		//connection.setUploadSchemaOption(EXIXMPPConnection.UPLOAD_BINARY);
		/**/
		
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
				m.setBody("esto es para crear el mensaje que ser· enviado un millÛn de veces para probar que los mensajes se hacen m·s pequeÒos usando SWB.");
				for(int i = 0 ; i < 1000 ; i++){
					connection.sendPacket(m);
				}
			}
			else if(msg.startsWith("«")){
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
	    	System.out.println("Message RCVD: " + message.getBody());
	    }
	};

	@Override
	public void processMessage(Chat chat, Message message) {
		// TODO Auto-generated method stub
	}
	
}


/** Tareas a realizar **/

/** Smack **/
// Aprender a usar Smack, enviando y recibiendo mensajes con otro usuario (en Spark)
// Aprender a interceptar todo tipo de mensajes (iq, stream, auth, challenge, etc) entrantes y salientes -> PacketInterceptor

/** EXIficient **/
// Aprender a usar EXIficient, codificando y decodificando strings XML
//TODO: Evitar que se cree el elemento <?xml version="1.0" encoding="UTF-8"?> tras la decodificacion
//TODO: aplicar codificacion saliente y decodificacion de packetes entrantes al cliente Smack (problema: codificacion de String -> funciona para TODOS los mensajes con ISO-8859-1? Por el momento si)

/** Openfire **/
// Instalar Openfire para desarrollo (build)
//TODO: Aprender a interceptar y cambiar mensajes para poder enviar mensajes no XML (que luego ser√°n EXI).

//TODO: Implementar el protocolo XEP-0322




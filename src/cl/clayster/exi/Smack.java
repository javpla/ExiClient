package cl.clayster.exi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;


public class Smack implements MessageListener{ 
	
	static String servidor = "localhost";
	static String usuario = "exiuser";
	static String password = "exiuser";
	static String contacto = "javier@exi.clayster.cl/Spark";	// usuario al cual se le env√≠an mensajes
	static boolean exi = true;
	/*
	
	static final String servidor = "clayster.cl";
	static final String contacto = "gogonet1@jabber.se";	// usuario al cual se le env√≠an mensajes
	static final String usuario = "javier.placencio";
	static final String password = "pla123";
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
		
		EXISetupConfiguration exiConfig = new EXISetupConfiguration(false);
		exiConfig.setAlignment(EXISetupConfiguration.BYTE_PACKED);
		exiConfig.setBlockSize(2048);
		exiConfig.setStrict(false);
		exiConfig.setValueMaxLength(300);
		
		EXIXMPPConnection connection = new EXIXMPPConnection(config, exiConfig, new File("C:/Users/Javier/workspace/Personales/ExiClient/res/canonicalSchemas/cs.xsd"));
		connection.connect();
		connection.login(usuario, password);
		
		connection.addEXIEventListener(new EXIPacketLogger(""));
		
		/**
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
		*/
		
		// chatmanager to interchange messages
		ChatManager chatmanager = connection.getChatManager();
		Chat newChat = chatmanager.createChat(contacto, showMsgThread);
		//newChat.sendMessage("aeiou ·ÈÌÛ˙ ‡ËÏÚ˘ ‰ÎÔˆ¸ AEIOU ¡…Õ”⁄ ¿»Ã“Ÿ ƒÀœ÷‹");
		/*
		Message newMessage = new Message();
		newMessage.setBody("Mensaje largo y con propiedades extra. BLABLABLABLABALBALABLABDLAD ADS ABL ABDK LSABD KASB DKASD BAKLD BASKDLAB LKSAB KLAS");
		newMessage.setProperty("colorName", "red");
		newMessage.setProperty("color", new Color(0, 127, 255)); // color is Serializable and thus can be sent as a property (also primitives)
		newMessage.setProperty("colorName", "verde");
		newMessage.setProperty("color", new Color(0, 255, 0));
		newMessage.setProperty("colorName", "rojo");
		newMessage.setProperty("color", new Color(255, 0, 0));
		newMessage.setProperty("colorName", "azul");
		newMessage.setProperty("color", new Color(0, 0, 255));
		newMessage.setProperty("colorName", "blanco");
		newMessage.setProperty("color", new Color(255, 255, 255));
		newMessage.setProperty("colorName", "negro");
		newMessage.setProperty("color", new Color(0, 0, 0));
		newMessage.setProperty("colorName", "gris");
		newMessage.setProperty("color", new Color(127, 127, 127));
		newChat.sendMessage(newMessage);
		
		chatmanager.addChatListener(
			    new ChatManagerListener() {
			        @Override
			        public void chatCreated(Chat chat, boolean createdLocally)
			        {
			            if (!createdLocally){
			            	chat.addMessageListener(parrot);
			            }
			            chat.addMessageListener(showMsgThread);
			        }
			    });
		*/
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String msg;
		while (!(msg = br.readLine()).equals("bye")) {
			if(msg.equals("U")){
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
			else if(msg.equals("T")){
				Message message = new Message(contacto);
				PacketExtension pe = new PacketExtension() {
					@Override
					public String toXML() {
						return "<fields xmlns='urn:xmpp:iot:sensordata' seqnr='1' done='true'>"
								+ "<node nodeId='Device01'>"
									+ "<timestamp value='2013-03-07T16:24:30'>"
										+ "<numeric name='Temperature' momentary='true' automaticReadout='true' value='23.4' unit='∞C'/>"
									+ "</timestamp>"
								+ "</node>"
							+ "</fields>";
					}
					@Override public String getNamespace() {return null;}
					@Override public String getElementName() {return null;}
				};
				message.addExtension(pe);
				connection.sendPacket(message);
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




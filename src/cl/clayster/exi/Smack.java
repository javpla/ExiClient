package cl.clayster.exi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import javax.xml.transform.TransformerException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.xml.sax.SAXException;

import com.siemens.ct.exi.CodingMode;
import com.siemens.ct.exi.EncodingOptions;
import com.siemens.ct.exi.FidelityOptions;
import com.siemens.ct.exi.exceptions.EXIException;


public class Smack implements MessageListener{
	
	
	static final String servidor = "exi.clayster.cl";
	static final String contacto = "javier@exi.clayster.cl";	// usuario al cual se le env√≠an mensajes
	static final String usuario = "exiuser";
	static final String password = "exiuser";
	static boolean exi = true;
	/*
	
	static final String servidor = "clayster.cl";
	static final String contacto = "gogonet1@jabber.se";	// usuario al cual se le env√≠an mensajes
	static final String usuario = "javier.placencio";
	static final String password = "pla123";
	static boolean exi = false;
	/**/
	
	public static void main(String[] args) throws XMPPException, IOException{
		boolean test = false;
		if(test){
			String archivo = new String(Files.readAllBytes(new File("C:/Users/Javier/workspace/Personales/ExiClient/res/xml.xsd").toPath()));
			try {
				byte[] exiBody = EXIProcessor.encodeEXIBody(archivo);
				System.out.println("EXI Body: " + EXIUtils.bytesToHex(exiBody));
				String xml = EXIProcessor.decodeExiBodySchemaless(exiBody);
			//	System.out.println("XML: " + xml);
				
				byte[] exiDoc = EXIProcessor.encodeSchemaless(archivo, false);
				System.out.println("EXI Document: " + EXIUtils.bytesToHex(exiDoc));
				xml = EXIProcessor.decodeSchemaless(exiDoc);
				System.out.println("XML: " + xml);
				
			} catch (EXIException | SAXException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return;
		}
		if(test){
			String txt = "<?xml version=\"1.0\"?>"
			+ "<exi:streamStart xmlns:exi='http://jabber.org/protocol/compress/exi' version=\"1.0\" to=\"jabber.example.org\" xml:lang=\"en\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" >"
			+ "<exi:xmlns prefix=\"stream\" namespace=\"http://etherx.jabber.org/streams\" />"
			+ "<exi:xmlns prefix=\"\" namespace=\"jabber:client\" />"
			+ "<exi:xmlns prefix=\"xml\" namespace=\"http://www.w3.org/XML/1998/namespace\" />"
			+ "</exi:streamStart>";
			try {
				EncodingOptions eo = EncodingOptions.createDefault();
				eo.setOption(EncodingOptions.INCLUDE_COOKIE);
				eo.setOption(EncodingOptions.INCLUDE_OPTIONS);
				String exiHex = EXIUtils.bytesToHex(EXIProcessor.encodeSchemaless(txt, eo, FidelityOptions.createDefault()));
				System.out.println("Con EXI Options(" + exiHex.length() + "):");
				System.out.println(exiHex);
				
				exiHex = EXIUtils.bytesToHex(EXIProcessor.encodeSchemaless(txt, false));
				System.out.println("Sin EXI Options(" + exiHex.length() + "):");
				System.out.println(exiHex);
			} catch (EXIException | SAXException e) {
				e.printStackTrace();
			}
			return;
		}
		
		// visualize XMLs sent and received 
		System.setProperty("smack.debugEnabled", "true");
		XMPPConnection.DEBUG_ENABLED = true;
		
		//create a connection to localhost on a specific port and login
		ConnectionConfiguration config = new ConnectionConfiguration(servidor);
		//config.setCompressionEnabled(true);
		
		EXISetupConfiguration exiConfig = new EXISetupConfiguration();
		exiConfig.setAlignment(CodingMode.COMPRESSION);
		exiConfig.setBlockSize(2048);
		
		EXIXMPPConnection connection = new EXIXMPPConnection(config, exiConfig);
		connection.connect();
		connection.login(usuario, password);
		
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




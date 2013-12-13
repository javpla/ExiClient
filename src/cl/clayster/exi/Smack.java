package cl.clayster.exi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import javax.xml.transform.TransformerException;

import org.dom4j.DocumentException;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.xml.sax.SAXException;

import com.siemens.ct.exi.exceptions.EXIException;


public class Smack implements MessageListener{
	
	static String xml = "<challenge ns=\"urn:ietf:params:xml:ns:xmpp-sasl\">cmVhbG09Imphdmllci5wbGFjZW5jaW8iLG5vbmNlPSIralNIbjZ6TXpwMjR6dk42N3RtY216QmtZYXA3VXV5eWJWRElya0JwIixxb3A9ImF1dGgiLGNoYXJzZXQ9dXRmLTgsYWxnb3JpdGhtPW1kNS1zZXNz</challenge>";
	//static String xml = "<message id=\"WLDXv-25\" to=\"smackuser@javier.placencio/Smack\" from=\"javier@javier.placencio/Spark 2.6.3\" type=\"chat\"><!--comentario--><body>blablablaaaa blaa </body><thread>lfLRoe</thread><x xmlns=\"jabber:x:event\"><offline/><composing/></x></message>";
	static final String servidor = "javier.placencio";
	static final String contacto = "javier" + "@" + servidor;	// usuario al cual se le env√≠an mensajes
	static final String usuario = "smackuser";
	static final String password = "smackuser";
	
	
	
	public static void main(String[] args) throws XMPPException, IOException{
		
		boolean exi = true;
		boolean test = false;
		if(test){
			encodingDecodingTest();
			return;
		}
		
		/*
		try {
			String value = WinRegistry.readString (
				    WinRegistry.HKEY_LOCAL_MACHINE,                             //HKEY
				   "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion",           //Key
				   "ProductName");												//ValueName
			System.out.println("Windows Distribution = " + value);
		} catch (IllegalArgumentException | IllegalAccessException| InvocationTargetException e1) {
			e1.printStackTrace();
		}                  
		
		try {
			WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, "EXIClient", "configId", "dsajiod2131");
System.out.println("EXI configId = " + WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "EXIClient", "configId"));
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e1) {
			e1.printStackTrace();
		}
		*/
		
		// visualize XMLs sent and received 
		System.setProperty("smack.debugEnabled", "true");
		XMPPConnection.DEBUG_ENABLED = true;
				
		/*try {
			EXIUtils.generateBoth();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//create a connection to localhost on a specific port and login
		ConnectionConfiguration config = new ConnectionConfiguration(servidor);
		EXIXMPPConnection connection = new EXIXMPPConnection(config);
		connection.connect();
		
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
		
		connection.login(usuario, password);
		
		// Start EXI
		//connection.enableEXI(true);
		if(exi){
			try{
				connection.proposeEXICompression();
			} catch (DocumentException e) {
				System.err.println("Unable to propose EXI compression.");
				System.err.println("Reason: " + e.getMessage());
			}
		} 
		
		// chatmanager to interchange messages
		ChatManager chatmanager = connection.getChatManager();
		Chat newChat = chatmanager.createChat(contacto, showMsgThread);
		newChat.sendMessage("aeiou ·ÈÌÛ˙ ‡ËÏÚ˘ ‰ÎÔˆ¸ AEIOU ¡…Õ”⁄ ¿»Ã“Ÿ ƒÀœ÷‹");
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
			if(msg.equalsIgnoreCase("iq")){
				IQ iq = new IQ() {
					
					@Override
					public String getChildElementXML() {
						return "<query xmlns=\"jabber:iq:roster\"></query>";
					}
				};
				iq.setType(IQ.Type.GET);
				connection.sendPacket(iq);
				continue;
			}
			else
				newChat.sendMessage(msg);
        }
		connection.disconnect();
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
	
	
	public static void encodingDecodingTest() throws IOException{
		System.out.println("QUICK TEST.-");
		try {
			String testXML = "<xml>"
					+ "<auth mechanism=\"DIGEST-MD5\" xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\"></auth>"
					+ "<response xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">Y2hhcnNldD11dGYtOCx1c2VybmFtZT0ic21hY2t1c2VyIixyZWFsbT0iamF2aWVyLnBsYWNlbmNpbyIsbm9u"
					+ "Y2U9IlU5Ym03YXQ2RXQwdzZoaFM2b2VxRndsbUhwWUt1ZnNxZnYrL1d0QkIiLG5jPTAwMDAwMDAxLGNub25jZT0iaFpua25wdGVMRUtLWjBucnhYcnY3ZWZ3aWlBZ2x3Y0RkbjNCR"
					+ "WZTaCIsZGlnZXN0LXVyaT0ieG1wcC9qYXZpZXIucGxhY2VuY2lvIixtYXhidWY9NjU1MzYscmVzcG9uc2U9MzBlNDAzMGNhODA2OWRkYWM2YTNmZDg5ZTRiOWQyMjIscW9wPWF1dG"
					+ "gsYXV0aHppZD0ic21hY2t1c2VyIg==</response>"
					+ "<stream:stream to=\"javier.placencio\" xmlns=\"jabber:client\" xmlns:stream=\"http://etherx.jabber.org/streams\" version=\"1.0\">"
					+ "<iq id=\"IeRz1-0\" type=\"set\"><bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\"><resource>Smack</resource></bind></iq>"
					+ "<iq id=\"IeRz1-1\" type=\"set\"><session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/></iq><iq id=\"IeRz1-2\" type=\"get\">"
					+ "<query xmlns=\"jabber:iq:roster\"></query></iq><presence id=\"IeRz1-3\"></presence>"
					+ "<message id=\"IeRz1-4\" to=\"javier@javier.placencio\" from=\"smackuser@javier.placencio/Smack\" type=\"chat\"><body>Hola!</body>"
					+ "<thread>O5YBk0</thread></message>"
					+ "</stream:stream>"
					+ "</xml>";
			//testXML = EXIUtils.readFile("C:/Users/Javier/workspace/Personales/ExiClient/res/stanzaerror.xsd");
			//System.out.println("XML: " + testXML);
			EXIProcessor ep = new EXIProcessor(EXIUtils.canonicalSchemaLocation);
			
			System.out.println("encoding... " + testXML);
			byte[] schemaInformed = ep.encodeToByteArray(testXML);
			byte[] schemaless = EXIProcessor.encodeSchemaless(testXML);
			if(schemaInformed.equals(schemaless)){
				System.out.println("Son iguales: " + EXIUtils.bytesToHex(schemaInformed));
			}
			else{
				System.out.println("schemaInformed(" + schemaInformed.length + "): " + EXIUtils.bytesToHex(schemaInformed));
				System.out.println("schemaless(" + schemaless.length + "): " + EXIUtils.bytesToHex(schemaless));
				System.out.println("decoding...");
				/*
				String aux = ep.decode(schemaInformed);
				System.out.println("schemaInformed(" + aux.length() + "): " + aux);
				aux = EXIProcessor.decodeSchemaless(schemaless);
				System.out.println("schemaless(" + aux.length() + "): " + aux);
				/**/
			}
		} catch (EXIException | SAXException | TransformerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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




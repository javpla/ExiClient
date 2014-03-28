package cl.clayster.packet;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class Rejected implements PacketExtension {
	
	/*
	 * <rejected xmlns='urn:xmpp:iot:sensordata' seqnr='3'/>
	 */
	
	private String seqnr;
	
	public Rejected(String seqnr){
		this.seqnr = seqnr;
	}
	
	@Override
	public String getElementName() {
		return "rejected";
	}

	@Override
	public String getNamespace() {
		return "urn:xmpp:iot:sensordata";
	}

	@Override
	public String toXML() {
		StringBuilder buf = new StringBuilder();
        buf.append("<").append(getElementName())
        	.append(" xmlns=\"").append(getNamespace()).append("\"");
        if(seqnr != null)	buf.append(" seqnr=\"").append(getSeqnr()).append('\"');
        buf.append("/>");
        return buf.toString();
	}
	

	public String getSeqnr() {
		return seqnr;
	}

	public void setSeqnr(String seqnr) {
		this.seqnr = seqnr;
	}
	
	public static class Provider implements PacketExtensionProvider {
		@Override
		public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
			return new Rejected(parser.getAttributeValue("", "seqnr"));
		}
	}

}

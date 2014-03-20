package cl.clayster.packet;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class Done implements PacketExtension {
	
	/**
	 * <done xmlns='urn:xmpp:iot:sensordata' seqnr='4'/>
	 */
	
	private String seqnr;
	
	public Done(String seqnr){
		this.seqnr = seqnr;
	}
	
	@Override
	public String getElementName() {
		return "done";
	}

	@Override
	public String getNamespace() {
		return "urn:xmpp:iot:sensordata";
	}

	@Override
	public String toXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<").append(getElementName())
    		.append(" xmlns=\"").append(getNamespace());
        if(seqnr != null) buf.append("\" seqnr=\"").append(getSeqnr());
        buf.append("\"/>");
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
			String seqnr = null;
			if(parser.getEventType() == XmlPullParser.START_TAG && "done".equals(parser.getName())){
				seqnr = parser.getAttributeValue(null, "seqnr");
			}
			return new Done(seqnr);
		}
	}
	
}

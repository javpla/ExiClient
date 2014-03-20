package cl.clayster.packet;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class StringElement implements PacketExtension {
	
	/**
	 * <string name='Device ID' identification='true' automaticReadout='true' value='Device01'/>
	 */
	
	private String name, identification, automaticReadout, value;
	
	public StringElement(String name, String identification, String automaticReadout, String value){
		this.name = name;
		this.identification = identification;
		this.automaticReadout = automaticReadout;
		this.value = value;
	}
	
	@Override
	public String getElementName() {
		return "string";
	}

	@Override
	public String getNamespace() {
		return "urn:xmpp:iot:sensordata";
	}

	@Override
	public String toXML() {
		StringBuilder buf = new StringBuilder();
        buf.append("<").append(getElementName());
        if(name != null)	buf.append(" name=\"").append(getName());
        if(identification != null)	buf.append("\" identification=\"").append(getIdentification());
        if(automaticReadout != null)	buf.append("\" automaticReadout=\"").append(getAutomaticReadout());
        if(value != null)	buf.append("\" value=\"").append(getValue());
        buf.append("\"/>");
        return buf.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public String getAutomaticReadout() {
		return automaticReadout;
	}

	public void setAutomaticReadout(String automaticReadout) {
		this.automaticReadout = automaticReadout;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public static class Provider implements PacketExtensionProvider {
		@Override
		public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
			return new StringElement(parser.getAttributeValue("", "name"), parser.getAttributeValue("", "identification"),
					parser.getAttributeValue("", "automaticReadout"), parser.getAttributeValue("", "value"));
		}
	}

}

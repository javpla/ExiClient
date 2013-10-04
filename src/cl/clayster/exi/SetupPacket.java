package cl.clayster.exi;

import org.jivesoftware.smack.packet.Packet;

public class SetupPacket extends Packet {
	
	String xml;
	
	public SetupPacket(String xml){
		this.xml = xml;
	}

	@Override
	public String toXML() {
		return xml;
	}

}

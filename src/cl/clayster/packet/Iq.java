package cl.clayster.packet;

import org.jivesoftware.smack.packet.IQ;

public class Iq extends IQ{
	
	@Override
	public String getChildElementXML() {
		return null;
	}
}

package cl.clayster.packet;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

public class Iq extends IQ implements IQProvider{

	@Override
	public IQ parseIQ(XmlPullParser parser) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
		return null;
	}}

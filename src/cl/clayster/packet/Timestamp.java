package cl.clayster.packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class Timestamp implements PacketExtension {
	
	/*
	<timestamp value='2013-03-07T19:00:00'>
		<numeric name='Temperature' momentary='true' automaticReadout='true' value='23.4' unit='°C'/>
		<numeric name='Runtime' status='true' automaticReadout='true' value='12345' unit='h'/>
		<string name='Device ID' identification='true' automaticReadout='true' value='Device01'/>
	</timestamp>
	 */
	
	private String value;
	
	private List<Numeric> numerics = new ArrayList<Numeric>();
	private List<StringElement> strings = new ArrayList<StringElement>();
	
	public Timestamp(String value){
		this.value = value;
	}
	
	public Timestamp(String value, List<Numeric> numerics, List<StringElement> strings){
		this.value = value;
		if(numerics != null)	this.numerics = numerics;
		if(strings != null)	this.strings = strings;
	}

	@Override
	public String getElementName() {
		return "timestamp";
	}

	@Override
	public String getNamespace() {
		return "urn:xmpp:iot:sensordata";
	}

	@Override
	public String toXML() {
		StringBuilder buf = new StringBuilder();
        buf.append("<").append(getElementName());
        if(value != null)	buf.append(" value=\"").append(getValue());
        buf.append("\">");
        
        for(Numeric n : numerics){
        	buf.append(n.toXML());
        }
        for(StringElement s : strings){
        	buf.append(s.toXML());
        }
        
        buf.append("</").append(getElementName()).append(">");
        return buf.toString();
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Iterator<Numeric> getNumerics() {
        synchronized (numerics) {
            return Collections.unmodifiableList(new ArrayList<Numeric>(numerics)).iterator();
        }
    }

	public void setNumerics(List<Numeric> numerics) {
		this.numerics = numerics;
	}
	
	public void addNumerics(Numeric n){
		this.numerics.add(n);
	}

	public Iterator<StringElement> getStrings() {
        synchronized (numerics) {
            return Collections.unmodifiableList(new ArrayList<StringElement>(strings)).iterator();
        }
    }

	public void setStrings(List<StringElement> strings) {
		this.strings = strings;
	}
	
	public void addString(StringElement s){
		this.strings.add(s);
	}
	
	public static class Provider implements PacketExtensionProvider {
		@Override
		public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
			Timestamp timestamp = new Timestamp(parser.getAttributeValue("", "value"));
			parser.next();
			int eventType = parser.getEventType();
	    	do{
	    		if(eventType == XmlPullParser.START_TAG){
	    			if("numeric".equals(parser.getName())){
	    				timestamp.addNumerics((Numeric) new Numeric.Provider().parseExtension(parser));
	    			}
	    			else if("string".equals(parser.getName())){
	    				timestamp.addString((StringElement) new StringElement.Provider().parseExtension(parser));
	    			}
	    		}
	    		eventType = parser.next();
	    	}while(!(eventType == XmlPullParser.END_TAG && "timestamp".equals(parser.getName())));
			return timestamp;
		}
	}

}

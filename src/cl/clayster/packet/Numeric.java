package cl.clayster.packet;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class Numeric implements PacketExtension {
	
	/**
	 * <numeric name='Runtime' status='true' automaticReadout='true' value='12345' unit='h'/>
	 */
	
	private String name, historicalHour, historicalDay, status, momentary, automaticReadout, value, unit;
	
	public Numeric(String name, String historicalHour, String historicalDay, String status, String momentary, String automaticReadout, String value, String unit){
		this.name = name;
		this.historicalHour = historicalHour;
		this.historicalDay = historicalDay;
		this.status = status;
		this.momentary = momentary;
		this.automaticReadout = automaticReadout;
		this.value = value;
		this.unit = unit;
	}
	
	@Override
	public String getElementName() {
		return "numeric";
	}

	@Override
	public String getNamespace() {
		return "urn:xmpp:iot:sensordata";
	}

	@Override
	public String toXML() {
		StringBuilder buf = new StringBuilder();
        buf.append("<").append(getElementName());
        if(name != null) buf.append(" name=\"").append(getName()).append('\"');
        if(historicalHour != null) buf.append(" historicalHour=\"").append(getHistoricalHour()).append('\"');
        if(historicalDay != null) buf.append(" historicalDay=\"").append(getHistoricalDay()).append('\"');
        if(status != null) buf.append(" status=\"").append(getStatus()).append('\"');
        if(momentary != null) buf.append(" momentary=\"").append(getMomentary()).append('\"');
        if(automaticReadout != null) buf.append(" automaticReadout=\"").append(getAutomaticReadout()).append('\"');
        if(value != null) buf.append(" value=\"").append(getValue()).append('\"');
        if(unit != null) buf.append(" unit=\"").append(getUnit()).append('\"');
        buf.append("/>");
        return buf.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHistoricalHour() {
		return historicalHour;
	}

	public void setHistoricalHour(String historicalHour) {
		this.historicalHour = historicalHour;
	}

	public String getHistoricalDay() {
		return historicalDay;
	}

	public void setHistoricalDay(String historicalDay) {
		this.historicalDay = historicalDay;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMomentary() {
		return momentary;
	}

	public void setMomentary(String momentary) {
		this.momentary = momentary;
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

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	
	public static class Provider implements PacketExtensionProvider {
		@Override
		public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
			return new Numeric(parser.getAttributeValue("", "name"), parser.getAttributeValue(null, "historicalHour"),
					parser.getAttributeValue(null, "historicalDay"), parser.getAttributeValue(null, "status"),
					parser.getAttributeValue(null, "momentary"), parser.getAttributeValue(null, "automaticReadout"),
					parser.getAttributeValue(null, "value"), parser.getAttributeValue(null, "unit"));
	    			}
	}
	

}

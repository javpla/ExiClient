package cl.clayster.packet;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class Numeric implements PacketExtension {
	
	/**
	 * <numeric name='Runtime' status='true' automaticReadout='true' value='12345' unit='h'/>
	 */
	
	private String name, automaticReadout, value, unit, computed, historical, historicalSecond, historicalMinute, historicalHour, historicalDay
	, historicalWeek, historicalMonth, historicalQuarter, historicalYear, historicalOther, identity, momentary, peak, status, module, stringIds;
	
	public Numeric(){
		
	}
	
	public Numeric(String name, String value, String unit, String automaticReadout, String computed, String historical, String historicalSecond
			, String historicalMinute, String historicalHour, String historicalDay, String historicalWeek, String historicalMonth, String historicalQuarter
			, String historicalYear, String historicalOther, String identity, String momentary, String peak, String status, String module, String stringIds){
		this.name = name;
		this.value = value;
		this.unit = unit;
		this.automaticReadout = automaticReadout;
		this.computed = computed;
		this.historical = historical;
		this.historicalSecond = historicalSecond;
		this.historicalMinute = historicalMinute;
		this.historicalHour = historicalHour;
		this.historicalDay = historicalDay;
		this.historicalWeek = historicalWeek;
		this.historicalMonth = historicalMonth;
		this.historicalQuarter = historicalQuarter;
		this.historicalYear = historicalYear;
		this.historicalOther = historicalOther;
		this.identity = identity;
		this.momentary = momentary;
		this.peak = peak;
		this.status = status;
		this.module = module;
		this.stringIds = stringIds;
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
        if(value != null) buf.append(" value=\"").append(getValue()).append('\"');
        if(unit != null) buf.append(" unit=\"").append(getUnit()).append('\"');
        if(automaticReadout != null) buf.append(" automaticReadout=\"").append(getAutomaticReadout()).append('\"');
        if(computed != null) buf.append(" computed=\"").append(getComputed()).append('\"');
        if(historical != null) buf.append(" historical=\"").append(getHistorical()).append('\"');
        if(historicalSecond != null) buf.append(" historicalSecond=\"").append(getHistoricalSecond()).append('\"');
        if(historicalMinute != null) buf.append(" historicalMinute=\"").append(getHistoricalMinute()).append('\"');
        if(historicalHour != null) buf.append(" historicalHour=\"").append(getHistoricalHour()).append('\"');
        if(historicalDay != null) buf.append(" historicalDay=\"").append(getHistoricalDay()).append('\"');
        if(historicalWeek != null) buf.append(" historicalWeek=\"").append(getHistoricalWeek()).append('\"');
        if(historicalMonth != null) buf.append(" historicalMonth=\"").append(getHistoricalMonth()).append('\"');
        if(historicalQuarter != null) buf.append(" historicalQuarter=\"").append(getHistoricalQuarter()).append('\"');
        if(historicalYear != null) buf.append(" historicalYear=\"").append(getHistoricalYear()).append('\"');
        if(historicalOther != null) buf.append(" historicalOther=\"").append(getHistoricalOther()).append('\"');
        if(identity != null) buf.append(" identity=\"").append(getIdentity()).append('\"');
        if(momentary != null) buf.append(" momentary=\"").append(getMomentary()).append('\"');
        if(peak != null) buf.append(" peak=\"").append(getPeak()).append('\"');
        if(status != null) buf.append(" status=\"").append(getStatus()).append('\"');
        if(module != null) buf.append(" module=\"").append(getModule()).append('\"');
        if(stringIds != null) buf.append(" stringIds=\"").append(getStringIds()).append('\"');
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
	

	
	public String getComputed() {
		return computed;
	}

	public void setComputed(String computed) {
		this.computed = computed;
	}

	public String getHistorical() {
		return historical;
	}

	public void setHistorical(String historical) {
		this.historical = historical;
	}

	public String getHistoricalSecond() {
		return historicalSecond;
	}

	public void setHistoricalSecond(String historicalSecond) {
		this.historicalSecond = historicalSecond;
	}

	public String getHistoricalMinute() {
		return historicalMinute;
	}

	public void setHistoricalMinute(String historicalMinute) {
		this.historicalMinute = historicalMinute;
	}

	public String getHistoricalWeek() {
		return historicalWeek;
	}

	public void setHistoricalWeek(String historicalWeek) {
		this.historicalWeek = historicalWeek;
	}

	public String getHistoricalMonth() {
		return historicalMonth;
	}

	public void setHistoricalMonth(String historicalMonth) {
		this.historicalMonth = historicalMonth;
	}

	public String getHistoricalQuarter() {
		return historicalQuarter;
	}

	public void setHistoricalQuarter(String historicalQuarter) {
		this.historicalQuarter = historicalQuarter;
	}

	public String getHistoricalYear() {
		return historicalYear;
	}

	public void setHistoricalYear(String historicalYear) {
		this.historicalYear = historicalYear;
	}

	public String getHistoricalOther() {
		return historicalOther;
	}

	public void setHistoricalOther(String historicalOther) {
		this.historicalOther = historicalOther;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getPeak() {
		return peak;
	}

	public void setPeak(String peak) {
		this.peak = peak;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getStringIds() {
		return stringIds;
	}

	public void setStringIds(String stringIds) {
		this.stringIds = stringIds;
	}




	public static class Provider implements PacketExtensionProvider {
		@Override
		public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
			Numeric n = new Numeric(parser.getAttributeValue(null, "name"), parser.getAttributeValue(null, "value")
					, parser.getAttributeValue(null, "unit"), parser.getAttributeValue(null, "automaticReadout"), parser.getAttributeValue(null, "computed")
					, parser.getAttributeValue(null, "historical"), parser.getAttributeValue(null, "historicalSecond")
					, parser.getAttributeValue(null, "historicalMinute"), parser.getAttributeValue(null, "historicalHour")
					, parser.getAttributeValue(null, "historicalDay"), parser.getAttributeValue(null, "historicalWeek")
					, parser.getAttributeValue(null, "historicalMonth"), parser.getAttributeValue(null, "historicalQuarter")
					, parser.getAttributeValue(null, "historicalYear"), parser.getAttributeValue(null, "historicalOther")
					, parser.getAttributeValue(null, "identity"), parser.getAttributeValue(null, "momentary")
					, parser.getAttributeValue(null, "peak"), parser.getAttributeValue(null, "status")
					, parser.getAttributeValue(null, "module"), parser.getAttributeValue(null, "stringIds"));
			return n;
		}
	}
	

}

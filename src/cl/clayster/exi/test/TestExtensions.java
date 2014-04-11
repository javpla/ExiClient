package cl.clayster.exi.test;

import org.jivesoftware.smack.packet.PacketExtension;

public class TestExtensions {

	public static PacketExtension msgExt[] = {
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<started xmlns='urn:xmpp:iot:sensordata' seqnr='4'/>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "started";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<done xmlns='urn:xmpp:iot:sensordata' seqnr='4'/>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "done";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<failure xmlns='urn:xmpp:iot:sensordata' seqnr='2' done='true'>"
						+ "<error nodeId='Device01' timestamp='2013-03-07T17:13:30'>"
							+ "Timeout."
						+ "</error>"
					+ "</failure>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "failure";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<fields xmlns='urn:xmpp:iot:sensordata' seqnr='1' done='true'>"
						+ "<node nodeId='Device01'>"
							+ "<timestamp value='2013-03-07T16:24:30'>"
								+ "<numeric name='Temperature' momentary='true' automaticReadout='true' value='23.4' unit='°C'/>"
							+ "</timestamp>"
						+ "</node>"
					+ "</fields>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "fields";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<fields xmlns='urn:xmpp:iot:sensordata' seqnr='4'>"
						+ "<node nodeId='Device01'>"
							+ "<timestamp value='2013-03-07T19:00:00'>"
							   + "<numeric name='Temperature' momentary='true' automaticReadout='true' value='23.4' unit='°C'/>" 
							   + "<numeric name='Runtime' status='true' automaticReadout='true' value='12345' unit='h'/>"
							   + "<string name='Device ID' identification='true' automaticReadout='true' value='Device01'/>"
							+ "</timestamp>"
						 + "</node>"
						+ "</fields>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "fields";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<fields xmlns='urn:xmpp:iot:sensordata' seqnr='4'>"
				         + "<node nodeId='Device01'>"
				            + "<timestamp value='2013-03-07T18:00:00'>"
				               + "<numeric name='Temperature' historicalHour='true' automaticReadout='true' value='24.5' unit='°C'/>" 
				            + "</timestamp>"
				            + "<timestamp value='2013-03-07T17:00:00'>"
				               + "<numeric name='Temperature' historicalHour='true' automaticReadout='true' value='25.1' unit='°C'/>" 
				            + "</timestamp>"
				            + "<timestamp value='2013-03-07T16:00:00'>"
				               + "<numeric name='Temperature' historicalHour='true' automaticReadout='true' value='25.2' unit='°C'/>" 
				            + "</timestamp>"
				            + "<timestamp value='2013-03-07T00:00:00'>"
				               + "<numeric name='Temperature' historicalHour='true' historicalDay='true' automaticReadout='true' value='25.2' unit='°C'/>" 
				            + "</timestamp>"
				         + "</node>"
				      + "</fields>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "fields";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<fields xmlns=\"urn:xmpp:iot:sensordata\" seqnr=\"7\" done=\"true\">"
						+ "<node nodeId=\"Device05\"><timestamp value=\"2013-03-07T22:20:45\">"
						+ "<numeric name=\"Temperature\" momentary=\"true\" automaticReadout=\"true\""
						+ " value=\"23.4\" unit=\"°C\" module=\"Whatchamacallit\" stringIds=\"1\"/>"
						+ "<numeric name=\"Temperature, Min\" momentary=\"true\" automaticReadout=\"true\""
						+ " value=\"23.4\" unit=\"°C\" module=\"Whatchamacallit\" stringIds=\"1,2\"/>"
						+ "<numeric name=\"Temperature, Max\" momentary=\"true\" automaticReadout=\"true\""
						+ " value=\"23.4\" unit=\"°C\" module=\"Whatchamacallit\" stringIds=\"1,3\"/>"
						+ "<numeric name=\"Temperature, Mean\" momentary=\"true\" automaticReadout=\"true\""
						+ " value=\"23.4\" unit=\"°C\" module=\"Whatchamacallit\" stringIds=\"1,4\"/>"
						+ "</timestamp></node></fields>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "fields";}
		}
	};
	
	public static PacketExtension iqExt[] = {
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<req xmlns='urn:xmpp:iot:sensordata' seqnr='1' momentary='true'/>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "req";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<req xmlns='urn:xmpp:iot:sensordata' seqnr='4' all='true' when='2013-03-07T19:00:00'/>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "req";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<req xmlns='urn:xmpp:iot:sensordata' seqnr='5' momentary='true'>"
						+ "<node nodeId='Device02'/>"
						+ "<node nodeId='Device03'/>"
						+ "</req>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "req";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<req xmlns='urn:xmpp:iot:sensordata' seqnr='6' momentary='true'>"
						+ "<node nodeId='Device04'/>"
						+ "<field name='Energy'/>"
						+ "<field name='Power'/>"
						+ "</req>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "req";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<cancel xmlns='urn:xmpp:iot:sensordata' seqnr='8'/>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "cancel";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<accepted xmlns='urn:xmpp:iot:sensordata' seqnr='4' queued='true'/>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "accepted";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<cancelled xmlns='urn:xmpp:iot:sensordata' seqnr='8'/>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "cancelled";}
		},
		new PacketExtension() {
			@Override
			public String toXML() {
				return "<error type='CANCEL' code='7'>"
						+ "<forbidden xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>"
						+ "<text xmlns='urn:ietf:params:xml:ns:xmpp-stanzas' xml:lang='en'>Access denied.</text>"
						+ "<rejected xmlns='urn:xmpp:iot:sensordata' seqnr='3'/>"
					+ "</error>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "rejected";}
		}
		/*,
		new PacketExtension() {
			@Override
			public String toXML() {
				//TODO: misses 'code' attribute, and type should be 'CANCEL'
				return "<error type='cancel'>"
						+ "<forbidden xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>"
						+ "<text xmlns='urn:ietf:params:xml:ns:xmpp-stanzas' xml:lang='en'>Access denied.</text>"
						+ "<rejected xmlns='urn:xmpp:iot:sensordata' seqnr='3'/>"
					+ "</error>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "rejected";}
		}
		*/
		,new PacketExtension() {
			@Override
			public String toXML() {
				return "<req xmlns='urn:xmpp:iot:sensordata' seqnr='01' momentary='true'/>";
			}
			@Override public String getNamespace() {return "urn:xmpp:iot:sensordata";}
			@Override public String getElementName() {return "req";}
		}
	};
}

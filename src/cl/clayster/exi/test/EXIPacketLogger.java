package cl.clayster.exi.test;

import cl.clayster.exi.EXIEventListener;
import cl.clayster.exi.EXIUtils;

public class EXIPacketLogger implements EXIEventListener{
	
	boolean blackText = true;
	
	public EXIPacketLogger(boolean blackText){
		this.blackText = blackText;
	}

	@Override
	public void compressionStarted() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void packetDecoded(String xml, byte[] exi) {
		System.err.println("EXI:(" + exi.length + ")" + EXIUtils.bytesToHex(exi));
		System.err.println("XML:(" + xml.length() + ")" + xml);
	}

	@Override
	public void packetEncoded(String xml, byte[] exi) {
		System.out.println("XML:(" + xml.length() + ")" + xml);
		System.out.println("EXI:(" + exi.length + ")" + EXIUtils.bytesToHex(exi));
	}
	
/*
	@Override
	public void packetDecoded(String xml, byte[] exi) {
		if(blackText){
			System.out.println("received");
			System.out.println("EXI:(" + exi.length + ")" + EXIUtils.bytesToHex(exi));
			System.out.println("XML:(" + xml.length() + ")" + xml);
		}
		else{
			System.err.println("received");
			System.err.println("EXI:(" + exi.length + ")" + EXIUtils.bytesToHex(exi));
			System.err.println("XML:(" + xml.length() + ")" + xml);			
		}
	}

	@Override
	public void packetEncoded(String xml, byte[] exi) {
		if(blackText){
			System.out.println("sent");
			System.out.println("XML:(" + xml.length() + ")" + xml);
			System.out.println("EXI:(" + exi.length + ")" + EXIUtils.bytesToHex(exi));
		}
		else{
			System.err.println("sent");
			System.err.println("XML:(" + xml.length() + ")" + xml);
			System.err.println("EXI:(" + exi.length + ")" + EXIUtils.bytesToHex(exi));
		}
	}
*/
}

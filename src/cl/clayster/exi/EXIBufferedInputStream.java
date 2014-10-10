package cl.clayster.exi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

class EXIBufferedInputStream extends BufferedInputStream {
	
	int br = 0;

	public EXIBufferedInputStream(InputStream in) {
		super(in);
	}
	
	public EXIBufferedInputStream(InputStream in, int n) {
		super(in, n);
	}

	@Override
	public synchronized int read() throws IOException {
		int r = super.read();
		br++;
		return r;
	}
	
	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		int r = super.read(b, off, len);
		br += r;
		return r;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		int r = super.read(b);
		br += r;
		return r;
	}
	
	int getPos(){
		return pos;
	}
	
	void setPos(int p){
		this.pos = p;
	}
	
	int getCount(){
		return count;
	}
	
	int resetBR(){
		int r = br;
		br = 0;
		return r;
	}
	
	/*
	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		int r = super.read(b, off, len);
		byte[] read = new byte[r];
		System.arraycopy(b, 0, read, 0, r);
		System.out.println("Bytes read(" + r + "): " + EXIUtils.bytesToHex(read));
		if(r > 900){
			System.out.println("");
		}
		return r;
	}
	
	@Override
	public synchronized int read() throws IOException {
		int r = super.read();
		int v = r & 0xFF;
		System.out.println("Byte read: " + EXIUtils.hexArray[v >>> 4] + EXIUtils.hexArray[v & 0x0F]);
		return r;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		int r = super.read(b);
		byte[] read = new byte[r];
		System.arraycopy(b, 0, read, 0, r);
		System.out.println("Bytes read(" + r + "): " + EXIUtils.bytesToHex(read));
		if(r > 900){
			System.out.println("");
		}
		return r;
	}
	
	*/
}

package io.github.javaherobrine.net.ui;
public class Hex {
	public static final char CHARMAP[]= {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	public static String toHex(byte[] data) {
		StringBuilder builder=new StringBuilder(data.length<<1);
		for(int i=0;i<data.length;++i) {
			builder.append(CHARMAP[(data[i]&0xFF)>>4]);
			builder.append(CHARMAP[data[i]&0xF]);
		}
		return builder.toString();
	}
	public static String toHex(byte[] data,char delimiter) {
		StringBuilder builder=new StringBuilder(data.length*3);
		for(int i=0;i<data.length;++i) {
			builder.append(CHARMAP[(data[i]&0xFF)>>4]);
			builder.append(CHARMAP[data[i]&0xF]);
			builder.append(delimiter);
		}
		return builder.toString();
	}
	public static byte[] getBytes(String str) {
		byte[] block=new byte[str.length()>>1];
		for(int i=0;i<block.length;++i) {
			block[i]=(byte)(Character.digit(str.charAt(1|(i<<1)),16)+(Character.digit(str.charAt(i<<1),16)<<4));
		}
		return block;
	}
	public static String toHex(byte b) {
		return Character.toString(CHARMAP[(b&0xFF)>>4])+Character.toString(CHARMAP[b&0xF]);
	}
}

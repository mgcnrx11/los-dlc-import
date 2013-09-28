package im.jianyu.mgrrdump;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

public class MGRRDump {
	
	public static byte[] CODE_TABLE_START_BYTE_ARRAY = new byte[]{0x00, 0x00, 0x00, 0x27, 0x00, 0x00, 0x00, 0x00};
	
	public static HashMap<byte[], Integer> codeTable = new HashMap<byte[], Integer>();

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		BufferedInputStream in = new BufferedInputStream(
				new FileInputStream(
						"/Volumes/Macintosh Data/Yujian/Desktop/ckmsg/ckmsg_pe06_us.dat"));
		System.out.println("Available bytes:" + in.available());
		byte[] byteArray = new byte[in.available()];
		in.read(byteArray);
		in.close();

		int startByteArrayAddress = MGRRDump.compareByte(byteArray) + MGRRDump.CODE_TABLE_START_BYTE_ARRAY.length;
		System.out.println("码表开始位置："+Integer.toHexString(startByteArrayAddress));
		
		MGRRDump.readCodeTable(byteArray, startByteArrayAddress);
		
		for (byte[] key : codeTable.keySet()) {
			char value = (char)codeTable.get(key).intValue();
			System.out.println("Key: "+ MGRRDump.byteArrayToHexString(key) + "  Value: " + value);
		}
		
	}
	
	public static void readCodeTable(byte[] byteArray, int address) {
		
		for (int i = address; i < byteArray.length; i=i+8) {
			if (byteArray[i]==0x00 && byteArray[i+1] == 0x03) {
				byte[] tempByteArray = new byte[8];
				System.arraycopy(byteArray, i, tempByteArray, 0, tempByteArray.length);
				//MGRRDump.dumpHex(tempByteArray);
				Integer key = Integer.valueOf((int)byteArray[i+7]);
				Integer value = Integer.valueOf((int)byteArray[i+3]);
				codeTable.put(new byte[]{byteArray[i+6],byteArray[i+7]}, value);
				//System.out.println("Key: "+ key + "  Value: " + value);
			}
		}
	}
	
	
	/**
	 * 
	 * @param byteArrayToCompare
	 * @return 最后一个匹配的数组地址
	 */
	public static int compareByte(byte[] byteArrayToCompare) {
		int matchCount = 0;
		int lastAddress = 0;
		for (int i = 0; i < byteArrayToCompare.length - MGRRDump.CODE_TABLE_START_BYTE_ARRAY.length; i=i+2) {
			//查找时按16字节一组进行，不跨组查找
			if (i % 16 > 8) {
				continue;
			}
			
			//1. 截取i至i+8字节，创建一个新数组
			byte[] tempByteArray = new byte[MGRRDump.CODE_TABLE_START_BYTE_ARRAY.length];
			System.arraycopy(byteArrayToCompare, i, tempByteArray, 0, tempByteArray.length);

			//2. 比较这个数组与CODE_TABLE_START_BYTE_ARRAY数组是否完全相同
			for (int j = 0; j < tempByteArray.length; j++) {
				if (j == tempByteArray.length-1) {
					//3. 数组完全相同，记录并打印地址位置，即i值
					if (tempByteArray[j] == MGRRDump.CODE_TABLE_START_BYTE_ARRAY[j]) {
						//System.out.println(Integer.toHexString(i));
						matchCount++;
						lastAddress = i;
						//MGRRDump.dumpHex(tempByteArray);
					}
				}
				if (tempByteArray[j] == MGRRDump.CODE_TABLE_START_BYTE_ARRAY[j]) {
					continue;
				} else {
					break;
				}
			}
		}
		System.out.println("符合【0000 0027 0000 0000】的个数："+matchCount);
		return lastAddress;
	}

	public static String byteArrayToHexString(byte[] src) {
		String num = "0123456789ABCDEF";
		String str = "";
		for (int i = 0; i < src.length; i++) {
			int high = src[i] >> 4 & 0x0f;
			int low = src[i] & 0x0f;
			str = (num.charAt(high) + "" + num.charAt(low) + "");

			if ((i + 1) % 2 == 0) {
				if ((i + 1) % 16 == 0) {
					str = str + "\n";
				} else {
					str = str + " ";
				}
			}
		}
		return str;
	}
	
	public static void dumpHex(byte[] src) {
		dumpHex(src, src.length);
	}

	public static void dumpHex(byte[] src, int len) {
		String num = "0123456789ABCDEF";
		System.out.println("len = " + len);
		for (int i = 0; i < len; i++) {
			int high = src[i] >> 4 & 0x0f;
			int low = src[i] & 0x0f;
			System.out.print(num.charAt(high) + "" + num.charAt(low) + "");

			if ((i + 1) % 2 == 0) {
				if ((i + 1) % 16 == 0) {
					System.out.println();
				} else {
					System.out.print(" ");
				}
			}

		}

		System.out.println();
	}
}

package im.jianyu.mgrrdump;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class ByteTool {
	
	public static int getIntFromData(byte[] srcBytes, int offset, int length) {
		byte[] converted = new byte[length];
		System.arraycopy(srcBytes, offset, converted, 0, length);
		return ByteTool.byte2int(converted);
	}

	public static char[] getChars(byte[] bytes) {
		Charset cs = Charset.forName("ISO-8859-1");
		ByteBuffer bb = ByteBuffer.allocate(bytes.length);
		bb.put(bytes);
		bb.flip();
		CharBuffer cb = cs.decode(bb);

		return cb.array();
	}
	
	/**
	 * 
	 * @param byteArrayToCompare
	 * @return 最后一个匹配的数组地址
	 */
	public static int compareByteReturnLastMatchAddress(byte[] byteArrayToCompare, byte[] byteArrayForCompare) {
		int matchCount = 0;
		int lastAddress = 0;
		for (int i = 0; i < byteArrayToCompare.length
				- byteArrayForCompare.length; i = i + 2) {
			// 查找时按16字节一组进行，不跨组查找
			if (i % 16 > 8) {
				continue;
			}

			// 1. 截取i至i+8字节，创建一个新数组
			byte[] tempByteArray = new byte[byteArrayForCompare.length];
			System.arraycopy(byteArrayToCompare, i, tempByteArray, 0,
					tempByteArray.length);

			// 2. 比较这个数组与CODE_TABLE_START_BYTE_ARRAY数组是否完全相同
			for (int j = 0; j < tempByteArray.length; j++) {
				if (j == tempByteArray.length - 1) {
					// 3. 数组完全相同，记录并打印地址位置，即i值
					if (tempByteArray[j] == byteArrayForCompare[j]) {
						// System.out.println(Integer.toHexString(i));
						matchCount++;
						lastAddress = i;
						// MGRRDump.dumpHex(tempByteArray);
					}
				}
				if (tempByteArray[j] == byteArrayForCompare[j]) {
					continue;
				} else {
					break;
				}
			}
		}
		System.out.println("符合的个数：" + matchCount);
		return lastAddress;
	}

	public static int byte2int(byte[] res) {
		int targets = (res[3] & 0xff) | ((res[2] << 8) & 0xff00) // | 表示安位或
				| ((res[1] << 24) >>> 8) | (res[0] << 24);
		return targets;
	}

	public static String byteArrayToHexStringWithoutFormat(byte[] src) {
		String num = "0123456789ABCDEF";
		String str = "";
		for (int i = 0; i < src.length; i++) {
			int high = src[i] >> 4 & 0x0f;
			int low = src[i] & 0x0f;
			str += (num.charAt(high) + "" + num.charAt(low) + "");

			if ((i + 1) % 2 == 0) {
				if ((i + 1) % 16 == 0) {
					str = str + "\n";
				} else {
					// str = str + " ";//暂时不加空格
				}
			}
		}
		return str;
	}

	public static String byteArrayToHexString(byte[] src) {
		String num = "0123456789ABCDEF";
		String str = "";
		for (int i = 0; i < src.length; i++) {
			int high = src[i] >> 4 & 0x0f;
			int low = src[i] & 0x0f;
			str += (num.charAt(high) + "" + num.charAt(low) + " ");

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

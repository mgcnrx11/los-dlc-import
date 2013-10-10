package im.jianyu.mgrrdump;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MGRRDump {

	public static HashMap<byte[], String> codeTable = new HashMap<byte[], String>();

	public static int MCD_START_ADDRESS = 0x00;
	public static int MCD_SCRIPT_CONTROL_ADDRESS;
	public static int MCD_SCRIPT_SCREEN_COUNT;
	public static int MCD_CODE_TABLE_ADDRESS;
	public static int MCD_CODE_TABLE_COUNT;
	public static int MCD_SCRIPT_START_OFFSET;
	
	public static String FILE_NAME = "";

	public static void main(String[] args) throws IOException {

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				"G:\\Backup\\Desktop\\event\\ckmsg\\ckmsg_codec_3st_us.dat"));
		byte[] scriptData = new byte[in.available()];
		in.read(scriptData);
		in.close();
		
		// 头部信息解析
		MCD_START_ADDRESS = analyzeHeaderReturnMCDStartAddress(scriptData);
		MCD_SCRIPT_SCREEN_COUNT = MCD_START_ADDRESS + 0x04;
		MCD_CODE_TABLE_ADDRESS = MCD_START_ADDRESS + 0x08;
		MCD_CODE_TABLE_COUNT = MCD_START_ADDRESS + 0x0C;
		MCD_SCRIPT_START_OFFSET = 0x28;

		// 获取码表开始位置
		byte[] codeTableStart = new byte[4];
		System.arraycopy(scriptData, MCD_CODE_TABLE_ADDRESS,
				codeTableStart, 0, 4);
		int codeTableStartAddress = MCD_START_ADDRESS
				+ ByteTool.byte2int(codeTableStart);
		//System.out.println("码表开始位置："
		//		+ Integer.toHexString(codeTableStartAddress));
		// 获取码表长度
		byte[] codeTableCountByte = new byte[4];
		System.arraycopy(scriptData, MCD_CODE_TABLE_COUNT,
				codeTableCountByte, 0, 4);
		int codeTableCount = ByteTool.byte2int(codeTableCountByte);
		//System.out.println("码表长度：" + codeTableCount);
		// 读取码表
		MGRRDump.readCodeTable(scriptData, codeTableStartAddress,
				codeTableCount);


		// Dump脚本
		MGRRDump.exportScript(scriptData);
	}
	
	/**
	 * 头部信息解析，返回MCD地址，若没有MCD地址，则返回0
	 * @param byteArray
	 * @return
	 */
	public static int analyzeHeaderReturnMCDStartAddress(byte[] byteArray) {
		// 1. 文件类型标识，后面0x00填充
		byte[] fileTypeByte = new byte[4-1];// 最后一字节为0x00，去掉
		System.arraycopy(byteArray, 0, fileTypeByte, 0, 4-1);
		String fileType = String.valueOf(ByteTool.getChars(fileTypeByte));
		//System.out.println(fileType);
		
		// 2. 封包的文件数
		byte[] packageCountByte = new byte[4];
		System.arraycopy(byteArray, 4, packageCountByte, 0, 4);
		int packageCount = ByteTool.byte2int(packageCountByte);
		//System.out.println(packageCount);
		
		// 3. 指针1，指向packageCount个封包文件各自的起始地址列表，绝对值
		byte[] packageAddressListByte = new byte[4];
		System.arraycopy(byteArray, 8, packageAddressListByte, 0, 4);
		int packageAddressList = ByteTool.byte2int(packageAddressListByte);
		//System.out.println(packageAddressList);
		
		// 4.1  指针2，封包文件扩展名列表起始位置
		byte[] extendNameAddressListByte = new byte[4];
		System.arraycopy(byteArray, 12, extendNameAddressListByte, 0, 4);
		int extendNameAddressList = ByteTool.byte2int(extendNameAddressListByte);
		//System.out.println(extendNameAddressList);
		
		// 4.2 读取封包文件扩展名
		String[] extendNameArray = new String[packageCount];
		for (int i = 0; i < packageCount; i++) {
			byte[] extendNameByte = new byte[4-1];// 最后一字节为0x00，去掉
			System.arraycopy(byteArray, extendNameAddressList + i * 4, extendNameByte, 0, 4 - 1);
			extendNameArray[i] = String.valueOf(ByteTool.getChars(extendNameByte));
			//System.out.println(ByteTool.getChars(extendNameByte));
		}
		
		// 5.1 指针3：封包文件全名列表起始位置
		byte[] fullNameAddressListByte = new byte[4];
		System.arraycopy(byteArray, 16, fullNameAddressListByte, 0, 4);
		int fullNameAddressList = ByteTool.byte2int(fullNameAddressListByte);
		//System.out.println(fullNameAddressList);
		
		// 5.2 读取封包文件全名
		byte[] fullNameMaxLengthByte = new byte[4];
		System.arraycopy(byteArray, fullNameAddressList, fullNameMaxLengthByte, 0, 4);
		int fullNameMaxLength = ByteTool.byte2int(fullNameMaxLengthByte);
		String[] fullNameArray = new String[packageCount];
		for (int i = 0; i < packageCount; i++) {
			byte[] fullNameByte = new byte[fullNameMaxLength];
			System.arraycopy(byteArray, fullNameAddressList + 4 + i * fullNameMaxLength, fullNameByte, 0, fullNameMaxLength);
			fullNameArray[i] = String.valueOf(ByteTool.getChars(fullNameByte));
			//System.out.println(ByteTool.getChars(fullNameByte));
		}
		
		// 6. 需要确定该文件是否包含mcd文件，若有，则读取其偏移地址
		boolean isIncludeMCD = false;
		for (int i = 0; i < extendNameArray.length; i++) {
			isIncludeMCD = extendNameArray[i].matches("mcd");
			if(isIncludeMCD) {
				byte[] mcdStartByte = new byte[4];
				System.arraycopy(byteArray, packageAddressList + i * 4, mcdStartByte, 0, 4);
				MCD_START_ADDRESS = ByteTool.byte2int(mcdStartByte);
				FILE_NAME = fullNameArray[i];
				break;
			}
		}
		//System.out.println(MCD_START_ADDRESS);
		return MCD_START_ADDRESS;
	}

	public static void exportScript(byte[] scriptData) throws IOException {
		String outputFileName = "G:/Backup/Desktop/event/" + FILE_NAME.substring(0, FILE_NAME.indexOf(".")) +".txt";
		Writer fileWriter = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outputFileName),"UTF-16"));
		char[] wlnChar = { '\r', '\n' };

		
		int scriptReadControlAddressP1 = ByteTool.getIntFromData(scriptData, MCD_START_ADDRESS, 4);
		int scriptScreenCount = ByteTool.getIntFromData(scriptData, MCD_SCRIPT_SCREEN_COUNT, 4);
		
		for (int i = MCD_START_ADDRESS + scriptReadControlAddressP1, screenCount = 1; i < (MCD_START_ADDRESS + scriptReadControlAddressP1) + scriptScreenCount * 16; i = i + 16) {
			 int scriptReadControlAddressP2 = ByteTool.getIntFromData(scriptData, i, 4) + MCD_START_ADDRESS;
			 
			 // 进入文本读取控制的第二部分
			 int realScriptReadControlAddress = ByteTool.getIntFromData(scriptData, scriptReadControlAddressP2, 4) + MCD_START_ADDRESS;
			 int lineCount = ByteTool.getIntFromData(scriptData, scriptReadControlAddressP2 + 4, 4);
			 int screenCharacterCount = ByteTool.getIntFromData(scriptData, scriptReadControlAddressP2 + 12, 4);

			 // 控制格式，打印Tag
			 System.out.println("#### " + FILE_NAME.substring(0, FILE_NAME.indexOf(".")) + "_screen_"+screenCount+"_" + Integer.toHexString(i) + " ####");
			 fileWriter.write("#### " + FILE_NAME.substring(0, FILE_NAME.indexOf(".")) + "_screen_" +screenCount+"_" + Integer.toHexString(i) + " ####");
			 fileWriter.write(wlnChar);
			 for (int j = 0; j < lineCount; j++) {
				 int scriptLineAddress = ByteTool.getIntFromData(scriptData, realScriptReadControlAddress + j * 24, 4) + MCD_START_ADDRESS;
				 int lineCharacterCount = ByteTool.getIntFromData(scriptData, realScriptReadControlAddress + j * 24 + 8, 4);
				 int allLineCount = ByteTool.getIntFromData(scriptData, realScriptReadControlAddress + j * 24 + 12, 4);
				 
				 // 正式读取
				 int charOffset = 0;
				 for (int k = 0; k < allLineCount; k++) {
					byte[] customizedEncode = new byte[2];
					System.arraycopy(scriptData, scriptLineAddress + charOffset, customizedEncode, 0, 2);
					// 根据自定义码表替换字符
					for (byte[] key : codeTable.keySet()) {
						if (key[0] == customizedEncode[0] && key[1] == customizedEncode[1]) {
							customizedEncode = key;
							break;
						}
					}
					String value = "";
					value = codeTable.get(customizedEncode);
					if (value == null) {
						String error = ByteTool.byteArrayToHexStringWithoutFormat(customizedEncode);
						System.out.print("[" + error + "]");
						fileWriter.write("[" + error + "]");
						if (error.equals("8009") || error.equals("800A")) {
							// 针对jp脚本8009\800A后面做出特殊处理
							charOffset += 2;
							continue;
						}
						charOffset += 4;
						continue;
					}
					System.out.print(value);
					if (value.equals("\n")) {
						fileWriter.write(wlnChar);
					} else {
						fileWriter.write(value);
					}
					charOffset += 4;
					
				 }
			 }
			 // 控制格式
			 System.out.println();
			 fileWriter.write(wlnChar);
			 screenCount++;
		}
		
		fileWriter.close();
	}
	
	public static void dumpScript(byte[] byteArray, int offset) {
		// 先读取文本长度
		byte[] scriptLengthByteArray = new byte[] { byteArray[offset],
				byteArray[offset + 1], byteArray[offset + 2],
				byteArray[offset + 3] };
		int scriptLength = ByteTool.byte2int(scriptLengthByteArray);
		//System.out.println("文本长度：" + scriptLength);

		for (int i = offset + MCD_SCRIPT_START_OFFSET; i < offset + scriptLength; i = i + 2) {
			byte[] origin = new byte[2];
			origin[0] = byteArray[i];
			origin[1] = byteArray[i + 1];
			
			byte[] controlByte = new byte[] { byteArray[i + 2],
					byteArray[i + 3] };
			// 根据自定义码表替换字符
			for (byte[] key : codeTable.keySet()) {
				if (key[0] == origin[0] && key[1] == origin[1]) {
					origin = key;
					break;
				}
			}
			String value = "";
			value = codeTable.get(origin);
			if (value == null) {
				String error = ByteTool.byteArrayToHexStringWithoutFormat(origin);
				System.out.print("[" + error + "]");
				if (error.equals("8008")) {
					// 针对jp脚本8008后面做出特殊处理，因为8008 0000.
					i = i + 2;
				}
				continue;
			}
			System.out.print(value);
			if (value != "\n") { // 换行符2个字节，其他符合都是4个字节，所以进行特殊处理
				i = i + 2;
			} else {
				// 判断i+2,i+3是否超出scriptLength长度
				if (i + 2 >= offset + scriptLength || i + 4 >= offset + scriptLength) {
					break;
				}
			}
		}
	}

	/**
	 * 读取码表内容到HashMap中
	 * 
	 * @param byteArray
	 * @param address
	 * @param count
	 */
	public static void readCodeTable(byte[] byteArray, int address, int count) {

		for (int i = address; i < (address + count * 8); i = i + 8) {
			byte[] tempByteArray = new byte[2];
			System.arraycopy(byteArray, i + 2, tempByteArray, 0,
					tempByteArray.length);
			String valueString = new String(tempByteArray,
					Charset.forName("UTF-16BE"));
			codeTable.put(new byte[] { byteArray[i + 6], byteArray[i + 7] },
					valueString);
		}

		// 在码表中增加空格和换行符
		codeTable.put(new byte[] { (byte) 0x80, 0x01 }, " "); // 半角空格
		codeTable.put(new byte[] { (byte) 0x80, 0x00 }, "\n"); // 换行符
		
		// 打印码表
//		for (byte[] key : codeTable.keySet()) {
//			String value = codeTable.get(key);
//			System.out.println("Key: " + ByteTool.byteArrayToHexString(key)
//					+ "  Value: " + value);
//		}
	}

}
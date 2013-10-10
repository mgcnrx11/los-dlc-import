package im.jianyu.mgrrdump;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;

public class MGRRDump {

	public static HashMap<byte[], String> codeTable = new HashMap<byte[], String>();

	public static int MCD_START_ADDRESS = 0x00;
	public static int MCD_SCRIPT_CONTROL_ADDRESS;
	public static int MCD_SCRIPT_SCREEN_COUNT;
	public static int MCD_CODE_TABLE_ADDRESS;
	public static int MCD_CODE_TABLE_COUNT;
	public static int MCD_SCRIPT_START_OFFSET;
	
	// 头部信息
	public static String FILE_TYPE = "";
	public static String FILE_NAME = "";
	public static int PACKAGE_COUNT;

	public static void main(String[] args) throws IOException {

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				"/Volumes/Macintosh Data/Yujian/Desktop/event/ev0000_jp.dat"));
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
		FILE_TYPE = ByteTool.getStringFromData(byteArray, 0, 4-1);// 最后一字节为0x00，去掉
		System.out.println(FILE_TYPE);
		
		// 2. 封包的文件数
		PACKAGE_COUNT = ByteTool.getIntFromData(byteArray, 4, 4);
		System.out.println(PACKAGE_COUNT);
		
		// 3. 指针1，指向packageCount个封包文件各自的起始地址列表，绝对值
		int packageAddressList = ByteTool.getIntFromData(byteArray, 8, 4);
		System.out.println(packageAddressList);
		
		// 4.1  指针2，封包文件扩展名列表起始位置
		int extendNameAddressList = ByteTool.getIntFromData(byteArray, 12, 4);
		System.out.println(extendNameAddressList);
		
		// 4.2 读取封包文件扩展名
		String[] extendNameArray = new String[PACKAGE_COUNT];
		for (int i = 0; i < PACKAGE_COUNT; i++) {
			extendNameArray[i] = ByteTool.getStringFromData(byteArray, extendNameAddressList + i * 4, 4-1);// 最后一字节为0x00，去掉
			System.out.println(extendNameArray[i]);
		}
		
		// 5.1 指针3：封包文件全名列表起始位置
		int fullNameAddressList = ByteTool.getIntFromData(byteArray, 16, 4);
		System.out.println(fullNameAddressList);
		
		// 5.2 读取封包文件全名
		int fullNameMaxLength = ByteTool.getIntFromData(byteArray, fullNameAddressList, 4);
		String[] fullNameArray = new String[PACKAGE_COUNT];
		for (int i = 0; i < PACKAGE_COUNT; i++) {
			fullNameArray[i] = ByteTool.getStringFromData(byteArray, fullNameAddressList + 4 + i * fullNameMaxLength, fullNameMaxLength);
			System.out.println(fullNameArray[i]);
		}
		
		// 6. 需要确定该文件是否包含mcd文件，若有，则读取其偏移地址
		boolean isIncludeMCD = false;
		for (int i = 0; i < extendNameArray.length; i++) {
			isIncludeMCD = extendNameArray[i].matches("mcd");
			if(isIncludeMCD) {
				MCD_START_ADDRESS = ByteTool.getIntFromData(byteArray, packageAddressList + i * 4, 4);
				FILE_NAME = fullNameArray[i].substring(0, fullNameArray[i].lastIndexOf("d")+1);//去除末尾的字符
				break;
			}
		}
		System.out.println(MCD_START_ADDRESS);
		return MCD_START_ADDRESS;
	}

	public static void exportScript(byte[] scriptData) throws IOException {
		String outputFileName = "/Volumes/Macintosh Data/Yujian/Desktop/MGRRExport/" + FILE_NAME +".txt";
		Writer fileWriter = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(outputFileName),"UTF-16"));
		char[] wlnChar = { '\r', '\n' };
		
		// 输出头部信息
		fileWriter.write(FILE_TYPE + "封包文件数：" + PACKAGE_COUNT + "\n\n");
		fileWriter.write("MCD文件名称：" + FILE_NAME + "\n\n");
		fileWriter.write("MCD文件起始绝对地址：" + Integer.toHexString(MCD_START_ADDRESS) + "H" + "\n\n");
		fileWriter.write("<表1-文本读取控制>起始绝对地址：" + Integer.toHexString(MCD_START_ADDRESS + ByteTool.getIntFromData(scriptData, MCD_START_ADDRESS, 4)) + "H" + "\n\n");
		fileWriter.write("<表2-自定义代码表>起始绝对地址：" + Integer.toHexString(MCD_START_ADDRESS + ByteTool.getIntFromData(scriptData, MCD_START_ADDRESS + 8, 4)) + "H" + "\n\n");
		fileWriter.write("<表3-未知>起始绝对地址：" + Integer.toHexString(MCD_START_ADDRESS + ByteTool.getIntFromData(scriptData, MCD_START_ADDRESS + 16, 4)) + "H" + "\n\n");
		fileWriter.write("<表4-未知>起始绝对地址：" + Integer.toHexString(MCD_START_ADDRESS + ByteTool.getIntFromData(scriptData, MCD_START_ADDRESS + 24, 4)) + "H" + "\n\n");
		fileWriter.write("<表5-未知>起始绝对地址：" + Integer.toHexString(MCD_START_ADDRESS + ByteTool.getIntFromData(scriptData, MCD_START_ADDRESS + 32, 4)) + "H" + "\n\n");
		
		fileWriter.write("--------------- 脚本正文 ---------------\n");
		
		int scriptReadControlAddressP1 = ByteTool.getIntFromData(scriptData, MCD_START_ADDRESS, 4);
		int scriptScreenCount = ByteTool.getIntFromData(scriptData, MCD_SCRIPT_SCREEN_COUNT, 4);
		
		
		
		for (int i = MCD_START_ADDRESS + scriptReadControlAddressP1, screenCount = 1; i < (MCD_START_ADDRESS + scriptReadControlAddressP1) + scriptScreenCount * 16; i = i + 16) {
			 int scriptReadControlAddressP2 = ByteTool.getIntFromData(scriptData, i, 4) + MCD_START_ADDRESS;
			 
			 // 进入文本读取控制的第二部分
			 int realScriptReadControlAddress = ByteTool.getIntFromData(scriptData, scriptReadControlAddressP2, 4) + MCD_START_ADDRESS;
			 int lineCount = ByteTool.getIntFromData(scriptData, scriptReadControlAddressP2 + 4, 4);
			 int screenCharacterCount = ByteTool.getIntFromData(scriptData, scriptReadControlAddressP2 + 12, 4);

			 // 控制格式，打印Tag
			 int scriptLineStartAddress = ByteTool.getIntFromData(scriptData, realScriptReadControlAddress, 4) + MCD_START_ADDRESS;
			 System.out.println("#### " + FILE_NAME.substring(0, FILE_NAME.indexOf(".")) + "_screen_"+screenCount+"_" + Integer.toHexString(scriptLineStartAddress) + " ####");
			 fileWriter.write("#### " + FILE_NAME.substring(0, FILE_NAME.indexOf(".")) + "_screen_" +screenCount+"_" + Integer.toHexString(scriptLineStartAddress) + " ####");
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
						System.out.print("{" + error + "}");
						fileWriter.write("{" + error + "}");
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
		
		// 输出码表
		fileWriter.write("--------------- 自定义码表 ---------------\n");
		for (byte[] key : codeTable.keySet()) {
			String value = codeTable.get(key);
			if (value.equals("\n")) {
				value = "\\n";
			} else if (value.equals(" ")) {
				value = "\\w";
			}
			fileWriter.write(value + " 自定义[" + ByteTool.byteArrayToHexStringWithoutFormat(key) + "] UTF-16BE{" + ByteTool.byteArrayToHexStringWithoutFormat(codeTable.get(key).getBytes(Charset.forName("UTF-16BE"))) +"}\n");
		}
		fileWriter.close();
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
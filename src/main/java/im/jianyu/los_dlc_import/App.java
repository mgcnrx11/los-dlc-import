package im.jianyu.los_dlc_import;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 * 
 */
public class App {

	public List<String> tag;

	public Map<String, String> enMap;

	public Map<String, String> cnMap;

	int i = 0;

	public static void main(String[] args) {
		App app = new App();
		app.ReadDataReturn();
		//app.ReadData();
		System.out.println("tag size: "+app.tag.size());
		System.out.println("cn size: "+app.cnMap.size());

		app.importData();

		System.out.println("没有被替换的：");
		for (String str : app.cnMap.keySet()) {
			System.out.println(app.enMap.get(str));
			System.out.println(app.cnMap.get(str));
		}

		System.out.println("一共替换了" + app.i + "个");
		
		app.checkEng();
	}
	
	public void checkImportFile() {
		try {
			FileReader reader = new FileReader(
					"/Volumes/Macintosh Data/Yujian/Dropbox/los/test");
			BufferedReader br = new BufferedReader(reader);
			String row;
			while ((row = br.readLine()) != null) {
				if(row.startsWith("####")) {
					br.readLine();
					if (br.readLine().equals("")) {
					} else {
						System.out.println(row);
					}
				} else {
					System.out.println(row);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void checkEng() {
		try {
			FileReader reader = new FileReader(
					"/Volumes/Macintosh Data/Yujian/Dropbox/los/test.txt");
			BufferedReader br = new BufferedReader(reader);
			String row;
			String engScript="";
			
			FileWriter fileWriter = new FileWriter(
					"/Volumes/Macintosh Data/Yujian/Dropbox/los/unrelaced" + ".txt");

			int rowCount = 0;
			int engCount = 0;
			int chnCount = 0;
			int scriptCount = 0;
			char[] wlnChar = { '\r', '\n' };
			while ((row = br.readLine()) != null) {
				rowCount++;
				boolean containChinese = false;

				if (row.startsWith("#")) {
					engScript = br.readLine();
					scriptCount++;
					rowCount++;
					for (int i = 0; i < engScript.length(); i++) {
						if(engScript.substring(i, i + 1).matches(
								"[\\u4e00-\\u9fa5]+")) {
							containChinese = true;
							chnCount++;
							break;
						}
					}
				}
				
				if (containChinese == false && !row.equals("")) {
					//System.out.println("Row " + rowCount + " is Eng!");
					if (row.startsWith("#### CREDIT")) {
						//break;
					}
					engCount++;
					fileWriter.write(row);
					fileWriter.write(wlnChar);
					fileWriter.write(engScript);
					fileWriter.write(wlnChar);
					fileWriter.write(wlnChar);
				}
			}
			
			fileWriter.close();
			br.close();
			
			System.out.println("Total " + engCount + " unreplaced");
			System.out.println("Total " + chnCount + " replaced");
			System.out.println("Total " + scriptCount + " script");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void importData() {
		try {
			FileReader read = new FileReader(
					"/Volumes/Macintosh Data/Yujian/Dropbox/los/test");
			FileWriter fileWriter = new FileWriter(
					"/Volumes/Macintosh Data/Yujian/Dropbox/los/test" + ".txt");
			BufferedReader br = new BufferedReader(read);
			String row;
			char[] wlnChar = { '\r', '\n' };

			while ((row = br.readLine()) != null) {
				boolean isMatch = false;

				for (String str : tag) {
					String trimRow;
					trimRow = row.replace("#### ", "");
					trimRow = trimRow.replace(" ####", "");
					if (trimRow.equals(str) && !trimRow.equals(row)) {
						fileWriter.write(row);
						fileWriter.write(wlnChar);
						row = br.readLine();
						try {
							fileWriter.write(cnMap.get(str));
							fileWriter.write(wlnChar);
							cnMap.remove(str);
							isMatch = true;
							if (!row.equals(enMap.get(str))) {
								System.out.println("--D--"+row);
							}else {
								System.out.println("--A--"+row);
							}
							i++;
							break;
						} catch (NullPointerException e) {
							System.out.println(i+"--"+row);
						}
					}
				}

				if (isMatch)
					continue;
				fileWriter.write(row);
				fileWriter.write(wlnChar);
			}
			read.close();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void ReadData() {
		try {
			FileReader read = new FileReader(
					"/Volumes/Macintosh Data/Yujian/Dropbox/los/LoS翻译成品0729/战斗提示_ch_完成.txt");
			BufferedReader br = new BufferedReader(read);
			String row;
			tag = new ArrayList<String>();
			enMap = new HashMap<String, String>();
			cnMap = new HashMap<String, String>();
			while ((row = br.readLine()) != null) {
				if (row.equals(""))
					continue;
				String[] strArray = row.split("	");
				tag.add(strArray[0]);
				enMap.put(strArray[0], strArray[1]);
				cnMap.put(strArray[0], strArray[2]);
				// System.out.println(row);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void ReadDataReturn() {
		try {
			FileReader read = new FileReader(
					"/Volumes/Macintosh Data/Yujian/Dropbox/los/LoS翻译成品0729/unreplaced_0807查漏补缺_2.txt");
			BufferedReader br = new BufferedReader(read);
			String row;
			tag = new ArrayList<String>();
			enMap = new HashMap<String, String>();
			cnMap = new HashMap<String, String>();
			while ((row = br.readLine()) != null) {
				if (row.equals(""))
					continue;
				String[] strArray = row.split("	");
				tag.add(strArray[0]);
				enMap.put(strArray[0], strArray[1]);
				cnMap.put(strArray[0], br.readLine());
				// System.out.println(row);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

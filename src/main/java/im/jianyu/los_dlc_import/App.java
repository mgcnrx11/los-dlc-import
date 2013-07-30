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
		app.ReadData();
		
		app.importData();
		
		for (String str:app.cnMap.keySet()) {
			//System.out.println(app.cnMap.get(str));
			//System.out.println(app.enMap.get(str));
		}
		
		System.out.println("一共替换了" + app.i + "个");
	}
	
	public void importData() {
		try {
			FileReader read = new FileReader("/Volumes/Macintosh Data/Yujian/Dropbox/los/test");
			FileWriter fileWriter = new FileWriter("/Volumes/Macintosh Data/Yujian/Dropbox/los/test" + ".txt");  
			BufferedReader br = new BufferedReader(read);
			String row;
			char[] wlnChar = { '\r', '\n' };
			
			while((row = br.readLine()) != null) {
				boolean isMatch = false;

				for (String str : tag) {
					String trimRow;
					trimRow = row.replace("#### ", "");
					trimRow = trimRow.replace(" ####", "");
					if (trimRow.equals(str)) {
						//System.out.println(row + "--" + i);
						fileWriter.write(row);
						fileWriter.write(wlnChar);
						row = br.readLine();
						if (row.contains(enMap.get(str))) {
							//System.out.println(cnMap.get(str) + "--" + i++);
							
							fileWriter.write(cnMap.get(str));
							fileWriter.write(wlnChar);
							cnMap.remove(str);
							isMatch = true;
							i++;
							continue;
						}
					}
				}
				if (isMatch) continue;
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
			FileReader read = new FileReader("/Volumes/Macintosh Data/Yujian/Dropbox/los/LoS翻译成品0729/LoS原画标题_ch_完成_130729.txt");
			BufferedReader br = new BufferedReader(read);
			String row;
			tag = new ArrayList<String>();
			enMap = new HashMap<String, String>();
			cnMap = new HashMap<String, String>();
			while ((row = br.readLine()) != null) {
				if (row.equals("")) continue;
				String[] strArray = row.split("	");
				tag.add(strArray[0]);
				enMap.put(strArray[0], strArray[1]);
				cnMap.put(strArray[0], strArray[2]);
				//System.out.println(row);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

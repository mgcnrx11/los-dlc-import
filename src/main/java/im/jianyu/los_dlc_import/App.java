package im.jianyu.los_dlc_import;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Hello world!
 * 
 */
public class App {
	public static void main(String[] args) {
		App app = new App();
		app.ReadData();
	}

	public void ReadData() {
		try {
			FileReader read = new FileReader("/Volumes/Macintosh Data/Yujian/Dropbox/los/LoS翻译成品0729/LoS原画标题_ch_完成_130729.txt");
			BufferedReader br = new BufferedReader(read);
			String row;
			while ((row = br.readLine()) != null) {
				//System.out.println(row);
				String[] strArray = row.split("	");
				for (String str : strArray) {
					System.out.print(str+"---");
				}
				System.out.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

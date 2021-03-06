package testing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
	private static final boolean CREATE_DATA = false;
	
	public static void main(String[] args) {
		String outDataPath;
		String currentWorkPath = System.getProperty("user.dir");
		if(CREATE_DATA){
			int dataSize = 20;
			int maxNum = 100;
			
			// System.out.println("currentWorkPath:"+currentWorkPath);
			outDataPath = currentWorkPath + "/testdata-" + dataSize + ".txt";
			
			PrintWriter outPWriter = null;
			try {
				outPWriter = new PrintWriter(outDataPath, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			outPWriter.println(dataSize);
			
			Random rand = new Random();
			StringBuilder tmpOutData = new StringBuilder();
			tmpOutData.append(rand.nextInt(maxNum+1));
			for(int i=2; i<=dataSize;i++){
				tmpOutData.append(","+rand.nextInt(maxNum+1));
			}
			outPWriter.print(tmpOutData.toString());
			
			outPWriter.close();
		}
		String dataFile = "testdata-20.txt";
		String inDataPath = currentWorkPath + "/" + dataFile;
		Scanner inputScanner = null;
		try{
			inputScanner = new Scanner(new FileInputStream(inDataPath));
			
		}catch(Exception e){
			System.out.println("input file error!");
			System.exit(0);
		}
		
		inputScanner.nextLine();
		String[] tmpData = inputScanner.nextLine().split(",");
		int[] data = new int[tmpData.length];
		for(int i=0;i<data.length;i++){
			data[i] = Integer.parseInt(tmpData[i]);
		}
		tmpData = null;
		
		int numCores = Runtime.getRuntime().availableProcessors();
//		System.out.println(numCores);
		
		int numDivide = numCores;
		
		
	}

}

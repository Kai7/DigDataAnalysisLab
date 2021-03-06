package KaiSeven;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class RewriteInputData {

	public static void main(String[] args) {
		Scanner inputScanner = null;
		String inDataPath = "/home/kai7/Program/BigDataAnalysisLab/Clustering/DataSet/clustering_test.txt";
		try{
			inputScanner = new Scanner(new FileInputStream(inDataPath));
		}catch (IOException e) {
			System.out.println("Error : Can't get current working directory !");
			System.exit(0);
		}
		PrintWriter outputPrinteWriter = null;
		String outDataPath = inDataPath + ".rewrite";
		try{
			outputPrinteWriter = new PrintWriter(outDataPath, "UTF-8");
		}catch(FileNotFoundException | UnsupportedEncodingException e){
			System.out.println("output file error!");
			System.exit(0);
		}
		
		StringTokenizer tokenizer;
		while(inputScanner.hasNext()){
			tokenizer = new StringTokenizer(inputScanner.nextLine());
			String tmpString1 = tokenizer.nextToken();
			String tmpString2 = tokenizer.nextToken();
			System.out.println(tmpString1 + " " + tmpString2);
			outputPrinteWriter.println(tmpString1 + " " + tmpString2);
		}
		inputScanner.close();
		outputPrinteWriter.close();
		System.out.println("done");
	}

}

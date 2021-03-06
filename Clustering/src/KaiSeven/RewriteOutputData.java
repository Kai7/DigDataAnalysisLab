package KaiSeven;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class RewriteOutputData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner inputScanner = null;
		String inDataPath = "/home/kai7/Program/BigDataAnalysisLab/Clustering/DataSet/clustering_test.txt.rewrite.result";
		try {
			inputScanner = new Scanner(new FileInputStream(inDataPath));
		} catch (IOException e) {
			System.out.println("Error : Can't get current working directory !");
			System.exit(0);
		}

		PrintWriter outputPrintWriter = null;
		String outDataPath = inDataPath + ".rewrite";
		try {
			outputPrintWriter = new PrintWriter(outDataPath, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.out.println("output file error!");
			System.exit(0);
		}

		int countCluster = 0;
		while (inputScanner.hasNext()) {
			countCluster += 1;
			String nextLineString =  inputScanner.nextLine();
//			System.out.println(nextLineString);
			String[] vectors = nextLineString.split("\\[");
			for (int i = 1; i < vectors.length; i++) {
//				System.out.println(vectors[i]);
				System.out.println(vectors[i]);
				int rightBracketIndex = vectors[i].indexOf(']');
				vectors[i] = vectors[i].substring(0, rightBracketIndex);
				String[] tmpValuesString  = vectors[i].split(",");
				for(int j=0; j<tmpValuesString.length;j++){
					outputPrintWriter.print(Double.parseDouble(tmpValuesString[j]) + "\t");
				}
				outputPrintWriter.println(countCluster);
			}
		}
		inputScanner.close();
		outputPrintWriter.close();
		System.out.println("------------------------------------------------------\ndone");
	}

}

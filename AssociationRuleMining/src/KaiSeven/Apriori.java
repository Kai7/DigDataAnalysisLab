package KaiSeven;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Apriori {
	private static boolean COLORFUL_DISPLAY = false;
	private static boolean SHOW_PATTERM = false;
	private static boolean SHOW_EACH_SIZE = false;
	private static boolean WRITE_TO_FILE = true;

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	private ArrayList<HashSet<Integer>> DataSet;
	private int ItemsCount;
	private ArrayList<ItemsetSupport> FrequentItemsets;
	private String inputFilePath;
	private String outputFilePath;
	private PrintWriter dataOutputWriter;
	private int countTransaction;
	private int countFrequentPatterm;
	private double min_sup_ratio;
	private int min_sup;

	public Apriori() {
		DataSet = new ArrayList<HashSet<Integer>>();
		ItemsCount = 0;
		FrequentItemsets = new ArrayList<ItemsetSupport>();
	}

	public Apriori(String inDataPath, String outDataPath, double minsupRatio) {
		long startTime = System.currentTimeMillis();
		
		DataSet = new ArrayList<HashSet<Integer>>();

		inputFilePath = inDataPath;
		outputFilePath = outDataPath;
		min_sup_ratio = minsupRatio;
		countFrequentPatterm = 0;
		
		Scanner DataInput = null;
		try {
			DataInput = new Scanner(new FileInputStream(inDataPath));
		} catch (FileNotFoundException e) {
			System.out.println("input file error!");
			System.out.println(inDataPath + " : not found.");
			System.exit(0);
		}

		HashSet<Integer> TmpItemSet = null;
		int tmpNum;
		String TmpHandlingLine = null;
		StringTokenizer HandlingLine = null;
		while (DataInput.hasNextLine()) {
			countTransaction += 1;
			TmpItemSet = new HashSet<Integer>();
			TmpHandlingLine = DataInput.nextLine();
			HandlingLine = new StringTokenizer(TmpHandlingLine, " ,");
			while (HandlingLine.hasMoreTokens()) {
				tmpNum = Integer.parseInt(HandlingLine.nextToken());
				if (tmpNum > ItemsCount)
					ItemsCount = tmpNum;
				TmpItemSet.add(tmpNum);
			}
			DataSet.add(TmpItemSet);
		}
		FrequentItemsets = new ArrayList<ItemsetSupport>();
		min_sup = (int) Math.ceil(min_sup_ratio * DataSet.size());
		
		System.out.println("######################################################");
		System.out.println("Sale DataBase has " + countTransaction + " transactions");
		System.out.println("Given min_sup_ratio : " + min_sup_ratio + " , choose min_sup : " + min_sup);
		System.out.println("######################################################");
		System.out.println();
		
		if (WRITE_TO_FILE) {
			try {
				dataOutputWriter = new PrintWriter(outputFilePath, "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				System.out.println("output file error!");
				System.exit(0);
			}

			dataOutputWriter.println("######################################################");
			dataOutputWriter.println("Sale DataBase has " + countTransaction + " transactions");
			dataOutputWriter.println("Given min_sup_ratio : " + min_sup_ratio + " , choose min_sup : " + min_sup);
			dataOutputWriter.println("######################################################");
		}
		
		mining();
		
		long stopTime = System.currentTimeMillis();
		
		System.out.println(getSimpleResult(stopTime - startTime, false));
		
		if (WRITE_TO_FILE) {
			dataOutputWriter.println(getSimpleResult(stopTime - startTime, true));
			dataOutputWriter.close();
		}
		// System.out.println("--Data Information----------------------");
		// System.out.println("DataSet.size() : " + DataSet.size() +
		// " and  min_sup : " + min_sup);
		// System.out.println("----------------------------------------");
	}

	public void mining() {
		System.out.println("Start Mining ...");
		createFI1();

		if (FrequentItemsets.size() == 0)
			return;

		int tmpFrontIndex;
		int FrontIndex = 0;
		int ItemsetSize = 2;
		while (true) {
			tmpFrontIndex = countFrequentPatterm;
			if (!calculateFrequentItemsets(FrontIndex, ItemsetSize))
				break;
			FrontIndex = tmpFrontIndex;
			
			if(SHOW_EACH_SIZE){
				System.out.println("------------------------------------------------------");
				System.out.println("Frequent " + ItemsetSize + "-itemsets size : " + (countFrequentPatterm - FrontIndex));
				System.out.println("------------------------------------------------------");
			}
			
			if(WRITE_TO_FILE){
				dataOutputWriter.println("------------------------------------------------------");
				dataOutputWriter.println("Frequent " + ItemsetSize + "-itemsets size : " + (countFrequentPatterm - FrontIndex));
				dataOutputWriter.println("------------------------------------------------------");
			}
			ItemsetSize++;
		}
	}

	public ArrayList<ItemsetSupport> getFrequentItemsets() {
		return FrequentItemsets;
	}

	public String toString() {
		String FITable = new String();
		FITable += "There are " + FrequentItemsets.size() + " frequent itemsets !";
		for (int i = 0; i < FrequentItemsets.size(); i++) {
			FITable += "\n { ";
			for (int item : FrequentItemsets.get(i).itemset) {
				FITable += item + " ";
			}
			FITable += "} : " + FrequentItemsets.get(i).support;
		}

		return FITable;
	}

	public String getSimpleResult(long executeTime, boolean flagToFile) {
		String result = new String();
		if (COLORFUL_DISPLAY && !flagToFile) {
			result += ANSI_GREEN + "======================================================" + ANSI_RESET + "\n";
			result += ANSI_GREEN +"Apriori"+ ANSI_RESET + " Mining Result\n";
			result += "Transaction size : " + countTransaction + " and  min_sup : " + ANSI_RED + min_sup_ratio + ANSI_RESET + "\n";
			result += "There are " + ANSI_CYAN + countFrequentPatterm + ANSI_RESET + " frequent itemsets !\n";
			result += "Execution Time : " + ANSI_CYAN + executeTime + ANSI_RESET + "(ms)\n";
			result += ANSI_GREEN + "======================================================" + ANSI_RESET + "\n";

		} else {
			result += "======================================================\n";
			result += "Apriori Mining Result\n";
			result += "Transaction size : " + countTransaction + " and  min_sup : " + min_sup_ratio + "\n";
			result += "There are " + countFrequentPatterm + " frequent itemsets !\n";
			result += "Execution Time : " + executeTime + "(ms)\n";
			result += "======================================================\n";
		}
		return result;
	}

	private void createFI1() {
		ArrayList<ItemsetSupport> tmpTable = new ArrayList<ItemsetSupport>();
		ItemsetSupport tmpATI = null;
		// for (int i = 1; i <= ItemsCount; i++) {
		for (int i = 0; i < ItemsCount + 1; i++) {
			tmpATI = new ItemsetSupport();
			tmpATI.itemset.add(i);
			tmpTable.add(tmpATI);
		}
		for (HashSet<Integer> itemset : DataSet) {
			for (int item : itemset) {
				// tmpTable.get(item - 1).support += 1;
				tmpTable.get(item).support += 1;
			}
		}
		for (int i = 0; i < ItemsCount; i++) {
			if (tmpTable.get(i).support >= min_sup) {
				countFrequentPatterm += 1;
				if(SHOW_PATTERM){
					System.out.println(tmpTable.get(i).toString());
				}
				if(WRITE_TO_FILE){
					dataOutputWriter.println(tmpTable.get(i).toString());
				}
				FrequentItemsets.add(tmpTable.get(i));
			}
		}
		
		if(SHOW_EACH_SIZE){
			System.out.println("------------------------------------------------------");
			System.out.println("Frequent 1-itemsets size : " + FrequentItemsets.size());
			System.out.println("------------------------------------------------------");
		}
		
		if(WRITE_TO_FILE){
			dataOutputWriter.println("------------------------------------------------------");
			dataOutputWriter.println("Frequent 1-itemsets size : " + countFrequentPatterm);
			dataOutputWriter.println("------------------------------------------------------");
		}
	}

	private boolean calculateFrequentItemsets(int FrontIndex, int ItemsetSize) {
		// create new itemsets from previous
		ArrayList<ItemsetSupport> tmpTable = new ArrayList<ItemsetSupport>();

		HashSet<HashSet<Integer>> CheckFISet = new HashSet<HashSet<Integer>>();
		for (int i = FrontIndex; i < FrequentItemsets.size(); i++) {
			CheckFISet.add(FrequentItemsets.get(i).itemset);
		}

		HashSet<Integer> S1 = null;
		HashSet<Integer> S2 = null;
		HashSet<Integer> tmpItemset = null;
		HashSet<Integer> tmpItemsetbackup = null;
		HashSet<HashSet<Integer>> CandidateFrequentItemsets = new HashSet<HashSet<Integer>>();

		int DiffCount, DiffNum;
		boolean JoinFlag;
		for (int i = FrontIndex; i < FrequentItemsets.size(); i++) {
			for (int j = i + 1; j < FrequentItemsets.size(); j++) {
				DiffNum = -1;
				DiffCount = 0;
				JoinFlag = true;
				S1 = FrequentItemsets.get(i).itemset;
				S2 = FrequentItemsets.get(j).itemset;
				for (int item : S1) {
					if (DiffCount > 1) {
						JoinFlag = false;
						break;
					}
					if (!S2.contains(item)) {
						DiffCount += 1;
						DiffNum = item;
					}
				}
				if (!JoinFlag)
					continue;

				// try use addAll
				tmpItemset = new HashSet<Integer>(S2);
				tmpItemset.add(DiffNum);
				tmpItemsetbackup = new HashSet<Integer>(tmpItemset);
				for (int item : tmpItemsetbackup) {
					tmpItemset.remove(item);
					if (!CheckFISet.contains(tmpItemset)) {
						JoinFlag = false;
						break;
					}
					tmpItemset.add(item);
				}
				if (!JoinFlag)
					continue;

				CandidateFrequentItemsets.add(tmpItemset);
			}
		}
		if (CandidateFrequentItemsets.size() == 0)
			return false;

		ItemsetSupport tmpItemsetSupport = null;
		for (HashSet<Integer> ItemSet : CandidateFrequentItemsets) {
			tmpItemsetSupport = new ItemsetSupport(ItemSet);
			tmpTable.add(tmpItemsetSupport);
		}

		boolean ContainFlag;
		HashSet<Integer> tmpDataSetItemset = null;
		tmpItemset = null;
		for (int i = 0; i < tmpTable.size(); i++) {
			tmpItemset = tmpTable.get(i).itemset;
			for (int j = 0; j < DataSet.size(); j++) {
				ContainFlag = true;
				tmpDataSetItemset = DataSet.get(j);
				for (int item : tmpItemset) {
					if (!tmpDataSetItemset.contains(item)) {
						ContainFlag = false;
						break;
					}
				}

				if (ContainFlag) {
					// System.out.println("add");
					tmpTable.get(i).support += 1;
				}
			}
		}
		for (int i = tmpTable.size() - 1; i >= 0; i--) {
			if (tmpTable.get(i).support < min_sup)
				tmpTable.remove(i);
		}

		// add new frequent itemsets
		if (tmpTable.size() > 0) {
			for(int i=0; i<tmpTable.size(); i++){
				countFrequentPatterm += 1;
				if(SHOW_PATTERM){
					System.out.println(tmpTable.get(i).toString());
				}
				if(WRITE_TO_FILE){
					dataOutputWriter.println(tmpTable.get(i).toString());
				}
			}
			FrequentItemsets.addAll(tmpTable);
			return true;
		} else
			return false;
	}

	private class ItemsetSupport {
		public HashSet<Integer> itemset;
		public int support;

		public ItemsetSupport() {
			itemset = new HashSet<Integer>();
			support = 0;
		}

		public ItemsetSupport(HashSet<Integer> its) {
			itemset = new HashSet<Integer>(its);
			support = 0;
		}
		
		public String toString() {
			String result = "{";
			Iterator<Integer> itor = itemset.iterator();
			result += itor.next();
			while (itor.hasNext()) {
				result += "," + itor.next();
			}
			result += "} : " + support;
			return result;
		}
	}
}

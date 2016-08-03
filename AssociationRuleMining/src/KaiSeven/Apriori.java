package KaiSeven;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Apriori {
	public static final String ANSI_RESET = "\033[0m";
	public static final String ANSI_RED = "\033[31m";
	public static final String ANSI_YELLOW = "\033[33m";
	private ArrayList<HashSet<Integer>> DataSet;
	private int ItemsCount;
	private ArrayList<ItemsetSupport> FrequentItemsets;
	private double min_sup_ratio;
	private int min_sup;

	public Apriori() {
		DataSet = new ArrayList<HashSet<Integer>>();
		ItemsCount = 0;
		FrequentItemsets = new ArrayList<ItemsetSupport>();
	}

	public Apriori(String DataPath, double minsupRatio) {
		DataSet = new ArrayList<HashSet<Integer>>();

		Scanner DataInput = null;
		try {
			DataInput = new Scanner(new FileInputStream(DataPath));
		} catch (FileNotFoundException e) {
			System.out.println("input file error!");
			System.exit(0);
		}

		HashSet<Integer> TmpItemSet = null;
		int tmpNum;
		String TmpHandlingLine = null;
		StringTokenizer HandlingLine = null;
		while (DataInput.hasNextLine()) {
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
		min_sup_ratio = minsupRatio;
		min_sup = (int) ((int) DataSet.size() * minsupRatio) + 1;

//		System.out.println("--Data Information----------------------");
//		System.out.println("DataSet.size() : " + DataSet.size() + " and  min_sup : " + min_sup);
//		System.out.println("----------------------------------------");
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
			tmpFrontIndex = FrequentItemsets.size();
			if (!calculateFrequentItemsets(FrontIndex, ItemsetSize))
				break;
			FrontIndex = tmpFrontIndex;
			System.out.println("Frequent " + ItemsetSize + "-itemsets size : "
					+ (FrequentItemsets.size() - FrontIndex));			
			ItemsetSize++;
		}
		System.out.println("-------------------------------------------");
	}

	public ArrayList<ItemsetSupport> getFrequentItemsets() {
		return FrequentItemsets;
	}

	public String toString() {
		String FITable = new String();
		FITable += "There are " + FrequentItemsets.size()
				+ " frequent itemsets !";
		for (int i = 0; i < FrequentItemsets.size(); i++) {
			FITable += "\n { ";
			for (int item : FrequentItemsets.get(i).itemset) {
				FITable += item + " ";
			}
			FITable += "} : " + FrequentItemsets.get(i).support;
		}

		return FITable;
	}
	
	public String getSimpleResult(){
		String Result = new String();
		Result += "========================================\n";
		Result += "FPGrowth Mining Result\n";
		Result += "Data size : " + DataSet.size() + " and  min_sup : " + ANSI_RED + min_sup_ratio + ANSI_RESET + "\n";
		Result += "There are " + ANSI_YELLOW +  FrequentItemsets.size() + ANSI_RESET + " frequent itemsets !\n";
		Result += "========================================\n";		
		return Result;
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
				FrequentItemsets.add(tmpTable.get(i));
			}
		}
		System.out.println("Frequent 1-itemsets size : "
				+ FrequentItemsets.size());
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

				// System.out.println("got!");
				// System.out.print("{");
				// for(int item : tmpItemset){
				// System.out.print(item + " ");
				// }
				// System.out.println("}");
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

		// System.out.println("DB size " + DataSet.size());
		// System.out.println("tT size " + tmpTable.size());
		// calculate frequent itemsets
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
			FrequentItemsets.addAll(tmpTable);
			return true;
		} else
			return false;
	}

	// private ArrayList<ItemsetSupport> createNewItemsetsFromPrevious(
	// int FrontIndex, int ItemsetSize) {
	//
	// return null;
	// }

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

//		public ItemsetSupport(int[] its) {
//			itemset = new HashSet<Integer>();
//			for (int i : its) {
//				itemset.add(i);
//			}
//			support = 0;
//		}
	}
}
package KaiSeven;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class FPGrowth {
	private static boolean COLORFUL_DISPLAY = false;
	private static boolean SHOW_PATTERM = false;
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

	// private ArrayList<ArrayList<Integer>> DataSet;
	// private HashSet<ItemsetSupport> FrequentItemsets;
	private FrequentPatternTree frequentPattermTree;
	private HeaderTable headerTable;
	private String inputFilePath;
	private String outputFilePath;
	private PrintWriter dataOutputWriter;
	private int countTransaction;
	private int countFrequentPatterm;
	private double min_sup_ratio;
	private int min_sup;

	public FPGrowth(String inDataPath, String outDataPath, double minsupRatio) {
		long startTime = System.currentTimeMillis();
		
		inputFilePath = inDataPath;
		outputFilePath = outDataPath;
		min_sup_ratio = minsupRatio;
		countFrequentPatterm = 0;
		
		Scanner DataInput = null;
		try {
			DataInput = new Scanner(new FileInputStream(inDataPath));
		} catch (FileNotFoundException e) {
			System.out.println("input file error ! ");
			System.out.println(inDataPath + " : not found.");
			System.exit(0);
		}
		
		// First Scan Database & Count Frequent Item Table
		HashMap<Integer, Integer> countItemFrequency = new HashMap<Integer, Integer>();
		countTransaction = 0;
		int tmpItemNum;
		StringTokenizer tmpTokenizer = null;
		while (DataInput.hasNextLine()) {
			countTransaction += 1;
			tmpTokenizer = new StringTokenizer(DataInput.nextLine(), " ,");
			while (tmpTokenizer.hasMoreTokens()) {
				tmpItemNum = Integer.parseInt(tmpTokenizer.nextToken());
				if (countItemFrequency.get(tmpItemNum) == null)
					countItemFrequency.put(tmpItemNum, 1);
				else
					countItemFrequency.put(tmpItemNum, countItemFrequency.get(tmpItemNum) + 1);
			}
		}

		// min_sup = 2;
		min_sup = (int) Math.ceil(min_sup_ratio * countTransaction);

		int tmpItemCount;
		LinkedList<ItemSup> frequentItemList = new LinkedList<ItemSup>();
		for (int item : countItemFrequency.keySet()) {
			tmpItemCount = countItemFrequency.get(item);
			if (tmpItemCount >= min_sup)
				frequentItemList.add(new ItemSup(item, tmpItemCount));
		}

		ItemSup[] frequentItemTable = new ItemSup[frequentItemList.size()];
		for (int i = 0; i < frequentItemTable.length; i++)
			frequentItemTable[i] = frequentItemList.remove();
		Arrays.sort(frequentItemTable, new ItemSupComparator());

		// Build Compare Map & Frequent Item Set
		HashMap<Integer, Integer> orderItemMap = new HashMap<Integer, Integer>();
		HashSet<Integer> frequentItemSet = new HashSet<Integer>();
		for (int i = 0; i < frequentItemTable.length; i++) {
			orderItemMap.put(frequentItemTable[i].item, i);
			frequentItemSet.add(frequentItemTable[i].item);
		}

		// Second Scan Database
		// Delete Non-Frequent Items & Sort by Compare Map
		// Build Header Table and FP-Tree
		try {
			DataInput = new Scanner(new FileInputStream(inDataPath));
		} catch (FileNotFoundException e) {
			System.out.println("input file error!");
			System.exit(0);
		}

		headerTable = new HeaderTable(frequentItemTable);
		FPNode[] ptrRearHTable = new FPNode[headerTable.length];
		for (int i = 0; i < ptrRearHTable.length; i++)
			ptrRearHTable[i] = null;

		frequentPattermTree = new FrequentPatternTree();

		LinkedList<Integer> tmpTransactionList = new LinkedList<Integer>();
		while (DataInput.hasNextLine()) {
			tmpTokenizer = new StringTokenizer(DataInput.nextLine(), " ,");
			while (tmpTokenizer.hasMoreTokens()) {
				tmpItemNum = Integer.parseInt(tmpTokenizer.nextToken());
				if (frequentItemSet.contains(tmpItemNum))
					tmpTransactionList.add(tmpItemNum);
			}
			Collections.sort(tmpTransactionList, new OrderItemMapComparator(orderItemMap));

			frequentPattermTree.addNewTransaction(tmpTransactionList, orderItemMap);
		}

		HashSet<Integer> emptyItemSet = new HashSet<Integer>();

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

		System.out.println("Start mining ...");
		mining(frequentPattermTree, headerTable, emptyItemSet);

		long stopTime = System.currentTimeMillis();

		System.out.println(getSimpleResult(stopTime - startTime, false));

		if (WRITE_TO_FILE) {
			dataOutputWriter.println(getSimpleResult(stopTime - startTime, true));
			dataOutputWriter.close();
		}
	}

	public void mining(FrequentPatternTree FPTree, HeaderTable HTable, HashSet<Integer> ItemSet) {
		if (HTable.length == 0)
			return;

		// System.out.println("HeaderTable size : " + HT.length);
		FrequentPatternTree subFPTree = null; // recursive variable
		HeaderTable subHTable = null; // recursive variable
		HashSet<Integer> expItemSet = null; // recursive variable

		FPNode ptrFPNode = null;
		FPNode ptrParentFPNode = null;
		HashSet<Integer> tmpFreqItemSet = null;
		HashMap<Integer, Integer> countItemFrequency = null;
		HashMap<Integer, Integer> subHTableItemMap = null;
		// ArrayList<Integer> tmpFreqItemList = null;
		ItemsetSupport expItemsetSup = null;
		int Count;
		int Index;
		for (int i = 0; i < HTable.length; i++) {
			// HT.HTNodeArr[i].item);
			ptrFPNode = HTable.HTNodeArr[i].headerFPNode;
			if (ptrFPNode == null)
				System.out.println("error");
			Count = 0;
			expItemSet = new HashSet<Integer>(ItemSet);
			expItemSet.add(ptrFPNode.item);
			countItemFrequency = new HashMap<Integer, Integer>();
			while (ptrFPNode != null) {
				Count += ptrFPNode.count;
				ptrParentFPNode = ptrFPNode.parent;
				while (ptrParentFPNode.item != -1) {
					if (countItemFrequency.get(ptrParentFPNode.item) != null)
						countItemFrequency.put(ptrParentFPNode.item, countItemFrequency.get(ptrParentFPNode.item) + ptrFPNode.count);
					else
						countItemFrequency.put(ptrParentFPNode.item, ptrFPNode.count);
					ptrParentFPNode = ptrParentFPNode.parent;
				}
				ptrFPNode = ptrFPNode.nextFP;
			}
			expItemsetSup = new ItemsetSupport(expItemSet, Count);

			if (SHOW_PATTERM) {
				System.out.println(expItemsetSup.toString());
			}

			if (WRITE_TO_FILE) {
				dataOutputWriter.println(expItemsetSup.toString());
			}
			// System.out.println(expItemsetSup.toString());
			countFrequentPatterm += 1;

			tmpFreqItemSet = new HashSet<Integer>();
			for (int item : countItemFrequency.keySet()) {
				if (countItemFrequency.get(item) >= min_sup)
					tmpFreqItemSet.add(item);
			}
			if (tmpFreqItemSet.size() == 0)
				continue;
			countItemFrequency = null;

			// create new sub-FPtree and HeaderTable
			ptrFPNode = HTable.HTNodeArr[i].headerFPNode;
			// tmpFreqItemList = new ArrayList<Integer>(tmpFreqItemSet);
			// initial HeaderTable and buildtmpItemMap
			// this version is non-order for HeaderTable
			subHTable = new HeaderTable(tmpFreqItemSet.size());
			subHTableItemMap = new HashMap<Integer, Integer>();
			Index = 0;
			for (int item : tmpFreqItemSet) {
				subHTable.HTNodeArr[Index] = new HTNode(item);
				subHTableItemMap.put(item, Index);
				Index += 1;
			}
			FPNode tmpNewFPNode = null;

			subFPTree = new FrequentPatternTree();

			while (ptrFPNode != null) {
				// System.out.println("build sub-branch & sub-tree...");
				// new a sub-branch
				ptrParentFPNode = ptrFPNode.parent;
				while (ptrParentFPNode.item != -1 && !(tmpFreqItemSet.contains(ptrParentFPNode.item))) {
					ptrParentFPNode = ptrParentFPNode.parent;
				}

				if (ptrParentFPNode.item == -1) { // this branch has no frequent
													// pattern list
					ptrFPNode = ptrFPNode.nextFP;
					continue;
				}

				tmpNewFPNode = new FPNode(ptrParentFPNode.item, ptrFPNode.count);

				ptrParentFPNode = ptrParentFPNode.parent;
				while (ptrParentFPNode.item != -1) {
					while (ptrParentFPNode.item != -1 && !(tmpFreqItemSet.contains(ptrParentFPNode.item))) {
						ptrParentFPNode = ptrParentFPNode.parent;
					}
					if (ptrParentFPNode.item == -1)
						break;
					tmpNewFPNode.parent = new FPNode();
					tmpNewFPNode.parent.firstchild = tmpNewFPNode;
					tmpNewFPNode = tmpNewFPNode.parent;
					tmpNewFPNode.item = ptrParentFPNode.item;
					tmpNewFPNode.count = ptrFPNode.count;
					ptrParentFPNode = ptrParentFPNode.parent;
				}
				// add the sub-branch to subTree & link tmpHeaderTable to the
				// branch
				subFPTree.addNewBranch(tmpNewFPNode, subHTable, subHTableItemMap);

				// update tmpFPNode
				ptrFPNode = ptrFPNode.nextFP;
			}
			mining(subFPTree, subHTable, expItemSet);
		}

	}

	public String getSimpleResult(long executeTime, boolean flagToFile) {
		String result = new String();
		if (COLORFUL_DISPLAY && !flagToFile) {
			result += ANSI_GREEN + "======================================================" + ANSI_RESET + "\n";
			result += ANSI_GREEN + "FPGrowth" + ANSI_RESET +" Mining Result\n";
			result += "Transaction size : " + countTransaction + " and  min_sup : " + ANSI_RED + min_sup_ratio + ANSI_RESET + "\n";
			result += "There are " + ANSI_CYAN + countFrequentPatterm + ANSI_RESET + " frequent itemsets !\n";
			result += "Execution Time : " + ANSI_CYAN + executeTime + ANSI_RESET + "(ms)\n";
			result += ANSI_GREEN + "======================================================" + ANSI_RESET + "\n";

		} else {
			result += "======================================================\n";
			result += "FPGrowth Mining Result\n";
			result += "Transaction size : " + countTransaction + " and  min_sup : " + min_sup_ratio + "\n";
			result += "There are " + countFrequentPatterm + " frequent itemsets !\n";
			result += "Execution Time : " + executeTime + "(ms)\n";
			result += "======================================================\n";
		}
		return result;
	}

	private class FrequentPatternTree {
		public FPNode root;

		public FrequentPatternTree() {
			root = new FPNode();
		}

		public void addNewTransaction(LinkedList<Integer> someTransaction, HashMap<Integer, Integer> orderItemMap) {
			FPNode ptrCurrentNode = root;
			FPNode ptrSearchNode = null;
			FPNode tmpNode = null;
			int tmpItem;
			int tmpItemMapNum;
			int initialListSize = someTransaction.size();

			for (int i = 0; i < initialListSize; i++) {
				tmpItem = someTransaction.remove();
				if (ptrCurrentNode.firstchild == null) {
					tmpNode = new FPNode(tmpItem, 1);
					tmpNode.parent = ptrCurrentNode;
					ptrCurrentNode.firstchild = tmpNode;
					tmpItemMapNum = orderItemMap.get(tmpItem);
					if (headerTable.HTNodeArr[tmpItemMapNum].rearFPNode == null)
						headerTable.HTNodeArr[tmpItemMapNum].headerFPNode = tmpNode;
					else
						headerTable.HTNodeArr[tmpItemMapNum].rearFPNode.nextFP = tmpNode;
					headerTable.HTNodeArr[tmpItemMapNum].rearFPNode = tmpNode;

					ptrCurrentNode = ptrCurrentNode.firstchild;
				} else {
					// linear search the item
					ptrSearchNode = ptrCurrentNode.firstchild;
					if (ptrSearchNode.item == tmpItem) {
						ptrSearchNode.count += 1;
						ptrCurrentNode = ptrSearchNode;
					} else {
						while (ptrSearchNode.next != null) {
							if (ptrSearchNode.next.item == tmpItem)
								break;
							ptrSearchNode = ptrSearchNode.next;
						}
						if (ptrSearchNode.next != null) {
							ptrSearchNode.next.count += 1;
							ptrCurrentNode = ptrSearchNode.next;
						} else {
							tmpNode = new FPNode(tmpItem, 1);
							tmpNode.parent = ptrCurrentNode;
							ptrSearchNode.next = tmpNode;
							tmpItemMapNum = orderItemMap.get(tmpItem);
							if (headerTable.HTNodeArr[tmpItemMapNum].rearFPNode == null)
								headerTable.HTNodeArr[tmpItemMapNum].headerFPNode = tmpNode;
							else
								headerTable.HTNodeArr[tmpItemMapNum].rearFPNode.nextFP = tmpNode;
							headerTable.HTNodeArr[tmpItemMapNum].rearFPNode = tmpNode;

							ptrCurrentNode = ptrSearchNode.next;
						}
					}
				}
			}
		}

		public void addNewBranch(FPNode branchRoot, HeaderTable subHTable, HashMap<Integer, Integer> subHTableItemMap) {
			FPNode ptrNewNode = branchRoot;
			FPNode ptrSearchNode;
			FPNode ptrInsertNode;
			int index;
			ptrInsertNode = root;
			while (ptrNewNode != null) {
				// if the insert root is leaf
				if (ptrInsertNode.firstchild == null) {
					ptrInsertNode.firstchild = ptrNewNode;
					ptrNewNode.parent = ptrInsertNode;
					while (ptrNewNode != null) {
						index = subHTableItemMap.get(ptrNewNode.item);
						if (subHTable.HTNodeArr[index].headerFPNode == null) {
							subHTable.HTNodeArr[index].headerFPNode = ptrNewNode;
							subHTable.HTNodeArr[index].rearFPNode = ptrNewNode;
						} else {
							subHTable.HTNodeArr[index].rearFPNode.nextFP = ptrNewNode;
							subHTable.HTNodeArr[index].rearFPNode = ptrNewNode;
						}
						ptrNewNode = ptrNewNode.firstchild;
					}
					break;
				} else {
					// search the fit node
					// the fit node is firstchild
					ptrSearchNode = ptrInsertNode.firstchild;
					if (ptrSearchNode.item == ptrNewNode.item) {
						ptrSearchNode.count += ptrNewNode.count;
						ptrNewNode = ptrNewNode.firstchild;
						ptrInsertNode = ptrSearchNode;
						continue;
					}
					// the fit node is not firstchild
					while (ptrSearchNode.next != null) {
						if (ptrSearchNode.next.item == ptrNewNode.item)
							break;
						ptrSearchNode = ptrSearchNode.next;
					}
					// if no fit node
					if (ptrSearchNode.next == null) {
						ptrSearchNode.next = ptrNewNode;
						ptrNewNode.parent = ptrInsertNode;
						while (ptrNewNode != null) {
							index = subHTableItemMap.get(ptrNewNode.item);
							if (subHTable.HTNodeArr[index].headerFPNode == null) {
								subHTable.HTNodeArr[index].headerFPNode = ptrNewNode;
								subHTable.HTNodeArr[index].rearFPNode = ptrNewNode;
							} else {
								subHTable.HTNodeArr[index].rearFPNode.nextFP = ptrNewNode;
								subHTable.HTNodeArr[index].rearFPNode = ptrNewNode;
							}
							ptrNewNode = ptrNewNode.firstchild;
						}
						break;
					}

					// there is fit node
					ptrSearchNode.next.count += ptrNewNode.count;
					ptrNewNode = ptrNewNode.firstchild;
					ptrInsertNode = ptrSearchNode.next;
				}
			}
		}
	}

	private class HeaderTable {
		private HTNode[] HTNodeArr;
		private int length;

		public HeaderTable() {
			HTNodeArr = null;
			length = 0;
		}

		public HeaderTable(int Size) {
			HTNodeArr = new HTNode[Size];
			length = Size;
		}

		public HeaderTable(ItemSup[] frequentItemTable) {
			HTNodeArr = new HTNode[frequentItemTable.length];
			for (int i = 0; i < frequentItemTable.length; i++)
				HTNodeArr[i] = new HTNode(frequentItemTable[i].item);
			length = HTNodeArr.length;
		}

		public void show() {
			System.out.print(HTNodeArr[0].item);
			for (int i = 1; i < HTNodeArr.length; i++) {
				System.out.print(" < " + HTNodeArr[i].item);
			}
			System.out.print("\n");
		}
	}

	private class FPNode {
		public int item;
		public int count;
		public FPNode parent;
		public FPNode firstchild;
		public FPNode next;
		public FPNode nextFP;

		public FPNode() {
			item = -1;
			count = 0;
			parent = null;
			firstchild = null;
			next = null;
			nextFP = null;
		}

		public FPNode(int it) {
			item = it;
			count = 0;
			parent = null;
			firstchild = null;
			next = null;
			nextFP = null;
		}

		public FPNode(int it, int c) {
			item = it;
			count = c;
			parent = null;
			firstchild = null;
			next = null;
			nextFP = null;
		}
	}

	private class HTNode {
		public int item;
		public FPNode headerFPNode;
		public FPNode rearFPNode;

		public HTNode() {
			item = -1;
			headerFPNode = null;
			rearFPNode = null;
		}

		public HTNode(int it) {
			item = it;
			headerFPNode = null;
			rearFPNode = null;
		}
	}

	private class ItemSup {
		public int item;
		public int supper;

		public ItemSup() {
			item = -1;
			supper = 0;
		}

		public ItemSup(int it) {
			item = it;
			supper = 0;
		}

		public ItemSup(int it, int sup) {
			item = it;
			supper = sup;
		}
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

		public ItemsetSupport(HashSet<Integer> its, int sup) {
			itemset = new HashSet<Integer>(its);
			support = sup;
		}

		public void show() {
			Iterator<Integer> itor = itemset.iterator();
			System.out.print("{ " + itor.next());
			while (itor.hasNext()) {
				System.out.print(", " + itor.next());
			}
			System.out.println(" } : " + support);
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

	private class ItemSupComparator implements Comparator<ItemSup> {
		@Override
		public int compare(ItemSup i1, ItemSup i2) {
			if (i1.supper == i2.supper)
				return i2.item - i1.item;
			return i1.supper - i2.supper;
		}
	}

	private class OrderItemMapComparator implements Comparator<Integer> {
		HashMap<Integer, Integer> CompareMap;

		public OrderItemMapComparator() {
			CompareMap = null;
		}

		public OrderItemMapComparator(HashMap<Integer, Integer> IMap) {
			CompareMap = IMap;
		}

		// this design is for decreasing sort
		@Override
		public int compare(Integer i1, Integer i2) {
			return CompareMap.get(i2) - CompareMap.get(i1);
		}
	}
}

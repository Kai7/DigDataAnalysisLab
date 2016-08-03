import java.awt.print.Printable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;


public class FPGrowthAlpha {
	public static final String ANSI_RESET = "\033[0m";
	public static final String ANSI_RED = "\033[31m";
	public static final String ANSI_YELLOW = "\033[33m";
	private ArrayList<ArrayList<Integer>> DataSet;
	private HashSet<ItemsetSupport> FrequentItemsets;
	private FrequentPatternTree FPTree;
	private HeaderTable HTable;
	HashMap<Integer, Integer> OrderItemMap;
	private int ItemsCount;
	private double min_sup_ratio;
	private int min_sup;

	public FPGrowthAlpha() {
		DataSet = new ArrayList<ArrayList<Integer>>();
		ItemsCount = 0;
	}

	public FPGrowthAlpha(String DataPath, double minsupRatio) {
		DataSet = new ArrayList<ArrayList<Integer>>();

		Scanner DataInput = null;
		try {
			DataInput = new Scanner(new FileInputStream(DataPath));
		} catch (FileNotFoundException e) {
			System.out.println("input file error!");
			System.exit(0);
		}

		ArrayList<Integer> TmpItemList = null;
		int tmpNum;
		String TmpHandlingLine = null;
		StringTokenizer HandlingLine = null;
		while (DataInput.hasNextLine()) {
			TmpItemList = new ArrayList<Integer>();
			TmpHandlingLine = DataInput.nextLine();
			HandlingLine = new StringTokenizer(TmpHandlingLine, " ,");
			while (HandlingLine.hasMoreTokens()) {
				tmpNum = Integer.parseInt(HandlingLine.nextToken());
				if (tmpNum > ItemsCount)
					ItemsCount = tmpNum;
				TmpItemList.add(tmpNum);
			}
			DataSet.add(TmpItemList);
		}
		FPTree = null;
		HTable = null;
		OrderItemMap = null;
		min_sup_ratio = minsupRatio;
		min_sup = (int) ((int) DataSet.size() * minsupRatio) + 1;

//		System.out.println("--Data Information----------------------");
//		System.out.println("DataSet.size() : " + DataSet.size() + " and  min_sup : " + min_sup);
//		System.out.println("----------------------------------------");
	}

	public void mining() {
		System.out.println("Start Mining ...");
		// create frequent itemsets size 1
		int[] TmpFIT = new int[ItemsCount + 1];
		ArrayList<Integer> TmpIntArrList = null;
		for (int i = 0; i < DataSet.size(); i++) {
			TmpIntArrList = DataSet.get(i);
			for (int j = 0; j < TmpIntArrList.size(); j++) {
				TmpFIT[TmpIntArrList.get(j)] += 1;
			}
		}
		TmpIntArrList = null;

		HashSet<Integer> FIS = new HashSet<Integer>();
		ItemSup tmpItemSup = null;
		ArrayList<ItemSup> FT1 = new ArrayList<ItemSup>();
		for (int i = 0; i < TmpFIT.length; i++) {
			if (TmpFIT[i] >= min_sup) {
				tmpItemSup = new ItemSup(i, TmpFIT[i]);
				FT1.add(tmpItemSup);
				FIS.add(i);
			}
		}
		TmpFIT = null;
		tmpItemSup = null;

		// sort frequent itemsets size 1
		Collections.sort(FT1, new ItemSupComparator());

		// create compare map
		OrderItemMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < FT1.size(); i++) {
			OrderItemMap.put(FT1.get(i).item, i);
		}

		// delete non-frequent items in DataSet
		for (int i = 0; i < DataSet.size(); i++) {
			TmpIntArrList = DataSet.get(i);
			for (int j = TmpIntArrList.size() - 1; j >= 0; j--) {
				if (!FIS.contains(TmpIntArrList.get(j))) {
					TmpIntArrList.remove(j);
				}
			}
			Collections.sort(TmpIntArrList, new ItemMapComparator(OrderItemMap));
		}
		FIS = null;

		// build Header Table
		HTable = new HeaderTable(FT1);
		// build FP-tree & HeaderTable
		FPNode[] rearHTablePointer = new FPNode[FT1.size()];
		for (int i = 0; i < rearHTablePointer.length; i++) {
			rearHTablePointer[i] = null;
		}

		FPTree = new FrequentPatternTree();
		int tmpItem;
		int IndexCount;
		int ItemMapNum;
		FPNode CurrentPointer = null;
		FPNode SearchPointer = null;
		FPNode tmpNode = null;
		for (int i = 0; i < DataSet.size(); i++) {
			TmpIntArrList = DataSet.get(i);
			CurrentPointer = FPTree.root;
			SearchPointer = null;
			tmpNode = null;
			IndexCount = 0;
			while (IndexCount < TmpIntArrList.size()) {
				if (CurrentPointer.firstchild == null) {
					tmpItem = TmpIntArrList.get(IndexCount);
					tmpNode = new FPNode(tmpItem);
					tmpNode.count += 1;
					tmpNode.parent = CurrentPointer;
					CurrentPointer.firstchild = tmpNode;
					ItemMapNum = OrderItemMap.get(tmpItem);
					if (rearHTablePointer[ItemMapNum] == null) {
						HTable.HTNodeArr[ItemMapNum].nextFP = tmpNode;
						rearHTablePointer[ItemMapNum] = tmpNode;
					} else {
						rearHTablePointer[ItemMapNum].nextFP = tmpNode;
						rearHTablePointer[ItemMapNum] = tmpNode;
					}
					CurrentPointer = CurrentPointer.firstchild;
				} else {
					tmpItem = TmpIntArrList.get(IndexCount);
					// linear search the item
					SearchPointer = CurrentPointer.firstchild;
					if (SearchPointer.item != tmpItem) {
						// the first child is't item
						while (SearchPointer.next != null) {
							if (SearchPointer.next.item == tmpItem)
								break;
							SearchPointer = SearchPointer.next;
						}
						if (SearchPointer.next == null) {
							// not found
							tmpNode = new FPNode(tmpItem);
							tmpNode.count += 1;
							tmpNode.parent = CurrentPointer;
							SearchPointer.next = tmpNode;
							ItemMapNum = OrderItemMap.get(tmpItem);
							if (rearHTablePointer[ItemMapNum] == null) {
								HTable.HTNodeArr[ItemMapNum].nextFP = tmpNode;
								rearHTablePointer[ItemMapNum] = tmpNode;
							} else {
								rearHTablePointer[ItemMapNum].nextFP = tmpNode;
								rearHTablePointer[ItemMapNum] = tmpNode;
							}
							CurrentPointer = tmpNode;
						} else {
							// found
							SearchPointer.next.count += 1;
							CurrentPointer = SearchPointer.next;
						}
					} else {
						SearchPointer.count += 1;
						CurrentPointer = SearchPointer;
					}
				}
				IndexCount += 1;
			}
		}

//		HTable.show();
		// mining the sub-FPTrees of FP-Tree
		FrequentItemsets = new HashSet<ItemsetSupport>();
		HashSet<Integer> TmpItemSet = new HashSet<Integer>();
		miningSubTree(FPTree, HTable, TmpItemSet);
//		showFrequentItemsets();
//		System.out.println("FrequentItemsets size : " + FrequentItemsets.size());
	}

	private void miningSubTree(FrequentPatternTree Tree, HeaderTable HT, HashSet<Integer> ItemSet) {
		if (HT.length == 0) {
			return;
		}

//		System.out.println("HeaderTable size : " + HT.length);
		FrequentPatternTree tmpTree = null; // recursive variable
		HeaderTable tmpHT = null; // recursive variable
		FPNode[] tmpHTrearPointer = null;
		FPNode tmpFPNode = null;
		FPNode tmpPointer = null;
		HashSet<Integer> tmpItemSet = null; // recursive variable
		HashSet<Integer> tmpFreqItemSet = null;
		HashMap<Integer, Integer> ItemCount = null;
		HashMap<Integer, Integer> tmpItemMap = null;
		// ArrayList<Integer> tmpFreqItemList = null;
		ItemsetSupport tmpItemsetSup = null;
		int Count;
		int Index;
		for (int i = 0; i < HT.length; i++) {
//			System.out.println("i:" + i + " for item : " + HT.HTNodeArr[i].item);
			tmpFPNode = HT.HTNodeArr[i].nextFP;
			if (tmpFPNode == null)
				System.out.println("error");
			Count = 0;
			tmpItemSet = new HashSet<Integer>(ItemSet);
			tmpItemSet.add(tmpFPNode.item);
			ItemCount = new HashMap<Integer, Integer>();
			while (tmpFPNode != null) {
				Count += tmpFPNode.count;
				tmpPointer = tmpFPNode.parent;
				while (tmpPointer.item != -1) {
					if (ItemCount.get(tmpPointer.item) != null)
						ItemCount.put(tmpPointer.item, ItemCount.get(tmpPointer.item) + tmpFPNode.count);
					else
						ItemCount.put(tmpPointer.item, tmpFPNode.count);
					tmpPointer = tmpPointer.parent;
				}
				tmpFPNode = tmpFPNode.nextFP;
			}
			tmpItemsetSup = new ItemsetSupport(tmpItemSet, Count);
			FrequentItemsets.add(tmpItemsetSup);
			tmpFreqItemSet = new HashSet<Integer>();
			for (int item : ItemCount.keySet()) {
				if (ItemCount.get(item) >= min_sup)
					tmpFreqItemSet.add(item);
			}
			if(tmpFreqItemSet.size() == 0)
				continue;
			ItemCount = null;

			// create new sub-FPtree and HeaderTable
			tmpFPNode = HT.HTNodeArr[i].nextFP;
			// tmpFreqItemList = new ArrayList<Integer>(tmpFreqItemSet);
			// initial HeaderTable and buildtmpItemMap
			// this version is non-order for HeaderTable
			tmpHT = new HeaderTable(tmpFreqItemSet.size());
			tmpItemMap = new HashMap<Integer, Integer>();
			Index = 0;
			for (int item : tmpFreqItemSet) {
				tmpHT.HTNodeArr[Index] = new HTNode(item);
				tmpItemMap.put(item, Index);
				Index += 1;
			}
			tmpHTrearPointer = new FPNode[tmpHT.length];
			for (int j = 0; j < tmpHTrearPointer.length; j++) {
				tmpHTrearPointer[j] = null;
			}
			FPNode tmpNewFPNode = null;
			FPNode tmpInsertPointer = null;
			FPNode tmpSearchPointer = null;
			tmpTree = new FrequentPatternTree();
			while (tmpFPNode != null) {
				// System.out.println("bulid sub-branch & sub-tree...");
				// new a sub-branch
				tmpPointer = tmpFPNode.parent;
				while (tmpPointer.item != -1 && !(tmpFreqItemSet.contains(tmpPointer.item))) {
					tmpPointer = tmpPointer.parent;
				}

				if (tmpPointer.item == -1) { // this branch has no frequent pattern list
					tmpFPNode = tmpFPNode.nextFP;
					continue;
				}

				tmpNewFPNode = new FPNode();
				tmpNewFPNode.item = tmpPointer.item;
				tmpNewFPNode.count = tmpFPNode.count;

				tmpPointer = tmpPointer.parent;
				while (tmpPointer.item != -1) {
					while (tmpPointer.item != -1 && !(tmpFreqItemSet.contains(tmpPointer.item))) {
						tmpPointer = tmpPointer.parent;
					}
					if (tmpPointer.item == -1)
						break;
					tmpNewFPNode.parent = new FPNode();
					tmpNewFPNode.parent.firstchild = tmpNewFPNode;
					tmpNewFPNode = tmpNewFPNode.parent;
					tmpNewFPNode.item = tmpPointer.item;
					tmpNewFPNode.count = tmpFPNode.count;
					tmpPointer = tmpPointer.parent;
				}
				// add the sub-branch to subTree & link tmpHeaderTable to the
				// branch
				tmpInsertPointer = tmpTree.root;
				while (tmpNewFPNode != null) {
					// if the insert root is leaf
					if (tmpInsertPointer.firstchild == null) {
						tmpInsertPointer.firstchild = tmpNewFPNode;
						tmpNewFPNode.parent = tmpInsertPointer;
						while (tmpNewFPNode != null) {
							Index = tmpItemMap.get(tmpNewFPNode.item);
							if (tmpHT.HTNodeArr[Index].nextFP == null) {
								tmpHT.HTNodeArr[Index].nextFP = tmpNewFPNode;
								tmpHTrearPointer[Index] = tmpNewFPNode;
							} else {
								tmpHTrearPointer[Index].nextFP = tmpNewFPNode;
								tmpHTrearPointer[Index] = tmpNewFPNode;
							}
							tmpNewFPNode = tmpNewFPNode.firstchild;
						}
						break;
					} else {
						// search the fit node
						// the fit node is firstchild
						tmpSearchPointer = tmpInsertPointer.firstchild;
						if (tmpSearchPointer.item == tmpNewFPNode.item) {
							tmpSearchPointer.count += tmpNewFPNode.count;
							tmpNewFPNode = tmpNewFPNode.firstchild;
							tmpInsertPointer = tmpSearchPointer;
							continue;
						}
						// the fit node is not firstchild
						while (tmpSearchPointer.next != null) {
							if (tmpSearchPointer.next.item == tmpNewFPNode.item)
								break;
							tmpSearchPointer = tmpSearchPointer.next;
						}
						// if no fit node
						if (tmpSearchPointer.next == null) {
							tmpSearchPointer.next = tmpNewFPNode;
							tmpNewFPNode.parent = tmpInsertPointer;
							while (tmpNewFPNode != null) {
								Index = tmpItemMap.get(tmpNewFPNode.item);
								if (tmpHT.HTNodeArr[Index].nextFP == null) {
									tmpHT.HTNodeArr[Index].nextFP = tmpNewFPNode;
									tmpHTrearPointer[Index] = tmpNewFPNode;
								} else {
									tmpHTrearPointer[Index].nextFP = tmpNewFPNode;
									tmpHTrearPointer[Index] = tmpNewFPNode;
								}
								tmpNewFPNode = tmpNewFPNode.firstchild;
							}
							break;
						}

						// there is fit node
						tmpSearchPointer.next.count += tmpNewFPNode.count;
						tmpNewFPNode = tmpNewFPNode.firstchild;
						tmpInsertPointer = tmpSearchPointer.next;
						continue;
					}
				}
				// update tmpFPNode
				tmpFPNode = tmpFPNode.nextFP;
			}
			miningSubTree(tmpTree, tmpHT, tmpItemSet);
		}

	}

	private void showFrequentItemsets() {
		System.out.println("FrequentItemsets size : " + FrequentItemsets.size());
		for (ItemsetSupport IS : FrequentItemsets) {
			IS.show();
		}
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
	
	private class FrequentPatternTree {
		public FPNode root;

		public FrequentPatternTree() {
			root = new FPNode();
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

		public HeaderTable(ArrayList<ItemSup> FT1) {
			HTNodeArr = new HTNode[FT1.size()];
			for (int i = 0; i < FT1.size(); i++) {
				HTNodeArr[i] = new HTNode(FT1.get(i).item);
			}
			length = HTNodeArr.length;
		}

		public HTNode getNodeOfItemNum(int itemNum) {
			return HTNodeArr[OrderItemMap.get(itemNum)];
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
	}

	private class HTNode {
		public int item;
		public FPNode nextFP;

		public HTNode() {
			item = -1;
			nextFP = null;
		}

		public HTNode(int it) {
			item = it;
			nextFP = null;
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
	}

	private class ItemSupComparator implements Comparator<ItemSup> {
		@Override
		public int compare(ItemSup i1, ItemSup i2) {
			if (i1.supper == i2.supper)
				return i2.item - i1.item;
			return i1.supper - i2.supper;
		}
	}

	private class ItemMapComparator implements Comparator<Integer> {
		HashMap<Integer, Integer> CompareMap;

		public ItemMapComparator() {
			CompareMap = null;
		}

		public ItemMapComparator(HashMap<Integer, Integer> IMap) {
			CompareMap = IMap;
		}

		// this design is for decreasing sort
		@Override
		public int compare(Integer i1, Integer i2) {
			return CompareMap.get(i2) - CompareMap.get(i1);
		}
	}
}
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashSet<Integer> s1 = new HashSet<Integer>();
		s1.add(1); s1.add(2); s1.add(3); s1.add(4); s1.add(5);
		
		HashSet<Integer> s2 = new HashSet<Integer>(s1);
		
		s2.remove(2);
		
		HashSet<HashSet<Integer>> S  = new HashSet<HashSet<Integer>>();
		S.add(s1);
		S.add(s2);
		
//		HashSet<Integer> s3 = new HashSet<Integer>(s2);
//		
//		if(S.contains(s3)) {
//			System.out.println("S contain s3");
//		}
		
		HashSet<HashSet<Integer>> SS1 = new HashSet<HashSet<Integer>>();
		HashSet<Integer> tmps1 = new HashSet<Integer>(s1);
		HashSet<Integer> tmpSet = null;
		Iterator<Integer> IterS1 = s1.iterator();
		int tmpInt;
		for(int item : tmps1) {
			s1.remove(item);
			tmpSet = new HashSet<Integer>(s1);
			SS1.add(tmpSet);
			s1.add(item);
		}
		
		for (HashSet<Integer> ItS : SS1) {
			System.out.print("[ ");
			for (int i : ItS) {
				System.out.print(i + "  ");
			}
			System.out.println("]");
		}
		
		System.out.println("done");
		System.out.println("--------------------------------");
		
		int[] arr = new int[100];
		for (int i=0; i<arr.length; i++) {
			System.out.print(arr[i] + " ");
		}
		System.out.print("\n");
		System.out.println("--------------------------------");
	}

}
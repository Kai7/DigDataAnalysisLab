import java.io.IOException;

public class test {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String current = "";
		try {
			current = new java.io.File(".").getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Current dir:" + current);
		String currentDir = System.getProperty("user.dir");
		System.out.println("Current dir using System:" + currentDir);
		
//		boolean[] tmpB = new boolean[100];
//		for(int i=0; i<tmpB.length; i++){
//			if(tmpB[i])
//				System.out.println(i+":true");
//			else 
//				System.out.println(i+":false");
//		}
		
		int[] tmpI = new int[100];
		for(int i=0; i<tmpI.length; i++){
			System.out.println(i);
		}
	}

}

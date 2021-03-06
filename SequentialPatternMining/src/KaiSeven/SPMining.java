package KaiSeven;

public class SPMining {

	/**
	 * @param args
	 */
	private static double MINSUP_RATIO = 0.5;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inDataPath = "/home/kaichi/Documents/BDALab/SequentialPatternMining/SPMiningDataSet/C50S10T2.5N10000.ascii"; 
		
//		double minsup_ratio;
//		for(int i=1; i>=1; i--){
//			minsup_ratio = (double)i/1000;
//			PrefixSpan PSMiner = new PrefixSpan(inDataPath,inDataPath +".Apriori.minsup-" + minsup_ratio + ".result", (double)i/1000);
//			PSMiner.mining();
//		}
		
		int[] minsups = {50, 100, 500, 1000};
		int minsup;
		for(int i=0; i< minsups.length; i++){
			minsup = minsups[i];
			PrefixSpan PSMiner = new PrefixSpan(inDataPath,inDataPath +".PrefixSpan.minsup-" + minsup + ".result", minsup);
			PSMiner.mining();
		}
		
		
//		String inDataPath = "/home/kaichi/Documents/Simple.ascii", outDataPath = "/home/kaichi/Documents/Simple_miningResult.txt";
//		PrefixSpan PSMiner = new PrefixSpan(inDataPath, outDataPath, MINSUP_RATIO);
//		PSMiner.mining();
		
		System.out.println("------------------------------------------------------\ndone");
	}

}

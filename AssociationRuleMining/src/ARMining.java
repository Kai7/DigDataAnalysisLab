import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ARMining {

	private static boolean INTERACTIVE = false;

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	// private static int MINSUP = 10;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		KaiSeven.FPGrowth FPGrowthMiner;
		KaiSeven.Apriori AprioriMiner;
		String inDataPath = "";
		String outDataPath = "";
		double minsup_ratio = 0.0;

		if (INTERACTIVE) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			if (args.length == 0) {
				String currentPath = "";
				try {
					currentPath = new java.io.File(".").getCanonicalPath();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Error : Can't get current working directory !");
					System.exit(0);
				}

				System.out.println("No arguments, you can use the command : ");
				System.out.println("    java -jar ARMining.jar [DataSet_Path] [MinSup_Ratio(%)] ");
				System.out.println("example :");
				System.out.println("    java -jar ARMining.jar /home/user/data-dir/dataset.txt 0.5");
				System.out.println("to config your input data and minimal support.");
				System.out.println(ANSI_GREEN + "+Input+++++++++++++++++++++++++++++++++++++++++++++++" + ANSI_RESET);
				System.out.println(" 1) Simple.txt");
				System.out.println(" 2) D1kT10N500.txt");
				System.out.println(" 3) D10kT10N1k.txt");
				System.out.println(" 4) D100kT10N1k.txt");
				System.out.println(" 5) Mushroom.txt");
				System.out.print("choose a dataset file (1-5 or 0 to exit) : ");
				int numFile = -1;
				try {
					numFile = Integer.parseInt(br.readLine());
				} catch (NumberFormatException | IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
				switch (numFile) {
				case 0:
					System.exit(0);
				case 1:
					inDataPath = currentPath + "/ARMiningDataSet/Simple.txt";
					break;
				case 2:
					inDataPath = currentPath + "/ARMiningDataSet/D1kT10N500.txt";
					break;
				case 3:
					inDataPath = currentPath + "/ARMiningDataSet/D10kT10N1k.txt";
					break;
				case 4:
					inDataPath = currentPath + "/ARMiningDataSet/D100kT10N1k.txt";
					break;
				case 5:
					inDataPath = currentPath + "/ARMiningDataSet/Mushroom.txt";
					break;
				default:
					System.out.println("Unknown file ! (please choose 1-5)");
					System.exit(0);
				}

				System.out.print("minimal support ratio(%) (0-100): ");
				try {
					minsup_ratio = Double.parseDouble(br.readLine()) / 100;
				} catch (NumberFormatException | IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
				if (!isSuitableSupport(minsup_ratio)) {
					System.out.print("support is not suitable.");
					System.exit(0);
				}
			} else if (args.length == 2) {
				inDataPath = args[0];
				minsup_ratio = Double.parseDouble(args[1]) / 100;
				if (!isSuitableSupport(minsup_ratio)) {
					System.out.print("support is not suitable.");
					System.exit(0);
				}
			} else {
				System.out.println("Unknows arguments");
				System.out.println("Example : java -jar ARMining [DataSet] [MinSup_Ratio]");
				System.exit(0);
			}

			System.out.println(ANSI_GREEN + "+Input+++++++++++++++++++++++++++++++++++++++++++++++" + ANSI_RESET);
			System.out.println(" 1) Apriori");
			System.out.println(" 2) FPGrowth");
			System.out.println(" 3) both two");
			System.out.print("Choose a method (1-3) : ");
			
			int numAlgo = -1;
			try {
				numAlgo = Integer.parseInt(br.readLine());
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
			System.out.println();
			switch (numAlgo) {
			case 1:
				AprioriMiner = new KaiSeven.Apriori(inDataPath, inDataPath + ".Apriori.minsup-" + minsup_ratio + ".result", minsup_ratio);
				break;
			case 2:
				FPGrowthMiner = new KaiSeven.FPGrowth(inDataPath, inDataPath + ".FPGrowth.minsup-" + minsup_ratio + ".result", minsup_ratio);
				break;
			case 3:
				AprioriMiner = new KaiSeven.Apriori(inDataPath, inDataPath + ".Apriori.minsup-" + minsup_ratio + ".result", minsup_ratio);
				FPGrowthMiner = new KaiSeven.FPGrowth(inDataPath, inDataPath + ".FPGrowth.minsup-" + minsup_ratio + ".result", minsup_ratio);
				break;
			default:
				System.out.println("Unknown algorithm ! (please choose 1-3)");
				System.exit(0);
			}
		} else {
			String currentPath = "";
			try {
				currentPath = new java.io.File(".").getCanonicalPath();
			} catch (IOException e) {
				System.out.println("Error : Can't get current working directory !");
				System.exit(0);
			}

			inDataPath = currentPath + "/ARMiningDataSet/D1kT10N500.txt";
			inDataPath = currentPath + "/ARMiningDataSet/D10kT10N1k.txt";
			inDataPath = currentPath + "/ARMiningDataSet/D100kT10N1k.txt";

			// inDataPath = currentPath + "/ARMiningDataSet/Simple.txt";

			for (int i = 10; i >= 1; i--) {
				minsup_ratio = (double) i / 1000;
				// FPGrowthAlphaMethod(inDataPath, (double) i / 1000);
				AprioriMiner = new KaiSeven.Apriori(inDataPath, inDataPath +".Apriori.minsup-" + minsup_ratio + ".result", minsup_ratio);
//				FPGrowthMiner = new KaiSeven.FPGrowth(inDataPath, inDataPath + ".FPGrowth.minsup-" + minsup_ratio + ".result", minsup_ratio);
			}
		}
	}

	private static boolean isSuitableSupport(double sup) {
		if (sup >= 1)
			return false;
		else if (sup <= 0)
			return false;
		else
			return true;
	}

}

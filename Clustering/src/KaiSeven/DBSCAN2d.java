package KaiSeven;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.JPanel;

public class DBSCAN2d {
	private static boolean WRITE_TO_FILE = true;

	public double Eps;
	public double Epspow2;
	public int minPts;
	public Graph2d graph2d;

	public DBSCAN2d(String inDataPath, String outDataPath, double eps, int minpts) {
		Scanner dataInput = null;
		try {
			dataInput = new Scanner(new FileInputStream(inDataPath));
		} catch (FileNotFoundException e) {
			System.out.println("input file error!");
			System.exit(0);
		}
		Eps = eps;
		Epspow2 = Math.pow(Eps, 2);
		minPts = minpts;

		LinkedList<Point2d> tmpPoint2dList = new LinkedList<Point2d>();
		Point2d tmpPoint2d;
		double tmpx, tmpy;

		StringTokenizer tokenizer;
		tokenizer = new StringTokenizer(dataInput.nextLine());
		tmpx = Double.parseDouble(tokenizer.nextToken());
		tmpy = Double.parseDouble(tokenizer.nextToken());
		tmpPoint2d = new Point2d(tmpx, tmpy);
		tmpPoint2dList.add(tmpPoint2d);
		while (dataInput.hasNext()) {
			tokenizer = new StringTokenizer(dataInput.nextLine());
			tmpx = Double.parseDouble(tokenizer.nextToken());
			tmpy = Double.parseDouble(tokenizer.nextToken());
			tmpPoint2d = new Point2d(tmpx, tmpy);

			Iterator<Point2d> tmpListIterator = tmpPoint2dList.listIterator();
			while (tmpListIterator.hasNext()) {
				Point2d tmpOtherPoint2d = tmpListIterator.next();
				determineNeighbor(tmpPoint2d, tmpOtherPoint2d);
			}

			tmpPoint2dList.add(tmpPoint2d);
		}

		graph2d = new Graph2d(tmpPoint2dList);

		graph2d.cluster();

		if (WRITE_TO_FILE) {
			PrintWriter dataOutputWriter = null;
			try {
				dataOutputWriter = new PrintWriter(outDataPath, "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				System.out.println("output file error!");
				System.exit(0);
			}
			for (int i = 0; i < graph2d.point2ds.length; i++) {
				Point2d ptrPoint2d = graph2d.point2ds[i];
				dataOutputWriter.println(ptrPoint2d.x + "\t" + ptrPoint2d.y + "\t" + ptrPoint2d.cluster);
			}
			dataOutputWriter.close();
		}
	}

	private void determineNeighbor(Point2d p1, Point2d p2) {
		if (Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2) <= Epspow2) {
			p1.neighborPoints.add(p2);
			p2.neighborPoints.add(p1);
		}
		return;
	}

	class Graph2d {
		public Point2d[] point2ds;

		public Graph2d(LinkedList<Point2d> Point2dList) {

			point2ds = new Point2d[Point2dList.size()];
			LinkedList<Integer> tmpNonCorePointIndexList = new LinkedList<Integer>();
			for (int i = 0; i < point2ds.length; i++) {
				point2ds[i] = Point2dList.remove();
				if (point2ds[i].neighborPoints.size() >= minPts) {
					point2ds[i].flagClass = 'c';
				} else {
					tmpNonCorePointIndexList.add(i);
				}
			}

			while (tmpNonCorePointIndexList.size() > 0) {
				int index = tmpNonCorePointIndexList.remove();
				LinkedList<Point2d> neighbors = point2ds[index].neighborPoints;
				Iterator<Point2d> tmpPoint2dIterator = neighbors.listIterator();
				while (tmpPoint2dIterator.hasNext()) {
					if (tmpPoint2dIterator.next().flagClass == 'c') {
						point2ds[index].flagClass = 'b';
						break;
					}
				}
				if (point2ds[index].flagClass == 'u') {
					point2ds[index].flagClass = 'n';
				}
			}

			// for (int i = 0; i < point2ds.length; i++) {
			// System.out.println("(" + point2ds[i].x + "," + point2ds[i].y +
			// "):" + point2ds[i].flagClass);
			// Iterator<Point2d> ptrTmpPoint2dList =
			// point2ds[i].neighborPoints.listIterator();
			// while (ptrTmpPoint2dList.hasNext()) {
			// Point2d ptrTmpPoint2d = ptrTmpPoint2dList.next();
			// System.out.print("(" + ptrTmpPoint2d.x + "," + ptrTmpPoint2d.y +
			// ") , ");
			// }
			// System.out.println();
			// System.out.println("--------------------");
			// }

		}

		// private void cluster() {
		// int countCluster = 0;
		// for (int i = 0; i < point2ds.length; i++) {
		// if (point2ds[i].flagVisit)
		// continue;
		// if (point2ds[i].flagClass == 'n')
		// continue;
		// countCluster += 1;
		// point2ds[i].cluster = countCluster;
		// point2ds[i].flagVisit = true;
		// LinkedList<Point2d> visitList = new LinkedList<Point2d>();
		// if (point2ds[i].flagClass == 'c') {
		// visitList.add(point2ds[i]);
		// while (visitList.size() > 0) {
		// Point2d tmpPoint2d = visitList.remove();
		// Iterator<Point2d> tmpPoint2dIterator =
		// tmpPoint2d.neighborPoints.listIterator();
		// while (tmpPoint2dIterator.hasNext()) {
		// // add c to list
		// Point2d visitPoint2d = tmpPoint2dIterator.next();
		// if (visitPoint2d.flagClass == 'n' || visitPoint2d.flagVisit)
		// continue;
		// visitPoint2d.cluster = countCluster;
		// visitPoint2d.flagVisit = true;
		// if (visitPoint2d.flagClass == 'c')
		// visitList.add(visitPoint2d);
		// }
		// }
		// } else {
		// Iterator<Point2d> tmpPoint2dIterator =
		// point2ds[i].neighborPoints.listIterator();
		// while (tmpPoint2dIterator.hasNext()) {
		// Point2d visitPoint2d = tmpPoint2dIterator.next();
		// if (visitPoint2d.flagClass == 'c') {
		// visitPoint2d.cluster = countCluster;
		// visitPoint2d.flagVisit = true;
		// visitList.add(visitPoint2d);
		// break;
		// }
		// }
		// if (visitList.size() == 0) {
		// System.out.println("Error: visitList.size() == 0");
		// System.exit(0);
		// }
		// while (visitList.size() > 0) {
		// Point2d tmpPoint2d = visitList.remove();
		// tmpPoint2dIterator = tmpPoint2d.neighborPoints.listIterator();
		// while (tmpPoint2dIterator.hasNext()) {
		// // add c to list
		// Point2d visitPoint2d = tmpPoint2dIterator.next();
		// if (visitPoint2d.flagClass == 'n' || visitPoint2d.flagVisit)
		// continue;
		// visitPoint2d.cluster = countCluster;
		// visitPoint2d.flagVisit = true;
		// if (visitPoint2d.flagClass == 'c')
		// visitList.add(visitPoint2d);
		// }
		// }
		// }
		// }
		// }

		private void cluster() {
			int countCluster = 0;
			for (int i = 0; i < point2ds.length; i++) {
				if (point2ds[i].flagVisit)
					continue;
				if (point2ds[i].flagClass == 'n' || point2ds[i].flagClass == 'b')
					continue;
				countCluster += 1;
				point2ds[i].cluster = countCluster;
				point2ds[i].flagVisit = true;
				LinkedList<Point2d> visitList = new LinkedList<Point2d>();

				visitList.add(point2ds[i]);
				while (visitList.size() > 0) {
					Point2d tmpPoint2d = visitList.remove();
					Iterator<Point2d> tmpPoint2dIterator = tmpPoint2d.neighborPoints.listIterator();
					while (tmpPoint2dIterator.hasNext()) {
						// add c to list
						Point2d visitPoint2d = tmpPoint2dIterator.next();
						if (visitPoint2d.flagClass == 'n' || visitPoint2d.flagVisit)
							continue;
						visitPoint2d.cluster = countCluster;
						visitPoint2d.flagVisit = true;
						if (visitPoint2d.flagClass == 'c')
							visitList.add(visitPoint2d);
					}
				}

			}
		}

		public void draw() {
			return;
		}
	}

	public class Point2d {
		public char flagClass; // u: unknown ; c: core ; b: border ; n: noise
		public boolean flagVisit;
		public int cluster;
		public double x, y;
		public LinkedList<Point2d> neighborPoints;

		public Point2d(double tmpx, double tmpy) {
			flagClass = 'u';
			flagVisit = false;
			cluster = 0;
			x = tmpx;
			y = tmpy;
			neighborPoints = new LinkedList<Point2d>();
		}
	}

	public class CorePoint2d extends Point2d {

		public CorePoint2d(double tmpx, double tmpy) {
			super(tmpx, tmpy);
		}

	}
}

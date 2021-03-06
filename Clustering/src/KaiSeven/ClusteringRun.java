package KaiSeven;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class ClusteringRun  extends ApplicationFrame{
	
	public static void main(String[] args)  {
		DBSCAN2d DBSCANCluster;
		DBSCAN dbscan;

		String inDataPath = "";

		String currentPath = "";
		try {
			currentPath = new java.io.File(".").getCanonicalPath();
		} catch (IOException e) {
			System.out.println("Error : Can't get current working directory !");
			System.exit(0);
		}

		inDataPath = currentPath + "/DataSet/clustering_test.txt";
//		inDataPath = currentPath + "/DataSet/Simple.txt";


		double eps = 3;
		int minpts = 15;


//		DBSCANCluster = new DBSCAN2d(inDataPath, inDataPath + ".DBSCAN.eps-" + eps + ".minpts-" + minpts + ".result", eps, minpts);
		DBSCANCluster = new DBSCAN2d(inDataPath, inDataPath + ".DBSCAN.result", eps, minpts);
		dbscan = new DBSCAN(inDataPath, inDataPath + ".NewDBSCAN.result", eps, minpts);
		
		
		Point2d[] points = new Point2d[DBSCANCluster.graph2d.point2ds.length];
		
		
		String type = "";
		
//		ClusteringRun demo = new ClusteringRun("Clustering Result", points, type);
//		demo.pack();
//		RefineryUtilities.centerFrameOnScreen(demo);
//		demo.setVisible(true);
//		
		System.out.println("------------------------------------------------------\ndone");
	}
	
	public ClusteringRun(String title, Point2d[] points, String type) {
		super(title);
		JPanel chartPanel = createDemoPanel(points, type);
		chartPanel.setPreferredSize(new java.awt.Dimension(550, 350));
		setContentPane(chartPanel);
	}
	
	private static XYDataset createData(Point2d[] points, String type) {
		HashMap<Integer, HashSet<Point2d>> pointsSetMap = new HashMap<Integer, HashSet<Point2d>>();
		
		
		XYSeriesCollection my_data_series = new XYSeriesCollection();
		for(int c: pointsSetMap.keySet()){
			XYSeries s = new XYSeries("Cluster " + c);
			for(Point2d p: pointsSetMap.get(c)){
				s.add(p.x, p.y);
			}
			my_data_series.addSeries(s);
		}

		return my_data_series;
	}

	private static JFreeChart createChart(XYDataset dataset) {
		JFreeChart chart = ChartFactory.createScatterPlot("Scatter Plot Demo 1", "X", "Y", dataset, PlotOrientation.VERTICAL, true, false, false);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setNoDataMessage("NO DATA");
		plot.setDomainZeroBaselineVisible(true);
		plot.setRangeZeroBaselineVisible(true);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setSeriesOutlinePaint(0, Color.black);
		renderer.setUseOutlinePaint(true);
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(false);
		domainAxis.setTickMarkInsideLength(2.0f);
		domainAxis.setTickMarkOutsideLength(0.0f);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setTickMarkInsideLength(2.0f);
		rangeAxis.setTickMarkOutsideLength(0.0f);

		return chart;
	}

	/**
	 * Creates a panel for the demo (used by SuperDemo.java).
	 * 
	 * @return A panel.
	 */
	public static JPanel createDemoPanel(Point2d[] points, String type) {
		JFreeChart chart = createChart(createData(points, type));
		ChartPanel chartPanel = new ChartPanel(chart);
		// chartPanel.setVerticalAxisTrace(true);
		// chartPanel.setHorizontalAxisTrace(true);
		// popup menu conflicts with axis trace
		chartPanel.setPopupMenu(null);

		chartPanel.setDomainZoomable(true);
		chartPanel.setRangeZoomable(true);
		return chartPanel;
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *            ignored.
	 */
	
	public static class Point2d {
		char flagClass; // u: unknown ; c: core ; b: border ; n: noise
		int cluster;
		public double x, y;
		public LinkedList<Point2d> neighborPoints;

		public Point2d(double tmpx, double tmpy) {
			flagClass = 'u';
			cluster = 0;
			x = tmpx;
			y = tmpy;
			neighborPoints = new LinkedList<Point2d>();
		}
		
	}
}

package KaiSeven;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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

public class ClusteringDraw extends ApplicationFrame {

	/**
	 * A demonstration application showing a scatter plot.
	 * 
	 * @param title
	 *            the frame title.
	 */
	public ClusteringDraw(String title, String inDataPath) {
		super(title);
		JPanel chartPanel = createDemoPanel(inDataPath);
		chartPanel.setPreferredSize(new java.awt.Dimension(550, 350));
		setContentPane(chartPanel);
	}

	private static XYDataset createData(String inDataPath) {
		Scanner dataInput = null;
		try {
			dataInput = new Scanner(new FileInputStream(inDataPath));
		} catch (FileNotFoundException e) {
			System.out.println("input file error!");
			System.out.println(inDataPath+ " is not found!");
			System.exit(0);
		}
		
		HashMap<Integer, HashSet<Point2d>> pointsSetMap = new HashMap<Integer, HashSet<Point2d>>();
		while (dataInput.hasNext()) {
			StringTokenizer tokenizer = new StringTokenizer(dataInput.nextLine());
			double x = Double.parseDouble(tokenizer.nextToken());
			double y = Double.parseDouble(tokenizer.nextToken());
			Point2d tmpPoint = new Point2d(x, y);
			int c = Integer.parseInt(tokenizer.nextToken());
			if(pointsSetMap.get(c) == null){
				HashSet<Point2d> tmpPointsSet = new HashSet<Point2d>();
				pointsSetMap.put(c, tmpPointsSet);
			}
			pointsSetMap.get(c).add(tmpPoint);
		}
		dataInput.close();
		
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
	public static JPanel createDemoPanel(String inDataPath) {
		JFreeChart chart = createChart(createData(inDataPath));
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
	public static void main(String[] args) {
		String inDataPath = "";

		String currentPath = "";
		try {
			currentPath = new java.io.File(".").getCanonicalPath();
		} catch (IOException e) {
			System.out.println("Error : Can't get current working directory !");
			System.exit(0);
		}

//		inDataPath = currentPath + "/DataSet/clustering_test.txt.NewDBSCAN.result";
//		ClusteringDraw demo = new ClusteringDraw("Clustering Result", inDataPath);
//		demo.pack();
//		RefineryUtilities.centerFrameOnScreen(demo);
//		demo.setVisible(true);
//		
		inDataPath = currentPath + "/DataSet/clustering_test.txt.DBSCAN.result";
		ClusteringDraw demo2 = new ClusteringDraw("Clustering Result", inDataPath);
		demo2.pack();
		RefineryUtilities.centerFrameOnScreen(demo2);
		demo2.setVisible(true);
		
		inDataPath = currentPath + "/DataSet/clustering_test.txt.rewrite.result.rewrite";
		ClusteringDraw demo3 = new ClusteringDraw("Clustering Result", inDataPath);
		demo3.pack();
		RefineryUtilities.centerFrameOnScreen(demo3);
		demo3.setVisible(true);
		
	}

	private static class Point2d {
		public double x;
		public double y;

		public Point2d() {
			x = 0;
			y = 0;
		}

		public Point2d(double xx, double yy) {
			x = xx;
			y = yy;
		}
	}
}
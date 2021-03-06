package KaiSeven;

import java.util.HashSet;

public class Point {
	public double x;
	public double y;
	public HashSet<Point> neighborPoints;

	public Point() {
		x = 0.0;
		y = 0.0;
		neighborPoints = new HashSet<Point>();
	}

	public Point(double xx, double yy) {
		x = xx;
		y = yy;
		neighborPoints = new HashSet<Point>();
	}

	public void addNeighbor(Point otherPoint) {
		neighborPoints.add(otherPoint);
	}

	public static double sqrtDistanceOf(Point p1, Point p2) {
		return Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2);
	}
}

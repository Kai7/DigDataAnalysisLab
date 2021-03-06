package datastructure.kdTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import datapattern.DataVector;

import functions.distanceFunction.DistanceEuclidean;
import functions.distanceFunction.DistanceFunction;

public class KDTree {
	private int countNode = 0;
	private KDNode root = null;
	private int countDimension = 0;

	private Random randomGen = new Random(System.currentTimeMillis());

	DistanceFunction disCalculator = new DistanceEuclidean();

	public KDTree() {

	}

	public int size() {
		return countNode;
	}

	public void buildTree(List<DataVector> dataVectors) {
		if (dataVectors.size() == 0) {
			return;
		}
		countDimension = dataVectors.get(0).size();

		root = generateNode(0, dataVectors, 0, dataVectors.size() - 1);
	}

	private KDNode generateNode(int currentD, List<DataVector> dataVectors, int start, int end) {
		if (start > end) {
			return null;
		}

		countNode++;
		if (start == end) {
			return new KDNode(dataVectors.get(start), currentD);
		}

		int mid = (end - start + 1) / 2;
		DataVector midDataVector = randomizeSelect(dataVectors, mid, start, end, currentD);
		KDNode genKDNode = new KDNode(midDataVector, currentD);
		currentD++;
		if (currentD == countDimension) {
			currentD = 0;
		}

		genKDNode.left = generateNode(currentD, dataVectors, start, start + mid - 1);
		genKDNode.right = generateNode(currentD, dataVectors, start + mid + 1, end);

		return genKDNode;
	}

	private DataVector randomizeSelect(List<DataVector> dataVectors, int rank, int start, int end, int currentD) {
		while (true) {
			if (start == end) {
				return dataVectors.get(start);
			}

			int r = randomizePartition(dataVectors, start, end, currentD) - start + 1;

			if (r == rank) {
				return dataVectors.get(start + r - 1);
			} else if (r > rank) {
				end = start + r - 2;
			} else {
				rank = rank - r;
				start = start + r;
			}
		}
	}

	private int randomizePartition(List<DataVector> dataVectors, int start, int end, int currentD) {
		int i = randomGen.nextInt(end - start);
		swap(dataVectors, i, end);

		DataVector pivotDataVector = dataVectors.get(end);
		int f = start - 1;
		for (int r = f + 1; r < end - 1; r++) {
			if (dataVectors.get(r).getValueOf(currentD) <= pivotDataVector.getValueOf(currentD)) {
				f++;
				swap(dataVectors, f, r);
			}
		}
		f++;
		swap(dataVectors, f, end);

		return f;
	}

	private void swap(List<DataVector> dataVectors, int ii, int jj) {
		DataVector tmpDataVector = dataVectors.get(ii);
		dataVectors.set(ii, dataVectors.get(jj));
		dataVectors.set(jj, tmpDataVector);
	}

	public DataVector getNearest(DataVector targetPoint) {
		if (root == null) {
			return null;
		}

		Stack<KDNode> traceBackKDNodes = new Stack<KDNode>();
		KDNode ptrSearchKDNode = root;
		while (ptrSearchKDNode != null) {
			traceBackKDNodes.push(ptrSearchKDNode);
			int d = ptrSearchKDNode.splitD;
			if (targetPoint.data[d] <= ptrSearchKDNode.vector.data[d]) {
				ptrSearchKDNode = ptrSearchKDNode.left;
			} else {
				ptrSearchKDNode = ptrSearchKDNode.right;
			}
		}

		DataVector nearestPoint = traceBackKDNodes.lastElement().vector;
		double minDistance = disCalculator.calculateDistance(targetPoint, nearestPoint);

		while (!traceBackKDNodes.isEmpty()) {
			KDNode traceKDNode = traceBackKDNodes.pop();
			double tmpDistance = disCalculator.calculateDistance(targetPoint, traceKDNode.vector);
			if (tmpDistance < minDistance) {
				nearestPoint = traceKDNode.vector;
				minDistance = tmpDistance;
			}

			int sd = traceKDNode.splitD;
			double verticalDistance = Math.abs(targetPoint.data[sd] - traceKDNode.vector.data[sd]);
			if (verticalDistance >= minDistance) {
				continue;
			}
			if (targetPoint.data[sd] <= traceKDNode.vector.data[sd]) {
				if (traceKDNode.right != null) {
					ptrSearchKDNode = traceKDNode.right;
					while (ptrSearchKDNode != null) {
						traceBackKDNodes.push(ptrSearchKDNode);
						int d = ptrSearchKDNode.splitD;
						if (targetPoint.data[d] <= ptrSearchKDNode.vector.data[d]) {
							ptrSearchKDNode = ptrSearchKDNode.left;
						} else {
							ptrSearchKDNode = ptrSearchKDNode.right;
						}
					}
					// traceBackKDNodes.push(traceKDNode.right);
				}
			} else {
				if (traceKDNode.left != null) {
					ptrSearchKDNode = traceKDNode.left;
					while (ptrSearchKDNode != null) {
						traceBackKDNodes.push(ptrSearchKDNode);
						int d = ptrSearchKDNode.splitD;
						if (targetPoint.data[d] <= ptrSearchKDNode.vector.data[d]) {
							ptrSearchKDNode = ptrSearchKDNode.left;
						} else {
							ptrSearchKDNode = ptrSearchKDNode.right;
						}
					}
					// traceBackKDNodes.push(traceKDNode.left);
				}
			}
		}

		return nearestPoint;
	}

	public List<DataVector> getVectorsWithinRadius(DataVector targetPoint, double radius) {
		if (root == null) {
			return null;
		}

		List<DataVector> result = new ArrayList<DataVector>();
		Stack<KDNode> traceKDNodes = new Stack<KDNode>();
		traceKDNodes.push(root);

		while (!traceKDNodes.isEmpty()) {
			KDNode traceKDNode = traceKDNodes.pop();
			double tmpDistance = disCalculator.calculateDistance(targetPoint, traceKDNode.vector);
			if (tmpDistance <= radius) {
				result.add(traceKDNode.vector);
			}

			int sd = traceKDNode.splitD;
			double verticalDistance = Math.abs(targetPoint.data[sd] - traceKDNode.vector.data[sd]);
			if (verticalDistance < radius) {
				if (traceKDNode.left != null) {
					traceKDNodes.push(traceKDNode.left);
				}
				if (traceKDNode.right != null) {
					traceKDNodes.push(traceKDNode.right);
				}
			} else {
				if (targetPoint.data[sd] <= targetPoint.data[sd]) {
					if (traceKDNode.left != null) {
						traceKDNodes.push(traceKDNode.left);
					}
				} else {
					if (traceKDNode.right != null) {
						traceKDNodes.push(traceKDNode.right);
					}
				}
			}
		}

		return result;
	}
}

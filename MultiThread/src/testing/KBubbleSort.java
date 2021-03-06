package testing;

public class KBubbleSort implements Runnable {
	public int[] data;
	public int front = -1;
	public int rear = -1;
	
	public KBubbleSort() {
		data = null;
	}

	public KBubbleSort(int[] data) {
		this.data = data;
	}

	@Override
	public void run() {
		if (data == null)
			return;
		for (int i = 0; i < data.length - 1; i++) {
			for (int j = i + 1; j < data.length; j++) {
				if (data[i] > data[j]) {
					int tmp = data[i];
					data[i] = data[j];
					data[j] = tmp;
				}
			}
		}
	}
}

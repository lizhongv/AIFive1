public class Evaluate {
	// 棋型权重
	private static final int FIVE = 50000;// 连五
	private static final int HUO_FOUR = 5000;// 活四
	private static final int CHONG_FOUR = 1000;// 冲四
	private static final int HUO_THREE = 500;// 活三
	private static final int MIAN_THREE = 100;// 眠三
	private static final int HUO_TWO = 50;// 活二

	// 位置权重
	private int[][] blackValue;// 保存每一空位下黑子的价值
	private int[][] whiteValue;// 保存每一空位下白子的价值
	private int[][] staticValue;// 保存每一点的位置价值

	private static final int LARGE_NUMBER = 10000000;
	private static final int SEARCH_DEPTH = 3;
	private static final int SAMPLE_NUMBER = 10;

	private ChessBoard cb;

	public Evaluate(ChessBoard cb) {
		this.cb = cb;
		blackValue = new int[ChessBoard.COLS + 1][ChessBoard.ROWS + 1];
		whiteValue = new int[ChessBoard.COLS + 1][ChessBoard.ROWS + 1];
		staticValue = new int[ChessBoard.COLS + 1][ChessBoard.ROWS + 1];
		for (int i = 0; i <= ChessBoard.COLS; i++) {
			for (int j = 0; j <= ChessBoard.ROWS; j++) {
				blackValue[i][j] = 0;
				whiteValue[i][j] = 0;
			}
		}
		for (int i = 0; i <= ChessBoard.COLS / 2; i++) {
			for (int j = 0; j <= ChessBoard.ROWS / 2; j++) {
				staticValue[i][j] = i < j ? i : j;
				staticValue[ChessBoard.COLS - i][j] = staticValue[i][j];
				staticValue[i][ChessBoard.ROWS - j] = staticValue[i][j];
				staticValue[ChessBoard.COLS - i][ChessBoard.ROWS - j] = staticValue[i][j];
			}
		}
	}

	// 空位的价值=黑棋的价值+白棋的价值+位置价值
	int[] getTheBestPosition() {// 查找计算机下棋的最佳位置
		for (int i = 0; i <= cb.COLS; i++) {
			for (int j = 0; j <= cb.ROWS; j++) {
				blackValue[i][j] = 0;
				whiteValue[i][j] = 0;
				if (cb.boardStatus[i][j] == 0) {
					for (int m = 1; m <= 4; m++) {// 每个点的分值为四个方向分值之和
						blackValue[i][j] += evaluateValue(1, i, j, m);
						whiteValue[i][j] += evaluateValue(2, i, j, m);
					}
				}
			}
		}
		int k = 0;
		int[][] totalValue = new int[(cb.COLS + 1) * (cb.ROWS + 1)][3];// 定义具有三个列的二维数组
		for (int i = 0; i <= cb.COLS; i++) {
			for (int j = 0; j <= cb.ROWS; j++) {
				if (cb.boardStatus[i][j] == 0) {
					totalValue[k][0] = i;// 第一列是该点的列坐标
					totalValue[k][1] = j;// 第二列是该店的行坐标
					totalValue[k][2] = blackValue[i][j] + whiteValue[i][j] + staticValue[i][j];// 第三列是该点的总分值
					k++;
				}
			}
		}
		sort(totalValue);// 对总分值降序排序
		// k的目的为了解决出现多个最大分值的问题，最大分值一定出现在最开始的几个位置上
		k = 1;
		int maxValue = totalValue[0][2];
		while (totalValue[k][2] == maxValue) {
			k++;
		}
		int r = (int) (Math.random() * k);// 多个点同时具有最大得分，随机选取一个作为最佳点
		int[] position = new int[2];
		position[0] = totalValue[r][0];
		position[1] = totalValue[r][1];
		return position;
	}

	private int evaluateValue(int color, int col, int row, int dir) {// 棋子放入指定位置，在指定方向上得到7个变量情况
		int k, m;
		int value = 0;
		// 空数4 连数3 空数3 下棋位置:连棋数1 空数1 连棋数2 空数2
		int chessCount1 = 1;// 放入棋子后可以形成的连续棋子数
		int chessCount2 = 0;
		int chessCount3 = 0;
		int spaceCount1 = 0;
		int spaceCount2 = 0;
		int spaceCount3 = 0;
		int spaceCount4 = 0;
		switch (dir) {
		case 1:// 水平方向
			for (k = col + 1; k <= cb.COLS; k++) {// 向增加的方向查找相同颜色连续的棋子
				if (cb.boardStatus[k][row] == color) {
					chessCount1++;
				} else {
					break;
				}
			}
			while ((k <= cb.COLS) && (cb.boardStatus[k][row] == 0)) {
				spaceCount1++;
				k++;
			}
			if (spaceCount1 == 1) {
				while ((k <= cb.COLS) && (cb.boardStatus[k][row] == color)) {
					chessCount2++;
					k++;
				}
				while ((k <= cb.COLS) && (cb.boardStatus[k][row] == 0)) {
					spaceCount2++;
					k++;
				}
			}

			for (k = col - 1; k >= 0; k--) {// 向减少的方向查找相同颜色连续的棋子
				if (cb.boardStatus[k][row] == color) {
					chessCount1++;
				} else {
					break;
				}
			}
			while (k >= 0 && (cb.boardStatus[k][row] == 0)) {
				spaceCount3++;
				k--;
			}
			if (spaceCount3 == 1) {
				while ((k >= 0) && (cb.boardStatus[k][row] == color)) {
					chessCount3++;
					k--;
				}
				while ((k >= 0) && (cb.boardStatus[k][row] == 0)) {
					spaceCount4++;
					k--;
				}
			}
			break;

		case 2:// 垂直方向
			for (k = row + 1; k <= cb.ROWS; k++) {// 向增加的方向查找相同颜色连续的棋子
				if (cb.boardStatus[col][k] == color) {
					chessCount1++;
				} else {
					break;
				}
			}
			while ((k <= cb.ROWS) && (cb.boardStatus[col][k] == 0)) {
				spaceCount1++;
				k++;
			}
			if (spaceCount1 == 1) {
				while ((k <= cb.ROWS) && (cb.boardStatus[col][k] == color)) {
					chessCount2++;
					k++;
				}
				while ((k <= cb.ROWS) && (cb.boardStatus[col][k] == 0)) {
					spaceCount2++;
					k++;
				}
			}
			for (k = row - 1; k >= 0; k--) {// 向相反的方向查找相同颜色的棋子
				if (cb.boardStatus[col][k] == color) {
					chessCount1++;
				} else {
					break;
				}
			}
			while ((k >= 0) && (cb.boardStatus[col][k] == 0)) {
				spaceCount3++;
				k--;
			}
			if (spaceCount3 == 1) {
				while ((k >= 0) && (cb.boardStatus[col][k] == color)) {
					chessCount3++;
					k--;
				}
				while ((k >= 0) && (cb.boardStatus[col][k] == 0)) {
					spaceCount4++;
					k--;
				}
			}
			break;

		case 3:// 左上到右下
			for (k = col + 1, m = row + 1; (k <= cb.COLS) && (m <= cb.ROWS); k++, m++) {// 向增加的方向查找相同颜色的棋子
				if (cb.boardStatus[k][m] == color) {
					chessCount1++;
				} else {
					break;
				}
			}
			while ((k <= cb.COLS) && (m <= cb.ROWS) && (cb.boardStatus[k][m] == 0)) {
				spaceCount1++;
				k++;
				m++;
			}
			if (spaceCount1 == 1) {
				while ((k <= cb.COLS) && (m <= cb.ROWS) && (cb.boardStatus[k][m] == color)) {
					chessCount2++;
					k++;
					m++;
				}
				while ((k <= cb.COLS) && (m <= cb.ROWS) && (cb.boardStatus[k][m] == 0)) {
					spaceCount2++;
					k++;
					m++;
				}
			}

			for (k = col - 1, m = row - 1; (k >= 0) && (m >= 0); k--, m--) {// 向相反方向查找相同颜色连续的棋子
				if (cb.boardStatus[k][m] == color) {
					chessCount1++;
				} else {
					break;
				}
			}
			while ((k >= 0) && (m >= 0) && (cb.boardStatus[k][m] == 0)) {
				spaceCount3++;
				k--;
				m--;
			}
			if (spaceCount3 == 1) {
				while ((k >= 0) && (m >= 0) && (cb.boardStatus[k][m] == color)) {
					chessCount3++;
					k--;
					m--;
				}
				while ((k >= 0) && (m >= 0) && (cb.boardStatus[k][m] == 0)) {
					spaceCount4++;
					k--;
					m--;
				}
			}
			break;

		case 4:// 右上到左下
			for (k = col + 1, m = row - 1; k <= cb.COLS && m >= 0; k++, m--) {
				if (cb.boardStatus[k][m] == color) {
					chessCount1++;
				} else {
					break;
				}
			}
			while (k <= cb.COLS && m >= 0 && (cb.boardStatus[k][m] == 0)) {
				spaceCount1++;
				k++;
				m--;
			}
			if (spaceCount1 == 1) {
				while ((k <= cb.COLS) && (m >= 0) && (cb.boardStatus[k][m] == color)) {
					chessCount2++;
					k++;
					m--;
				}
				while ((k <= cb.COLS) && (m >= 0) && (cb.boardStatus[k][m] == 0)) {
					spaceCount2++;
					k++;
					m--;
				}
			}

			for (k = col - 1, m = row + 1; k >= 0 && m <= cb.ROWS; k--, m++) {// 向相反的方向查找相同颜色连续的棋子
				if (cb.boardStatus[k][m] == color) {
					chessCount1++;
				} else {
					break;
				}
			}
			while ((k >= 0) && (m <= cb.ROWS) && (cb.boardStatus[k][m] == 0)) {
				spaceCount3++;
				k--;
				m++;
			}
			if (spaceCount3 == 1) {
				while ((k >= 0) && (m <= cb.ROWS) && (cb.boardStatus[k][m] == color)) {
					chessCount3++;
					k--;
					m++;
				}
				while ((k >= 0) && (m <= cb.ROWS) && (cb.boardStatus[k][m] == 0)) {
					spaceCount4++;
					k--;
					m++;
				}
			}
			break;
		}
		if (chessCount1 + chessCount2 + chessCount3 + spaceCount1 + spaceCount2 + spaceCount3 + spaceCount4 >= 5) {
			// 只有同色棋子数+两端的空位数不少于5时，才有价值
			value = getValue(chessCount1, chessCount2, chessCount3, spaceCount1, spaceCount2, spaceCount3, spaceCount4);
		}
		return value;
	}

	private int getValue(int chessCount1, int chessCount2, int chessCount3, int spaceCount1, int spaceCount2,
			int spaceCount3, int spaceCount4) {// 根据7个变量，得到相应的棋型
		int value = 0;

		switch (chessCount1) {
		case 5:
			value = FIVE;// 连五
			break;
		case 4:
			if ((spaceCount1 > 0) && (spaceCount3 > 0)) {
				value = HUO_FOUR;// 活四 OAAAAO
			} else {
				value = CHONG_FOUR;// 冲四 OAAAA
			}
			break;
		case 3:
			if (((spaceCount1 == 1) && (chessCount2 >= 1)) && ((spaceCount3 == 1) && (chessCount3 >= 1))) {
				value = HUO_FOUR;// 活四 AOAAAOA
			} else if (((spaceCount1 == 1) && (chessCount2 >= 1)) || (((spaceCount3 == 1) && (chessCount3 >= 1)))) {
				value = CHONG_FOUR;// 冲四 AAAOA
			} else if (((spaceCount1 > 1) && (spaceCount3 > 0)) || ((spaceCount1 > 0) && (spaceCount3 > 1))) {
				value = HUO_THREE;// 活三 OOAAAO
			} else {
				value = MIAN_THREE;// 眠三
			}
			break;
		case 2:
			if ((spaceCount1 == 1) && (chessCount2 >= 2) && (spaceCount3 == 1) && (chessCount3 >= 2)) {
				value = HUO_FOUR;// 活四 AAOAAOAA
			} else if (((spaceCount1 == 1) && (chessCount2 >= 2)) || ((spaceCount3 == 1) && (chessCount3 >= 2))) {
				value = CHONG_FOUR;// 冲四 AAOAA
			} else if (((spaceCount1 == 1) && (chessCount2 == 1) && (spaceCount2 > 0) && (spaceCount3 > 0))
					|| ((spaceCount3 == 1) && (chessCount3 == 1)) && (spaceCount1 > 0) && (spaceCount4 > 0)) {
				value = HUO_THREE;// 活三 OAAOAO
			} else if ((spaceCount1 > 0) && (spaceCount4 > 0)) {
				value = HUO_TWO;
			}
			break;

		case 1:
			if (((spaceCount1 == 1) && (chessCount2 >= 3)) || (spaceCount3 == 1) && (spaceCount3 >= 3)) {
				value = CHONG_FOUR; // AOAAA
			} else if (((spaceCount1 == 1) && (chessCount2 == 2) && (spaceCount2 >= 1) && (spaceCount3 >= 1))
					|| ((spaceCount3 == 1) && (chessCount3 == 2) && (spaceCount1 >= 1) && (spaceCount4 >= 1))) {
				value = HUO_THREE;// OAOAAO
			} else if (((spaceCount1 == 1) && (chessCount2 == 2) && ((spaceCount2 >= 1) || (spaceCount3 >= 1)))
					|| ((spaceCount3 == 1) && (chessCount3 == 2) && ((spaceCount1 >= 1) || (spaceCount4 >= 1)))) {
				value = MIAN_THREE;// OAOAAO
			} else if (((spaceCount1 == 1) && (chessCount2 == 2) && (spaceCount2 > 1) && (spaceCount3 > 0))
					|| ((spaceCount3 == 1) && (chessCount3 == 1) && (spaceCount1 > 0) && (spaceCount4 > 1))) {
				value = HUO_TWO;// OAOAOO
			}
			break;
		default:
			value = 0;
			break;
		}
		return value;
	}

	private void sort(int[][] allValue) {// 使用冒泡排序法
		for (int i = 0; i < allValue.length - 1; i++) {
			for (int j = 0; j < allValue.length - 1; j++) {
				int ti, tj, tvalue;
				if (allValue[j][2] < allValue[j + 1][2]) {
					tvalue = allValue[j][2];
					allValue[j][2] = allValue[j + 1][2];
					allValue[j + 1][2] = tvalue;
					ti = allValue[j][0];
					allValue[j][0] = allValue[j + 1][0];
					allValue[j + 1][0] = ti;
					tj = allValue[j][1];
					allValue[j][1] = allValue[j + 1][1];
					allValue[j + 1][1] = tj;
				}
			}
		}
	}

	private int evaluateGame() {
		int value = 0;
		int i, j, k;
		int[] line = new int[cb.COLS + 1];
		for (j = 0; j <= cb.ROWS; j++) {
			for (i = 0; i <= cb.COLS; i++) {
				line[i] = cb.boardStatus[i][j];
			}
			value += evaluateLine(line, cb.COLS + 1, 1);
			value -= evaluateLine(line, cb.COLS + 1, 2);
		}
		for (i = 0; i <= cb.COLS; i++) {
			for (j = 0; j <= cb.ROWS; j++) {
				line[j] = cb.boardStatus[i][j];
			}
			value += evaluateLine(line, cb.ROWS + 1, 1);
			value -= evaluateLine(line, cb.ROWS + 1, 2);
		}
		for (j = 4; j <= cb.ROWS; j++) {
			for (k = 0; k <= j; k++) {
				line[k] = cb.boardStatus[k][j - k];
			}
			value += evaluateLine(line, j + 1, 1);
			value -= evaluateLine(line, j + 1, 2);
		}
		for (j = 1; j <= cb.ROWS - 4; j++) {
			for (k = 0; k <= cb.COLS - j; k++) {
				line[k] = cb.boardStatus[k + j][cb.ROWS - k];
			}
			value += evaluateLine(line, cb.ROWS + 1 - j, 1);
			value -= evaluateLine(line, cb.ROWS + 1 - j, 2);
		}
		for (j = 0; j <= cb.ROWS - 4; j++) {
			for (k = 0; k <= cb.ROWS - j; k++) {
				line[k] = cb.boardStatus[k][k + j];
			}
			value += evaluateLine(line, cb.ROWS + 1 - j, 1);
			value -= evaluateLine(line, cb.ROWS + 1 - j, 2);
		}

		for (i = 1; i <= cb.COLS - 4; i++) {
			for (k = 0; k <= cb.ROWS - i; k++) {
				line[k] = cb.boardStatus[k + i][k];
			}
			value += evaluateLine(line, cb.ROWS + 1 - i, 1);
			value -= evaluateLine(line, cb.ROWS + 1 - i, 2);
		}
		if (cb.computerColor == 1) {
			return value;
		} else {
			return -value;
		}
	}

	private int evaluateLine(int lineState[], int num, int color) {
		int chess, space1, space2;
		int i, j, k;
		int value = 0;
		int begin, end;
		for (i = 0; i < num; i++)
			if (lineState[i] == color) {
				chess = 1;
				begin = i;
				for (j = begin + 1; (j < num) && (lineState[j] == color); j++) {
					chess++;
				}
				if (chess < 2) {
					continue;
				}
				end = j - 1;
				space1 = 0;
				space2 = 0;
				for (j = begin - 1; (j >= 0) && ((lineState[j] == 0) || (lineState[j] == color)); j--) {
					space1++;
				}
				for (j = end + 1; (j < num) && ((lineState[j] == 0) || (lineState[j] == color)); j++) {
					space2++;
				}
				if (chess + space1 + space2 >= 5) {
					value += getValue(chess, space1, space2);
				}
				i = end + 1;
			}
		return value;
	}

	private int getValue(int chessCount, int spaceCount1, int spaceCount2) {
		int value = 0;
		switch (chessCount) {
		case 5:
			value = FIVE;
			break;
		case 4:
			if ((spaceCount1 > 0) && (spaceCount2 > 0)) {
				value = HUO_FOUR;
			} else {
				value = CHONG_FOUR;
			}
			break;
		case 3:
			if ((spaceCount1 > 0) && (spaceCount2 > 0)) {
				value = HUO_THREE;
			} else {
				value = MIAN_THREE;
			}
			break;
		case 2:
			if ((spaceCount1 > 0) && (spaceCount2 > 0)) {
				value = HUO_TWO;
			}
			break;
		default:
			value = 0;
			break;
		}
		return value;
	}

	private int[][] getTheMostValuablePositions() {
		int i, j, k = 0;
		int[][] allValue = new int[(cb.COLS + 1) * (cb.ROWS + 1)][3];
		for (i = 0; i < cb.COLS; i++) {
			for (j = 0; j < cb.ROWS; j++) {
				if (cb.boardStatus[i][j] == 0) {
					allValue[k][0] = i;
					allValue[k][1] = j;
					allValue[k][2] = blackValue[i][j] + whiteValue[i][j] + staticValue[i][j];
					k++;
				}
			}
		}
		sort(allValue);
		int size = k < SAMPLE_NUMBER ? k : SAMPLE_NUMBER;
		int valuablePositions[][] = new int[size][3];
		for (i = 0; i < size; i++) {
			valuablePositions[i][0] = allValue[i][0];
			valuablePositions[i][1] = allValue[i][1];
			valuablePositions[i][2] = allValue[i][2];
		}
		return valuablePositions;
	}

	private int min(int depth) {
		if (depth == 0) {
			return evaluateGame();
		}
		for (int i = 0; i <= cb.COLS; i++) {
			for (int j = 0; j <= cb.ROWS; j++) {
				blackValue[i][j] = 0;
				whiteValue[i][j] = 0;
				if (cb.boardStatus[i][j] == 0) {
					for (int m = 1; m <= 4; m++) {
						blackValue[i][j] += evaluateValue(1, i, j, m);
						whiteValue[i][j] += evaluateValue(2, i, j, m);
					}
				}
			}
		}
		int bestValue = LARGE_NUMBER;
		int value;
		int valuablePositions[][] = getTheMostValuablePositions();
		for (int i = 0; i < valuablePositions.length; i++) {
			if (cb.computerColor == 1) {
				if (whiteValue[valuablePositions[i][0]][valuablePositions[i][1]] >= FIVE) {
					return -10 * FIVE;
				}
			} else {
				if (blackValue[valuablePositions[i][0]][valuablePositions[i][1]] >= FIVE) {
					return -10 * FIVE;
				}
			}
			cb.boardStatus[valuablePositions[i][0]][valuablePositions[i][1]] = cb.computerColor == 1 ? 2 : 1;
			value = max(depth - 1);
			cb.boardStatus[valuablePositions[i][0]][valuablePositions[i][1]] = 0;
			if (value < bestValue) {
				bestValue = value;
			}
		}
		return bestValue;
	}

	private int max(int depth) {
		for (int i = 0; i <= cb.COLS; i++) {
			for (int j = 0; j <= cb.ROWS; j++) {
				blackValue[i][j] = 0;
				whiteValue[i][j] = 0;
				if (cb.boardStatus[i][j] == 0) {
					for (int m = 1; m <= 4; m++) {
						blackValue[i][j] += evaluateValue(1, i, j, m);
						whiteValue[i][j] += evaluateValue(2, i, j, m);
					}
				}
			}
		}
		int bestValue;
		bestValue = -LARGE_NUMBER;
		int value;
		int valuablePositions[][] = getTheMostValuablePositions();
		for (int i = 0; i < valuablePositions.length; i++) {
			if (cb.computerColor == 1) {
				if (blackValue[valuablePositions[i][0]][valuablePositions[i][1]] >= FIVE) {
					return 10 * FIVE;
				}
			} else {
				if (whiteValue[valuablePositions[i][0]][valuablePositions[i][1]] >= FIVE) {
					return 10 * FIVE;
				}
			}
			cb.boardStatus[valuablePositions[i][0]][valuablePositions[i][1]] = cb.computerColor;
			if (value > bestValue) {
				bestValue = value;
			}
		}
		return bestValue;
	}
}
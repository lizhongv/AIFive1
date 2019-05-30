//A剪枝：任一极小层结点的β值不大于他任一前驱极大值层结点的α值，终止该结点以下搜索过程
//B剪枝：任意极大层结点的α值不小于他任意前驱极小值层结点的β值，终止该结点以下搜索过程

//棋局估值

public class Evaluate {
	// 棋型权重
	private static final int FIVE = 50000;// 连五
	private static final int HUO_FOUR = 5000;// 活四
	private static final int CHONG_FOUR = 1000;// 冲四
	private static final int HUO_THREE = 500;// 活三
	private static final int MIAN_THREE = 100;// 眠三
	private static final int HUO_TWO = 50;// 活二

	// 位置权重
	private int[][] blackValue;// 空位下棋后的价值
	private int[][] whiteValue;// 空位下白棋后的价值
	private int[][] staticValue;// 位置价值

	private static final int LARGE_NUMBER = 10000000;// 正无穷
	private static final int SEARCH_DEPTH = 3;// 搜索深度
	private static final int SAMPLE_NUMBER = 10;// 搜索样本数

	private ChessBoard cb;// 棋盘

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
		System.out.println();
		for (int i = 0; i <= ChessBoard.COLS / 2; i++) {// 棋盘位置权值初始化
			for (int j = 0; j <= ChessBoard.ROWS / 2; j++) {
				staticValue[i][j] = i < j ? i : j;
				staticValue[ChessBoard.COLS - i][j] = staticValue[i][j];
				staticValue[i][ChessBoard.ROWS - j] = staticValue[i][j];
				staticValue[ChessBoard.COLS - i][ChessBoard.ROWS - j] = staticValue[i][j];
			}
		}
		/*
		 * 检验 System.out.println("棋盘位置初始化"); for (int i = 0; i <= ChessBoard.COLS; i++)
		 * { for (int j = 0; j <= ChessBoard.ROWS; j++) {
		 * System.out.print(staticValue[i][j]); } System.out.println(); }
		 */
	}

	int[] getTheBestPosition() {// 极小极大搜索算法
		for (int i = 0; i <= cb.COLS; i++) {
			for (int j = 0; j <= cb.ROWS; j++) {
				blackValue[i][j] = 0;
				whiteValue[i][j] = 0;
				if (cb.boardStatus[i][j] == 0) {
					for (int m = 1; m <= 4; m++) {// 黑棋（白棋）的价值
						blackValue[i][j] += evaluateValue(1, i, j, m);
						whiteValue[i][j] += evaluateValue(2, i, j, m);
					}
				}
			}
		}

		int maxValue = -LARGE_NUMBER;
		int value;
		int[] position = new int[2];
		int valuablePositions[][] = getTheMostValuablePositions();
		for (int i = 0; i < valuablePositions.length; i++) {
			if (valuablePositions[i][2] >= FIVE) {// 已经连五
				position[0] = valuablePositions[i][0];
				position[1] = valuablePositions[i][1];
				break;
			}
			cb.boardStatus[valuablePositions[i][0]][valuablePositions[i][1]] = cb.computerColor;// 试着在该点下棋
			value = min(SEARCH_DEPTH, -LARGE_NUMBER, LARGE_NUMBER);
			cb.boardStatus[valuablePositions[i][0]][valuablePositions[i][1]] = 0;// 再将该点恢复为空
			// ******************检验
			System.out.println(
					i + "best=(" + valuablePositions[i][0] + "," + valuablePositions[i][1] + ")<" + value + ">");
			if (value > maxValue) {// 估值最大点为机器下棋点
				maxValue = value;
				position[0] = valuablePositions[i][0];
				position[1] = valuablePositions[i][1];
			}
		}
		return position;
	}

	private int min(int depth, int alpha, int beta) {// 搜索人下棋的最佳位置
		if (depth == 0) {// 如果搜索到最底层，直接返回当前的估值
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
			cb.boardStatus[valuablePositions[i][0]][valuablePositions[i][1]] = cb.computerColor == 1 ? 2 : 1;// 试着在该点人下棋
			value = max(depth - 1, alpha, beta);
			cb.boardStatus[valuablePositions[i][0]][valuablePositions[i][1]] = 0;// 再将该点恢复为空
			// *******************检验
			System.out.println(i + "min" + " depth=" + depth + "(" + +valuablePositions[i][0] + ","
					+ valuablePositions[i][1] + ")<" + value + ">" + "beta=" + beta + "alpha=" + alpha);
			if (value < beta) {// beta保存当前的最小估值
				beta = value;
				System.out.println("<beta=" + beta + "><value=" + value + ">");
				if (alpha >= beta) {// alpha剪枝
					return alpha;
				}
			}
		}
		return beta;
	}

	private int max(int depth, int alpha, int beta) {// 搜索机器下棋的最佳位置
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
			value = min(depth - 1, alpha, beta);
			cb.boardStatus[valuablePositions[i][0]][valuablePositions[i][1]] = 0;
			// **************************检验
			System.out.println(i + "max" + " depth=" + depth + "(" + +valuablePositions[i][0] + ","
					+ valuablePositions[i][1] + ")<" + value + ">" + "alpha=" + alpha + "beta=" + beta);
			if (value > alpha) {// alpha保存当前的最大估值
				alpha = value;
				System.out.println("<alpha=" + alpha + "><value=" + value + ">");
				if (alpha >= beta) {// beta剪枝
					return beta;
				}
			}
		}
		return alpha;
	}

	// **********************棋局的价值****************************
	private int evaluateGame() {
		// 棋局的价值=黑棋棋局的价值-白棋棋局的价值
		int value = 0;
		int i, j, k;
		int[] line = new int[cb.COLS + 1];// 四个方向上所有棋型
		for (j = 0; j <= cb.ROWS; j++) {// 水平：行价值和
			for (i = 0; i <= cb.COLS; i++) {
				line[i] = cb.boardStatus[i][j];
			}
			value += evaluateLine(line, cb.COLS + 1, 1);
			value -= evaluateLine(line, cb.COLS + 1, 2);
		}
		for (i = 0; i <= cb.COLS; i++) {// 垂直：列价值和
			for (j = 0; j <= cb.ROWS; j++) {
				line[j] = cb.boardStatus[i][j];
			}
			value += evaluateLine(line, cb.ROWS + 1, 1);
			value -= evaluateLine(line, cb.ROWS + 1, 2);
		}
		for (j = 4; j <= cb.ROWS; j++) {// 左下右上：斜线价值和
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
		for (j = 0; j <= cb.ROWS - 4; j++) {// 左上右下：斜线价值和
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
		// lineState[]记录某一行/列/斜方向 空位 1：黑棋子 2：白棋子 num：行/列/斜方向的长度
		// 行/列/斜线上： 所有可能棋型价值和
		int chess, space1, space2;// chess:连续棋子的个数 space1:连续棋子前方空位数 space2:连续棋子后方空位数
		int i, j, k;
		int value = 0;
		int begin, end;// 棋子开始下标，结束下标
		for (i = 0; i < num; i++)
			if (lineState[i] == color) {
				chess = 1;
				begin = i;
				for (j = begin + 1; (j < num) && (lineState[j] == color); j++) {
					chess++;
				}
				if (chess < 2) {// 指定颜色连续棋子个数小于2
					continue;
				}
				end = j - 1;
				space1 = 0;
				space2 = 0;
				// 计算连续棋子前面的空位数(包含同颜色棋子)
				for (j = begin - 1; (j >= 0) && ((lineState[j] == 0) || (lineState[j] == color)); j--) {
					space1++;
				}
				// 计算连续棋子后面的空位数（包含同颜色棋子）
				for (j = end + 1; (j < num) && ((lineState[j] == 0) || (lineState[j] == color)); j++) {
					space2++;
				}
				if (chess + space1 + space2 >= 5) {// 只有5个以上才有价值
					value += getValue(chess, space1, space2);
				}
				i = end + 1;// 关键点
			}
		return value;
	}

	private int getValue(int chessCount, int spaceCount1, int spaceCount2) {
		// 根据 {连续棋子前面的空位数，连续棋子数，连续棋子后面的空位数}判断棋型，给出价值分数
		// chessCount+spaceCount1+spaceCount2>=5
		int value = 0;
		switch (chessCount) {
		case 5:
			value = FIVE;// *AAAAA*
			break;
		case 4:
			if ((spaceCount1 > 0) && (spaceCount2 > 0)) {
				value = HUO_FOUR;// *0AAAA0*
			} else {
				value = CHONG_FOUR;// *0AAAB
			}
			break;
		case 3:
			if ((spaceCount1 > 0) && (spaceCount2 > 0)) {
				value = HUO_THREE;// *0AAA0*
			} else {
				value = MIAN_THREE;// *0AAAB
			}
			break;
		case 2:
			if ((spaceCount1 > 0) && (spaceCount2 > 0)) {
				value = HUO_TWO;// *0AA0*
			}
			break;
		default:
			value = 0;// 其余棋型没有价值
			break;
		}
		return value;
	}

	// *********************************棋型的价值***********************************
	private int evaluateValue(int color, int col, int row, int dir) {// 棋子放入指定位置，在指定方向上得到的棋型价值
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
		switch (dir) {// 将指定方向的棋型进行分解
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
			while ((k >= 0) && (cb.boardStatus[k][row] == 0)) {
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
			for (k = col + 1, m = row - 1; (k <= cb.COLS) && (m >= 0); k++, m--) {
				if (cb.boardStatus[k][m] == color) {
					chessCount1++;
				} else {
					break;
				}
			}
			while ((k <= cb.COLS) && (m >= 0) && (cb.boardStatus[k][m] == 0)) {
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
			for (k = col - 1, m = row + 1; (k >= 0) && (m <= cb.ROWS); k--, m++) {// 向相反的方向查找相同颜色连续的棋子
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
		// 统计棋型价值 **只有同色棋子数+两端的空位数不少于5时，才有价值
		if (chessCount1 + chessCount2 + chessCount3 + spaceCount1 + spaceCount2 + spaceCount3 + spaceCount4 >= 5) {
			value = getValue(chessCount1, chessCount2, chessCount3, spaceCount1, spaceCount2, spaceCount3, spaceCount4);
		}
		return value;
	}

	private int getValue(int chessCount1, int chessCount2, int chessCount3, int spaceCount1, int spaceCount2,
			int spaceCount3, int spaceCount4) {// 根据棋型分解的7个变量，得到相应的棋型价值
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

	// ****************************样本********************
	private int[][] getTheMostValuablePositions() {// 搜索的样本：空位的价值
		int i, j, k = 0;// k：空位数
		int[][] allValue = new int[(cb.COLS + 1) * (cb.ROWS + 1)][3];
		for (i = 0; i <= cb.COLS; i++) {
			for (j = 0; j <= cb.ROWS; j++) {
				if (cb.boardStatus[i][j] == 0) {
					allValue[k][0] = i;
					allValue[k][1] = j;
					allValue[k][2] = blackValue[i][j] + whiteValue[i][j] + staticValue[i][j];
					k++;
				}
			}
		}

		sort(allValue);
		int size = k < SAMPLE_NUMBER ? k : SAMPLE_NUMBER;// min{空位数，样本数}
		int valuablePositions[][] = new int[size][3];
		for (i = 0; i < size; i++) {
			valuablePositions[i][0] = allValue[i][0];
			valuablePositions[i][1] = allValue[i][1];
			valuablePositions[i][2] = allValue[i][2];
		}
		return valuablePositions;
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

}
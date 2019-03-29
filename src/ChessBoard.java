
//创建棋盘类

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ChessBoard extends JPanel {
	// 棋盘设置
	public static final int MARGIN = 15; // 边距
	public static final int SPAN = 20; // 网格宽度
	public static final int ROWS = 14; // 棋盘行数
	public static final int COLS = 14; // 棋盘列数
	Image img;// 棋盘背景
	private Five f;
	int[][] boardStatus;// 记录棋盘 0:无棋子，1：黑棋子，2：白棋子

	// 棋子设置
	Chess[] chessList = new Chess[100];
	int chessCount; // 棋子数目
	int manChessCount;
	int computerChessCount;
	int computerColor;// 计算机棋子颜色 1：黑棋，2：白棋

	// 游戏设置
	boolean isBlack = true; // 下一步该哪一方下棋：默认先手是黑棋
	boolean isGamming = false; // 是否正在游戏中
	boolean isComputerGo;// 是否该计算机下棋

	public ChessBoard(Five f) {
		this.f = f;
		boardStatus = new int[COLS + 1][ROWS + 1];
		for (int i = 0; i <= COLS; i++) {
			for (int j = 0; j <= ROWS; j++) {
				boardStatus[i][j] = 0;
			}
		}
		img = Toolkit.getDefaultToolkit().getImage("img/board.jpg");
		this.addMouseListener(new MouseMonitor()); // 在鼠标点击的位置下棋
		this.addMouseMotionListener((MouseMotionListener) new MouseMotionMonitor());
		// 鼠标在移动时 可以下棋的位置：鼠标形状设置为手形，不可以下棋的位置：鼠标形状设置为标准箭头光标
	}

	public Dimension getPreferredSize() { // 封装组件的宽度、高度
		return new Dimension(MARGIN * 2 + SPAN * COLS, MARGIN * 2 + SPAN * ROWS);
	}

	class MouseMotionMonitor extends MouseMotionAdapter {// 内部监听类
		public void mouseMoved(MouseEvent e) {
			int col = (e.getX() - MARGIN + SPAN / 2) / SPAN;
			int row = (e.getY() - MARGIN + SPAN / 2) / SPAN;
			if (col < 0 || col > COLS || row < 0 || row > ROWS || !isGamming || hasChess(col, row))
				ChessBoard.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));// 默认光标
			else
				ChessBoard.this.setCursor(new Cursor(Cursor.HAND_CURSOR));// 手型光标
		}
	}

	class MouseMonitor extends MouseAdapter { // 内部监听类
		public void mousePressed(MouseEvent e) {
			// 以下四种情况不能下棋
			if (!isGamming)
				return;
			if (isComputerGo)
				return;
			// 将鼠标单击的像素坐标转换成网格索引
			int col = (e.getX() - MARGIN + SPAN / 2) / SPAN;
			int row = (e.getY() - MARGIN + SPAN / 2) / SPAN;
			if (col < 0 || col > COLS || row < 0 || row > ROWS)// 落在棋盘外不能下棋
				return;
			if (hasChess(col, row))// 如果(x,y)位置已经有棋子则不能下棋
				return;

			// 内部逻辑
			manGo(col, row);
			if (!isGamming)
				return;
			computerGo();
		}
	}

	public void restartGame() { // 重新开始
		for (int i = 0; i < chessList.length; i++) {
			chessList[i] = null;
		}
		for (int i = 0; i <= COLS; i++) {
			for (int j = 0; j <= ROWS; j++) {
				boardStatus[i][j] = 0;
			}
		}
		isGamming = true;
		isBlack = !f.whiteFirst.isSelected();// 选中复选框，先手白棋
		isComputerGo = !f.manFirst.isSelected();// 选中复选框，计算机先行
		computerColor = isComputerGo ? 1 : 2;
		chessCount = 0;
		manChessCount = 0;
		computerChessCount = 0;

		// 内部逻辑
		if (isComputerGo) {
			computerGo();
		}
		paintComponent(this.getGraphics());
	}

	public void goback() { // 悔棋
		if ((isComputerGo) || (chessCount < 2))
			return;
		int i = chessList[chessCount - 1].getCol();
		int j = chessList[chessCount - 1].getRow();
		boardStatus[i][j] = 0;
		i = chessList[chessCount - 2].getCol();
		j = chessList[chessCount - 2].getRow();
		boardStatus[i][j] = 0;
		chessList[chessCount - 1] = null;
		chessList[chessCount - 2] = null;
		chessCount -= 2;
		manChessCount -= 1;
		computerChessCount -= 1;
		paintComponent(this.getGraphics());
	}

	public void giveUp() {
		String colorName = isBlack ? "白棋" : "黑棋";// 计算机赢了
		String msg = String.format("恭喜,%s赢了！", colorName);
		JOptionPane.showMessageDialog(ChessBoard.this, msg);
		isGamming = false;
	}

	public void about() {
		if (isGamming) {
			String colorName = isBlack ? "黑棋" : "白棋";
			String msg = "default:computer first,black first.Now:is Gamming,man is" + colorName;
			JOptionPane ab = new JOptionPane();
			ab.showMessageDialog(this, msg);
		} else {
			String msg = "default:computer first,black first.Now,is not Gamming……";
			JOptionPane ab = new JOptionPane();
			ab.showMessageDialog(this, msg);
		}
	}

	private void computerGo() {// 实现计算机下棋
		Evaluate e = new Evaluate(this);
		int pos[] = e.getTheBestPosition();// pos[0]:X坐标，pos[1]:Y坐标
		computerChessCount++;
		putChess(pos[0], pos[1], isBlack ? Color.black : Color.white, computerChessCount);
	}

	public void manGo(int col, int row) {// 人在指定坐标下棋
		manChessCount++;
		putChess(col, row, isBlack ? Color.black : Color.white, manChessCount);
	}

	public void putChess(int col, int row, Color color, int num) {// 在指定坐标位置下棋
		Chess ch = new Chess(ChessBoard.this, col, row, color, num);
		chessList[chessCount++] = ch;
		boardStatus[col][row] = (color == Color.BLACK) ? 1 : 2;
		paintComponent(this.getGraphics());
		if (isWin(col, row)) {
			String colorName = isBlack ? "黑棋" : "白棋";
			String msg = String.format("恭喜,%s赢了！", colorName);
			JOptionPane.showMessageDialog(ChessBoard.this, msg);
			isGamming = false;
		} else {
			isBlack = !isBlack;
			isComputerGo = !isComputerGo;
		}
	}

	public void paintComponent(Graphics g) { // 画棋盘
		super.paintComponent(g);
		g.drawImage(img, 0, 0, this);
		for (int i = 0; i <= ROWS; i++) { // 画横线
			g.drawLine(MARGIN, MARGIN + i * SPAN, MARGIN + COLS * SPAN, MARGIN + i * SPAN);
		}
		for (int i = 0; i <= COLS; i++) { // 画竖线
			g.drawLine(MARGIN + i * SPAN, MARGIN, MARGIN + i * SPAN, MARGIN + ROWS * SPAN);
		}
		// 画9个黑方块
		g.fillRect(MARGIN + 3 * SPAN - 2, MARGIN + 3 * SPAN - 2, 5, 5);
		g.fillRect(MARGIN + (COLS / 2) * SPAN - 2, MARGIN + 3 * SPAN - 2, 5, 5);
		g.fillRect(MARGIN + (COLS - 3) * SPAN - 2, MARGIN + 3 * SPAN - 2, 5, 5);
		g.fillRect(MARGIN + 3 * SPAN - 2, MARGIN + (ROWS / 2) * SPAN - 2, 5, 5);
		g.fillRect(MARGIN + (COLS / 2) * SPAN - 2, MARGIN + (ROWS / 2) * SPAN - 2, 5, 5);
		g.fillRect(MARGIN + (COLS - 3) * SPAN - 2, MARGIN + (ROWS / 2) * SPAN - 2, 5, 5);
		g.fillRect(MARGIN + 3 * SPAN - 2, MARGIN + (ROWS - 3) * SPAN - 2, 5, 5);
		g.fillRect(MARGIN + (COLS / 2) * SPAN - 2, MARGIN + (ROWS - 3) * SPAN - 2, 5, 5);
		g.fillRect(MARGIN + (COLS - 3) * SPAN - 2, MARGIN + (ROWS - 3) * SPAN - 2, 5, 5);
		for (int i = 0; i < chessCount; i++) { // 循环绘制棋子
			chessList[i].draw(g);
			if (i == chessCount - 1) { // 最后一个棋子
				int xPos = chessList[i].getCol() * SPAN + MARGIN;
				int yPos = chessList[i].getRow() * SPAN + MARGIN;
				g.setColor(Color.red);
				g.drawRect(xPos - Chess.DIAMETER / 2, yPos - Chess.DIAMETER / 2, Chess.DIAMETER, Chess.DIAMETER);
			}
		}
	}

	private boolean hasChess(int col, int row) { // 测试该位置是否已经有棋子
		for (int i = 0; i < chessCount; i++) {
			Chess ch = chessList[i];
			if (ch != null && ch.getCol() == col && ch.getRow() == row)
				return true;
		}
		return false;
	}

	private boolean hasChess(int col, int row, Color color) {// 判断某个点有没有黑子，或者白子
		for (int i = 0; i < chessCount; i++) {
			Chess ch = chessList[i];
			if (ch != null && ch.getCol() == col && ch.getRow() == row && ch.getColor() == color)
				return true;
		}
		return false;
	}

	private boolean isWin(int col, int row) { // 判断胜负的方法
		int continueCount = 1; // 连续棋子的个数
		Color c = isBlack ? Color.black : Color.white;

		for (int x = col - 1; x >= 0; x--) { // 横向向左查找
			if (hasChess(x, row, c))
				continueCount++;
			else
				break;
		}
		for (int x = col + 1; x <= COLS; x++) { // 横向向右查找
			if (hasChess(x, row, c))
				continueCount++;
			else
				break;
		}
		if (continueCount >= 5)
			return true;
		else
			continueCount = 1;

		for (int y = row - 1; y >= 0; y--) { // 纵向向上搜索
			if (hasChess(col, y, c))
				continueCount++;
			else
				break;
		}
		for (int y = row + 1; y <= ROWS; y++) { // 纵向向下搜索
			if (hasChess(col, y, c))
				continueCount++;
			else
				break;
		}
		if (continueCount >= 5)
			return true;
		else
			continueCount = 1;

		for (int x = col + 1, y = row - 1; y >= 0 && x <= COLS; x++, y--) {
			if (hasChess(x, y, c)) // 向右上寻找
				continueCount++;
			else
				break;
		}
		for (int x = col - 1, y = row + 1; x >= 0 && y <= ROWS; x--, y++) {
			if (hasChess(x, y, c)) // 向左下寻找
				continueCount++;
			else
				break;
		}
		if (continueCount >= 5)
			return true;
		else
			continueCount = 1;

		for (int x = col - 1, y = row - 1; x >= 0 && y >= 0; x--, y--) { // 向左上寻找
			if (hasChess(x, y, c))
				continueCount++;
			else
				break;
		}
		for (int x = col + 1, y = row + 1; x < COLS && y <= ROWS; x++, y++) {
			if (hasChess(x, y, c)) // 向右下寻找
				continueCount++;
			else
				break;
		}
		if (continueCount >= 5)
			return true;
		else
			return false;
	}
}

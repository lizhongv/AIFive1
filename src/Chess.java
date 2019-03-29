
//创建旗子类

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

public class Chess {
	public static final int DIAMETER = ChessBoard.SPAN - 2;
	private int col; // 旗子在棋盘中的x索引
	private int row; // 旗子在棋盘中的y索引
	private Color color; // 颜色
	private int num;// 棋子上显示数字
	ChessBoard cb;

	public Chess(ChessBoard cb, int col, int row, Color color, int num) {
		this.cb = cb;
		this.col = col;
		this.row = row;
		this.color = color;
		this.num = num;
	}

	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}

	public Color getColor() {
		return color;
	}

	public int getNum() {
		return num;
	}

	// 画旗子
	public void draw(Graphics g) {
		int xPos = col * cb.SPAN + cb.MARGIN;
		int yPos = row * cb.SPAN + cb.MARGIN;
		Graphics2D g2d = (Graphics2D) g;
		// 颜色渐变
		RadialGradientPaint paint = null;// 圆形辐射颜色渐变模式填充某一形状
		int x = xPos + DIAMETER / 4;
		int y = yPos - DIAMETER / 4;
		float[] f = { 0f, 1f };// 颜色渐变范围
		Color[] c = { Color.WHITE, Color.BLACK };
		if (color == Color.black) {
			paint = new RadialGradientPaint(x, y, DIAMETER, f, c);
		} else if (color == Color.white) {
			paint = new RadialGradientPaint(x, y, DIAMETER * 2, f, c);
		}
		g2d.setPaint(paint);
		// 以下两行使边界更均匀
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
		Ellipse2D e = new Ellipse2D.Float(xPos - DIAMETER / 2, yPos - DIAMETER / 2, DIAMETER, DIAMETER);// 定义椭圆
		g2d.fill(e);

		// 棋子上写数字
		if (color == Color.white) {
			Font drawFont = new Font("Arial", Font.PLAIN, 8);
			g2d.setFont(drawFont);
			g2d.setColor(Color.black);
			g2d.drawString(String.valueOf(num), xPos - DIAMETER / 8, yPos + DIAMETER / 8);
		} else if (color == Color.black) {
			Font drawFont = new Font("Arial", Font.PLAIN, 8);
			g2d.setFont(drawFont);
			g2d.setColor(Color.RED);
			g2d.drawString(String.valueOf(num), xPos - DIAMETER / 8, yPos + DIAMETER / 8);
		}
	}
}

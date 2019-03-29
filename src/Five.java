
//创建窗口

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JToolBar;

public class Five extends JFrame {
	private JToolBar toolbar;
	private JButton startButton, backButton, exitButton, giveupButton, aboutButton;
	private ChessBoard boardPanel;
	JCheckBox manFirst, whiteFirst;// 默认计算机先手、先手执黑棋

	public Five() {
		super("五子棋人机对战");
		toolbar = new JToolBar();
		manFirst = new JCheckBox("先手");
		whiteFirst = new JCheckBox("白棋");
		startButton = new JButton("开始");
		backButton = new JButton("悔棋");
		exitButton = new JButton("退出");
		giveupButton = new JButton("放弃");
		aboutButton = new JButton("关于");
		toolbar.add(startButton);
		toolbar.add(backButton);
		toolbar.add(giveupButton);
		toolbar.add(exitButton);
		toolbar.add(aboutButton);
		toolbar.add(manFirst);
		toolbar.add(whiteFirst);
		this.add(toolbar, BorderLayout.NORTH);

		boardPanel = new ChessBoard(this);// 引用本身
		this.add(boardPanel, BorderLayout.CENTER);
		this.setLocation(200, 200);
		this.pack();
		this.setResizable(false);

		this.setDefaultCloseOperation(EXIT_ON_CLOSE); // 窗体设置
		this.setVisible(true);

		ActionMonitor monitor = new ActionMonitor(); // 监听器
		startButton.addActionListener(monitor);
		backButton.addActionListener(monitor);
		exitButton.addActionListener(monitor);
		giveupButton.addActionListener(monitor);
		aboutButton.addActionListener(monitor);
	}

	class ActionMonitor implements ActionListener { // 内部监听类
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == startButton) {
				boardPanel.restartGame();
			} else if (e.getSource() == backButton) {
				boardPanel.goback();
			} else if (e.getSource() == exitButton) {
				System.exit(0);
			} else if (e.getSource() == giveupButton) {
				boardPanel.giveUp();
			} else if (e.getSource() == aboutButton) {
				boardPanel.about();
			}
		}
	}

	public static void main(String[] args) {
		new Five();
	}
}
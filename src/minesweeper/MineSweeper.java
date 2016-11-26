package minesweeper;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class MineSweeper{
	//ゲームの起動
	public static void main(String[] args) {
		boolean fast = false;
		for(int i = 0; i < args.length; i++){
			if(args[i].equals("-skip")){
				fast = true;
			}
		}
		if(args.length >= 2){
			new StartFrame(args[0],args[1],fast);
		}else{
			new StartFrame(fast);
		}
	}
}

//ゲーム画面のパネル
@SuppressWarnings("serial")
class GamePanel extends JPanel{
	private int count = 0;
	private final int maxcount;
	private boolean gameStatus = true;

	public GamePanel(int size, int bombs, boolean cheat){
		GamePanel panel = this;
		this.maxcount = size*size-bombs;
		this.setLayout(new GridLayout(size,size));
		byte[][] fieldbase = new byte[size][size];
		//フィールド基礎の生成
		//ボムの配置
		for(int i = 0; i < bombs; i++){
			Random rand = new Random();
			int x = rand.nextInt(size);
			int y = rand.nextInt(size);
			if(fieldbase[x][y] == 9){
				i--;
				continue;
			}else{
				fieldbase[x][y] = 9;
			}
		}
		//値の設定
		for(int i = 0; i < fieldbase.length; i++){
			for(int j = 0; j < fieldbase[i].length; j++){
				if(fieldbase[i][j] != 9){
					for(int k = -1; k <= 1; k++){
						for(int l = -1; l <= 1; l++){
							try{
								if(fieldbase[i+k][j+l]==9)fieldbase[i][j]++;
							}catch(ArrayIndexOutOfBoundsException e){
							}
						}
					}
				}
			}
		}
		//フィールドの実体生成
		FieldPanel[][] field = new FieldPanel[size][size];
		for(int i = 0; i < field.length; i++){
			for(int j = 0; j < field[i].length; j++){
				field[i][j] = new FieldPanel(fieldbase[i][j]);
				this.add(field[i][j]);
			}
		}
		//チートONの時の出力
		if(cheat){
			for(int i =  0; i < fieldbase.length; i++){
				for(int j = 0; j < fieldbase[i].length; j++){
					if(fieldbase[i][j]==9){
						System.out.print("*");
					}else if(fieldbase[i][j]==0){
						System.out.print(" ");
					}else{
						System.out.print(fieldbase[i][j]);
					}
				}
				System.out.println();
			}
		}
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e){
				Point point = e.getPoint();
				FieldPanel pa = (FieldPanel)panel.getComponentAt(point);
				int x = 0;
				int y = 0;
				for(int i = 0; i < field.length; i++){
					for(int j = 0; j < field[i].length; j++){
						if(field[i][j].equals(pa)){
							x = i;
							y = j;
							break;
						}
					}
				}
				if(gameStatus){
					if(e.getButton() == MouseEvent.BUTTON1 && !e.isShiftDown()){
						//左クリック
						if(pa.checkValue()){
							JFrame endframe = new JFrame();
							JLabel label = new JLabel("GAMEOVER");
							label.setHorizontalAlignment(JLabel.CENTER);
							label.setFont(new Font("MSゴシック", Font.PLAIN, 20));
							endframe.add(label);
							endframe.setBounds(SwingUtilities.getWindowAncestor(panel).getX()+50, SwingUtilities.getWindowAncestor(panel).getY()+50, 200, 100);
							endframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
							endframe.setVisible(true);
							for(int i = 0; i < field.length; i++){
								for(int j = 0; j < field[i].length; j++){
									field[i][j].end(true);
								}
							}
							endframe.addKeyListener(new KeyAdapter(){
								@Override
								public void keyTyped(KeyEvent e){
									if(e.getKeyCode() == KeyEvent.VK_ENTER){
										System.exit(0);
									}
								}
							});
							repaint();
							gameStatus = false;
						}else{
							count += pa.openField(field, x, y);
							panel.repaint();
						}
					}else if(e.getButton() == MouseEvent.BUTTON3 || e.getButton() == MouseEvent.BUTTON1 && e.isShiftDown()){
						//右クリック
						pa.flag();
						panel.repaint();
					}
				}
				if(count == maxcount){
					JFrame endframe = new JFrame();
					JLabel label = new JLabel("CLEAR");
					label.setHorizontalAlignment(JLabel.CENTER);
					label.setFont(new Font("MSゴシック", Font.PLAIN, 40));
					endframe.add(label);
					endframe.setBounds(SwingUtilities.getWindowAncestor(panel).getX()+50, SwingUtilities.getWindowAncestor(panel).getY()+50, 200, 150);
					endframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					endframe.setVisible(true);
					for(int i = 0; i < field.length; i++){
						for(int j = 0; j < field[i].length; j++){
							pa.end(false);
						}
					}
					endframe.addKeyListener(new KeyAdapter(){
						@Override
						public void keyTyped(KeyEvent e) {
							if(e.getKeyCode() == KeyEvent.VK_ENTER){
								System.exit(0);
							}
						}
					});
					repaint();
					gameStatus = false;
				}
			}
		});
	}
}

//フィールドの1マス
@SuppressWarnings("serial")
class FieldPanel extends JPanel{
	private byte status = 0;
	private byte value;

	public FieldPanel(byte value){
		this.value = value;
	}
	public boolean checkValue(){
		if(value == 9){
			status = 3;
			repaint();
			return true;
		}else{
			return false;
		}
	}
	public void flag(){
		if(status == 0){
			status = 2;
		}else if(status == 2){
			status = 0;
		}
	}
	public void end(boolean bombed){
		if(value == 9){
			if(bombed){
				status = 3;
			}else{
				status = 4;
			}
		}else{
			status = 1;
		}
	}
	public int openField(FieldPanel[][] field, int x, int y){
		int count = 0;
		if(status == 0){
			status = 1;
			count++;
			if(value == 0){
				for(int i = -1; i <= 1; i++){
					for(int j = -1; j <= 1; j++){
						if(!(i==0&&j==0)){
							try{
								count += field[x+i][y+j].openField(field,x+i,y+j);
							}catch(ArrayIndexOutOfBoundsException e){
							}
						}
					}
				}
			}
		}
		return count;
	}
	//描画
	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		switch(status){
		case 1: g2.setColor(Color.GRAY); break;
		case 3: g2.setColor(Color.RED);break;
		default: g2.setColor(Color.LIGHT_GRAY); break;
		}
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(Color.BLACK);
		g2.draw(new Rectangle2D.Double(0,0,getWidth(),getHeight()));
		g2.setFont(new Font("MSゴシック",Font.PLAIN, 30));
		if(status == 1){
			if(value != 0){
				g2.drawString(value+"", getWidth()/2-5, getHeight()/2+5);
			}
		}else if(status == 2){
			g2.setColor(Color.RED);
			g2.setStroke(new BasicStroke(getWidth()/20));
			g2.drawLine(getWidth()/2, getHeight()/10, getWidth()/2, getHeight()*9/10);
			int[] x = {getWidth()/2,getWidth()*9/10,getWidth()/2};
			int[] y = {getHeight()/10,getHeight()/4,getHeight()/2};
			g2.fill(new Polygon(x,y,3));
			g2.drawLine(getWidth()/5, getHeight()*9/10, getWidth()*4/5, getHeight()*9/10);
		}else if(status == 3){
			g2.setColor(Color.ORANGE);
			g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
		}else if(status == 4){
			g2.fill(new Ellipse2D.Double(getWidth()/5, getHeight()/5, getWidth()*3/5, getHeight()*3/5));
		}
	}
}

//ゲーム起動のフレーム
@SuppressWarnings("serial")
class StartFrame extends JFrame{

	private final JPanel panel;
	private final CardLayout layout;
	private final JTextField tf1;
	private final JTextField tf2;
	private final JLabel label;

	public StartFrame(boolean b){
		this("10","10",b);
	}

	public StartFrame(String size, String bombs, boolean b){
		this.setTitle("MineSweeper");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setBounds(100, 30, 500, 500);

		panel = new JPanel();
		layout = new CardLayout();
		panel.setLayout(layout);
		JPanel startPanel = new JPanel();
		startPanel.setLayout(null);

		JLabel titlelabel = new JLabel("マインスイーパー");
		titlelabel.setFont(new Font("MSゴシック", Font.PLAIN, 30));
		titlelabel.setBounds(this.getWidth()/2-140, this.getHeight()/5-10, 280, 40);
		startPanel.add(titlelabel);

		JLabel label1 = new JLabel("フィールドのサイズ");
		label1.setBounds(this.getWidth()/4-60, this.getHeight()*3/10, 120, 20);
		startPanel.add(label1);
		tf1 = new JTextField(size, 5);
		tf1.setBounds(this.getWidth()/4-60, this.getHeight()*2/5-10, 120, 20);
		tf1.setHorizontalAlignment(JTextField.RIGHT);
		startPanel.add(tf1);

		JLabel label2 = new JLabel("爆弾の数");
		label2.setBounds(this.getWidth()*3/4-40, this.getHeight()*3/10, 80, 20);
		startPanel.add(label2);
		tf2 = new JTextField(bombs, 5);
		tf2.setBounds(this.getWidth()*3/4-60, this.getHeight()*2/5, 120, 20);
		tf2.setHorizontalAlignment(JTextField.RIGHT);
		startPanel.add(tf2);

		JButton button = new JButton("START");
		button.setBounds(this.getWidth()/2-40, this.getHeight()/2, 80, 20);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gameStart(false);
			}
		});
		button.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e){
				if(e.isControlDown()){
					gameStart(true);
				}else{
					gameStart(false);
				}
			}
		});
		startPanel.add(button);

		label = new JLabel();
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setBounds(this.getWidth()/2-75, this.getHeight()*4/5-50, 300, 20);
		startPanel.add(label);

		panel.add(startPanel, "start");

		this.add(panel);
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter	() {
			@Override
			public void windowOpened(WindowEvent e) {
				button.requestFocusInWindow();
			}
		});
		if(b){
			gameStart(false);
		}
	}
	public void gameStart(boolean b){
		try{
			if(Integer.parseInt(tf1.getText())*Integer.parseInt(tf1.getText()) > Integer.parseInt(tf2.getText())){
				panel.add(new GamePanel(Integer.parseInt(tf1.getText()),Integer.parseInt(tf2.getText()),b),"game");
				layout.next(panel);
			}else{
				label.setText("爆弾の数が多すぎます");
			}
		}catch(NumberFormatException e){
			label.setText("値を正しく入力して下さい");
		}
	}
}

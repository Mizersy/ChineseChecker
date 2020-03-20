import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.management.MBeanOperationInfo;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;



public class ChineseChecker {
	
	public static MyFrame myFrame;
	public static int numberOfPlayers;	//游戏玩家的数量
	public static boolean numbersDone = false;//游戏玩家数量是否设定完毕
	public static boolean gameStatus = false;//游戏状态（是否暂停）
	public static char[] series = new char[6];//六种棋子
	public static int currentIndex = 0;//当前下标
	public static String pieceToMove;//将要移动的棋子
	public static int []piecePos;//记录棋子位置
	public static boolean isPieceToMoveAcquired = false;//是否已经选中将要移动的棋子
	public static boolean isTheStartingOfTheGame = false;//游戏是否开始
	public static boolean isPositionToGoToAcquired = false;//将要去的位置是否设定完毕
	public static int[] posToGoTo;
	public static boolean isANewGame = false;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		myFrame = new MyFrame();//实例化窗口
		myFrame.setSize(710, 870);//设置窗口大小
		myFrame.setVisible(true);//窗口可见
		Pieces pieces = new Pieces();//实例化棋子类
		
		/**等待设定游戏玩家数量**/
		while(!numbersDone){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		numbersDone = false;
		
		/*
		 * 游戏分为双人对战，四人对战和六人对战
		 */
		switch (numberOfPlayers) {
		case 2:
			series[0] = 'D';series[1] = 'A';
			break;
		case 4:
            series[0] = 'F';series[1] = 'B';series[2] = 'C';series[3] = 'E';
            break;
        case 6:
            series[0] = 'F';series[1] = 'A';series[2] = 'B';series[3] = 'C';series[4] = 'D';series[5] = 'E';
            break;
		default:
			break;
		} 
		//pieces.init();
		myFrame.init(series);
		initPlayers(series);
		
		pieces.init();
		
		/**一旦玩家数量设定完成，游戏开始**/
        isTheStartingOfTheGame = true;
        
        while (true) {
            Trace.isSkipped = false;

            Trace.nextStep();
            
            //展示当前玩家
            myFrame.showCurrent();
            myFrame.showCurrentPlayer();
            MyFrame.timer = new Timer();
            

            while (!isPositionToGoToAcquired) {
                
                if (MyFrame.timer.listen() <= 0) {//如果玩家超时
                    if (isPieceToMoveAcquired) //如果将要移动的的棋子设定过
                        PieceActionListener.clearPossiblePos();//清除之前的可能移动到的合法位置标记
                    pieceToMove = series[currentIndex] + "0";
                    piecePos = Pieces.getPiecePos("A0  ");
                    posToGoTo = piecePos ;
                    Services.msg("Player " + series[currentIndex] + " time exceeded!");//提示玩家超时
                    break;
                }
                if (Trace.isSkipped)//如果已经跳过了，就退出
                    break;
                try {
					Thread.sleep(0500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            if (Trace.isSkipped) continue;//如果已经跳过了，就到下一次循环
            isPositionToGoToAcquired = false;
            if (isANewGame) {
                isANewGame = false;
                continue;
            }
            /*记录轨迹 */
            Pieces.moveAward(pieceToMove, piecePos, posToGoTo);
            Trace.recordStep(pieceToMove, piecePos, posToGoTo);

            /*判断是否出现胜利者*/
            for (int order = 0; order < numberOfPlayers; order++) {
                if (isWinner(series[order])) {
                    Services.msg("Player " + series[order] + " wins!");
                    System.exit(0);
                }
            }
            Trace.step = Trace.step + 1;
        }
	}

	public static void setNumber(int number){//设定游戏玩家数量
		ChineseChecker.numbersDone = true;
        ChineseChecker.gameStatus = true;
        ChineseChecker.numberOfPlayers = number;
        ChineseChecker.myFrame.mNewGame.setEnabled(false);
	}
	
	public static void initPlayers(char[] players) {//初始化players
        for (char player : players) {
            if (player == 0)
                return;
            int[][] initPositions = myFrame.zoneOfPlayer[player - 'A'];
            for (int index = 0; index < 10; index++) {
                Pieces.cell[initPositions[index][0]][initPositions[index][1]] = "" + player + index + "  ";
            }
        }
    }
	
	public static boolean isWinner(char player) {//判断player是否已经获胜
        char opponentPlayer = player < 'D' ? (char) ((int) player + 3) : (char) ((int) player - 3);
        
        int[][] positionsBelongedToThePlayer = MyFrame.zoneOfPlayer[opponentPlayer - 'A'];

        for (int index = 0; index < 10; index++) {
            int[] position = positionsBelongedToThePlayer[index];
            if (Pieces.cell[position[0]][position[1]].charAt(0) != player)
                return false;
        }
        return true;
    }

}

class MyFrame extends JFrame{
	
	public Image image;
	
	public ImageIcon imgBoard;
	public static JLayeredPane panel;
	public static JLabel lblBoard,lblPlayer,lblTime,lblCurrent;
	public JMenuBar menuBar;
	public JMenu mControl = new JMenu("control"),
				mNewGame = new JMenu("New Game");
	public JMenuItem mTwoPlayers = new JMenuItem("Two players"),
	            mFourPlayers = new JMenuItem("Four players"),
	            mSixPlayers = new JMenuItem("Six players"),
	            mExit = new JMenuItem("Exit", KeyEvent.VK_X),
	            mPauseGame = new JMenuItem("Pause Game", KeyEvent.VK_P),
	            mOpen = new JMenuItem("Open",KeyEvent.VK_O), 
	    	            mSave = new JMenuItem("Save",KeyEvent.VK_S);
	public JLabel[][] pieces = new JLabel[6][10];
	public static final int[][][] zoneOfPlayer = {
            {{5, 1}, {5, 2}, {6, 2}, {5, 3}, {6, 3}, {7, 3}, {5, 4}, {6, 4}, {7, 4}, {8, 4}},
            {{1, 5}, {2, 6}, {2, 5}, {3, 7}, {3, 6}, {3, 5}, {4, 8}, {4, 7}, {4, 6}, {4, 5}},
            {{5, 13}, {6, 13}, {5, 12}, {7, 13}, {6, 12}, {5, 11}, {8, 13}, {7, 12}, {6, 11}, {5, 10}},
            {{13, 17}, {13, 16}, {12, 16}, {13, 15}, {12, 15}, {11, 15}, {13, 14}, {12, 14}, {11, 14}, {10, 14}},
            {{17, 13}, {16, 12}, {16, 13}, {15, 11}, {15, 12}, {15, 13}, {14, 10}, {14, 11}, {14, 12}, {14, 13}},
            {{13, 5}, {12, 5}, {13, 6}, {11, 5}, {12, 6}, {13, 7}, {10, 5}, {11, 6}, {12, 7}, {13, 8}}};
	public static Timer timer;
	public MyFrame(){
		
		imgBoard = new ImageIcon("./res/Board.jpg");
		lblTime = new JLabel();
        lblTime.setForeground(Color.BLUE);
        lblTime.setFont(new Font("黑体", Font.BOLD, 20));
        lblTime.setBounds(50, 50, 280, 30);
		
        lblCurrent = new JLabel("当前玩家");
        lblCurrent.setFont(new Font("当前玩家", 20, 20));
        lblCurrent.setBounds(40, 80, 200, 30);
        
		lblBoard = new JLabel(imgBoard);
		lblBoard.setBounds(0, 20, 685, 800);
		
		lblPlayer = new JLabel(new ImageIcon());
        lblPlayer.setBounds(50, 100, 50, 50);
		
		panel = new JLayeredPane();
        panel.add(lblBoard,JLayeredPane.DEFAULT_LAYER);
        panel.add(lblPlayer, JLayeredPane.MODAL_LAYER);
        panel.add(lblTime, JLayeredPane.MODAL_LAYER);
        
        
        setLayeredPane(panel);
        
        mControl.setMnemonic(KeyEvent.VK_C);
        mControl.add(mOpen);
        mControl.add(mSave);
        mControl.add(mNewGame);
        mControl.add(mPauseGame);
        
        mNewGame.add(mTwoPlayers);
        mNewGame.add(mFourPlayers);
        mNewGame.add(mSixPlayers);
        
        menuBar = new JMenuBar();
        menuBar.add(mControl);
        setJMenuBar(menuBar);
        
        /**设置两个玩家、四个玩家或六个玩家**/
        mTwoPlayers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ChineseChecker.setNumber(2);
			}
		});
        mFourPlayers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ChineseChecker.setNumber(4);
			}
		});
        mSixPlayers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ChineseChecker.setNumber(6);
			}
		});
        /**暂停游戏**/
        mPauseGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ChineseChecker.gameStatus = false;
			}
		});
        
        mOpen.addActionListener(new ActionListener() {//打开已保存过的游戏
        	@Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser();
                chooser.showOpenDialog(null);

                try {
                    File file = chooser.getSelectedFile();
                    if (file == null) {
                        throw new Exception("FileNotExisted");
                    }
                    Scanner scanner = new Scanner(file);
                    String[] temp = scanner.nextLine().split(",");
                    int num = Integer.parseInt(temp[0]);
                    char cur = temp[1].charAt(0);
                    int curIndex = Integer.parseInt(temp[2]);
                    int step = Integer.parseInt(temp[3]);

                    ChineseChecker.numbersDone = true;
                    if (!(ChineseChecker.numberOfPlayers == num) && !ChineseChecker.isTheStartingOfTheGame)
                        throw new Exception("PlayersNumberUnmatched");
                    ChineseChecker.numberOfPlayers = num;
                    System.out.println(num);
                    ChineseChecker.currentIndex = curIndex;
                    ChineseChecker.series[curIndex] = cur;
                    Trace.step = step;
                    //Diagram.getDiagram().init();
                    
                    while (!ChineseChecker.isTheStartingOfTheGame) {
                    }
                    while (scanner.hasNext()) {
                        in(scanner.nextLine());
                    }
                    ChineseChecker.myFrame.mNewGame.setEnabled(false);
                    ChineseChecker.isANewGame = true;
                    ChineseChecker.isPositionToGoToAcquired = true;
                } catch (FileNotFoundException e) {
                    // e.printStackTrace();
                } catch (Exception e) {
                    Services.msg("Import failed: " + e.getMessage());
                }
            }

            public void in(String line) {
                String[] temp = line.split(",");
                String piece = temp[0] + "  ";
                int i = Integer.parseInt(temp[1]), j = Integer.parseInt(temp[2]);
                int player = piece.charAt(0) - 65;
                int index = piece.charAt(1) - 48;
                Pieces.pieces[player][index][0] = i;
                Pieces.pieces[player][index][1] = j;
                Pieces.cell[i][j] = piece;
                int[] pos = {i, j};
                ChineseChecker.myFrame.move(piece,pos);
            }
		});
        
        mSave.addActionListener(new ActionListener() {//保存当前游戏
			
        	@Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser();
                chooser.showOpenDialog(null);
                File file = chooser.getSelectedFile();

                PrintWriter writer;
                try {
                    writer = new PrintWriter(file);
                    writer.write(new Output().s);
                    writer.close();
                } catch (FileNotFoundException e) {
                    // e.printStackTrace();
                } catch (Exception e) {
                    Services.msg("Export failed!");
                }
            }

            class Output {
                String s = "";

                Output() {
                    out();
                }

                void out() {
                    int num = ChineseChecker.numberOfPlayers;
                    System.out.println(num);
                    int[] position;
                    s += num;
                    char cur = ChineseChecker.series[ChineseChecker.currentIndex];
                    s += "," + cur;
                    int curIndex = ChineseChecker.currentIndex;
                    s += "," + curIndex;
                    int step = Trace.step;
                    s += "," + step;
                    char[][] player = {null, null, {'A', 'D'}, {'A', 'C', 'E'},
                            {'B', 'C', 'E', 'F'}, null,
                            {'A', 'B', 'C', 'D', 'E', 'F'}};
                    for (int i = 0; i < 10; i++) {
                        for (char j : player[num]) {
                            position = Pieces.getPiecePos(j - 65, i);
                            s += "\n" + j + i + "," + position[0] + "," + position[1];
                        }
                    }
                }
            }
		});
        
        TimerThread timerThread = new TimerThread("Timer");
        timerThread.start();
        
	}
	
	public void showCurrent() {
		panel.add(lblCurrent, JLayeredPane.MODAL_LAYER);
	}
	
	public void move(String pieceToMove, int[] targetPosition) {
        char playerOnTheMove = pieceToMove.charAt(0);
        int index = pieceToMove.charAt(1) - '0';
        int x = Map.map[targetPosition[0]][targetPosition[1]][0];
        int y = Map.map[targetPosition[0]][targetPosition[1]][1];

        pieces[playerOnTheMove - 'A'][index].setBounds(x, y, 39, 39);
    }
	
	public void init(char[] playerSeries) {
        for (char player : playerSeries) {
            if (player == 0)
                return;
            initPlayer(player);
        }
    }

    private void initPlayer(char player) {
        for (int pieceIndex = 0; pieceIndex < 10; pieceIndex++) {
            initPiece(player, pieceIndex);
        }
    }

    private void initPiece(char player, int pieceIndex) {
    	//Image image = 
        Map myMap = new Map();
        int playerIndex = player - 'A';
        int[] pieceSeries = zoneOfPlayer[player - 'A'][pieceIndex];
        int x = myMap.map[pieceSeries[0]][pieceSeries[1]][0];
        int y = myMap.map[pieceSeries[0]][pieceSeries[1]][1];

        pieces[playerIndex][pieceIndex] = new JLabel(new ImageIcon("./res/Piece_" + player + ".PNG"));
        pieces[playerIndex][pieceIndex]
                .addMouseListener(new PieceActionListener("" + player + pieceIndex));

        pieces[playerIndex][pieceIndex].setBounds(x, y, 39, 39);
        panel.add(pieces[playerIndex][pieceIndex], JLayeredPane.MODAL_LAYER);
    }

    public static void showCurrentPlayer() {
    	
        lblPlayer.setIcon(new ImageIcon("./res/Piece_" + ChineseChecker.series[ChineseChecker.currentIndex] + ".PNG"));
        panel.repaint();
    }
	
    
    class TimerThread extends Thread {
        TimerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                lblTime.setText(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
                panel.repaint();
                try {
                    Thread.sleep(0500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}

class Map {//map记录棋盘上每个点在myFrame中的坐标
	//map[i][j][0]表示点(i,j)在棋盘中的横坐标，map[i][j][1]表示点(i,j)在棋盘中的纵坐标
	public static int [][][] map = new int[18][18][2];
	public Map(){//计算各点坐标，并将其存入map中
		for (int i = 5; i <= 5; i++)	
			setSinglePosition(i, 1, 325 + 51 * (i - 5), 780 + 0 * (i - 5));
        for (int i = 5; i <= 6; i++)
            setSinglePosition(i, 2, 325 - 28 + 54 * (i - 5), 780 - 47);
        for (int i = 5; i <= 7; i++)
            setSinglePosition(i, 3, 325 - 28 * 2 + 54 * (i - 5), 780 - 47*2);
        for (int i = 5; i <= 8; i++)
            setSinglePosition(i, 4, 325 - 28 * 3 + 54 * (i - 5), 780 - 47*3);
        for (int i = 1; i <= 13; i++)
            setSinglePosition(i, 5, 0 + 28 * 0 + 54 * (i - 1), 780 - 47*4);
        for (int i = 2; i <= 13; i++)
            setSinglePosition(i, 6, 0 + 28 * 1 + 54 * (i - 2), 780 - 47*5);
        for (int i = 3; i <= 13; i++)
            setSinglePosition(i, 7, 0 + 28 * 2 + 54 * (i - 3), 780 - 47*6);
        for (int i = 4; i <= 13; i++)
            setSinglePosition(i, 8, 0 + 28 * 3 + 54 * (i - 4), 780 - 47*7);
        for (int i = 5; i <= 13; i++)
            setSinglePosition(i, 9, 0 + 28 * 4 + 54 * (i - 5), 780 - 47*8);
        for (int i = 5; i <= 14; i++)
            setSinglePosition(i, 10, 0 + 28 * 3 + 54 * (i - 5), 780 - 47*9);
        for (int i = 5; i <= 15; i++)
            setSinglePosition(i, 11, 0 + 28 * 2 + 54 * (i - 5), 780 - 47*10);
        for (int i = 5; i <= 16; i++)
            setSinglePosition(i, 12, 0 + 28 * 1 + 54 * (i - 5), 780 - 47*11);
        for (int i = 5; i <= 17; i++)
            setSinglePosition(i, 13, 0 + 28 * 0 + 54 * (i - 5), 780 - 47*12);
        for (int i = 10; i <= 13; i++)
            setSinglePosition(i, 14, 325 - 28 * 3 + 54 * (i - 10), 780 - 47*13);
        for (int i = 11; i <= 13; i++)
            setSinglePosition(i, 15, 325 - 28 * 2 + 54 * (i - 11),780 - 47*14);
        for (int i = 12; i <= 13; i++)
            setSinglePosition(i, 16, 325 - 28 * 1 + 54 * (i - 12), 780 - 47*15);
        for (int i = 13; i <= 13; i++)
            setSinglePosition(i, 17, 325 - 28 * 0 + 54 * (i - 13), 780 - 47*16);
	}
	public void setSinglePosition(int i,int j,double x,double y){
		map[i][j][0] = (int)x;
		map[i][j][1] = (int)y;
	}
}

class Pieces {//棋子类
	public static int [][][] pieces = new int [6][10][2];
	public static String [][] cell = new String[20][20];
	public final static String EMPTY_MARK = "**  ";
    public final static String OUTSIDE_MARK = "    ";
    public final static String SPACE_MARK = "  ";
    
	public Pieces(){
		
		for (int i = 1; i <= 17; i++) 
            for (int j = 1; j <= 17; j++) 
                cell[i][j]= OUTSIDE_MARK;//初始化认为所有cell都在棋盘外
		
		/**将所有在棋盘内的cell标记为EMPTY_MARK**/
        for (int j = 1; j <= 4; j++) 
            for (int i = 5; i <= j + 4; i++)
            	cell[i][j]= EMPTY_MARK;
        for (int j = 5; j <= 8; j++) 
            for (int i = j - 4; i <= 13; i++)
            	cell[i][j]= EMPTY_MARK;
        for (int j = 9; j <= 13; j++) 
            for (int i = 5; i <= j + 4; i++)
            	cell[i][j]= EMPTY_MARK;
        for (int j = 14; j <= 17; j++) 
            for (int i = j - 4; i <= 13; i++)
            	cell[i][j]= EMPTY_MARK;
	}
	public void init(){//初始化
		for (int i = 1;i <= 17;++i){
			for (int j = 1;j <= 17;++j){
				if (cell[i][j].charAt(0) >= 65 && cell[i][j].charAt(0) <= 70){
					setPosition(cell[i][j],i,j);
				}
			}
		}
	}
	
	public void setPosition(String s,int x,int y){
		int player = s.charAt(0) - 65;
		int index = s.charAt(1) -48;
		pieces[player][index][0] = x;
        pieces[player][index][1] = y;
	}
	
	public static void setPiecePos(String piece, int[] value) {
        int player = piece.charAt(0) - 65;
        int index = piece.charAt(1) - 48;
        pieces[player][index] = value;
    }
	
	public static int[] getPiecePos(String piece) {
        int player = piece.charAt(0) - 65;
        int index = piece.charAt(1) - 48;
        return new int[]{pieces[player][index][0], pieces[player][index][1]};
    }
	
	public static int[] getPiecePos(int player, int index) {
        return new int[]{pieces[player][index][0], pieces[player][index][1]};
    }
	
	public static void moveAward(String piece, int[] prevPos, int[] postPos) {//前进
        setPiecePos(piece, postPos);
        cell[prevPos[0]][prevPos[1]] = EMPTY_MARK;
        cell[postPos[0]][postPos[1]] = piece + SPACE_MARK;
    }
	
	public static class Next {


        public static int[][] getPossiblePosition(String pieceToMove) {
            /**
             * get all possible positions via adjacent hop/distant/consecutive hops.
             */
            int[] piecePosition = Pieces.getPiecePos(pieceToMove);
            int[][] possiblePosition = distantHopping(piecePosition);

            
            cell[piecePosition[0]][piecePosition[1]] = "    ";
            // apply Breadth-First-Search to accomplish consecutive hops
            for (int k = 0; possiblePosition[k][0] != 0; k++) {
                int[][] recursion = distantHopping(possiblePosition[k]);
                possiblePosition = Services.concatTwoArrays(possiblePosition, recursion);
            }
            possiblePosition = Services.concatTwoArrays(possiblePosition, adjacentHopping(piecePosition));
            
            cell[piecePosition[0]][piecePosition[1]] = pieceToMove;
            return possiblePosition;
        }

        public static int getPossiblePositionCount(String pieceToMove) {
            int count = 0;
            int[][] possiblePosition = getPossiblePosition(pieceToMove);

            for (int k = 0; possiblePosition[k][0] != 0; k++) {
                count++;
            }
            return count;
        }

        private static int[][] adjacentHopping(int[] piecePosition) {

            int[][] possiblePositions = new int[100][2];
            int pointer = 0;
            int[] displacementAdjacent = {-1, 0, 1};
            // (-1,-1)(-1,0)(0,-1)(0,1)(1,0)(1,1)

            for (int i : displacementAdjacent) {
                for (int j : displacementAdjacent) {
                    // check whether the adjacent position is empty
                    String current = cell[piecePosition[0] + i][piecePosition[1] + j];
                    if (current == EMPTY_MARK && i != -j) {
                        possiblePositions[pointer][0] = piecePosition[0] + i;
                        possiblePositions[pointer][1] = piecePosition[1] + j;
                        pointer++;
                    }
                }
            }
            return possiblePositions;
        }

        private static int[][] distantHopping(int[] piecePosition) {
            int[][] possiblePos = new int[100][2];
            int[] displacement = {-1, 0, 1};
            // stores possible direction

            for (int x : displacement) {
                for (int y : displacement) {
                    possiblePos = Services.concatTwoArrays(possiblePos,
                            distantHoppingForOneDirection(x, y, piecePosition, true));
                }
            }

            return possiblePos;
        }

        public static boolean isPosInsideDiagram(String position) {
            return !(position == OUTSIDE_MARK || position == null);
        }
        
        private static int[][] distantHoppingForOneDirection(int x, int y,
                                                             int[] piecePos, boolean isDistantHoppingDisabled) {
            /*
             * x: indicates up or down moveAward in x direction. y: indicates up or down
             * moveAward in x direction
             */
            int[][] possiblePos = new int[100][2];
            int[] displacement = (isDistantHoppingDisabled)?new int[]{1}:new int[]{1, 2, 3, 4, 5, 6, 7, 8};
            // stores possible displacements of coordinates in each direction
            int pointer = 0;
            boolean isDeadDirection;

            for (int i : displacement) {
                // avoid illegal direction
                if (x * y == -1)
                    continue;
                // avoid array index out of bound
                boolean isiInside = (x == 0) || ((x == -1) ? piecePos[0] > 1 + 1 : piecePos[0] < 17 - 1);
                boolean isjInside = (y == 0) || ((y == -1) ? piecePos[1] > 1 + 1 : piecePos[1] < 17 - 1);
                boolean isInside = isiInside
                        && isjInside
                        && isPosInsideDiagram(cell[piecePos[0] + i * 2 * x - 1 * x][piecePos[1] + i* 2 * y - 1 * y])
                        && isPosInsideDiagram(cell[piecePos[0] + i * 2 * x][piecePos[1] + i * 2 * y]);
                if (!isInside)
                    break;
                boolean isAvailable = (isDeadDirection = !(cell[piecePos[0] + i * x][piecePos[1] + i * y] == EMPTY_MARK)
                        && cell[piecePos[0] + i * 2 * x][ piecePos[1] + i * 2 * y] == EMPTY_MARK);

                label1:
                if (isAvailable) {
                    // position between object position and hopped piece
                    // ought to be empty
                    for (int ii = i + 1; ii < 2 * i; ii++) {
                        if (cell[piecePos[0] + ii * x][piecePos[1] + ii * y] != EMPTY_MARK) {
                            isDeadDirection = true;
                            break label1;
                        }
                    }
                    possiblePos[pointer][0] = piecePos[0] + i * 2 * x;
                    possiblePos[pointer][1] = piecePos[1] + i * 2 * y;
                    pointer++;
                }

                if (isDeadDirection)
                    break;
            }
            return possiblePos;
        }

    }
}

class PieceActionListener extends MouseAdapter{//棋子事件侦听
	private final static ImageIcon imgPossiblePosition = new ImageIcon("./res/Piece_Transparant.PNG");
    private static boolean isPieceSelected;
    private static JLabel[] lblPossible = new JLabel[50];
    private int[][] possiblePos = new int[50][2];
    private String piece;
    
    public PieceActionListener(String piece) {
        this.piece = piece + "  ";
    }

    public static void clearPossiblePos() {//清空可能到达的位置
        for (int index = 0; lblPossible[index] != null; index++) {
            ChineseChecker.myFrame.panel.remove(lblPossible[index]);
        }
        ChineseChecker.myFrame.repaint();
        isPieceSelected = false;
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {//鼠标点击事件处理
        if (ChineseChecker.series[ChineseChecker.currentIndex] != piece.charAt(0))
            return;
        if (!ChineseChecker.gameStatus)
            return;

        clearPossiblePos();
        showPossiblePos();
        isPieceSelected = true;
        ChineseChecker.pieceToMove = piece.substring(0, 2);
        ChineseChecker.piecePos = new int[]{Pieces.pieces[piece.charAt(0) - 65][piece.charAt(1) - 48][0], 
        									 Pieces.pieces[piece.charAt(0) - 65][piece.charAt(1) - 48][1]};
        ChineseChecker.isPieceToMoveAcquired = true;
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {//鼠标移动事件处理
        if (!ChineseChecker.gameStatus)
            return;
        if (ChineseChecker.series[ChineseChecker.currentIndex] != piece.charAt(0))
            return;
        if (isPieceSelected)
            return;
        showPossiblePos();
    }

    @Override
    public void mouseExited(MouseEvent arg0) {//鼠标挪开事件处理
        if (isPieceSelected)
            return;
        for (int k = 0; lblPossible[k] != null; k++) {
            ChineseChecker.myFrame.panel.remove(lblPossible[k]);
            ChineseChecker.myFrame.repaint();
        }
    }

    public void showPossiblePos() {//展示可能到达的位置
        possiblePos = Pieces.Next.getPossiblePosition(piece);
        for (int k = 0; k < possiblePos.length && possiblePos[k][0] != 0; k++) {
            lblPossible[k] = new JLabel(imgPossiblePosition);
            lblPossible[k].addMouseListener(new PossiblePositionListener(piece,
                    possiblePos[k]));
            ChineseChecker.myFrame.panel.add(lblPossible[k], JLayeredPane.MODAL_LAYER);
            int[] positionInCanvas = Map.map[possiblePos[k][0]][possiblePos[k][1]];
            lblPossible[k].setBounds(positionInCanvas[0], positionInCanvas[1], 39, 39);
        }
    }
}

class PossiblePositionListener extends MouseAdapter {

    public static String piece;
    public int[] position = new int[2];
    public final static String EMPTY_MARK = "**  ";
    public final static String OUTSIDE_MARK = "    ";
    public final static String SPACE_MARK = "  ";
    PossiblePositionListener(String piece, int[] position) {
        this.piece = piece;
        this.position = position;
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        if (!ChineseChecker.gameStatus)
            return;

        Pieces.cell[Pieces.getPiecePos(piece)[0]][Pieces.getPiecePos(piece)[1]] = EMPTY_MARK;
        Pieces.cell[position[0]][position[1]] = piece + SPACE_MARK;
        
        ChineseChecker.myFrame.move(piece, position);
        PieceActionListener.clearPossiblePos();
        ChineseChecker.posToGoTo = position;
        ChineseChecker.isPositionToGoToAcquired = true;
    }

}

class Services {

    public static void msg(String content) {
        JOptionPane.showMessageDialog(null, content, "Chinese Checkers",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static String in(String content) {
        return JOptionPane.showInputDialog(null, content, "Chinese Checkers",
                JOptionPane.INFORMATION_MESSAGE);
    }


    public static int[][] concatTwoArrays(int[][] targetArray, int[][] temporaryArray) {
        /** combine two 2D arrays into one. */
        int pointer = 0;// point to the operating index of a1
        while (targetArray[pointer][0] != 0) {
            pointer++;
        }
        for (int j = 0; ; j++) {
            boolean isRepeated = false;
            if (temporaryArray[j][0] == 0)
                break;
            for (int i = 0; i < pointer; i++)
                if (temporaryArray[j][0] == targetArray[i][0] && temporaryArray[j][1] == targetArray[i][1]) {
                    isRepeated = true;
                    break;
                }
            if (!isRepeated)
                targetArray[pointer++] = temporaryArray[j];
        }
        return targetArray;
    }
}

class Timer {

    private long startingTime;
    private long interruptedTime;
    private boolean paused;

    public Timer() {
        this.startingTime = System.currentTimeMillis();
    }

    public long listen() {
        if (paused)
            return (30000 - interruptedTime + startingTime) / 1000;
        long cur = System.currentTimeMillis();
        return (30000 - cur + startingTime) / 1000;
    }

    public void pause() {
        this.interruptedTime = System.currentTimeMillis();
        this.paused = true;
    }
}

class Trace {

    public static final int MAX_STACK_SIZE = 1000;
    public static int step = 1;
    public static boolean isSkipped;
    public static String[] pieceStack = new String[MAX_STACK_SIZE];
    public static int[][] prevPosStack = new int[MAX_STACK_SIZE][2];
    public static int[][] postPosStack = new int[MAX_STACK_SIZE][2];

    public boolean isSkipped() {
        return isSkipped;
    }
    
    public static void recordStep(String piece, int[] prev, int[] post) {
        pieceStack[step] = piece;
        prevPosStack[step] = prev;
        postPosStack[step] = post;
    }

    public static void nextStep() {
        ChineseChecker.currentIndex= Trace.step % ChineseChecker.numberOfPlayers;
    }
}
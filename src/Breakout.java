import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

// Names: Jonathan Cheung and Hansen Rao
// Date (final due date): 
// Purpose: Breakout game 

public class Breakout extends JPanel implements Runnable, ActionListener, MouseListener, MouseMotionListener, KeyListener {

	//Variables:
	static JFrame frame;
    Thread thread;
    Graphics g;
	final int FPS = 60;
	final int screenWidth = 600, screenHeight = 800;
	static Clip hit, background, exit;
	int themeCount = 1;
	int controlCount = -1;
	int [] score = new int [5];
	int difficulty;
	int scoreCount;
	int endless = 1;
	int lives = 3;
	int musicCount = 1;
	
	int[][] board = new int [4][10];
	Rectangle[][] rectangles = new Rectangle[4][10];
	
	boolean isRun = true;
	boolean playing = true;
	boolean win = false;
	
	int screenCount = 1;
    
	// Colors
	private final Color TEAL = new Color (0,255,255);
	private final Color ORANGE = new Color (255,165,0);
	private final Color CRIMSON = new Color (220,20,90);
	
	public Color randomColour() {
		int r = new Random().nextInt(256);
		int g = new Random().nextInt(256);
		int b = new Random().nextInt(256);
		return new Color(r,g,b);
	}
	
	// Paddle
	int paddlePos;
    double paddleX, paddleVelX;
	boolean paddleLeft, paddleRight;
    Rectangle paddle = new Rectangle((int)paddleX, 700, 50, 10);
    // Ball
    double ballPosX, ballPosY, ballVelX, ballVelY, defaultBallPosX, defaultBallPosY, defaultBallVelX, defaultBallVelY; 
	boolean ballActivated;
	boolean ballUp, ballLeft;
    Rectangle ball = new Rectangle((int)ballPosX, (int)ballPosY, 5, 5);
    
	public Breakout() {
		setPreferredSize(new Dimension(screenWidth, screenHeight));
		setVisible(true);
		
		thread = new Thread(this);
		thread.start();
	}
	
	public void initialize () {
    	
		newGame();
		
		// Set up the Menu
		// Set up the Game Menu
		JMenu gameMenu = new JMenu ("Game");
		JMenu switchTheme;
		JMenu changeDiff = new JMenu ("Change difficulty");
		JMenu optionMenu = new JMenu("Options");
		
		// Set up the Game MenuItems
		JMenuItem newOption, exitOption, toggleMusic, changeControl, origTheme, darkTheme, randomTheme;
		newOption = new JMenuItem ("New");
		exitOption = new JMenuItem ("Exit");
		switchTheme = new JMenu ("Switch Theme");
		toggleMusic = new JMenuItem ("Toggle Music");
		changeControl = new JMenuItem ("Change Controls");
		
		//Setup theme submenu
		origTheme = new JMenuItem ("Original Theme");
		darkTheme = new JMenuItem ("Dark Theme");
		randomTheme = new JMenuItem ("Seizure");
		switchTheme.add(origTheme);
		switchTheme.add(darkTheme);
		switchTheme.add(randomTheme);
		
		// Add each MenuItem to the Game Menu (with a separator)
		gameMenu.add (newOption);
		gameMenu.addSeparator ();
		gameMenu.add (exitOption);

		optionMenu.add (switchTheme);
		optionMenu.addSeparator ();
		optionMenu.add(toggleMusic);
		optionMenu.addSeparator ();
		optionMenu.add(changeControl);
		
		//setup change diff menu
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem easy, medium, hard, endless;
		easy = new JRadioButtonMenuItem ("Easy");
		medium = new JRadioButtonMenuItem ("Medium");
		medium.setSelected(true);
		hard = new JRadioButtonMenuItem ("Hard");
		endless = new JRadioButtonMenuItem ("Endless Mode");
		group.add(easy);
		group.add(medium);
		group.add(hard);
		changeDiff.add(easy);
		changeDiff.add(medium);
		changeDiff.add(hard);
		changeDiff.add(endless);
		
		JMenuBar menuBar = new JMenuBar ();
		menuBar.add (gameMenu);
		menuBar.add (optionMenu);
		menuBar.add (changeDiff);
		frame.setJMenuBar (menuBar);
		
		newOption.setActionCommand ("New");
		newOption.addActionListener (this);
		exitOption.setActionCommand ("Exit");
		exitOption.addActionListener (this);
		origTheme.setActionCommand ("Original Theme");
		origTheme.addActionListener (this);
		darkTheme.setActionCommand ("Dark Theme");
		darkTheme.addActionListener (this);
		randomTheme.setActionCommand ("Seizure");
		randomTheme.addActionListener (this);
		changeControl.setActionCommand ("Change Controls");
		changeControl.addActionListener (this);
		easy.setActionCommand ("Easy");
		easy.addActionListener (this);
		medium.setActionCommand ("Medium");
		medium.addActionListener (this);
		hard.setActionCommand ("Hard");
		hard.addActionListener (this);
		endless.setActionCommand ("Endless Mode");
		endless.addActionListener (this);
		toggleMusic.setActionCommand ("Toggle Music");
		toggleMusic.addActionListener (this);
	}

    public void newGame () {
    
    	lives = 3;
    	playing = true;
    	screenCount = 1;
    	win = false;
    	
    	// Paddle
        paddleX = 275; paddleVelX = 10; 
    	paddleLeft = false; paddleRight = false;
    	paddle = new Rectangle((int)paddleX, 700, 50, 10);
    	paddleVelX = 10;
    	
        // Ball
    	defaultBallPosX = 295; ballPosX = defaultBallPosX; 
    	defaultBallPosY = 600; ballPosY = defaultBallPosY; 
        
        if (difficulty == 1) {
        	 defaultBallVelX = 2.5; 
        	 defaultBallVelY = -2.5; 
        }
        else if (difficulty == 3) {
        	defaultBallVelX = 8;
        	defaultBallVelY = -8;
        }
        else {
        	defaultBallVelX = 5;
        	defaultBallVelY = -5;
        }
        ballVelX = defaultBallVelX;
        ballVelY = defaultBallVelY;
        
        ballActivated = false;
    	if (ballVelY > 0) 
    		ballUp = false;
    	else
    		ballUp = true;
    	if (ballVelX > 0)
    		ballLeft = false;
    	else
    		ballLeft = true;
    	ball = new Rectangle((int)ballPosX, (int)ballPosY, 5, 5);
    	
    	// setting up board
    	for (int row=0; row < board.length; row++) 
			for (int col=0; col < board[row].length; col++) 
				board[row][col] = 1;
		
    	// setting up rectangles
    	for (int row=0; row < board.length; row++) 
			for (int col=0; col < board[row].length; col++) 
				rectangles[row][col] = new Rectangle(col * 50 + 50, row * 12 + 150, 48, 10);
    	
    	// resetting score
    	for (int i = 0; i < score.length; i++)
    		score[i] = 0;
    	
    	scoreCount = 0;
	}
    
    public void resetBP() {
    	// Paddle
        paddleX = 275; paddleVelX = 10; 
    	paddleLeft = false; paddleRight = false;
    	paddle = new Rectangle((int)paddleX, 700, 50, 10);
    	paddleVelX = 10;
    	
        // Ball
    	defaultBallPosX = 295; ballPosX = defaultBallPosX; 
    	defaultBallPosY = 600; ballPosY = defaultBallPosY; 
        
        if (difficulty == 1) {
        	 defaultBallVelX = 2.5; 
        	 defaultBallVelY = -2.5; 
        }
        else if (difficulty == 3) {
        	defaultBallVelX = 8;
        	defaultBallVelY = -8;
        }
        else {
        	defaultBallVelX = 5;
        	defaultBallVelY = -5;
        }
        ballVelX = defaultBallVelX;
        ballVelY = defaultBallVelY;
        
        ballActivated = false;
    	if (ballVelY > 0) 
    		ballUp = false;
    	else
    		ballUp = true;
    	if (ballVelX > 0)
    		ballLeft = false;
    	else
    		ballLeft = true;
    	ball = new Rectangle((int)ballPosX, (int)ballPosY, 5, 5);
    }
    
	// update both ball and paddle
	public void update () {
		movePaddle();
		moveBall();
		keepInBound();
		checkCollision();
		checkBoardCollision();
		if (endless == -1)
			boardShift();
	}

	// moves the paddle left and right
	public void movePaddle() {
		if (controlCount == 1 ) {
			if(paddleLeft)
				paddle.x -= paddleVelX;
			else if(paddleRight)
				paddle.x += paddleVelX;
		}
		else
			paddle.x = (int) ((paddlePos - (paddle.getWidth()/2)) - 8);
	}

	// not allowing the paddle to go off screen (too far left or right)
	public void keepInBound() {
		if(paddle.x < 0)
			paddle.x = 0;
		else if(paddle.x > screenWidth - paddle.width)
			paddle.x = screenWidth - paddle.width;
	}
	
	// moves the ball 																	
	public void moveBall() {
		if (ballActivated == true) {
			ball.x += ballVelX;
			ball.y += ballVelY;
			ballLeft = ballVelX < 0;
		}
	}
	
	// Collision checker for blocks on board and score counter
	public void checkBoardCollision () {

		int digit = 0;
		int digit2 = 1;
		int digit3 = 2;

		for (int row = 0; row < board.length; row++)
			for (int col = 0; col < board[row].length; col++) {
				if (ball.intersects(rectangles[row][col]) && board[row][col] == 1) {
					//delete brick if hit
					board[row][col] = 0;
					
					//add to score count for board shift (varies with diff)
					if (difficulty == 1)
						scoreCount += 100;
					else if (difficulty == 3)
						scoreCount += 300;
					else
						scoreCount += 200;

					//save score to array when hit
					if (difficulty == 1)
						score[digit3] += 1;
					else if (difficulty == 3)
						score[digit3] += 3;
					else
						score[digit3] += 2;

					if (score[digit3] > 9) {
						score[digit3] -= 10;
						score[digit2] += 1;
					}

					if (score[digit2] > 9) {
						score[digit2] = 0;
						score[digit] += 1; 
					}

					//change ballVelY depending on where it hits the brick (move down if ball hits the bottom)
					if (ball.y + ball.height/2 > rectangles[row][col].y + rectangles[row][col].height/2 && ballUp) {
						ballVelY *= -1;
						ballUp = false;
					}

					//change ballVelY depending on where it hits the brick (move up if ball hits top)
					else if (ball.y + ball.height/2 < rectangles[row][col].y + rectangles[row][col].height/2 && !ballUp) {
						ballVelY *= -1;
						ballUp = true;
					}

					checkWin();
					
					hit.setFramePosition (0);
					hit.start();
				}
			}
	}
	
	// Collision checker for the ball ------------------------------------------                                                  
	public void checkCollision() {
		
		if (ball.intersects(paddle)) {
			//prevent looping ballVelY changing
			if (ballUp == false) {
				ballVelY += ballVelY * 0.025; // will gradually add speed as the game goes on; becomes harder
				ballVelY *= -1;
				ballUp = true;
			}
			
			double bCentre = (ball.x + (ball.width/2));
			//change directions if ball hits certain place on paddle
			
			if (bCentre < (paddle.x + (paddle.width/5*1))) {
				ballVelX = defaultBallVelX * -1 - defaultBallVelX * 0.25;
			}
			else if (bCentre < (paddle.x + (paddle.width/5*2))) {
				ballVelX = defaultBallVelX * -1; 
			}
			else if (bCentre < (paddle.x + (paddle.width/5*4))) {
				ballVelX = defaultBallVelX * 1; 
			}
			else if (bCentre < (paddle.x + (paddle.width/5*5))) {
				ballVelX = defaultBallVelX * 1 + defaultBallVelX * 0.25;
			}
			
			hit.setFramePosition (0);
		    hit.start();
		}
		
		//if ball hits floor, stop game loop
		else if (ball.y > 805 && ballActivated == true) {
			ballActivated = false;
			ballVelX = 0; ballVelY = 0;
			lives--;
			resetBP();
		}
		
		//if ball hits roof, bounce down
		if (ball.y <= 100) {
			if (ballUp == true) {
				ballVelY *= -1;
				ballUp = false;
				hit.setFramePosition (0);
			    hit.start();
			}
		}
		
		//if ball hits left wall
		else if (ball.x <= 0) {
			if (ballLeft == true) {
				ballVelX *= -1;
				ballLeft = false;
				hit.setFramePosition (0);
			    hit.start();
			}
		}
		
		//if ball hits right wall
		else if (ball.x + ball.getWidth() >= 600) {
			if (ballLeft == false) {
				ballVelX *= -1;
				ballLeft = true;
				hit.setFramePosition (0);
			    hit.start();
			}
		}
	}
	
	public void checkWin() {
		win = true;
		for (int row=0; row<board.length; row++) 
			for (int col=0; col<board[row].length; col++) 
				if (board[row][col] == 1) 
					win = false;
	}
	
	// ---------------------------------------------------------------------
	
	//calls upon board shifter if needed
	public void boardShift() {
		if (difficulty == 1) {
			if (scoreCount == 500) {
				boardShifter();
				scoreCount = 0;
			}
		}	
		else if (difficulty == 3) {
			if (scoreCount == 1500) {
				boardShifter();
				scoreCount = 0;
			}	
		}
			
		else {
			if (scoreCount == 1000) {
				boardShifter();
				scoreCount = 0;
			}
		}	
		
	}
	
	//board shifter
	public void boardShifter () {
		//shifting if scoreCount reaches 1000 and resetting
		for (int row = board.length-1; row > 0; row--) {
			for (int col = 0; col < board[row].length; col++) {
				board[row][col] = board[row-1][col];
			}
		}
		//creating new row
		for (int col = 0; col < board[0].length; col++)
			board[0][col] = 1;
	}
	
	public void mouseClicked(MouseEvent e) {
		if (playing == true && screenCount == 0)
			ballActivated = true;
		
		double mX = e.getX(), mY = e.getY();
		
		if (screenCount == 1) { // title screen
			if (mX >= 156 && mX <= 456 && mY >= 428 && mY <= 502) { // game start
				screenCount = 0;
				playing = true;
			}
			else if (mX >= 196 && mX <= 415 && mY >= 533 && mY <= 606) { // instructions screen
				screenCount = 4;
			}
			else if (mX >= 207 && mX <= 406 && mY >= 638 && mY <= 712) { // closes game (exit button)
				//exit sound
				//delay
				exit.start();
				try
				{
					Thread.sleep (500);
				}
				catch (InterruptedException e1)
				{
				}
				System.exit(0);	
			}
		}
		
		else if (screenCount == 2 || screenCount == 3) { // win screen button
			if (mX >= 158 && mX <= 458 && mY >= 489 && mY <= 649) // new game
				newGame();
		}
		
		else if (screenCount == 4) { // currently on instructions screen
			if (mX >= 0 && mX <= 158 && mY >= 780 && mY <= 827) 
				screenCount = 1;
			else if (mX >= 458  && mX <= 605  && mY >= 780 && mY <= 827) {
				screenCount = 0;
				playing = true;
			}
		}			
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
	
	public void actionPerformed (ActionEvent event)
	{
		String eventName = event.getActionCommand ();
		if (eventName.equals ("New"))
		{
			newGame();
		}
		
		else if (eventName.equals ("Exit"))
		{
			exit.start();
			try
			{
				Thread.sleep (500);
			}
			catch (InterruptedException e)
			{
			}
	    	System.exit(0);
		}
		
		else if (eventName.equals("Original Theme")) {
			themeCount = 1;
		}
		
		else if (eventName.equals("Dark Theme")) {
			themeCount = 2;
		}
		else if (eventName.equals("Seizure")) {
			themeCount = 3;
		}
		else if (eventName.equals("Change Controls")) {
			controlCount *= -1;
		}
		else if (eventName.equals("Easy")) {
			difficulty = 1;
			newGame();
		}
		else if (eventName.equals("Medium")) {
			difficulty = 2;
			newGame();
		}
		else if (eventName.equals("Hard")) {
			difficulty = 3;
			newGame();
		}
		else if (eventName.equals("Endless Mode")) {
			endless *= -1;
			newGame();
		}
		else if (eventName.equals("Toggle Music")) {
			musicCount *= -1;
			if (musicCount == 1) {
				background.start();
				background.loop(Clip.LOOP_CONTINUOUSLY);
			}
			else if (musicCount == -1)
				background.stop();	
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
			if(key == KeyEvent.VK_LEFT) {
				paddleLeft = true;
				paddleRight = false;
			}
			else if(key == KeyEvent.VK_RIGHT) {
				paddleRight = true;
				paddleLeft = false;
			}
			else if(key == KeyEvent.VK_F1) { // F1 is auto win
				playing = false;
				win = true;
				screenCount = 3; // convert to win screen
			}
			else if(key == KeyEvent.VK_F2) {// F2 is auto lose 
				playing = false;
				win = false;
				screenCount = 2; // convert to lose screen
			}
	}

	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if(key == KeyEvent.VK_LEFT)
			paddleLeft = false;
		else if(key == KeyEvent.VK_RIGHT)
			paddleRight = false;
	}

	public void mouseMotionListener(){  
        addMouseMotionListener(this);  
        
    }  
	public void mouseDragged(MouseEvent e) {
	
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		paddlePos = e.getX();
	}
	
	//draws components
    public void paintComponent (Graphics g) {
    	// Creating the various different screens
    	Font titleFont = new Font("Atari Classic", Font.BOLD, 60);
    	Font playFont = new Font("Atari Classic", Font.BOLD, 50);
    	Font instructionFont = new Font("Atari Classic", Font.BOLD, 30);
    	Font exitFont = new Font ("Atari Classic", Font.BOLD, 40);
    	Font smallFont = new Font ("Atari Classic", Font.PLAIN, 20);
    	
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

    	if (screenCount == 1) { // title screen
        	g2.setColor(Color.BLACK);
        	g2.fillRect(0, 0, 600, 800);
        	g2.setColor(Color.WHITE);
        	g2.setFont(titleFont);
        	g2.drawString("BREAKOUT", 55, 275);
        	for (int row = 0; row < 4; row ++) 
        		for (int col = 0; col < 7; col++) {
        			if (row == 0)
        				g2.setColor(Color.RED);
        			else if (row == 1)
        				g2.setColor(ORANGE);
        			else if (row == 2)
        				g2.setColor(Color.GREEN);
        			else 
        				g2.setColor(Color.YELLOW);
    				g2.fillRect(col * 65 + 73, row * 15 + 100, 60, 10);
        		}
        	g2.setColor(Color.WHITE);
        	g2.fillOval(385, 230, 22, 42);
        	// button background
        	g2.fillRect(150, 375, 300, 75); // play
        	g2.fillRect(190, 480, 220, 75); // instructions
        	g2.fillRect(200, 585, 200, 75); // exit
        	g2.setColor(Color.DARK_GRAY);
        	g2.fillRect(0, 320, 600, 5);
        	// button text
        	g2.setColor(Color.BLACK);
        	g2.setFont(playFont);
        	g2.drawString("PLAY", 200, 432);
        	g2.setFont(instructionFont);
        	g2.drawString("HOW TO", 205, 530);
        	g2.setFont(exitFont);
        	g2.drawString("EXIT", 220, 640);
    	}
    	
    	else if (screenCount == 2) { // lose screen
        	g2.setColor(Color.BLACK);
        	g2.fillRect(0, 0, 600, 800);
        	g2.setColor(Color.WHITE);
        	g2.setFont(titleFont);
        	g2.drawString("YOU", 190, 270);
        	g2.drawString("LOSE!!", 110, 350);
        	g2.drawRect(150, 435, 300, 160);
        	g2.setFont(exitFont);
        	g2.drawString("Play", 210, 490);
        	g2.drawString("Again", 200, 550);
    	}
    	else if (screenCount == 3) { // win screen
        	g2.setColor(Color.BLACK);
        	g2.fillRect(0, 0, 600, 800);
        	g2.setColor(Color.WHITE);
        	g2.setFont(titleFont);
        	g2.drawString("YOU", 190, 270);
        	g2.drawString("WIN!!!", 110, 350);
        	g2.drawRect(150, 435, 300, 160);
        	g2.setFont(exitFont);
        	g2.drawString("Play", 210, 490);
        	g2.drawString("Again", 200, 550);
    	}
    	else if (screenCount == 4) { // instructions screen
        	g2.setColor(Color.BLACK);
        	g2.fillRect(0, 0, 600, 800);

        	// buttons
        	g2.setColor(Color.WHITE);
        	g2.fillRect(0, 725, 150, 50);
        	g2.fillRect(450, 725, 150, 50);
        	g2.setColor(Color.BLACK);
        	g2.setFont(instructionFont);
        	g2.drawString("Back", 10, 760);
        	g2.drawString("Play", 462, 760);
        	//text
        	g2.setColor(Color.WHITE);
        	g2.setFont(exitFont);
        	g2.drawString("INSTRUCTIONS:", 30, 85); 
        	g2.setFont(smallFont);
        	g2.drawString("1. Mouse left and right", 30, 160);
        	g2.drawString("to move paddle", 90, 190);
        	g2.drawString("2. You can also use ", 30, 250);
        	g2.drawString("keyboard! (change in the", 90, 280);
			g2.drawString("options tab on the top)", 90, 310);
			g2.drawString("3. Click left mouse button", 30, 370);
			g2.drawString("to make the ball move.", 90, 400);
        	g2.drawString("4. Themes are available to", 30, 460);
        	g2.drawString("choose from in settings!", 90, 490);
        	g2.drawString("5. Choose endless mode to", 30, 550);
        	g2.drawString("play endlessly! :D", 90, 580);
        	g2.drawString("6. You're ready to play", 30, 640);
        	g2.drawString("this game. Good luck!", 90, 670);
        	// line
        	g2.setColor(Color.RED);
        	g2.fillRect(0, 110, 600, 5);
    	}
    	// -------------------------------------------------
    	
    	else { // actually drawing regular game
	    	g2.setColor(Color.BLACK);
	    	g2.fillRect(0, 0, screenWidth, screenHeight);
	    	g2.setColor(Color.WHITE);
	    	g2.fill(ball);
	    	g2.setColor(Color.DARK_GRAY);
	    	g2.fillRect(0, 95, 600, 5);
	    	if (themeCount == 1)
	    		g2.setColor(TEAL);
	    	else if (themeCount == 2)
	    		g2.setColor(Color.black);
	    	else if (themeCount == 3)
	    		g2.setColor(randomColour());
	    	g2.fill(paddle);
	    	drawScore(g);
	    	
	    	//draw board
	        for (int row=0; row < board.length; row++) 
	        	for (int col=0; col < board[row].length; col++) {
	        		if (board[row][col] == 1) {
	        			if (themeCount == 1 ) {
	        				if (row == 0)
	        					g2.setColor(Color.RED);
	        				else if (row == 1)      
	        					g2.setColor(ORANGE);
	        				else if (row == 2)
	        					g2.setColor(Color.GREEN);
	        				else if (row == 3)
	        					g2.setColor(Color.YELLOW);
	        			}
	
	        			else if (themeCount == 2)
	        				g2.setColor(Color.BLACK);
	        			
	        			else if (themeCount == 3)
	        				g2.setColor(randomColour());
	
	        			g2.fill(rectangles[row][col]);	
	        		}
	        	}
    	}
    }
    
    public void drawScore (Graphics g2) {
    	g2.setColor(CRIMSON);
    	for (int i=1; i<=lives; i++) {
        	g2.fillRect(i*50, 40, 15, 15);
    	}
    	
    	g2.setColor(Color.white);
    	
    	int x = 0;
    	
    	for (int i = 0; i < 3; i++) {
    		if (score[i] == 0)
    			draw0 (g2, x);
    		else if (score[i] == 1)
    			draw1 (g2, x);
    		else if (score[i] == 2)
    			draw2 (g2, x);
    		else if (score[i] == 3)
    			draw3 (g2, x);
    		else if (score[i] == 4)
    			draw4 (g2, x);
    		else if (score[i] == 5)
    			draw5 (g2, x);
    		else if (score[i] == 6)
    			draw6 (g2, x);
    		else if (score[i] == 7)
    			draw7 (g2, x);
    		else if (score[i] == 8)
    			draw8 (g2, x);
    		else if (score[i] == 9)
    			draw9 (g2, x);
    				
    		x += 30;
    	}


    	//Draw no. 0 at last 2 digits
    	g2.fillRect(490, 30, 20, 5);
    	g2.fillRect(490, 30, 5, 30);
    	g2.fillRect(490, 60, 20, 5);
    	g2.fillRect(505, 30, 5, 30);
    	
    	g2.fillRect(520, 30, 20, 5);
    	g2.fillRect(520, 30, 5, 30);
    	g2.fillRect(520, 60, 20, 5);
    	g2.fillRect(535, 30, 5, 30);
    	
    }
    	
    	//Draw no. 0
    public void draw0 (Graphics g2, int x) {
    	g2.fillRect(400 + x, 30, 20, 5);
    	g2.fillRect(400 + x, 30, 5, 30);
    	g2.fillRect(400 + x, 60, 20, 5);
    	g2.fillRect(415 + x, 30, 5, 30);
    }
    
    	//Draw no. 1
    public void draw1 (Graphics g2, int x) {
    	g2.fillRect(415 + x, 30, 5, 35);
    }
    	
    	//Draw no. 2
    public void draw2 (Graphics g2, int x) {
    	g2.fillRect(400+x, 30, 20, 5);
    	g2.fillRect(400+x, 50, 5, 15);	
    	g2.fillRect(400+x, 60, 20, 5);
    	g2.fillRect(415+x, 30, 5, 15);
    	g2.fillRect(400+x, 45, 20, 5);	
    }
    	
    	//Draw no. 3
    public void draw3 (Graphics g2, int x) {
    	g2.fillRect(400+x, 30, 20, 5);
    	g2.fillRect(415+x, 50, 5, 15);	
    	g2.fillRect(400+x, 60, 20, 5);
    	g2.fillRect(415+x, 30, 5, 15);
    	g2.fillRect(400+x, 45, 20, 5);
    }
    	
    	//Draw no. 4
    public void draw4 (Graphics g2, int x) {	
    	g2.fillRect(415+x, 30, 5, 35);	
    	g2.fillRect(400+x, 30, 5, 15);
    	g2.fillRect(400+x, 45, 20, 5);
    }
    	
    	//Draw no. 5
    public void draw5 (Graphics g2, int x) {
    	g2.fillRect(400+x, 30, 20, 5);
    	g2.fillRect(415+x, 50, 5, 15);	
    	g2.fillRect(400+x, 60, 20, 5);
    	g2.fillRect(400+x, 30, 5, 15);
    	g2.fillRect(400+x, 45, 20, 5);
    }
    	
    	//Draw no. 6
    public void draw6 (Graphics g2, int x ) {
    	g2.fillRect(400+x, 30, 20, 5);
    	g2.fillRect(415+x, 50, 5, 15);	
    	g2.fillRect(400+x, 60, 20, 5);
    	g2.fillRect(400+x, 30, 5, 30);
    	g2.fillRect(400+x, 45, 20, 5);
    }
    	
    	//Draw no. 7
    public void draw7 (Graphics g2, int x) {
    	g2.fillRect(400+x, 30, 20, 5);
    	g2.fillRect(415+x, 30, 5, 35);
    }
    	
    	//Draw no. 8
    public void draw8 (Graphics g2, int x) {
    	g2.fillRect(400+x, 30, 20, 5);
    	g2.fillRect(415+x, 30, 5, 30);	
    	g2.fillRect(400+x, 60, 20, 5);
    	g2.fillRect(400+x, 30, 5, 30);
    	g2.fillRect(400+x, 45, 20, 5);
    }
    	
    	//Draw no. 9
    public void draw9 (Graphics g2, int x) {
    	g2.fillRect(400+x, 30, 20, 5);
    	g2.fillRect(415+x, 30, 5, 30);	
    	g2.fillRect(400+x, 60, 20, 5);
    	g2.fillRect(400+x, 30, 5, 15);
    	g2.fillRect(400+x, 45, 20, 5);
    }
    
	// Game loop method
    public void run () {
		initialize();
		
    	while (isRun) {
			this.repaint();
			if (lives == 0 && playing == true) {
				playing = false;
				win = false;
				screenCount = 2; // convert to lose screen
			}
			else if (win && playing == true) {
				playing = false;
				win = true;
				screenCount = 3; // convert to win screen
			}
			if (playing == true) {
    			update();
				try {
					Thread.sleep(1000/FPS);
				} catch(Exception e) {
					e.printStackTrace();
				}
    		}
		}
	}
    
	// Main
	public static void main(String[] args) {
		frame = new JFrame ("Breakout");
		Breakout myPanel = new Breakout ();
		
		try {
			AudioInputStream sound = AudioSystem.getAudioInputStream(new File ("hit.wav"));
			hit = AudioSystem.getClip();
			hit.open(sound);
			sound = AudioSystem.getAudioInputStream(new File ("background.wav"));
			background = AudioSystem.getClip();
			background.open(sound);
			sound = AudioSystem.getAudioInputStream(new File ("exit.wav"));
			exit = AudioSystem.getClip();
			exit.open(sound);
		} 
		catch (Exception e) {
		}
		
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("font.ttf")));
		}
		catch (IOException|FontFormatException e) {
		}
		
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing (java.awt.event.WindowEvent windowEvent) {
		    	exit.start();
				try
				{
					Thread.sleep (500);
				}
				catch (InterruptedException e)
				{
				}
		    	System.exit(0);
		    }   
		});	
		
		frame.add (myPanel);
		frame.addKeyListener(myPanel);
		frame.addMouseListener(myPanel);
		frame.addMouseMotionListener(myPanel);
		frame.setVisible (true);
		frame.pack ();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		background.start();
		background.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
}
package Othello;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.text.Position;

public class GamePanel extends JPanel implements Runnable, MouseListener {

    public static final int WIDTH = 600 + 40;
    public static final int HEIGHT = 600 + 120 + 40;
    
    private BufferedImage image;
    private Graphics2D g;
    
    private Thread thread;
    private boolean running;

    private int FPS = 30;
    private int targetTime = 1000 / FPS;

    private boolean player1Turn;

    private List<Integer> player1Node;
    private List<Integer> player2Node;

    private int x, y;

    private int player1Score;
    private int player2Score;

    private boolean restart;

    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
    }

    public void addNotify() {
        super.addNotify();
        if(thread == null) {
            thread = new Thread(this);
            thread.start();
        }
        addMouseListener(this);
    }

    public void run() {
        init();

        long startTime;
        long urdTime;
        long waitTime;

        while(running) {
            startTime = System.nanoTime();
            update();
            render();
            draw();

            urdTime = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - urdTime;
            if(waitTime < 0) waitTime = targetTime;

            try {
                Thread.sleep(waitTime);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void init() {
        running = true;
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();

        player1Turn = true;

        player1Node = new ArrayList<>();
        player2Node = new ArrayList<>();
        
        player1Node.add(44);
        player1Node.add(55);
        player2Node.add(45);
        player2Node.add(54);

        x = 4; 
        y = 4;

        player1Score = 2;
        player2Score = 2;

        restart = false;
    }

    private void change_state(boolean first, int value, int step, boolean vertical, int location) {
        List<Integer> flip;

        if(first && !vertical) {
            flip = new ArrayList<>();
            while(player2Node.contains(value) && Math.abs((value - step) / 10 - value / 10) == 1) {
                flip.add(value);
                value += step;
                if(player1Node.contains(value)) {
                    player1Score += flip.size();
                    player2Score -= flip.size();
                    for(int i = 0; i < flip.size(); i++) {
                        player1Node.add(flip.get(i));
                        player2Node.remove(flip.get(i));
                    }
                    break;
                }
            }    
        } 
        else if(!first && !vertical) {
            flip = new ArrayList<>();
            while(player1Node.contains(value) && Math.abs((value - step) / 10 - value / 10) == 1) {
                flip.add(value);
                value += step;
                if(player2Node.contains(value)) {
                    player2Score += flip.size();
                    player1Score -= flip.size();
                    for(int i = 0; i < flip.size(); i++) {
                        player2Node.add(flip.get(i));
                        player1Node.remove(flip.get(i));
                    }
                    break;
                }
            } 
        }
        else if(first && vertical) {
            flip = new ArrayList<>();
            while(player2Node.contains(value) && value / 10 == location / 10) {
                flip.add(value);
                value += step;
                if(player1Node.contains(value)) {
                    player1Score += flip.size();
                    player2Score -= flip.size();
                    for(int i = 0; i < flip.size(); i++) {
                        player1Node.add(flip.get(i));
                        player2Node.remove(flip.get(i));
                    }
                    break;
                }
            } 
        }
        else if(!first && vertical) {
            flip = new ArrayList<>();
            while(player1Node.contains(value) && value / 10 == location / 10) {
                flip.add(value);
                value += step;
                if(player2Node.contains(value)) {
                    player2Score += flip.size();
                    player1Score -= flip.size();
                    for(int i = 0; i < flip.size(); i++) {
                        player2Node.add(flip.get(i));
                        player1Node.remove(flip.get(i));
                    }
                    break;
                }
            } 
        }
    }

    private void update() {
        
        if(player1Score + player2Score == 100) {
            restart = true;
        }

        int location = x + y * 10;

        int upperLeft = location - 11;
        int upperRight = location - 9;
        int up = location - 10;
        int right = location + 1;
        int left = location - 1;
        int lowerLeft = location + 9;
        int lowerRight = location + 11;
        int down = location + 10;

        if(!player1Node.contains(location) && !player2Node.contains(location)) {
            
            if(player1Turn) {
                player1Node.add(location);
                player1Score++;
                change_state(true, upperLeft, -11, false, location);
                change_state(true, upperRight, -9, false, location);   
                change_state(true, up, -10, false, location);
                change_state(true, left, -1, true, location);
                change_state(true, right, 1, true, location);
                change_state(true, lowerRight, 11, false, location);
                change_state(true, lowerLeft, 9, false, location);
                change_state(true, down, 10, false, location);
            }
    
            else {
                player2Node.add(location);
                player2Score++;
                change_state(false, upperLeft, -11, false, location);
                change_state(false, upperRight, -9, false, location);   
                change_state(false, up, -10, false, location);
                change_state(false, left, -1, true, location);
                change_state(false, right, 1, true, location);
                change_state(false, lowerRight, 11, false, location);
                change_state(false, lowerLeft, 9, false, location);
                change_state(false, down, 10, false, location);
            }
            player1Turn = !player1Turn;
        }

    }

    private void render() {
        // HBox
        g.setColor(new Color(153, 102, 0));
        g.fill3DRect(0, 0, WIDTH, 120, false);
        // Score
        g.setColor(Color.BLACK);        
        g.setFont(new Font("Arial", Font.PLAIN, 18));

        g.drawString("WHITE: " + String.valueOf(player1Score), 20, 40);
        g.drawString("BLACK: " + String.valueOf(player2Score), 20, 60);

        if(!restart) {
            if(player1Turn) g.drawString("WHITE's turn", WIDTH - 120, 40);
            else g.drawString("BLACK's turn", WIDTH - 120, 40);
        }
        else {
            if(player1Score > player2Score) g.drawString("WHITE wins", WIDTH / 2 - 50, 40);
            else if(player1Score < player2Score) g.drawString("BLACK wins", WIDTH / 2 - 50, 40);
            else g.drawString("DRAW", WIDTH / 2 - 50, 40);
        }

        // Khung bàn chơi
        g.setColor(new Color(153, 0, 0));
        g.fill3DRect(0, 120, WIDTH, HEIGHT - 120, true);
        // Bảng chơi
        g.setColor(new Color(154, 255, 154));
        g.fill3DRect(20, 140, WIDTH - 40, HEIGHT - 160, true);

        g.setColor(Color.BLACK);
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                g.drawRect(i * 60 + 20, j * 60 + 140, 60, 60);
                // g.drawString(String.valueOf(i * 10 + j), j * 60 + 43, i * 60 + 177);
            }
        }

        for(int i = 0; i < player1Node.size(); i++) {
            g.setColor(Color.WHITE);
            g.fillOval(player1Node.get(i) % 10 * 60 + 25, player1Node.get(i) / 10 * 60 + 145, 50, 50);
        }
        for(int i = 0; i < player2Node.size(); i++) {
            g.setColor(Color.BLACK);
            g.fillOval(player2Node.get(i) % 10 * 60 + 25, player2Node.get(i) / 10 * 60 + 145, 50, 50);
        }

    }

    private void draw() {
        Graphics g2 = getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        x = (e.getX() - 20) / 60;
        y = (e.getY() - 140) / 60;
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
             
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }

}
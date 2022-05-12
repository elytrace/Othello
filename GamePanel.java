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
import java.util.Random;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable, MouseListener {

    public static final int WIDTH = 480 + 40;            //  
    public static final int HEIGHT = 480 + 120 + 40;
    
    private BufferedImage image;                         //
    private Graphics2D g;
    
    private Thread thread;                               //
    private boolean running;

    private int FPS = 30;
    private int targetTime = 1000 / FPS;

    private boolean player1Turn;
    private boolean restart;
    private boolean adding;         

    private int x, y;

    private int[] playerScore;
    private int[] position;

    private List<Integer> flip;
    private List<Integer> possible_moves;

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

        position = new int[8 * 8];
        for(int i = 0; i < position.length; i++) position[i] = 0;
        
        position[27] = 1;
        position[28] = 2;
        position[35] = 2;
        position[36] = 1;

        x = -1; 
        y = -1;

        playerScore = new int[2];
        playerScore[0] = 2;
        playerScore[1] = 2;

        restart = false;

        flip = new ArrayList<>();
        possible_moves = new ArrayList<>();

        adding = false;
    }

    private void moveChecking(int player, int step, boolean vertical, int location) {
        int value = location + step;
        if(!vertical) {
            while(value >= 0 && value <= 63 && position[value] == 3-player && Math.abs((value - step) / 8 - value / 8) == 1) {
                flip.add(value);
                value += step;
                if(value < 0 || value > 63 || ((value - step) % 8 == 0 && value % 8 == 7) || ((value - step) % 8 == 7 && value % 8 == 0) || position[value] == 0) {
                    adding = false;
                    flip.clear();
                    break;
                }
                if(position[value] == player) {
                    adding = true;
                    break;
                }
            }    
        }
        else {
            while(value >= 0 && value <= 63 && position[value] == 3-player && value / 8 == location / 8) {
                flip.add(value);
                value += step;
                if(value < 0 || value > 63 || ((value - step) % 8 == 0 && value % 8 == 7) || ((value - step) % 8 == 7 && value % 8 == 0) || position[value] == 0) {
                    adding = false;
                    flip.clear();
                    break;
                }
                if(position[value] == player) {
                    adding = true;
                    break;
                }
            } 
        }
    }

    private void flip_single_line(int player) {
        if(adding) {
            playerScore[player-1] += flip.size();
            playerScore[2-player] -= flip.size();
            for(int i = 0; i < flip.size(); i++) {
                position[flip.get(i)] = player;
            }
        }
        flip.clear();
    }

    private void addPossibleMoves(int player, int location) {
        int count = 0;
        if(position[location] != 0) return;
        moveChecking(player, -9, false, location); count += flip.size(); flip.clear();
        moveChecking(player, -8, false, location); count += flip.size(); flip.clear();
        moveChecking(player, -7, false, location); count += flip.size(); flip.clear();
        moveChecking(player, -1, true, location); count += flip.size(); flip.clear();
        moveChecking(player, 1, true, location); count += flip.size(); flip.clear();
        moveChecking(player, 7, false, location); count += flip.size(); flip.clear();
        moveChecking(player, 8, false, location); count += flip.size(); flip.clear();
        moveChecking(player, 9, false, location); count += flip.size(); flip.clear();
        if(count > 0) possible_moves.add(location);
        flip.clear();
    }

    private void flip_multiple_line(int player, int location) {
        moveChecking(player, -9, false, location); flip_single_line(player);
        moveChecking(player, -8, false, location); flip_single_line(player);
        moveChecking(player, -7, false, location); flip_single_line(player);
        moveChecking(player, -1, true, location); flip_single_line(player);
        moveChecking(player, 1, true, location); flip_single_line(player);
        moveChecking(player, 7, false, location); flip_single_line(player);
        moveChecking(player, 8, false, location); flip_single_line(player);
        moveChecking(player, 9, false, location); flip_single_line(player);
    }

    private void update() {
        possible_moves.clear();
        for(int i = 0; i < 64; i++) {
            if(player1Turn) 
                addPossibleMoves(1, i);
            else 
                addPossibleMoves(2, i);
        }
        if(possible_moves.size() == 0) restart = true;
        
        // int location = x + y * 8;
        
        Random ran = new Random();
        int location = ran.nextInt(64);
        if(location < 0) return;

        if(position[location] == 0 && possible_moves.contains(location)) {
            if(player1Turn) {
                position[location] = 1;
                playerScore[0]++;
                flip_multiple_line(1, location);
            }
            else {
                position[location] = 2;
                playerScore[1]++;
                flip_multiple_line(2, location);
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

        g.drawString("WHITE: " + String.valueOf(playerScore[0]), 20, 40);
        g.drawString("BLACK: " + String.valueOf(playerScore[1]), 20, 60);

        if(!restart) {
            if(player1Turn) {
                g.drawString("WHITE's turn", WIDTH - 120, 40);
                g.setColor(Color.WHITE);
                g.fillOval(WIDTH - 90, 50, 50, 50);
            }
            else {
                g.drawString("BLACK's turn", WIDTH - 120, 40);
                g.setColor(Color.BLACK);
                g.fillOval(WIDTH - 90, 50, 50, 50);
            }
        }
        else {
            if(playerScore[0] > playerScore[1]) g.drawString("WHITE wins !", WIDTH / 2 - 50, 40);
            else if(playerScore[0] < playerScore[1]) g.drawString("BLACK wins !", WIDTH / 2 - 50, 40);
            else g.drawString("DRAW...", WIDTH / 2 - 50, 40);
        }

        // Khung bàn chơi
        g.setColor(new Color(139, 58, 98));
        g.fill3DRect(0, 120, WIDTH, HEIGHT - 120, true);
        
        // Bảng chơi
        g.setColor(new Color(255, 182, 203));
        g.fill3DRect(20, 140, WIDTH - 40, HEIGHT - 160, true);

        g.setColor(Color.BLACK);
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                g.drawRect(i * 60 + 20, j * 60 + 140, 60, 60);
                // g.drawString(String.valueOf(i * 10 + j), j * 60 + 43, i * 60 + 177);
            }
        }

        for(int i = 0; i < 64; i++) {
            if(position[i] == 1) {
                g.setColor(Color.WHITE);
                g.fillOval(i % 8 * 60 + 25, i / 8 * 60 + 145, 50, 50);
            }
            else if(position[i] == 2) {
                g.setColor(Color.BLACK);
                g.fillOval(i % 8 * 60 + 25, i / 8 * 60 + 145, 50, 50);
            }
        }

        for(int i = 0; i < possible_moves.size(); i++) {
            g.setColor(Color.GRAY);
            g.fillOval(possible_moves.get(i) % 8 * 60 + 25 + 20, possible_moves.get(i) / 8 * 60 + 145 + 20, 10, 10);
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
        // for(int i = 0; i < 10; i++) {
        //     for(int j = 0; j < 10; j++) {
        //         System.out.print(position[i * 10 + j] + " ");
        //     }
        //     System.out.print('\n');
        // }
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
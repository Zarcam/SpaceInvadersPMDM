package pedro.ieslaencanta.com.space;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pedro
 */
public class Game {
    //dimensiones de un terminal
    private static int COLUMNS = 80;
    private static int ROWS = 24;
    public static final  TextColor BACKGROUND = TextColor.RGB.Factory.fromString("#000000");
    //20 MHz
    private static int frecuency = 2;
    private int tick;
    private Terminal terminal;
    private Screen screen;

    private Wall[] walls;

    private ArrayList<Enemy> enemies;

    private boolean key_left_pressed;
    private boolean key_right_pressed;
    private boolean key_exit;
    private boolean key_shoot;

    private Ship ship;

    private enum STATES {
        PLAY,
        GAME_OVER
    }
    private STATES state;

    /**
     * Constructor por defecto
     */
    public Game() {
        this.key_left_pressed = false;
        this.key_right_pressed = false;
        this.key_exit = false;
        this.key_shoot = false;
        this.tick = 0;

        this.init();
        try {
            this.terminal = new DefaultTerminalFactory().createTerminal();
            this.screen = new TerminalScreen(this.terminal);
            //no se muestra el cursor
            screen.setCursorPosition(null);
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void init() {
        this.state = STATES.PLAY;
        this.ship = new Ship(40, 20);
        this.walls = new Wall[4];
        this.enemies = new ArrayList<>();
        Enemy.setVelocidad(500);

        this.initWalls();

        this.enemies.add(new Enemy(COLUMNS/2, 1, Enemy.EnemyType.C));
    }

    private void initWalls(){
        this.walls[0] = new Wall(7+1, 16);
        this.walls[1] = new Wall(28+1, 16);
        this.walls[2] = new Wall(49+1, 16);
        this.walls[3] = new Wall(70+1, 16);
    }

    /**
     * inicializar el juego mientra no se pulse la tecla escape
     */
    public void loop() {
        
        try {
            screen.startScreen();
            screen.clear();
            this.terminal.setBackgroundColor(TextColor.ANSI.CYAN);

            while (!this.key_exit) {
                try {
                    //se procesa la entrada
                    this.process_input();
                    //se actualiza el juego
                    this.update();

                    //se pinta
                    this.paint(this.screen);
                    //1000 es un segundo, frecuenca de 10 Hz son 10 veces por segundo
                    //frecuenca de 20 Hz son 20 veces por segundo, una vez cada 0,05 segundos
                    Thread.sleep((1 / Game.frecuency) * 1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //fin del bucle
            screen.stopScreen();
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void paint(Screen s) {
        try {
            TerminalSize terminalSize = s.getTerminalSize();
            //se repinta en negro
            for (int column = 0; column < terminalSize.getColumns(); column++) {
                for (int row = 0; row < terminalSize.getRows(); row++) {
                    s.setCharacter(column, row, new TextCharacter(
                            ' ',
                            TextColor.ANSI.DEFAULT,
                            BACKGROUND));

                }
            }
            if (this.state == STATES.PLAY) {
              this.ship.paint(s);

              for(Enemy i : this.enemies){
                  i.paint(s);
              }

              for(Wall i : this.walls){
                  i.paint(s);
              }
            } else if (this.state == STATES.GAME_OVER) {
                this.paintGameOver(s);
            }
            screen.refresh();
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void paintGameOver(Screen s) {
        //https://emojicombos.com/game-over-ascii-art
        //Game.BACKGROUND=TextColor.ANSI.BLACK;
        String game_over[] = {
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣠⡀⠀",
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⣤⠀⠀⠀⢀⣴⣿⡶⠀⣾⣿⣿⡿⠟⠛⠁",
            "⠀⠀⠀⠀⠀⠀⣀⣀⣄⣀⠀⠀⠀⠀⣶⣶⣦⠀⠀⠀⠀⣼⣿⣿⡇⠀⣠⣿⣿⣿⠇⣸⣿⣿⣧⣤⠀⠀⠀",
            "⠀⠀⢀⣴⣾⣿⡿⠿⠿⠿⠇⠀⠀⣸⣿⣿⣿⡆⠀⠀⢰⣿⣿⣿⣷⣼⣿⣿⣿⡿⢀⣿⣿⡿⠟⠛⠁⠀⠀",
            "⠀⣴⣿⡿⠋⠁⠀⠀⠀⠀⠀⠀⢠⣿⣿⣹⣿⣿⣿⣿⣿⣿⡏⢻⣿⣿⢿⣿⣿⠃⣼⣿⣯⣤⣴⣶⣿⡤⠀",
            "⣼⣿⠏⠀⣀⣠⣤⣶⣾⣷⠄⣰⣿⣿⡿⠿⠻⣿⣯⣸⣿⡿⠀⠀⠀⠁⣾⣿⡏⢠⣿⣿⠿⠛⠋⠉⠀⠀⠀",
            "⣿⣿⠲⢿⣿⣿⣿⣿⡿⠋⢰⣿⣿⠋⠀⠀⠀⢻⣿⣿⣿⠇⠀⠀⠀⠀⠙⠛⠀⠀⠉⠁⠀⠀⠀⠀⠀⠀⠀",
            "⠹⢿⣷⣶⣿⣿⠿⠋⠀⠀⠈⠙⠃⠀⠀⠀⠀⠀⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀",
            "⠀⠀⠈⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣤⣤⣴⣶⣦⣤⡀⠀",
            "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⡀⠀⠀⠀⠀⠀⠀⠀⣠⡇⢰⣶⣶⣾⡿⠷⣿⣿⣿⡟⠛⣉⣿⣿⣿⠆",
            "⠀⠀⠀⠀⠀⠀⢀⣤⣶⣿⣿⡎⣿⣿⣦⠀⠀⠀⢀⣤⣾⠟⢀⣿⣿⡟⣁⠀⠀⣸⣿⣿⣤⣾⣿⡿⠛⠁⠀",
            "⠀⠀⠀⠀⣠⣾⣿⡿⠛⠉⢿⣦⠘⣿⣿⡆⠀⢠⣾⣿⠋⠀⣼⣿⣿⣿⠿⠷⢠⣿⣿⣿⠿⢻⣿⣧⠀⠀⠀",
            "⠀⠀⠀⣴⣿⣿⠋⠀⠀⠀⢸⣿⣇⢹⣿⣷⣰⣿⣿⠃⠀⢠⣿⣿⢃⣀⣤⣤⣾⣿⡟⠀⠀⠀⢻⣿⣆⠀⠀",
            "⠀⠀⠀⣿⣿⡇⠀⠀⢀⣴⣿⣿⡟⠀⣿⣿⣿⣿⠃⠀⠀⣾⣿⣿⡿⠿⠛⢛⣿⡟⠀⠀⠀⠀⠀⠻⠿⠀⠀",
            "⠀⠀⠀⠹⣿⣿⣶⣾⣿⣿⣿⠟⠁⠀⠸⢿⣿⠇⠀⠀⠀⠛⠛⠁⠀⠀⠀⠀⠀⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀",
            "⠀⠀⠀⠀⠈⠙⠛⠛⠛⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀"};
        for (int i = 0; i < game_over.length; i++) {
            for (int j = 0; j < game_over[i].length(); j++) {
                s.setCharacter(j + 20,
                        i + 4, TextCharacter.fromCharacter(
                                game_over[i].charAt(j)
                        )[0].withForegroundColor(TextColor.ANSI.values()[(int) (Math.random() * TextColor.ANSI.values().length)]));
            }

        }

    }

    /**
     * Borrar el buffer de teclado para evitar saltos en el movimiento
     *
     */
    private void clear_keyboard_input() {
        KeyStroke keyStroke = null;
        do {
            try {
                keyStroke = screen.pollInput();
            } catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (keyStroke != null);
    }

    private void process_input() {
        this.key_left_pressed = false;
        this.key_right_pressed = false;
        this.key_shoot = false;
        try {
            //la lectura es no bloqueante
            KeyStroke keyStroke = screen.pollInput();

            if (keyStroke != null) {
                if (keyStroke.getKeyType() == KeyType.Escape) {
                    this.key_exit = true;
                } else if (keyStroke.getKeyType() == KeyType.ArrowLeft) {
                    this.key_left_pressed = true;
                } else if (keyStroke.getKeyType() == KeyType.ArrowRight) {
                    this.key_right_pressed = true;
                } else {
                    KeyType c = keyStroke.getKeyType();

                    //System.out.println(String.format("%2x", (int) c.toString().charAt(0))+" ....");
                    if ((int) c.toString().charAt(0) == 67) {
                        this.key_shoot = true;
                    }
                }
                //se borra el buffer
                //this.clear_keyboard_input();
            }

        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void wallCollision(){
        Bullet[] shipBullets = this.ship.getBullets();

        for(Wall i : this.walls){
            //Balas nave
            for(int j = 0; j < shipBullets.length; j++){
                if(i.colision(shipBullets[j])){
                    this.ship.getBullets()[j] = null;
                }
            }

            //Balas enemigos
            for(Enemy j : this.enemies){
                Bullet[] enemyBullets = j.getBullets();
                for(int k = 0; k < enemyBullets.length; k++){
                    if(i.colision(enemyBullets[k])){
                        j.getBullets()[k] = null;
                    }
                }
            }
        }
    }

    private void update() {
        if (this.state == STATES.PLAY) {
            //mover la nave
            if (this.key_left_pressed) {
                this.ship.moveHorizontal(-1, 0, COLUMNS);
            }
            if (this.key_right_pressed) {
                this.ship.moveHorizontal(1, 0, COLUMNS);
            }
            //se mueven las balas de la nave
            this.ship.moveBullets(0, ROWS);

            //se detectan las colisiones con los muros
            this.wallCollision();

            //se dispara si se ha pulsado la tecla
            if (this.key_shoot) {
               this.ship.shoot();
            }

            //Disparo de enemigos
            for(Enemy i : this.enemies){
                i.shoot();
            }

            //Movimiento balas enemigos
            for(Enemy i : this.enemies){
                i.moveBullets(0, ROWS);
            }

            //movimiento enemigos
            if(this.tick >= Enemy.getVelocidad()) {
                for (Enemy i : this.enemies) {
                    i.moveHorizontal(0, COLUMNS, ROWS);
                }
                this.tick = 0;
            }

            this.tick++;
        }
    }

    private void setKey_left_pressed(boolean key_left_pressed) {
        this.key_left_pressed = key_left_pressed;
    }

    private boolean isKey_right_pressed() {
        return key_right_pressed;
    }

    private void setKey_right_pressed(boolean key_right_pressed) {
        this.key_right_pressed = key_right_pressed;
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.loop();

    }
}

//---------------------------------------------------------------80 columns---|/* MazeRunner class * --------------- * This is the game class that controls reading the maze files,  * setting up the board, managing its contents, and starting and stopping * the creature threads through the levels. It also provides useful * accessor methods to the squares and the player. */import java.io.*;import java.util.Vector;public class MazeRunner {    public static void main(String args[])     {         MazeRunner game = new MazeRunner();         Square.setGame(game);         Creature.setGame(game);         Hyper.init();         game.play();    }    private Grid squares;       private Vector allCreatures;    private Human player;    private boolean levelInProgress, userStillAlive;    private static int NumLevels = 5;    private int level;        public MazeRunner()     {        userStillAlive = true;        levelInProgress = false;        allCreatures = new Vector();        level = 0;    }            public boolean isRunning()     {        return levelInProgress;    }            public Square getSquareAt(Location location)    {        return (Square)squares.elementAt(location);    }            public boolean inBounds(Location location)    {        return squares.inBounds(location);    }                public Square getRandomSquare()    {        return (Square)squares.randomElement();    }            public Human getHuman()    {        return player;    }    public void kill(Creature doomed)    {        allCreatures.removeElement(doomed);        doomed.stop();    }            public synchronized void play()    {        while (userStillAlive && level < NumLevels) {            readMazeFileForLevel(level);            startLevel(level);            try {                wait(); // wait for signal from a creature that level is over            } catch (InterruptedException ie) {}            stopLevel();        }        Display.drawStatusMessage(userStillAlive? "You won!" : "You lost!");    }    public synchronized void levelOver(boolean advanceToNextLevel)     {        levelInProgress = false;        userStillAlive = advanceToNextLevel;        Display.drawStatusMessage(advanceToNextLevel && ++level < NumLevels? "On to next level!" : "Game over!");        notify();       // send signal to game thread that level is over    }            private void startLevel(int levelNumber)    {        Display.drawStatusMessage("Level " + levelNumber + ": Hit space bar to begin.");        while (Display.getKeyFromUser() != ' ') // wait for user to type space            ;        levelInProgress = true;        for (int i = 0; i < allCreatures.size(); i++)            ((Thread)allCreatures.elementAt(i)).start();        Display.drawStatusMessage("Playing level " + levelNumber + "...");    }        private void stopLevel()    {        try { Thread.sleep(200);} // allow signal thread to leave        catch (InterruptedException ie) {}        for (int i = 0; i < allCreatures.size(); i++)            ((Thread)allCreatures.elementAt(i)).stop();        try { Thread.sleep(200);} // give other threads a chance to get stopped        catch (InterruptedException ie) {}    }    private void readMazeFileForLevel(int level)    {        String mazeDirectory = System.getProperty("user.dir") + java.io.File.separator + "Mazes" + java.io.File.separator;        String filename = mazeDirectory + "Level" + level + ".maze";        BufferedReader in;                  try {            in = new BufferedReader(new FileReader(filename));         }         catch (FileNotFoundException e) {               System.out.println("Cannot find file \"" + filename + "\".");            return;        }                try {            int numRows = Integer.valueOf(in.readLine().trim()).intValue();             int numCols = Integer.valueOf(in.readLine().trim()).intValue();                     squares = new Grid(numRows, numCols);            Display.configureForSize(numRows,numCols);            Display.drawStatusMessage("Loading level " + level +"...");            allCreatures.removeAllElements();   // empty previous creatures            Hyper.clear();                      // empty previous hypersquares                        for (int row = 0; row < numRows; row++) {                for (int col = 0; col < numCols; col++)                    readOneSquare(new Location(row, col), (char)in.read());                in.readLine();  // skip over newline at end of row            }        }        catch (IOException e) {             System.out.println("File improperly formatted, quitting");             return;        }       }    private void readOneSquare(Location location, char ch)    {        Square newSquare;        Enemy opponent;                switch (ch) {			case 'H': 					newSquare = new Empty(location);					player = new Human(newSquare);					newSquare.setOccupant(player);					allCreatures.addElement(player);					break;			case 'R': 					newSquare = new Empty(location);					opponent = new Rover(newSquare);					newSquare.setOccupant(opponent);					allCreatures.addElement(opponent);					break;			case 'J': 					newSquare = new Empty(location); 					opponent = new Jumper(newSquare);					newSquare.setOccupant(opponent);					allCreatures.addElement(opponent);					break;			case 'P': 					newSquare = new Empty(location); 					opponent = new Pacer(newSquare);					newSquare.setOccupant(opponent);					allCreatures.addElement(opponent);					break;			case 'E':					newSquare = new Empty(location); 					newSquare.putEnergy();					break;			case ' ' : 					newSquare = new Empty(location); 					break;			case '%' : 					newSquare = new Brick(location); 					break;			case '!' : 					newSquare = new Goal(location); 					break;			case '#' :					newSquare = new Ladder(location); 					break;			case '*' : 					newSquare = new Freezer(location);					break;			case '0' : case '1' : case '2': case '3' : case '4': 			case '5' : case '6' : case '7': case '8' : case '9': 					newSquare = new Hyper(location, ch);					break;			default: 					return;		}				squares.setElementAt(location, newSquare);		newSquare.draw();	}}
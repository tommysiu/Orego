package orego.play;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import orego.book.OpeningBook;
import orego.core.Board;
import orego.heuristic.Heuristic;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

// TODO When playing many games against GNU Go, occasionally this class complains that
// GNU Go has made an illegal move. I suspect that Player is actually making illegal
// moves (superko violations).

/** Chooses moves to play. The default implementation is a pure random player. */
public class Player implements Playable {

	/** The Board this player plays on. */
	private Board board;

	/** Used to generate moves in bestMove(). */
	private Heuristic[] heuristics;
	
	/** Move generator for the opening of the game. */
	private OpeningBook openingBook;

	/** Random number generator. */
	private MersenneTwisterFast random;

	/** A default player with a random policy. */
	public Player() {
		random = new MersenneTwisterFast();
		heuristics = new Heuristic[0];
	}

	public int acceptMove(int p) {
		try {
			// If move is legal, play it on the board
			return board.play(p);
		} catch (AssertionError e) {
			System.err.println("Move sequence leading to this error:");
			System.err.print(getMoveSequence());
			System.err.println(pointToString(p));
			System.err.flush();
			throw e;
		}
	}

	/**
	 * Accepts a sequence of moves as returned by getMoveSequence(). The moves
	 * are presumed to alternate starting with the current color to play, and to
	 * all be legal.
	 * 
	 * @param moves a sequence of moves separated by spaces, e.g., "a1 b2 pass".
	 */
	public void acceptMoveSequence(String moves) {
		Scanner s = new Scanner(moves);
		while (s.hasNext()) {
			acceptMove(at(s.next()));
		}
	}

	// TODO Remove arguments?
	/** Selects and plays a random moves, choosing the heuristically best move, with ties broken randomly. */
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		// Compute the heuristic value of each point
		IntSet vacantPoints = board.getVacantPoints();
		int[] values = new int[FIRST_POINT_BEYOND_BOARD];
		for (int p = 0; p < vacantPoints.size(); p++) {
			if ((board.getColor(p) == VACANT) && (board.isFeasible(p))) {
			for (Heuristic h : heuristics) {
				values[p] += h.evaluate(p, board);
			}
			} else {
				values[p] = Integer.MIN_VALUE;
			}
		}
		// Find the best move
		while (true) {
			// Compute heuristic values
			int bestMove = PASS;
			int bestValue = Integer.MIN_VALUE;
			int start = random.nextInt(vacantPoints.size());
			int i = start;
			do {
				int p = vacantPoints.get(i);
					if ((board.getColor(p) == VACANT) && (board.isFeasible(p))) {
						if (values[p] > bestValue) {
							bestValue = values[p];
							bestMove = p;
						}
					}
				// The magic number 457 is prime and larger than vacantPoints.size().
				// Advancing by 457 therefore skips "randomly" through the array,
				// in a manner analogous to double hashing.
				i = (i + 457) % vacantPoints.size();
			} while (i != start);
			// If there is a best move, try to play it
			if (board.play(bestMove) == PLAY_OK) { // Note play() instead of playFast()
				// The best move is legal -- play it
				return bestMove;
			} else {
				// The best move is not legal -- exclude it and try again
				values[bestMove] = Integer.MIN_VALUE;
			}
		}
	}

	public int bestMove() {
		int move;
		if (getOpeningBook() != null) { // play from opening book
			move = getOpeningBook().nextMove(getBoard());
			if (move != NO_POINT) {
				return move;
			}
		}
		move = selectAndPlayOneMove(random, board);
		// Replay the move with play() (instead of playFast()) to make sure it's
		// not a superko violation
		undo();
		if (board.isLegal(move)) {
			return move;
		}
		return PASS;
	}

	public int finalScore() {
		return board.finalScore();
	}

	public Board getBoard() {
		return board;
	}

	public Set<String> getCommands() {
		Set<String> result = new TreeSet<String>();
		
		// set any general property
		result.add("orego-set-param");
				
		return result;
	}

	public Set<String> getGoguiCommands() {
		Set<String> result = new TreeSet<String>();
		
		// set any general property
		result.add("none/Set Property/orego-set-param %s %s");
		
		return result;
	}

	/** Returns the array of heuristics associated with this player. */
	public Heuristic[] getHeuristics() {
		return heuristics;
	}
	
	/** Returns the number of milliseconds allocated per move. */
	public int getMillisecondsPerMove() {
		return 0; // this player responds almost instantly
	}

	/** Returns the move played at time t, or NO_POINT if t is negative. */
	public int getMove(int t) {
		return board.getMove(t);
	}

	/**
	 * Returns a String containing a human-readable list of all moves made so
	 * far on this board.
	 */
	public String getMoveSequence() {
		return board.getMoveSequence();
	}

	/** Returns this players opening book. */
	public OpeningBook getOpeningBook() {
		return openingBook;
	}

	/** Returns the current turn number. */
	public int getTurn() {
		return board.getTurn();
	}

	public String handleCommand(String command, StringTokenizer arguments) {
		// set any property
		if (command.equals("orego-set-param")) {
			try {
				String prop_name = arguments.nextToken();
				String prop_value = arguments.nextToken();
				
				setProperty(prop_name, prop_value);
				
				reset(); // when we change a parameter, we need to restart
				
				return "Set '" + prop_name + "' to '" + prop_value + "'";
			} catch (Exception e) {
				return e.toString();
			}
		}
		return null;
	}

	public void reset() {
		if (board == null) {
			board = new Board();
		} else { // The new board must have the same komi as the old one
			clearBoardWhilePreservingKomi();
		}
	}

	protected void clearBoardWhilePreservingKomi() {
		double komi = board.getKomi();
		board.clear();
		board.setKomi(komi);
	}

	protected void setBoard(Board board) {
		this.board = board;
	}

	/** Sets the board's color to play. Used only in tests and problems. */
	public void setColorToPlay(int color) {
		board.setColorToPlay(color);
	}

	/** Sets the heuristics. */
	public void setHeuristics(Heuristic[] heuristics) {
		this.heuristics = heuristics;
	}

	/** Sets the komi or handicap for the current game. */
	public void setKomi(double komi) {
		board.setKomi(komi);
	}

	/**
	 * Sets the opening book. For testing only -- to use the full book, use
	 * setProperty().
	 */
	public void setOpeningBook(OpeningBook book) {
		openingBook = book;
	}

	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("book")) {
			try {
				String genClass = value;
				if (!genClass.startsWith("orego.")) {
					genClass = "orego.book." + genClass;
				}
				openingBook = (OpeningBook) Class.forName(genClass)
						.newInstance();
				orego.experiment.Debug.debug("Opening book: " + openingBook);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		} else if (property.equals("heuristic")) {
			List<Heuristic> heuristics = new ArrayList<Heuristic>();
			if (!value.isEmpty()) {
				String[] heuristicClasses = value.split(":");
				for (int i = heuristicClasses.length - 1; i >= 0; i--) {
					String genClass = heuristicClasses[i];
					if (!genClass.contains(".")) {
						// set default path to heuristic if it isn't given
						genClass = "orego.heuristic." + genClass;
					}
					if (!genClass.endsWith("Heuristic")) {
						// complete the class name if a shortened version is used
						genClass = genClass + "Heuristic";
					}
					try {
						heuristics.add(0, (Heuristic) Class.forName(genClass)
									.newInstance());
					} catch (Exception e) {
						System.err.println("Cannot construct heuristic: " + value);
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
			setHeuristics(heuristics.toArray(new Heuristic[0]));			
		} else {
			throw new UnknownPropertyException(property
					+ " is not a known property");
		}
	}

	public void setRemainingTime(int seconds) {
		// do nothing, this player is almost instant
	}

	/**
	 * Plays all of the stones in diagram, row by row from top to bottom. See
	 * the code of BoardTest for example diagrams.
	 */
	public void setUpProblem(int colorToPlay, String[] diagram) {
		board.setUpProblem(colorToPlay, diagram);
	}

	public void setUpSgf(String filepath, int colorToPlay) {
		reset();
		board.setColorToPlay(colorToPlay);
		try {
			File file = new File(filepath);
			BufferedReader bf = new BufferedReader(new FileReader(file));
			String input = "";
			String current = "";
			char[][] ourBoard = new char[BOARD_WIDTH][BOARD_WIDTH];
			for (int i = 0; i < BOARD_WIDTH; i++) {
				for (int j = 0; j < BOARD_WIDTH; j++) {
					ourBoard[i][j] = '.';
				}
			}
			while ((current = bf.readLine()) != null) {
				input += current;
			}
			StringTokenizer stoken = new StringTokenizer(input, ";");
			stoken.nextToken();
			stoken.nextToken();
			String boardSetup = stoken.nextToken();
			stoken = new StringTokenizer(boardSetup, "[]()");
			int state = 0;
			String currentToken = "";
			while (stoken.hasMoreTokens()) {
				currentToken = stoken.nextToken();
				assert currentToken.length() == 2;
				if (currentToken.equals("AB")) {
					// Add black stones (handicap)
					state = 0;
				} else if (currentToken.equals("AW")) {
					// Add white stones
					state = 1;
				} else if (Character.isUpperCase(currentToken.charAt(0))) {
					// Other special SGF codes; ignore
					state = 2;
				} else if (state == 0) {
					if (currentToken.length() == 2) {
						int row = currentToken.charAt(1) - 'a';
						int col = currentToken.charAt(0) - 'a';
						ourBoard[row][col] = '#';
						// place black stone here.
					}
				} else if (state == 1) {
					if (currentToken.length() == 2) {
						int row = currentToken.charAt(1) - 'a';
						int col = currentToken.charAt(0) - 'a';
						ourBoard[row][col] = 'O';
						// place white stone here.
					}
				}
			}
			String[] arrayOfStrings = new String[BOARD_WIDTH];
			for (int i = 0; i < arrayOfStrings.length; i++) {
				arrayOfStrings[i] = new String(ourBoard[i]);
			}
			getBoard().setUpProblem(colorToPlay, arrayOfStrings);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	/** Slightly friendlier than the default; doesn't include the hex address. */
	public String toString() {
		return getClass().getCanonicalName();
	}

	public boolean undo() {
		if (getTurn() == 0) { // Beginning of game, can't undo
			return false;
		} else if (getTurn() == 1) { // Don't want to replay any moves
			clearBoardWhilePreservingKomi();
			return true;
		}
		int[] moves = new int[getTurn() - 1];
		board.fillMoves(moves, 0, getTurn() - 2);
		clearBoardWhilePreservingKomi();
		for (int p : moves) {
			board.play(p);
		}
		return true;
	}

}

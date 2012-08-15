package orego.patternanalyze;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import orego.core.Board;
import orego.patterns.RatedPattern;
import orego.util.BitVector;
import static orego.patterns.Pattern.*;
import static orego.core.Board.*;

public class PatternCounter5 {

	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;

	/** Seen[p] is the number of times p was seen. */
	protected int[] seen;
	
	/** Played[p] is the number of times p was played. */
	protected int[] played;
	
	public static void main(String[] args) {
		new PatternCounter5().run();
	}
	
	protected BitVector seenThisGame;
	
	protected BitVector playedThisGame;

	protected String outputFile;
	
	public PatternCounter5() {
		playedThisGame = new BitVector(NUMBER_OF_NEIGHBORHOODS);
		seenThisGame = new BitVector(NUMBER_OF_NEIGHBORHOODS);	
		outputFile = "GoodPatterns";
	}
	
	private static String TEST_DIRECTORY = "../../../Test Games/";

	public void run() {
		played = new int[NUMBER_OF_NEIGHBORHOODS];
		seen = new int[NUMBER_OF_NEIGHBORHOODS];
		readGames(TEST_DIRECTORY);
		writeOutput();
	}

	public void writeOutput()  {
		try {
			TreeSet<RatedPattern> patterns = new TreeSet<RatedPattern>();
			for (int p = 0; p < NUMBER_OF_NEIGHBORHOODS; p++) {
				if (seen[p] > 0) {
					patterns.add(new RatedPattern((char)p, (double)played[p] / seen[p]));
				}
			}
			PrintWriter bw = new PrintWriter(new FileWriter(new File(TEST_DIRECTORY + outputFile + ".txt")));
			for (RatedPattern p : patterns) {
				char pattern = p.getPattern();
				bw.println("\"" + arrayToString(neighborhoodToArray(pattern)) + "\", // "
						+ played[pattern] + "/" + seen[pattern] + " = " + p.getRatio());
			}
			System.out.println("Done.");
			System.out.println("Written to file "+TEST_DIRECTORY + outputFile + ".txt");
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void readGames(String path) {
		try {
			File directory = new File(path);
			System.out.println("Directory: " + directory.getAbsolutePath());
			String[] dirList = directory.list();
			for (int i = 0; i < dirList.length; i++) {
				String filename = path + File.separator + dirList[i];
				File file = new File(filename);
				if (file.isDirectory()) {
					readGames(filename);
				} else if (dirList[i].toLowerCase().endsWith(".sgf")) {
					Board board = sgfToGame(file);
					if (board != null) {						
						analyze(board);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public Board sgfToGame(File file) throws Exception {
		System.out.println("Starting file "+file.getName());
		Board board = new Board();
		String input = "";
		Scanner s = new Scanner(file);
		while (s.hasNextLine()) {
			input += s.nextLine();
		}
		input = input.replace("W[]", "W[tt]");
		input = input.replace("B[]", "B[tt]");
		StringTokenizer stoken = new StringTokenizer(input, "()[];");
		while (stoken.hasMoreTokens()) {
			String token = stoken.nextToken();
			if (token.equals("AB")) {
				s.close();
				return null; // Handicap game; ignore
			}
			if (token.equals("W") || token.equals("B")) {
				token = stoken.nextToken();
				if (token.equals("tt")) {
					board.play(PASS);
				} else {
					int legality = board.play(sgfToPoint(token));
					assert legality == PLAY_OK;
				}
			}
		}
		s.close();
		System.out.println("Finished file "+file.getName());
		return board;
	}
	
	protected int[] getPointsToAnalyze(int p) {
		return NEIGHBORS[p];
	}

	public void analyze(Board board) {
		int turn = board.getTurn();
		Board patternBoard = new Board();
		playedThisGame.clear();
		seenThisGame.clear();
		int legality = patternBoard.play(board.getMove(0));
		assert legality == PLAY_OK;
		for (int t = 1; t < turn; t++) {
			int currentPlay = board.getMove(t);
			int lastPlay = board.getMove(t - 1);
			if (ON_BOARD[lastPlay] && ON_BOARD[currentPlay]) {
				for (int p : getPointsToAnalyze(lastPlay)) {
					if (patternBoard.getColor(p) == VACANT) {
						char neighborhood;
						if (patternBoard.getColorToPlay() == BLACK) {
							neighborhood = patternBoard.getNeighborhood(p);
						} else {
							neighborhood = patternBoard.getNeighborhoodColorsReversed(p);
						}
						char transformed = getLowestTransformation(neighborhood);
						if (!seenThisGame.get(transformed)) {
							seenThisGame.set(transformed, true);
							seen[transformed]++;
							assert seen[transformed] >= 0;
						}
						if ((p == currentPlay) && !playedThisGame.get(transformed)) {
							playedThisGame.set(transformed, true);
							played[transformed]++;
							assert played[transformed] >= 0;
						}
					}
				}
			}
			assert patternBoard.isLegal(currentPlay);
			legality = patternBoard.play(currentPlay);
			assert legality == PLAY_OK : "Illegal move #" + t + " at " + pointToString(currentPlay) + " on board:\n" + patternBoard;
		}
	}

}
package orego.heuristic;

import java.io.*;
import java.util.ArrayList;

import ec.util.MersenneTwisterFast;

import orego.patternanalyze.*;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.NEIGHBORS;
import static orego.experiment.Debug.*;

import orego.core.Board;

public class DynamicPatternHeuristic extends Heuristic {

	private static DynamicPattern[] pattern8List;
	private static DynamicPattern[] pattern12List;
	private static DynamicPattern[] pattern20List;
	private static DynamicPattern[] pattern24List;
	
	private static boolean test;
	
	private static int PATTERNS_TO_LOAD = 100;
	
	static {
		pattern8List = new DynamicPattern[PATTERNS_TO_LOAD];
		pattern12List = new DynamicPattern[PATTERNS_TO_LOAD];
		pattern20List = new DynamicPattern[PATTERNS_TO_LOAD];
		pattern24List = new DynamicPattern[PATTERNS_TO_LOAD];			
		extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/pattern8.dat", pattern8List);
		extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/pattern12.dat", pattern12List);
		extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/pattern20.dat", pattern20List);
		extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/pattern24.dat", pattern24List);
	}
	
	public DynamicPatternHeuristic(int weight) {
		super(weight);
	}

	private static void extractPatternsFromFile(String fileName, DynamicPattern[] patternList) {
		ObjectInputStream input;
		try {
			input = new ObjectInputStream(new FileInputStream(
					new File(fileName)));
			DynamicPattern pattern = null;
			try {
				int counter = 0;
				while ((pattern = (DynamicPattern) input.readObject()) != null && counter < PATTERNS_TO_LOAD) {
					patternList[counter] = pattern;
					counter++;
				}
				input.close();
			} catch (EOFException ex) {
				input.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}

	public int evaluate(int p, Board board) {
		int returnValue = 0;
		long pattern8 = DynamicPattern.setupPattern(p, board, 8);
		long pattern12 = DynamicPattern.setupPattern(p, board, 12);
		long pattern20 = DynamicPattern.setupPattern(p, board, 20);
		long pattern24 = DynamicPattern.setupPattern(p, board, 24);
		for (DynamicPattern pattern : pattern24List) {
			if (pattern.match(pattern24, 24)) {
				returnValue += 4 * getWeight();
			}
		}		
		for (DynamicPattern pattern : pattern20List) {
			if (pattern.match(pattern20, 20)) {
				returnValue += 3 * getWeight();
			}
		}
		for (DynamicPattern pattern : pattern12List) {
			if (pattern.match(pattern12, 12)) {
				returnValue += 2 * getWeight();
			}
		}
		for (DynamicPattern pattern : pattern8List) {
			if (pattern.match(pattern8, 8)) {
				returnValue += 1 * getWeight();
			}
		}
		return returnValue;
	}
	
	public void prepare(Board board) {
		super.prepare(board);
		for (int p : NEIGHBORS[board.getMove(board.getTurn() - 1)]) {
			if (board.getColor(p) == VACANT) {
				int playValue = evaluate(p, board);
				if(playValue > 0) {
					recommend(p);
				}
				if(playValue < 0) {
					discourage(p); 
				}
			}
		}
	}

	public static void setTestMode(boolean value) {
		test = value;
	}
}
package edu.lclark.orego.move;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.*;

/** Static methods for creating some particular, widely-used movers. */
public class MoverFactory {

	/** Plays randomly except for eyelike points. */
	public static PredicateMover simpleRandom(Board board) {
		return new PredicateMover(board, new NotEyeLike(board));
	}

	/**
	 * Like simpleRandom, but only plays moves that are on the 3rd or 4th line
	 * or near another stone.
	 */
	public static PredicateMover feasible(Board board) {
		Predicate f = new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board)));
		return new PredicateMover(board, f);
	}

	/** Like feasible, but captures when possible. */
	public static SuggesterMover greedy(Board board) {
		Suggester s = new CaptureSuggester(board, new AtariObserver(board));
		return new SuggesterMover(board, s, feasible(board));
	}

}
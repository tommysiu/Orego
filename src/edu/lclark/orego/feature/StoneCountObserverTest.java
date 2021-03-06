package edu.lclark.orego.feature;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import edu.lclark.orego.core.Board;

public class StoneCountObserverTest {

	private Board board;
	
	private StoneCountObserver counter;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		counter = new StoneCountObserver(board);
	}

	@Test
	public void testCount() {
		String[] diagram = {
				"##.#O",
				"OO.O.",
				"....O",
				".#...",
				".#...",
		};
		board.setUpProblem(diagram, WHITE);
		board.play("c5");
		assertEquals(2, counter.getCount(BLACK));
		assertEquals(6, counter.getCount(WHITE));
	}

	@Test
	public void testClear() {
		String[] diagram = {
				"##.#O",
				"OO.O.",
				"....O",
				".#...",
				".#...",
		};
		board.setUpProblem(diagram, WHITE);
		board.play("c5");
		assertEquals(2, counter.getCount(BLACK));
		assertEquals(6, counter.getCount(WHITE));
		board.clear();
		assertEquals(0, counter.getCount(BLACK));
		assertEquals(0, counter.getCount(WHITE));
	}
	
	@Test
	public void testMercy(){
		StoneCountObserver mercyCounter = new StoneCountObserver(board);
		assertEquals(null, mercyCounter.mercyWinner());
		String[] diagram = {
				"###..",
				".....",
				".....",
				".....",
				".....",
		};
		board.setUpProblem(diagram, BLACK);
		assertEquals(null, mercyCounter.mercyWinner());
		board.play(board.getCoordinateSystem().at("d5"));
		assertEquals(BLACK, mercyCounter.mercyWinner());
		diagram = new String[] {
				"###..",
				".....",
				".....",
				"O....",
				"OOOOO",
		};
		board.setUpProblem(diagram, WHITE);
		assertEquals(null, mercyCounter.mercyWinner());
		board.play(board.getCoordinateSystem().at("b2"));
		assertEquals(WHITE, mercyCounter.mercyWinner());	
	}

}

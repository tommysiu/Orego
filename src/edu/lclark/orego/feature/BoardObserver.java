package edu.lclark.orego.feature;

import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.util.ShortList;

/** An object that is notified when the board changes. */
public interface BoardObserver {

	/** Update this observer after a move is played on the board. */
	public void update(StoneColor color, short location,
			ShortList capturedStones);

	/** Resets any data structures to the appropriate state for an empty board. */
	public void clear();

	/**
	 * Copies data from that, which is assumed to be of the same type as this.
	 * (We can't use generics for type safety because a Board will need tell a
	 * variety of observers to copy data from their originals.
	 */
	public void copyDataFrom(BoardObserver that);

}

package edu.lclark.orego.experiment;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Listens to a GTP player running in another process. Whenever that player
 * emits a line of output, the line is passed to an instance of Game.
 */
final class PlayerListener implements Runnable {

	/** Input from the other program. */
	private final InputStream fromProgram;

	/** The Game being played. */
	private final Game game;

	public PlayerListener(InputStream input, Game game) {
		this.fromProgram = input;
		this.game = game;
	}

	@Override
	public void run() {
		try (Scanner s = new Scanner(fromProgram)) {
			boolean finishedNormally = false;
			while (s.hasNextLine()) {
				// s is passed in in case there is a multi-line error message,
				// so game can dump all of the message to the output file
				final String line = s.nextLine();
				if (!line.isEmpty()) {
					finishedNormally = game.handleResponse(line, s);
				}
			}
			if (!finishedNormally) {
				game.handleResponse("? program crashed", s);
			}
		}
	}

}

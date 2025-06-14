package custom;

import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class Test {

	public static void main(String[] args) {
		// Warm-up (fa girare la JVM sulle stesse istruzioni molte volte)
		StateTablut s1 = new StateTablut();
		Pawn[][] board = s1.getBoard();
		System.out.println(board[3][4]);
		board[3][4] = Pawn.EMPTY;
		s1.setBoard(board);
		for (int i = 0; i < 10_000; i++) {
			CanonicalState.from(s1);
		}
		long bestTime = Long.MAX_VALUE;
		for (int i = 0; i < 1000; i++) {
			long t0 = System.nanoTime();
			CanonicalState.from(s1);
			long t1 = System.nanoTime();
			bestTime = Math.min(bestTime, t1 - t0);
		}
	}
}

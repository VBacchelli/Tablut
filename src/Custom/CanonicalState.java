package Custom;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class CanonicalState extends StateTablut {

	public enum Symmetry {
		IDENTITY((Integer i, Integer j) -> copy(i, j)), ROTATE_90((Integer i, Integer j) -> rotate90(i, j)),
		ROTATE_180((Integer i, Integer j) -> rotate180(i, j)), ROTATE_270((Integer i, Integer j) -> rotate270(i, j)),
		REFLECT_HORIZONTAL((Integer i, Integer j) -> reflectHorizontal(i, j)),
		REFLECT_VERTICAL((Integer i, Integer j) -> reflectVertical(i, j)),
		REFLECT_DIAGONAL_MAIN((Integer i, Integer j) -> reflectDiagonalMain(i, j)),
		REFLECT_DIAGONAL_ANTI((Integer i, Integer j) -> reflectDiagonalAnti(i, j));

		private static final Map<String, Symmetry> COMPOSITION_MAP = new HashMap<>();

		static {
			// Helper: chiave ordinata per sfruttare simmetrie commutative
			BiFunction<Symmetry, Symmetry, String> key = (a, b) -> a.name() + "|" + b.name();

			// Composizioni manuali
			COMPOSITION_MAP.put(key.apply(IDENTITY, IDENTITY), IDENTITY);
			COMPOSITION_MAP.put(key.apply(IDENTITY, ROTATE_90), ROTATE_90);
			COMPOSITION_MAP.put(key.apply(IDENTITY, ROTATE_180), ROTATE_180);
			COMPOSITION_MAP.put(key.apply(IDENTITY, ROTATE_270), ROTATE_270);
			COMPOSITION_MAP.put(key.apply(IDENTITY, REFLECT_HORIZONTAL), REFLECT_HORIZONTAL);
			COMPOSITION_MAP.put(key.apply(IDENTITY, REFLECT_VERTICAL), REFLECT_VERTICAL);
			COMPOSITION_MAP.put(key.apply(IDENTITY, REFLECT_DIAGONAL_MAIN), REFLECT_DIAGONAL_MAIN);
			COMPOSITION_MAP.put(key.apply(IDENTITY, REFLECT_DIAGONAL_ANTI), REFLECT_DIAGONAL_ANTI);

			COMPOSITION_MAP.put(key.apply(ROTATE_90, IDENTITY), ROTATE_90);
			COMPOSITION_MAP.put(key.apply(ROTATE_90, ROTATE_90), ROTATE_180);
			COMPOSITION_MAP.put(key.apply(ROTATE_90, ROTATE_180), ROTATE_270);
			COMPOSITION_MAP.put(key.apply(ROTATE_90, ROTATE_270), IDENTITY);
			COMPOSITION_MAP.put(key.apply(ROTATE_90, REFLECT_HORIZONTAL), REFLECT_DIAGONAL_MAIN);
			COMPOSITION_MAP.put(key.apply(ROTATE_90, REFLECT_VERTICAL), REFLECT_DIAGONAL_ANTI);
			COMPOSITION_MAP.put(key.apply(ROTATE_90, REFLECT_DIAGONAL_MAIN), REFLECT_VERTICAL);
			COMPOSITION_MAP.put(key.apply(ROTATE_90, REFLECT_DIAGONAL_ANTI), REFLECT_HORIZONTAL);

			COMPOSITION_MAP.put(key.apply(ROTATE_180, IDENTITY), ROTATE_180);
			COMPOSITION_MAP.put(key.apply(ROTATE_180, ROTATE_90), ROTATE_270);
			COMPOSITION_MAP.put(key.apply(ROTATE_180, ROTATE_180), IDENTITY);
			COMPOSITION_MAP.put(key.apply(ROTATE_180, ROTATE_270), ROTATE_90);
			COMPOSITION_MAP.put(key.apply(ROTATE_180, REFLECT_HORIZONTAL), REFLECT_VERTICAL);
			COMPOSITION_MAP.put(key.apply(ROTATE_180, REFLECT_VERTICAL), REFLECT_HORIZONTAL);
			COMPOSITION_MAP.put(key.apply(ROTATE_180, REFLECT_DIAGONAL_MAIN), REFLECT_DIAGONAL_ANTI);
			COMPOSITION_MAP.put(key.apply(ROTATE_180, REFLECT_DIAGONAL_ANTI), REFLECT_DIAGONAL_MAIN);

			COMPOSITION_MAP.put(key.apply(ROTATE_270, IDENTITY), ROTATE_270);
			COMPOSITION_MAP.put(key.apply(ROTATE_270, ROTATE_90), IDENTITY);
			COMPOSITION_MAP.put(key.apply(ROTATE_270, ROTATE_180), ROTATE_90);
			COMPOSITION_MAP.put(key.apply(ROTATE_270, ROTATE_270), ROTATE_180);
			COMPOSITION_MAP.put(key.apply(ROTATE_270, REFLECT_HORIZONTAL), REFLECT_DIAGONAL_ANTI);
			COMPOSITION_MAP.put(key.apply(ROTATE_270, REFLECT_VERTICAL), REFLECT_DIAGONAL_MAIN);
			COMPOSITION_MAP.put(key.apply(ROTATE_270, REFLECT_DIAGONAL_MAIN), REFLECT_HORIZONTAL);
			COMPOSITION_MAP.put(key.apply(ROTATE_270, REFLECT_DIAGONAL_ANTI), REFLECT_VERTICAL);

			COMPOSITION_MAP.put(key.apply(REFLECT_HORIZONTAL, IDENTITY), REFLECT_HORIZONTAL);
			COMPOSITION_MAP.put(key.apply(REFLECT_HORIZONTAL, ROTATE_90), REFLECT_DIAGONAL_ANTI);
			COMPOSITION_MAP.put(key.apply(REFLECT_HORIZONTAL, ROTATE_180), REFLECT_VERTICAL);
			COMPOSITION_MAP.put(key.apply(REFLECT_HORIZONTAL, ROTATE_270), REFLECT_DIAGONAL_MAIN);
			COMPOSITION_MAP.put(key.apply(REFLECT_HORIZONTAL, REFLECT_HORIZONTAL), IDENTITY);
			COMPOSITION_MAP.put(key.apply(REFLECT_HORIZONTAL, REFLECT_VERTICAL), ROTATE_180);
			COMPOSITION_MAP.put(key.apply(REFLECT_HORIZONTAL, REFLECT_DIAGONAL_MAIN), ROTATE_90);
			COMPOSITION_MAP.put(key.apply(REFLECT_HORIZONTAL, REFLECT_DIAGONAL_ANTI), ROTATE_270);

			COMPOSITION_MAP.put(key.apply(REFLECT_VERTICAL, IDENTITY), REFLECT_VERTICAL);
			COMPOSITION_MAP.put(key.apply(REFLECT_VERTICAL, ROTATE_90), REFLECT_DIAGONAL_MAIN);
			COMPOSITION_MAP.put(key.apply(REFLECT_VERTICAL, ROTATE_180), REFLECT_HORIZONTAL);
			COMPOSITION_MAP.put(key.apply(REFLECT_VERTICAL, ROTATE_270), REFLECT_DIAGONAL_ANTI);
			COMPOSITION_MAP.put(key.apply(REFLECT_VERTICAL, REFLECT_HORIZONTAL), ROTATE_180);
			COMPOSITION_MAP.put(key.apply(REFLECT_VERTICAL, REFLECT_VERTICAL), IDENTITY);
			COMPOSITION_MAP.put(key.apply(REFLECT_VERTICAL, REFLECT_DIAGONAL_MAIN), ROTATE_270);
			COMPOSITION_MAP.put(key.apply(REFLECT_VERTICAL, REFLECT_DIAGONAL_ANTI), ROTATE_90);

			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_MAIN, IDENTITY), REFLECT_DIAGONAL_MAIN);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_MAIN, ROTATE_90), REFLECT_VERTICAL);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_MAIN, ROTATE_180), REFLECT_DIAGONAL_ANTI);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_MAIN, ROTATE_270), REFLECT_HORIZONTAL);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_MAIN, REFLECT_HORIZONTAL), ROTATE_270);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_MAIN, REFLECT_VERTICAL), ROTATE_90);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_MAIN, REFLECT_DIAGONAL_MAIN), IDENTITY);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_MAIN, REFLECT_DIAGONAL_ANTI), ROTATE_180);

			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_ANTI, IDENTITY), REFLECT_DIAGONAL_ANTI);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_ANTI, ROTATE_90), REFLECT_HORIZONTAL);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_ANTI, ROTATE_180), REFLECT_DIAGONAL_MAIN);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_ANTI, ROTATE_270), REFLECT_VERTICAL);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_ANTI, REFLECT_HORIZONTAL), ROTATE_90);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_ANTI, REFLECT_VERTICAL), ROTATE_270);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_ANTI, REFLECT_DIAGONAL_MAIN), ROTATE_180);
			COMPOSITION_MAP.put(key.apply(REFLECT_DIAGONAL_ANTI, REFLECT_DIAGONAL_ANTI), IDENTITY);
		}

		public Symmetry compose(Symmetry other) {
			String k = this.name() + "|" + other.name();
			Symmetry result = COMPOSITION_MAP.get(k);
			if (result == null) {
				throw new IllegalArgumentException("Composizione non definita per " + this + " e " + other);
			}
			return result;
		}

		public int getContentIndexesForCell(int i, int j) {
			return this.transformation.apply(i, j);
		}

		private BiFunction<Integer, Integer, Integer> transformation;

		private Symmetry(BiFunction<Integer, Integer, Integer> transformation) {
			this.transformation = transformation;
		}

		public Symmetry getInverse() {
			switch (this) {
			case ROTATE_90:
				return ROTATE_270;
			case ROTATE_270:
				return ROTATE_90;
			default:
				return this; // IDENTITY, ROTATE_180, REFLECT_HORIZONTAL, REFLECT_VERTICAL
			}
		}

		private static int copy(int i, int j) {
			return i * 10 + j;
		}

		// Inverse of ROTATE_90 is ROTATE_270
		private static int rotate90(int i, int j) {
			return (9 - 1 - j) * 10 + i;
		}

		// Inverse of ROTATE_180 is itself
		private static int rotate180(int i, int j) {
			return (9 - 1 - i) * 10 + (9 - 1 - j);
		}

		// Inverse of ROTATE_270 is ROTATE_90
		private static int rotate270(int i, int j) {
			return j * 10 + (9 - 1 - i);
		}

		// Reflections are self-inverse
		private static int reflectHorizontal(int i, int j) {
			return (9 - 1 - i) * 10 + j;
		}

		private static int reflectVertical(int i, int j) {
			return i * 10 + (9 - 1 - j);
		}

		private static int reflectDiagonalMain(int i, int j) {
			return j * 10 + i;
		}

		private static int reflectDiagonalAnti(int i, int j) {
			return (9 - 1 - j) * 10 + (9 - 1 - i);
		}

		public Action reverseAction(Action a) {
			int rowFrom = a.getRowFrom();
			int colFrom = a.getColumnFrom();
			int rowTo = a.getRowTo();
			int colTo = a.getColumnTo();

			int originalFrom = transformation.apply(rowFrom, colFrom);
			int originalTo = transformation.apply(rowTo, colTo);

			Action applied = null;
			try {
				applied = new Action(((char) ((originalFrom % 10) + 'a')) + "" + (originalFrom / 10 + 1),
						((char) ((originalTo % 10) + 'a')) + "" + (originalTo / 10 + 1), a.getTurn());
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(2);
			}
			return applied;
		}

		public int applyToBoard(Pawn[][] board, Pawn[][] best, Pawn[][] newBoard) {
			int n = board.length;
			boolean isBest = false;
			int comparison = 0;
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++) {
					int originalIndexes = getContentIndexesForCell(i, j);
					newBoard[i][j] = board[originalIndexes / 10][originalIndexes % 10];
					if (!isBest) {
						comparison = newBoard[i][j].ordinal() - best[i][j].ordinal();
						if (comparison < 0)
							return comparison;
						else if (comparison > 0)
							isBest = true;
					}
				}
			return comparison;
		}

		private static final int BOARD_SIZE = 9;
		private static final int CELLS = BOARD_SIZE * BOARD_SIZE;

		private static final int[][] COORD_MAP = new int[values().length][CELLS];
		private static final long[][] ZOBRIST = new long[CELLS][Pawn.values().length];

		static {
			for (Symmetry s : values()) {
				int[] map = new int[CELLS];
				for (int i = 0; i < BOARD_SIZE; i++)
					for (int j = 0; j < BOARD_SIZE; j++) {
						int row = i, col = j;
						switch (s) {
						case ROTATE_90:
							row = BOARD_SIZE - 1 - j;
							col = i;
							break;
						case ROTATE_180:
							row = BOARD_SIZE - 1 - i;
							col = BOARD_SIZE - 1 - j;
							break;
						case ROTATE_270:
							row = j;
							col = BOARD_SIZE - 1 - i;
							break;
						case REFLECT_HORIZONTAL:
							row = BOARD_SIZE - 1 - i;
							break;
						case REFLECT_VERTICAL:
							col = BOARD_SIZE - 1 - j;
							break;
						case REFLECT_DIAGONAL_MAIN:
							row = j;
							col = i;
							break;
						case REFLECT_DIAGONAL_ANTI:
							row = BOARD_SIZE - 1 - j;
							col = BOARD_SIZE - 1 - i;
							break;
						default:
							break;
						}
						map[i * BOARD_SIZE + j] = row * BOARD_SIZE + col;
					}
				COORD_MAP[s.ordinal()] = map;
			}

			Random r = new Random(0xCAFEBABE);
			for (int i = 0; i < CELLS; i++)
				for (int p = 0; p < Pawn.values().length; p++)
					ZOBRIST[i][p] = r.nextLong();
		}

		private int[] map() {
			return COORD_MAP[this.ordinal()];
		}

		public long hashBoard(Pawn[][] board) {
			long h = 0;
			int[] map = map();
			for (int idx = 0; idx < CELLS; idx++) {
				int src = map[idx];
				Pawn p = board[src / BOARD_SIZE][src % BOARD_SIZE];
				h ^= ZOBRIST[idx][p.ordinal()];
			}
			return h;
		}

		public void applyToBoard(Pawn[][] board, Pawn[][] dest) {
			int[] map = map();
			for (int idx = 0; idx < CELLS; idx++) {
				int src = map[idx];
				dest[idx / BOARD_SIZE][idx % BOARD_SIZE] = board[src / BOARD_SIZE][src % BOARD_SIZE];
			}
		}

		static boolean boardsEqual(Pawn[][] board, Symmetry a, Symmetry b) {
			int[] ma = COORD_MAP[a.ordinal()];
			int[] mb = COORD_MAP[b.ordinal()];
			for (int idx = 0; idx < CELLS; idx++) {
				Pawn p1 = board[ma[idx] / BOARD_SIZE][ma[idx] % BOARD_SIZE];
				Pawn p2 = board[mb[idx] / BOARD_SIZE][mb[idx] % BOARD_SIZE];
				if (p1 != p2)
					return false;
			}
			return true;
		}
	}

	private Symmetry applied;
	private Symmetry inverse;
	private List<Symmetry> isSymmetricalBy = new ArrayList<>();
	private long hash;

	private CanonicalState(Symmetry applied, Symmetry inverse, Pawn[][] board, State.Turn turn, long hash) {
		super();
		this.applied = applied;
		this.inverse = inverse;
		this.setBoard(board);
		this.setTurn(turn);
		this.hash = hash;
	}

	public CanonicalState() {
		super();
	}

	public static CanonicalState from(State original) {
		return findCanonical(original.getBoard(), original.getTurn());
	}

	private static CanonicalState findCanonical(Pawn[][] board, State.Turn turn) {
		Symmetry[] values = Symmetry.values();
		Symmetry best = values[0];
		long bestHash = best.hashBoard(board);
		List<Symmetry> symmetries = new ArrayList<>();

		for (int i = 1; i < values.length; i++) {
			long h = values[i].hashBoard(board);
			if (h < bestHash) {
				bestHash = h;
				best = values[i];
			} else if (h == bestHash)
				symmetries.add(values[i].compose(best));
		}

		Pawn[][] newBoard = new Pawn[9][9];
		best.applyToBoard(board, newBoard);

		CanonicalState result = new CanonicalState(best, best.getInverse(), newBoard, turn, bestHash);
		result.isSymmetricalBy = symmetries;
		return result;
	}

	public Symmetry getApplied() {
		return applied;
	}

	public Symmetry getInverse() {
		return inverse;
	}

	public List<Symmetry> getIsSymmetricalBy() {
		return isSymmetricalBy;
	}

	public void setApplied(Symmetry applied) {
		this.applied = applied;
		this.inverse = applied.getInverse();
	}

	public long getHash() {
		return this.hash;
	}
}

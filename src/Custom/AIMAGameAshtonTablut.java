package Custom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.exceptions.*;
import Custom.BlackHeuristics;
import Custom.CanonicalState.Symmetry;
import Custom.Heuristics;
import Custom.WhiteHeuristics;

/**
 * 
 * Game engine inspired by the Ashton Rules of Tablut
 * 
 * 
 * @author A. Piretti, Andrea Galassi
 *
 */
public class AIMAGameAshtonTablut implements Game, aima.core.search.adversarial.Game<State, Action, State.Turn> {

	/**
	 * Number of repeated states that can occur before a draw
	 */
	private int repeated_moves_allowed;

	/**
	 * Counter for the moves without capturing that have occurred
	 */
	private int movesWithutCapturing;
	private String gameLogName;
	private File gameLog;
	private FileHandler fh;
	private Logger loggGame;
	private List<String> citadels;
	private Map<State, Map<Action, State>> results = new HashMap<>();
	private Map<State, Double> utilities = new HashMap<>();
	private Map<State, List<Symmetry>> drawConditions = new HashMap<>();
	private Map<Integer, List<State>> numberOfPawns = new HashMap<>();

	public AIMAGameAshtonTablut(int repeated_moves_allowed, int cache_size, String logs_folder, String whiteName,
			String blackName) {
		this(new StateTablut(), repeated_moves_allowed, cache_size, logs_folder, whiteName, blackName);
	}

	public AIMAGameAshtonTablut(State state, int repeated_moves_allowed, int cache_size, String logs_folder,
			String whiteName, String blackName) {
		super();
		this.repeated_moves_allowed = repeated_moves_allowed;
		this.movesWithutCapturing = 0;

		Path p = Paths.get(logs_folder + File.separator + "_" + whiteName + "_vs_" + blackName + "_"
				+ new Date().getTime() + "_gameLog.txt");
		p = p.toAbsolutePath();
		this.gameLogName = p.toString();
		File gamefile = new File(this.gameLogName);
		try {
			File f = new File(logs_folder);
			f.mkdirs();
			if (!gamefile.exists()) {
				gamefile.createNewFile();
			}
			this.gameLog = gamefile;
			fh = null;
			fh = new FileHandler(gameLogName, true);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		this.loggGame = Logger.getLogger("GameLog");
		loggGame.addHandler(this.fh);
		this.fh.setFormatter(new SimpleFormatter());
		loggGame.setLevel(Level.OFF);
		loggGame.fine("Players:\t" + whiteName + "\tvs\t" + blackName);
		loggGame.fine("Repeated moves allowed:\t" + repeated_moves_allowed + "\tCache:\t" + cache_size);
		loggGame.fine("Inizio partita");
		loggGame.fine("Stato:\n" + state.toString());
		this.citadels = new ArrayList<String>();
		// this.strangeCitadels = new ArrayList<String>();
		this.citadels.add("a4");
		this.citadels.add("a5");
		this.citadels.add("a6");
		this.citadels.add("b5");
		this.citadels.add("d1");
		this.citadels.add("e1");
		this.citadels.add("f1");
		this.citadels.add("e2");
		this.citadels.add("i4");
		this.citadels.add("i5");
		this.citadels.add("i6");
		this.citadels.add("h5");
		this.citadels.add("d9");
		this.citadels.add("e9");
		this.citadels.add("f9");
		this.citadels.add("e8");
		// this.strangeCitadels.add("e1");
		// this.strangeCitadels.add("a5");
		// this.strangeCitadels.add("i5");
		// this.strangeCitadels.add("e9");
	}

	@Override
	public State checkMove(State state, Action action)
			throws BoardException, ActionException, StopException, PawnException, DiagonalException, ClimbingException,
			ThroneException, OccupitedException, ClimbingCitadelException, CitadelException {

		if (isPossibleMove(state, action)) {

			// se sono arrivato qui, muovo la pedina
			state = this.movePawn(state, action);

			// a questo punto controllo lo stato per eventuali catture
			if (state.getTurn().equalsTurn("W")) {
				state = this.checkCaptureBlack(state, action);
			} else if (state.getTurn().equalsTurn("B")) {
				state = this.checkCaptureWhite(state, action);
			}

			// if something has been captured, clear cache for draws
			if (this.movesWithutCapturing == 0) {
				this.loggGame.fine("Capture! Draw cache cleared!");
			}

			// controllo pareggio
			/*
			 * int trovati = 0; for (State s : drawConditions) {
			 * 
			 * System.out.println(s.toString());
			 * 
			 * if (s.equals(state)) { // DEBUG: // // System.out.println("UGUALI:"); //
			 * System.out.println("STATO VECCHIO:\t" + s.toLinearString()); //
			 * System.out.println("STATO NUOVO:\t" + // state.toLinearString());
			 * 
			 * trovati++; if (trovati > repeated_moves_allowed) {
			 * state.setTurn(State.Turn.DRAW); this.loggGame.
			 * fine("Partita terminata in pareggio per numero di stati ripetuti"); break; }
			 * } else { // DEBUG: // // System.out.println("DIVERSI:"); //
			 * System.out.println("STATO VECCHIO:\t" + s.toLinearString()); //
			 * System.out.println("STATO NUOVO:\t" + // state.toLinearString()); } } if
			 * (trovati > 0) { this.loggGame.fine("Equal states found: " + trovati); }
			 * this.drawConditions.add(state.clone());
			 */

			this.loggGame.fine("Stato:\n" + state.toString());
			// System.out.println("Stato:\n" + state.toString());

			return state;
		}

		return null;
	}

	private State checkCaptureWhite(State state, Action a) {
		// controllo se mangio a destra
		if (a.getColumnTo() < state.getBoard().length - 2
				&& state.getPawn(a.getRowTo(), a.getColumnTo() + 1).equalsPawn("B")
				&& (state.getPawn(a.getRowTo(), a.getColumnTo() + 2).equalsPawn("W")
						|| state.getPawn(a.getRowTo(), a.getColumnTo() + 2).equalsPawn("T")
						|| state.getPawn(a.getRowTo(), a.getColumnTo() + 2).equalsPawn("K")
						|| (this.citadels.contains(state.getBox(a.getRowTo(), a.getColumnTo() + 2))
								&& !(a.getColumnTo() + 2 == 8 && a.getRowTo() == 4)
								&& !(a.getColumnTo() + 2 == 4 && a.getRowTo() == 0)
								&& !(a.getColumnTo() + 2 == 4 && a.getRowTo() == 8)
								&& !(a.getColumnTo() + 2 == 0 && a.getRowTo() == 4)))) {
			state.removePawn(a.getRowTo(), a.getColumnTo() + 1);
			this.movesWithutCapturing = -1;
			this.loggGame.fine("Pedina nera rimossa in: " + state.getBox(a.getRowTo(), a.getColumnTo() + 1));
		}
		// controllo se mangio a sinistra
		if (a.getColumnTo() > 1 && state.getPawn(a.getRowTo(), a.getColumnTo() - 1).equalsPawn("B")
				&& (state.getPawn(a.getRowTo(), a.getColumnTo() - 2).equalsPawn("W")
						|| state.getPawn(a.getRowTo(), a.getColumnTo() - 2).equalsPawn("T")
						|| state.getPawn(a.getRowTo(), a.getColumnTo() - 2).equalsPawn("K")
						|| (this.citadels.contains(state.getBox(a.getRowTo(), a.getColumnTo() - 2))
								&& !(a.getColumnTo() - 2 == 8 && a.getRowTo() == 4)
								&& !(a.getColumnTo() - 2 == 4 && a.getRowTo() == 0)
								&& !(a.getColumnTo() - 2 == 4 && a.getRowTo() == 8)
								&& !(a.getColumnTo() - 2 == 0 && a.getRowTo() == 4)))) {
			state.removePawn(a.getRowTo(), a.getColumnTo() - 1);
			this.movesWithutCapturing = -1;
			this.loggGame.fine("Pedina nera rimossa in: " + state.getBox(a.getRowTo(), a.getColumnTo() - 1));
		}
		// controllo se mangio sopra
		if (a.getRowTo() > 1 && state.getPawn(a.getRowTo() - 1, a.getColumnTo()).equalsPawn("B")
				&& (state.getPawn(a.getRowTo() - 2, a.getColumnTo()).equalsPawn("W")
						|| state.getPawn(a.getRowTo() - 2, a.getColumnTo()).equalsPawn("T")
						|| state.getPawn(a.getRowTo() - 2, a.getColumnTo()).equalsPawn("K")
						|| (this.citadels.contains(state.getBox(a.getRowTo() - 2, a.getColumnTo()))
								&& !(a.getColumnTo() == 8 && a.getRowTo() - 2 == 4)
								&& !(a.getColumnTo() == 4 && a.getRowTo() - 2 == 0)
								&& !(a.getColumnTo() == 4 && a.getRowTo() - 2 == 8)
								&& !(a.getColumnTo() == 0 && a.getRowTo() - 2 == 4)))) {
			state.removePawn(a.getRowTo() - 1, a.getColumnTo());
			this.movesWithutCapturing = -1;
			this.loggGame.fine("Pedina nera rimossa in: " + state.getBox(a.getRowTo() - 1, a.getColumnTo()));
		}
		// controllo se mangio sotto
		if (a.getRowTo() < state.getBoard().length - 2
				&& state.getPawn(a.getRowTo() + 1, a.getColumnTo()).equalsPawn("B")
				&& (state.getPawn(a.getRowTo() + 2, a.getColumnTo()).equalsPawn("W")
						|| state.getPawn(a.getRowTo() + 2, a.getColumnTo()).equalsPawn("T")
						|| state.getPawn(a.getRowTo() + 2, a.getColumnTo()).equalsPawn("K")
						|| (this.citadels.contains(state.getBox(a.getRowTo() + 2, a.getColumnTo()))
								&& !(a.getColumnTo() == 8 && a.getRowTo() + 2 == 4)
								&& !(a.getColumnTo() == 4 && a.getRowTo() + 2 == 0)
								&& !(a.getColumnTo() == 4 && a.getRowTo() + 2 == 8)
								&& !(a.getColumnTo() == 0 && a.getRowTo() + 2 == 4)))) {
			state.removePawn(a.getRowTo() + 1, a.getColumnTo());
			this.movesWithutCapturing = -1;
			this.loggGame.fine("Pedina nera rimossa in: " + state.getBox(a.getRowTo() + 1, a.getColumnTo()));
		}
		// controllo se ho vinto
		if (a.getRowTo() == 0 || a.getRowTo() == state.getBoard().length - 1 || a.getColumnTo() == 0
				|| a.getColumnTo() == state.getBoard().length - 1) {
			if (state.getPawn(a.getRowTo(), a.getColumnTo()).equalsPawn("K")) {
				state.setTurn(State.Turn.WHITEWIN);
				this.loggGame.fine("Bianco vince con re in " + a.getTo());
			}
		}
		// TODO: implement the winning condition of the capture of the last
		// black checker

		this.movesWithutCapturing++;
		return state;
	}

	private State checkCaptureBlackKingLeft(State state, Action a) {
		// ho il re sulla sinistra
		if (a.getColumnTo() > 1 && state.getPawn(a.getRowTo(), a.getColumnTo() - 1).equalsPawn("K")) {
			// System.out.println("Ho il re sulla sinistra");
			// re sul trono
			if (state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("e5")) {
				if (state.getPawn(3, 4).equalsPawn("B") && state.getPawn(4, 3).equalsPawn("B")
						&& state.getPawn(5, 4).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo(), a.getColumnTo() - 1));
				}
			}
			// re adiacente al trono
			if (state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("e4")) {
				if (state.getPawn(2, 4).equalsPawn("B") && state.getPawn(3, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo(), a.getColumnTo() - 1));
				}
			}
			if (state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("f5")) {
				if (state.getPawn(5, 5).equalsPawn("B") && state.getPawn(3, 5).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo(), a.getColumnTo() - 1));
				}
			}
			if (state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("e6")) {
				if (state.getPawn(6, 4).equalsPawn("B") && state.getPawn(5, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo(), a.getColumnTo() - 1));
				}
			}
			// sono fuori dalle zone del trono
			if (!state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("e5")
					&& !state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("e6")
					&& !state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("e4")
					&& !state.getBox(a.getRowTo(), a.getColumnTo() - 1).equals("f5")) {
				if (state.getPawn(a.getRowTo(), a.getColumnTo() - 2).equalsPawn("B")
						|| this.citadels.contains(state.getBox(a.getRowTo(), a.getColumnTo() - 2))) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo(), a.getColumnTo() - 1));
				}
			}
		}
		return state;
	}

	private State checkCaptureBlackKingRight(State state, Action a) {
		// ho il re sulla destra
		if (a.getColumnTo() < state.getBoard().length - 2
				&& (state.getPawn(a.getRowTo(), a.getColumnTo() + 1).equalsPawn("K"))) {
			// System.out.println("Ho il re sulla destra");
			// re sul trono
			if (state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("e5")) {
				if (state.getPawn(3, 4).equalsPawn("B") && state.getPawn(4, 5).equalsPawn("B")
						&& state.getPawn(5, 4).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo(), a.getColumnTo() + 1));
				}
			}
			// re adiacente al trono
			if (state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("e4")) {
				if (state.getPawn(2, 4).equalsPawn("B") && state.getPawn(3, 5).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo(), a.getColumnTo() + 1));
				}
			}
			if (state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("e6")) {
				if (state.getPawn(5, 5).equalsPawn("B") && state.getPawn(6, 4).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo(), a.getColumnTo() + 1));
				}
			}
			if (state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("d5")) {
				if (state.getPawn(3, 3).equalsPawn("B") && state.getPawn(5, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo(), a.getColumnTo() + 1));
				}
			}
			// sono fuori dalle zone del trono
			if (!state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("d5")
					&& !state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("e6")
					&& !state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("e4")
					&& !state.getBox(a.getRowTo(), a.getColumnTo() + 1).equals("e5")) {
				if (state.getPawn(a.getRowTo(), a.getColumnTo() + 2).equalsPawn("B")
						|| this.citadels.contains(state.getBox(a.getRowTo(), a.getColumnTo() + 2))) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo(), a.getColumnTo() + 1));
				}
			}
		}
		return state;
	}

	private State checkCaptureBlackKingDown(State state, Action a) {
		// ho il re sotto
		if (a.getRowTo() < state.getBoard().length - 2
				&& state.getPawn(a.getRowTo() + 1, a.getColumnTo()).equalsPawn("K")) {
			// System.out.println("Ho il re sotto");
			// re sul trono
			if (state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("e5")) {
				if (state.getPawn(5, 4).equalsPawn("B") && state.getPawn(4, 5).equalsPawn("B")
						&& state.getPawn(4, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo() + 1, a.getColumnTo()));
				}
			}
			// re adiacente al trono
			if (state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("e4")) {
				if (state.getPawn(3, 3).equalsPawn("B") && state.getPawn(3, 5).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo() + 1, a.getColumnTo()));
				}
			}
			if (state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("d5")) {
				if (state.getPawn(4, 2).equalsPawn("B") && state.getPawn(5, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo() + 1, a.getColumnTo()));
				}
			}
			if (state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("f5")) {
				if (state.getPawn(4, 6).equalsPawn("B") && state.getPawn(5, 5).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo() + 1, a.getColumnTo()));
				}
			}
			// sono fuori dalle zone del trono
			if (!state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("d5")
					&& !state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("e4")
					&& !state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("f5")
					&& !state.getBox(a.getRowTo() + 1, a.getColumnTo()).equals("e5")) {
				if (state.getPawn(a.getRowTo() + 2, a.getColumnTo()).equalsPawn("B")
						|| this.citadels.contains(state.getBox(a.getRowTo() + 2, a.getColumnTo()))) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo() + 1, a.getColumnTo()));
				}
			}
		}
		return state;
	}

	private State checkCaptureBlackKingUp(State state, Action a) {
		// ho il re sopra
		if (a.getRowTo() > 1 && state.getPawn(a.getRowTo() - 1, a.getColumnTo()).equalsPawn("K")) {
			// System.out.println("Ho il re sopra");
			// re sul trono
			if (state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("e5")) {
				if (state.getPawn(3, 4).equalsPawn("B") && state.getPawn(4, 5).equalsPawn("B")
						&& state.getPawn(4, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo() - 1, a.getColumnTo()));
				}
			}
			// re adiacente al trono
			if (state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("e6")) {
				if (state.getPawn(5, 3).equalsPawn("B") && state.getPawn(5, 5).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo() - 1, a.getColumnTo()));
				}
			}
			if (state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("d5")) {
				if (state.getPawn(4, 2).equalsPawn("B") && state.getPawn(3, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo() - 1, a.getColumnTo()));
				}
			}
			if (state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("f5")) {
				if (state.getPawn(4, 6).equalsPawn("B") && state.getPawn(3, 5).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo() - 1, a.getColumnTo()));
				}
			}
			// sono fuori dalle zone del trono
			if (!state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("d5")
					&& !state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("e6")
					&& !state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("f5")
					&& !state.getBox(a.getRowTo() - 1, a.getColumnTo()).equals("e5")) {
				if (state.getPawn(a.getRowTo() - 2, a.getColumnTo()).equalsPawn("B")
						|| this.citadels.contains(state.getBox(a.getRowTo() - 2, a.getColumnTo()))) {
					state.setTurn(State.Turn.BLACKWIN);
					this.loggGame
							.fine("Nero vince con re catturato in: " + state.getBox(a.getRowTo() - 1, a.getColumnTo()));
				}
			}
		}
		return state;
	}

	private State checkCaptureBlackPawnRight(State state, Action a) {
		// mangio a destra
		if (a.getColumnTo() < state.getBoard().length - 2
				&& state.getPawn(a.getRowTo(), a.getColumnTo() + 1).equalsPawn("W")) {
			if (state.getPawn(a.getRowTo(), a.getColumnTo() + 2).equalsPawn("B")) {
				state.removePawn(a.getRowTo(), a.getColumnTo() + 1);
				this.movesWithutCapturing = -1;
				this.loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.getRowTo(), a.getColumnTo() + 1));
			}
			if (state.getPawn(a.getRowTo(), a.getColumnTo() + 2).equalsPawn("T")) {
				state.removePawn(a.getRowTo(), a.getColumnTo() + 1);
				this.movesWithutCapturing = -1;
				this.loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.getRowTo(), a.getColumnTo() + 1));
			}
			if (this.citadels.contains(state.getBox(a.getRowTo(), a.getColumnTo() + 2))) {
				state.removePawn(a.getRowTo(), a.getColumnTo() + 1);
				this.movesWithutCapturing = -1;
				this.loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.getRowTo(), a.getColumnTo() + 1));
			}
			if (state.getBox(a.getRowTo(), a.getColumnTo() + 2).equals("e5")) {
				state.removePawn(a.getRowTo(), a.getColumnTo() + 1);
				this.movesWithutCapturing = -1;
				this.loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.getRowTo(), a.getColumnTo() + 1));
			}

		}

		return state;
	}

	private State checkCaptureBlackPawnLeft(State state, Action a) {
		// mangio a sinistra
		if (a.getColumnTo() > 1 && state.getPawn(a.getRowTo(), a.getColumnTo() - 1).equalsPawn("W")
				&& (state.getPawn(a.getRowTo(), a.getColumnTo() - 2).equalsPawn("B")
						|| state.getPawn(a.getRowTo(), a.getColumnTo() - 2).equalsPawn("T")
						|| this.citadels.contains(state.getBox(a.getRowTo(), a.getColumnTo() - 2))
						|| (state.getBox(a.getRowTo(), a.getColumnTo() - 2).equals("e5")))) {
			state.removePawn(a.getRowTo(), a.getColumnTo() - 1);
			this.movesWithutCapturing = -1;
			this.loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.getRowTo(), a.getColumnTo() - 1));
		}
		return state;
	}

	private State checkCaptureBlackPawnUp(State state, Action a) {
		// controllo se mangio sopra
		if (a.getRowTo() > 1 && state.getPawn(a.getRowTo() - 1, a.getColumnTo()).equalsPawn("W")
				&& (state.getPawn(a.getRowTo() - 2, a.getColumnTo()).equalsPawn("B")
						|| state.getPawn(a.getRowTo() - 2, a.getColumnTo()).equalsPawn("T")
						|| this.citadels.contains(state.getBox(a.getRowTo() - 2, a.getColumnTo()))
						|| (state.getBox(a.getRowTo() - 2, a.getColumnTo()).equals("e5")))) {
			state.removePawn(a.getRowTo() - 1, a.getColumnTo());
			this.movesWithutCapturing = -1;
			this.loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.getRowTo() - 1, a.getColumnTo()));
		}
		return state;
	}

	private State checkCaptureBlackPawnDown(State state, Action a) {
		// controllo se mangio sotto
		if (a.getRowTo() < state.getBoard().length - 2
				&& state.getPawn(a.getRowTo() + 1, a.getColumnTo()).equalsPawn("W")
				&& (state.getPawn(a.getRowTo() + 2, a.getColumnTo()).equalsPawn("B")
						|| state.getPawn(a.getRowTo() + 2, a.getColumnTo()).equalsPawn("T")
						|| this.citadels.contains(state.getBox(a.getRowTo() + 2, a.getColumnTo()))
						|| (state.getBox(a.getRowTo() + 2, a.getColumnTo()).equals("e5")))) {
			state.removePawn(a.getRowTo() + 1, a.getColumnTo());
			this.movesWithutCapturing = -1;
			this.loggGame.fine("Pedina bianca rimossa in: " + state.getBox(a.getRowTo() + 1, a.getColumnTo()));
		}
		return state;
	}

	private State checkCaptureBlack(State state, Action a) {

		this.checkCaptureBlackPawnRight(state, a);
		this.checkCaptureBlackPawnLeft(state, a);
		this.checkCaptureBlackPawnUp(state, a);
		this.checkCaptureBlackPawnDown(state, a);
		this.checkCaptureBlackKingRight(state, a);
		this.checkCaptureBlackKingLeft(state, a);
		this.checkCaptureBlackKingDown(state, a);
		this.checkCaptureBlackKingUp(state, a);

		this.movesWithutCapturing++;
		return state;
	}

	private State movePawn(State state, Action a) {
		State.Pawn pawn = state.getPawn(a.getRowFrom(), a.getColumnFrom());
		State.Pawn[][] newBoard = state.getBoard();
		// State newState = new State();
		this.loggGame.fine("Movimento pedina");
		// libero il trono o una casella qualunque
		if (a.getColumnFrom() == 4 && a.getRowFrom() == 4) {
			newBoard[a.getRowFrom()][a.getColumnFrom()] = State.Pawn.THRONE;
		} else {
			newBoard[a.getRowFrom()][a.getColumnFrom()] = State.Pawn.EMPTY;
		}

		// metto nel nuovo tabellone la pedina mossa
		newBoard[a.getRowTo()][a.getColumnTo()] = pawn;
		// aggiorno il tabellone
		state.setBoard(newBoard);
		// cambio il turno
		if (state.getTurn().equalsTurn(State.Turn.WHITE.toString())) {
			state.setTurn(State.Turn.BLACK);
		} else {
			state.setTurn(State.Turn.WHITE);
		}

		return state;
	}

	public File getGameLog() {
		return gameLog;
	}

	public int getMovesWithutCapturing() {
		return movesWithutCapturing;
	}

	@SuppressWarnings("unused")
	private void setMovesWithutCapturing(int movesWithutCapturing) {
		this.movesWithutCapturing = movesWithutCapturing;
	}

	public int getRepeated_moves_allowed() {
		return repeated_moves_allowed;
	}

	/////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Auxiliary method used to check wheter an action is allowed or not for a given
	 * state.
	 *
	 * @param state  Current state of the game
	 * @param action The action to be checked
	 *
	 * @return true if the action is allowed, false otherwise.
	 */
	private boolean isPossibleMove(State state, Action action) {
		this.loggGame.fine(action.toString());
		// controllo la mossa
		if (action.getTo().length() != 2 || action.getFrom().length() != 2) {
			return false;
		}
		int columnFrom = action.getColumnFrom();
		int columnTo = action.getColumnTo();
		int rowFrom = action.getRowFrom();
		int rowTo = action.getRowTo();

		// controllo se sono fuori dal tabellone
		if (columnFrom > state.getBoard().length - 1 || rowFrom > state.getBoard().length - 1
				|| rowTo > state.getBoard().length - 1 || columnTo > state.getBoard().length - 1 || columnFrom < 0
				|| rowFrom < 0 || rowTo < 0 || columnTo < 0) {
			return false;
		}

		// controllo che non vada sul trono
		if (state.getPawn(rowTo, columnTo).equalsPawn(State.Pawn.THRONE.toString())) {
			return false;
		}

		// controllo la casella di arrivo
		if (!state.getPawn(rowTo, columnTo).equalsPawn(State.Pawn.EMPTY.toString())) {
			return false;
		}
		if (this.citadels.contains(state.getBox(rowTo, columnTo))
				&& !this.citadels.contains(state.getBox(rowFrom, columnFrom))) {
			return false;
		}
		if (this.citadels.contains(state.getBox(rowTo, columnTo))
				&& this.citadels.contains(state.getBox(rowFrom, columnFrom))) {
			if (rowFrom == rowTo) {
				if (columnFrom - columnTo > 5 || columnFrom - columnTo < -5) {
					return false;
				}
			} else {
				if (rowFrom - rowTo > 5 || rowFrom - rowTo < -5) {
					return false;
				}
			}
		}

		// controllo se cerco di stare fermo
		if (rowFrom == rowTo && columnFrom == columnTo) {
			return false;
		}

		// controllo se sto muovendo una pedina giusta
		if (state.getTurn().equalsTurn(State.Turn.WHITE.toString())) {
			if (!state.getPawn(rowFrom, columnFrom).equalsPawn("W")
					&& !state.getPawn(rowFrom, columnFrom).equalsPawn("K")) {
				return false;
			}
		}
		if (state.getTurn().equalsTurn(State.Turn.BLACK.toString())) {
			if (!state.getPawn(rowFrom, columnFrom).equalsPawn("B")) {
				return false;
			}
		}

		// controllo di non muovere in diagonale
		if (rowFrom != rowTo && columnFrom != columnTo) {
			return false;
		}

		// controllo di non scavalcare pedine
		if (rowFrom == rowTo) {
			if (columnFrom > columnTo) {
				for (int i = columnTo; i < columnFrom; i++) {
					if (!state.getPawn(rowFrom, i).equalsPawn(State.Pawn.EMPTY.toString())) {
						if (state.getPawn(rowFrom, i).equalsPawn(State.Pawn.THRONE.toString())) {
							return false;
						} else {
							return false;
						}
					}
					if (this.citadels.contains(state.getBox(rowFrom, i))
							&& !this.citadels.contains(state.getBox(action.getRowFrom(), action.getColumnFrom()))) {
						return false;
					}
				}
			} else {
				for (int i = columnFrom + 1; i <= columnTo; i++) {
					if (!state.getPawn(rowFrom, i).equalsPawn(State.Pawn.EMPTY.toString())) {
						if (state.getPawn(rowFrom, i).equalsPawn(State.Pawn.THRONE.toString())) {
							return false;
						} else {
							return false;
						}
					}
					if (this.citadels.contains(state.getBox(rowFrom, i))
							&& !this.citadels.contains(state.getBox(action.getRowFrom(), action.getColumnFrom()))) {
						return false;
					}
				}
			}
		} else {
			if (rowFrom > rowTo) {
				for (int i = rowTo; i < rowFrom; i++) {
					if (!state.getPawn(i, columnFrom).equalsPawn(State.Pawn.EMPTY.toString())) {
						if (state.getPawn(i, columnFrom).equalsPawn(State.Pawn.THRONE.toString())) {
							return false;
						} else {
							return false;
						}
					}
					if (this.citadels.contains(state.getBox(i, columnFrom))
							&& !this.citadels.contains(state.getBox(action.getRowFrom(), action.getColumnFrom()))) {
						return false;
					}
				}
			} else {
				for (int i = rowFrom + 1; i <= rowTo; i++) {
					if (!state.getPawn(i, columnFrom).equalsPawn(State.Pawn.EMPTY.toString())) {
						if (state.getPawn(i, columnFrom).equalsPawn(State.Pawn.THRONE.toString())) {
							return false;
						} else {
							return false;
						}
					}
					if (this.citadels.contains(state.getBox(i, columnFrom))
							&& !this.citadels.contains(state.getBox(action.getRowFrom(), action.getColumnFrom()))) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////////////

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void clearCache(Integer newNumberOfPawn) {
		for (State state : numberOfPawns.get(newNumberOfPawn + 1)) {
			results.remove(state);
			utilities.remove(state);
			drawConditions.remove(state);
		}
	}

	/**
	 * Method that compute a list of all possible actions for the current player
	 * according to the rules of the game
	 *
	 * @param state Current state of the game
	 *
	 * @return List of all the Action allowed from current state for each pawn of
	 *         the player
	 */
	@Override
	public List<Action> getActions(State state) {
		State.Turn turn = state.getTurn();
		if (!drawConditions.containsKey(state)) {
			State s = state.clone();
			s.setTurn(Turn.DRAW);
			drawConditions.put(s, new ArrayList<>());
			drawConditions.get(s).add(((CanonicalState) state).getApplied());
		} else
			drawConditions.get(state)
					.add(drawConditions.get(state).getLast().compose(((CanonicalState) state).getApplied()));
		if (!results.containsKey(state)) {
			List<Action> possibleActions = new ArrayList<>();
			List<String> possibleActionsSymmetries = new ArrayList<>();
			int pawns = state.getNumberOf(Pawn.WHITE) + state.getNumberOf(Pawn.BLACK);
			if (!numberOfPawns.containsKey(pawns))
				numberOfPawns.put(pawns, new ArrayList<State>());
			numberOfPawns.get(pawns).add(state);
			results.put(state, new HashMap<>());
			// Loop through rows
			for (int i = 0; i < 9; i++) {
				// Loop through columns
				for (int j = 0; j < 9; j++) {
					State.Pawn p = state.getPawn(i, j);
					// If pawn color is equal of turn color
					if (p.toString().equals(turn.toString())
							|| (p.equals(State.Pawn.KING) && turn.equals(State.Turn.WHITE))) {
						boolean alreadyVisitedASymmetricalPawn = false;
						List<Symmetry> symmetriesOfState = ((CanonicalState) state).getIsSymmetricalBy();
						for (Symmetry s : symmetriesOfState) {
							int transformedCell = s.getContentIndexesForCell(i, j);
							int transformedRow = transformedCell / 10;
							int transformedColumn = transformedCell % 10;
							if (transformedRow < i || (transformedRow == i && transformedColumn < j)) {
								alreadyVisitedASymmetricalPawn = true;
								break;
							}
						}
						if (alreadyVisitedASymmetricalPawn)
							continue;
						// Search on top of pawn
						try {
							for (int k = i - 1; k >= 0; k--) {
								// Break if pawn is out of citadels, and it is moving on a citadel
								if (!citadels.contains(state.getBox(i, j)) && citadels.contains(state.getBox(k, j))) {
									break;
								}
								// Check if we are moving on an empty cell
								else if (state.getPawn(k, j).equalsPawn(State.Pawn.EMPTY.toString())) {

									String from = state.getBox(i, j);
									String to = state.getBox(k, j);
									Action action = new Action(from, to, turn);
									if (!possibleActionsSymmetries.contains(action.toString())
											&& isPossibleMove(state.clone(), action)) {
										possibleActions.add(action);
										possibleActionsSymmetries.addAll(symmetriesOfState.stream()
												.map(s -> s.reverseAction(action).toString()).toList());
									}
								}
								/*
								 * try { State result = CanonicalState.from(checkMove(state.clone(), action));
								 * if (!results.get(state).containsValue(result)) results.get(state).put(action,
								 * result); } catch (Exception e) { break; }
								 */
							}
							// Search on bottom of pawn
							for (int k = i + 1; k < state.getBoard().length; k++) {
								// Break if pawn is out of citadels, and it is moving on a citadel
								if (!citadels.contains(state.getBox(i, j)) && citadels.contains(state.getBox(k, j))) {
									break;
								}
								// Check if we are moving on an empty cell
								else if (state.getPawn(k, j).equalsPawn(State.Pawn.EMPTY.toString())) {
									String from = state.getBox(i, j);
									String to = state.getBox(k, j);

									Action action = new Action(from, to, turn);
									if (!possibleActionsSymmetries.contains(action.toString())
											&& isPossibleMove(state.clone(), action)) {
										possibleActions.add(action);
										possibleActionsSymmetries.addAll(symmetriesOfState.stream()
												.map(s -> s.reverseAction(action).toString()).toList());
									}
								}
								/*
								 * try { State result = CanonicalState.from(checkMove(state.clone(), action));
								 * if (!results.get(state).containsValue(result)) results.get(state).put(action,
								 * result); } catch (Exception e) { break; }
								 */
							}

							// Search on left of pawn
							for (int k = j - 1; k >= 0; k--) {
								// Break if pawn is out of citadels, and it is moving on a citadel
								if (!citadels.contains(state.getBox(i, j)) && citadels.contains(state.getBox(i, k))) {
									break;
								}
								// Check if we are moving on an empty cell
								else if (state.getPawn(i, k).equalsPawn(State.Pawn.EMPTY.toString())) {
									String from = state.getBox(i, j);
									String to = state.getBox(i, k);

									Action action = new Action(from, to, turn);
									if (!possibleActionsSymmetries.contains(action.toString())
											&& isPossibleMove(state.clone(), action)) {
										possibleActions.add(action);
										possibleActionsSymmetries.addAll(symmetriesOfState.stream()
												.map(s -> s.reverseAction(action).toString()).toList());
									}
								}
								/*
								 * try { State result = CanonicalState.from(checkMove(state.clone(), action));
								 * if (!results.get(state).containsValue(result)) results.get(state).put(action,
								 * result); } catch (Exception e) { break; }
								 */
							}
							// Search on right of pawn
							for (int k = j + 1; k < state.getBoard().length; k++) {
								// Break if pawn is out of citadels, and it is moving on a citadel
								if (!citadels.contains(state.getBox(i, j)) && citadels.contains(state.getBox(i, k))) {
									break;
								}
								// Check if we are moving on an empty cell
								else if (state.getPawn(i, k).equalsPawn(State.Pawn.EMPTY.toString())) {
									String from = state.getBox(i, j);
									String to = state.getBox(i, k);

									Action action = new Action(from, to, turn);
									if (!possibleActionsSymmetries.contains(action.toString())
											&& isPossibleMove(state.clone(), action)) {
										possibleActions.add(action);
										possibleActionsSymmetries.addAll(symmetriesOfState.stream()
												.map(s -> s.reverseAction(action).toString()).toList());
									}
								}
								/*
								 * try { State result = CanonicalState.from(checkMove(state.clone(), action));
								 * if (!results.get(state).containsValue(result)) results.get(state).put(action,
								 * result); } catch (Exception e) { break; }
								 */
							}
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(2);
						}
					}
				}
			}
			possibleActions.stream().forEach(p -> results.get(state).put(p, null));
			return possibleActions;
		}
		return results.get(state).keySet().stream().toList();

		// actions = results.get(canonical).keySet().stream().toList();
	}

	/**
	 * Method that performs an action in a given state and returns the resulting
	 * state
	 *
	 * @param state  Current state
	 * @param action Action admissible on the given state
	 *
	 * @return State obtained after performing the action
	 */
	@Override
	public State getResult(State state, Action action) {
		if (!results.containsKey(state))
			results.put(state, new HashMap<>());
		if (!results.get(state).containsKey(action) || results.get(state).get(action) == null)
			try {
				State result = movePawn(state.clone(), action);
				// State result = CanonicalState.from(checkMove(state.clone(), action));
				// if (!results.get(state).containsValue(result))
				if (state.getTurn().equalsTurn("B"))
					result = this.checkCaptureBlack(result, action);
				else if (state.getTurn().equalsTurn("W")) {
					result = this.checkCaptureWhite(result, action);
				}
				results.get(state).put(action, CanonicalState.from(result));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(3);
			}
		State result = CanonicalState.from(results.get(state).get(action).clone());
		Turn turn = result.getTurn();
		result.setTurn(Turn.DRAW);
		if (!drawConditions.containsKey(result) || !drawConditions.get(result)
				.contains(((CanonicalState) result).getApplied().compose(drawConditions.get(result).getLast())))
			result.setTurn(turn);
		return result;
	}

	/**
	 * Check if a state is terminal, since a player has either won or drawn (i.e.
	 * the game ends)
	 *
	 * @param state Current state of the game
	 *
	 * @return true if the current state is terminal, otherwise false
	 */
	@Override
	public boolean isTerminal(State state) {
		return state.getTurn().equals(State.Turn.WHITEWIN) || state.getTurn().equals(State.Turn.BLACKWIN)
				|| state.getTurn().equals(State.Turn.DRAW);
	}

	/**
	 * Method to evaluate a state using heuristics
	 *
	 * @param state Current state
	 * @param turn  Player that want to find the best moves in the search space
	 *
	 * @return Evaluation of the state
	 */
	@Override
	public double getUtility(State state, State.Turn turn) {
		if (!utilities.containsKey(state)) {
			// Terminal state
			if ((turn.equals(State.Turn.BLACK) && state.getTurn().equals(State.Turn.BLACKWIN))
					|| (turn.equals(State.Turn.WHITE) && state.getTurn().equals(State.Turn.WHITEWIN)))
				return Double.POSITIVE_INFINITY; // Win
			else if ((turn.equals(State.Turn.BLACK) && state.getTurn().equals(State.Turn.WHITEWIN))
					|| (turn.equals(State.Turn.WHITE) && state.getTurn().equals(State.Turn.BLACKWIN)))
				return Double.NEGATIVE_INFINITY; // Lose

			// Non-terminal state => get Heuristics for the current state
			Heuristics heuristics = turn.equals(State.Turn.WHITE) ? new WhiteHeuristics(state)
					: new BlackHeuristics(state);
			utilities.put(state, heuristics.evaluateState());
		}
		return utilities.get(state);
	}

	public Map<State, List<Symmetry>> getDrawConditions() {
		return drawConditions;
	}

	public void setDrawConditions(Map<State, List<Symmetry>> drawConditions) {
		this.drawConditions = new HashMap<State, List<Symmetry>>(drawConditions);
	}

	@Override
	public State getInitialState() {
		return null;
	}

	@Override
	public State.Turn[] getPlayers() {
		return State.Turn.values();
	}

	@Override
	public Turn getPlayer(State state) {
		return state.getTurn();
	}

	@Override
	public void endGame(State state) {
		this.loggGame.fine("Stato:\n" + state.toString());
	}
}

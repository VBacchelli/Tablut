package Custom;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class AIMAteursClient extends it.unibo.ai.didattica.competition.tablut.client.TablutClient {

	AIMAGameAshtonTablut tablut;
	IterativeDeepeningAlphaBetaSearch<CanonicalState, Action, Turn> aiPlayer;

	public AIMAteursClient(String player, int timeout, String ipAddress) throws UnknownHostException, IOException {
		super(player.toUpperCase(), "AIMAteurs", timeout, ipAddress);
        this.tablut = new AIMAGameAshtonTablut(0, -1, "logs", "white_ai", "black_ai");;
        this.aiPlayer = new TavolettaSearch(tablut, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, timeout-2);
		// TODO Auto-generated constructor stub
	}

	// setName(String)
	// String getName
	// declareName() = write the name to the server
	// write(Action) = write to the server an action
	// read() = gets state from server

	public static void main(String[] args) throws IOException {
		String ip = "localhost";
		int timeout = 60;

		if (args.length != 3) {
			System.out.printf("Usage: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip>\\n\")");
			System.exit(0);
		}
		try {
			timeout = Integer.parseInt(args[1]);
			ip = args[2];
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.printf("ERROR: Timeout must be an integer representing seconds\n"
					+ "USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip>\n");
			System.exit(1);
		}
		AIMAteursClient player = new AIMAteursClient(args[0], timeout, ip);
		player.run();
	}

	@Override
	public void run() {
	    try {
	        this.declareName();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    State state = new StateTablut();
	    
	    while (true) {
	        try {
	            this.read();
	        } catch (ClassNotFoundException | IOException e) {
	            e.printStackTrace();
	            System.exit(2);
	        }

	        state = this.getCurrentState();
	        if (!state.getTurn().equals(Turn.BLACK) && !state.getTurn().equals(Turn.WHITE))
	            break;

	        if (this.getPlayer().equals(state.getTurn())) {
	            Logger gameLogger = Logger.getLogger("GameLog");
	            gameLogger.setUseParentHandlers(false);	  
	    		aiPlayer.setLogEnabled(false);
	    		CanonicalState transformed = CanonicalState.from(((State) state));

	        	Action best=aiPlayer.makeDecision(transformed);
	        	System.out.println(transformed.getApplied());
	        	best = transformed.getApplied().reverseAction((Action)best);
	        	System.out.println(best);
	            try {
	                this.write(best);
	            } catch (ClassNotFoundException | IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}

}

package custom;

import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.Arrays;

/**
 * Heuristics for the evaluation of a white player state.<br>
 *
 * Description: the defender (white player) heuristics is
 * based on a weighted sum, with 4 different weights (WHITE_ALIVE,
 * BLACK_EATEN, KING_MOVEMENT, SAFE_PAWNS) and bonuses in
 * special cases.<br>
 * The key feature is the pre-evaluation of the king
 * check states: if the king can be captured in a specific
 * state, we immediately return the worst value (-Infinite)
 * for that state, without making further calculations.
 *
 * @author Gionnino9000
 */
public class WhiteHeuristics extends Heuristics{

    private final int WHITE_ALIVE = 0; // White pawns still alive
    private final int BLACK_EATEN = 1; // Black pawns eaten
    private final int KING_MOVEMENT = 2; // Directions in which the king can move
    private final int SAFE_PAWNS = 3; // White pawns that can't be eaten

    // Flag to enable console print
    private boolean print = false;

    private final Double[] gameWeights;

    public WhiteHeuristics(State state) {
        super(state);

        gameWeights = new Double[4];

        gameWeights[WHITE_ALIVE] = 35.0;
        gameWeights[BLACK_EATEN] = 18.0;
        gameWeights[KING_MOVEMENT] = 5.0;
        gameWeights[SAFE_PAWNS] = 42.0;
    }

    /**
     * @return the evaluation of the current state using a weighted sum
     */
    @Override
    public double evaluateState() {
        double stateValue = 0.0;

        int[] kingPos = kingPosition(state);

        // If king can be captured PRUNE THOSE MFS
        if (canBeCaptured(state, kingPos, State.Pawn.KING))
            return Double.NEGATIVE_INFINITY;

        int numbOfBlack = state.getNumberOf(State.Pawn.BLACK);
        int numbOfWhite = state.getNumberOf(State.Pawn.WHITE);
        // Values for the weighted sum
        double numberOfWhiteAlive = (double)  numbOfWhite / 12;
        double numberOfBlackEaten = (double)  (16 - numbOfBlack) / 16;

        double kingMovEval = evalKingMovement(kingPos);
        double evalKingEsc = evalKingEscapes(kingPos);
        double safePawns = getPawnsSafety();
        if (safePawns > 0)
            stateValue += (safePawns / numbOfWhite) * gameWeights[SAFE_PAWNS];

        stateValue += numberOfWhiteAlive * gameWeights[WHITE_ALIVE];
        stateValue += numberOfBlackEaten * gameWeights[BLACK_EATEN];

        stateValue += kingMovEval * gameWeights[KING_MOVEMENT];

        stateValue += evalKingEsc;

        if (print) {
            System.out.println("White pawns alive: " + numberOfWhiteAlive);
            System.out.println("Number of black pawns eaten: " + numberOfBlackEaten);
            System.out.println("King mobility eval: " + kingMovEval);
            System.out.println("Eval king escapes: " + evalKingEsc);
            System.out.println("|GAME|: value is " + stateValue);
        }

        return stateValue;
    }

    /**
     * @param kPos The king position
     *
     * @return a greater value if the king can move in one or more directions
     */
    private double evalKingMovement(int[] kPos) {
        int val = getKingMovement(state, kPos);

        if (val == 0)
            return 0.3;
        if (val == 1)
            return 1.0;

        return 1.2;
    }

    /**
     * @return the number of white pawns that can't be captured
     */
    private int getPawnsSafety() {
        int safe = 0;

        State.Pawn[][] board = state.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j].equalsPawn(State.Pawn.WHITE.toString())) {
                    safe += canBeCaptured(state, new int[]{i, j}, State.Pawn.WHITE) ? 0 : 1;
                }
            }
        }

        return safe;
    }

    /**
     * @param kPos The king Position
     *
     * @return a positive value for a SURE king escape (greater if there are more than once). If there are no escapes, 0.0
     */
    private double evalKingEscapes(int[] kPos) {
        int[] escapes = getKingEscapes(state, kPos);
        int numEsc = Arrays.stream(escapes).sum();
        if (numEsc > 1)
            return 200.0;

        // In case we have one escape only we check whether an enemy can block escape
        else if (numEsc == 1) {
            // Up escape
            if (escapes[0] == 1) {
                for(int i = kPos[0]-1; i >= 0; i--) {
                    int[] checkPos = new int[]{i, kPos[1]};
                    if (checkLeftSide(state, State.Pawn.BLACK, checkPos) || checkRightSide(state, State.Pawn.BLACK, checkPos)) {
                        return 0.0;
                    }
                }
                return 80.0;
            }
            // Down escape
            if (escapes[1] == 1) {
                for(int i = kPos[0]+1; i <= 8; i++) {
                    int[] checkPos = new int[]{i, kPos[1]};
                    if (checkLeftSide(state, State.Pawn.BLACK, checkPos) || checkRightSide(state, State.Pawn.BLACK, checkPos)) {
                        return 0.0;
                    }
                }
                return 80.0;
            }
            // Left escape
            if (escapes[2] == 1) {
                for(int i = kPos[1]-1; i >= 0; i--) {
                    int[] checkPos = new int[]{kPos[0], i};
                    if (checkUpside(state, State.Pawn.BLACK, checkPos) || checkDownside(state, State.Pawn.BLACK, checkPos)) {
                        return 0.0;
                    }
                }
                return 80.0;
            }
            // Right escape
            if (escapes[3] == 1) {
                for(int i = kPos[1]+1; i <= 8; i++) {
                    int[] checkPos = new int[]{kPos[0], i};
                    if (checkUpside(state, State.Pawn.BLACK, checkPos) || checkDownside(state, State.Pawn.BLACK, checkPos)) {
                        return 0.0;
                    }
                }
                return 80.0;
            }
        }
        return 0.0;
    }
}

package Custom;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class MoveResult {
    public final Action action;
    public final State resultingState;

    public MoveResult(Action action, State resultingState) {
        this.action = action;
        this.resultingState = resultingState;
    }
}

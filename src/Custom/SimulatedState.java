package Custom;

import java.util.ArrayList;
import java.util.List;

import aima.core.search.framework.Node;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class SimulatedState extends StateTablut {
	List<State> drawConditions;

	public SimulatedState(State original, List<State> drawConditions) {
		super();
		this.drawConditions = drawConditions;
		this.board = original.getBoard();
		this.turn = original.getTurn();
	}

	public List<State> getDrawConditions() {
		return drawConditions;
	}

	public static <S, A> List<State> findDrawConditions(Node<S, A> node) {
		State present = (State) node.getState();
		List<State> ret = new ArrayList<>();
		if (!node.isRootNode()) {
			State parent = (State) node.getParent().getState();
			if(present.getNumberOf(Pawn.BLACK) == parent.getNumberOf(Pawn.BLACK)
					&& present.getNumberOf(Pawn.WHITE) == parent.getNumberOf(Pawn.WHITE)) {
				ret.add(parent);
				if (parent instanceof SimulatedState)
					ret.addAll(((SimulatedState) parent).getDrawConditions());
				else
					ret.addAll(findDrawConditions(node.getParent()));
			}				
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static <S, A> S from(Node<S, A> node) {
		State state = (State) node.getState();
		SimulatedState ret = new SimulatedState(state, findDrawConditions(node));
		return (S)ret;
	}

	@Override
	public StateTablut clone() {
		return new SimulatedState(super.clone(), this.drawConditions);
	}
}

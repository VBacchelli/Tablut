package Custom;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;

import java.util.ArrayList;
import java.util.List;

public class TreeNode{
    private State state;
    private State.Turn turn;
    private TreeNode parent;
    private List<TreeNode> children;
    private Action action; //azione applicata per ottenere il nodo
    private int depth;
    private int visitCount;
    public  Double totalValue;
    private boolean hasBeenExpanded;
    

    public TreeNode(State state, TreeNode parent, Action action) {
    	this.state = state;
    	this.turn = state.getTurn();
    	if(parent!=null) {
    		this.parent = parent;
    		this.depth = parent.depth + 1;
    		this.action = action;
    		//
    	}
    	else {
    		this.parent = parent;
    		this.depth = 0;
    		this.action = null;
    	}
    	
    	this.hasBeenExpanded = false;
    	this.visitCount = 0;
    	this.totalValue = 0.0;
    	this.children = new ArrayList<TreeNode>();
    }
   
    /*private Double evaluateTerminalState() {
        State.Turn result = this.state.getTurn();

        if (result == State.Turn.WHITEWIN) {
            return this.turn == State.Turn.WHITE ? 1.0 : 0.0;
        } else if (result == State.Turn.BLACKWIN) {
            return this.turn == State.Turn.BLACK ? 1.0 : 0.0;
        } else if (result == State.Turn.DRAW) {
            return 0.5;
        }

        return null;
    }*/
    
    public State.Turn getTurn(){
    	return this.turn;
    }
    
    public State getState() { //we can read it differently
    	return this.state;
    }
    
    
    public void updateState(State state) {
    	//checks to do about state before updating it
    	this.state = state;
    }
    
    public TreeNode getParent(){
    	return parent;
    }
    
    public List<TreeNode> getChildren(){
    	return this.children;
    }
    
    public int getDepth() {
    	return depth;
    }
    
    public Action getOriginAction() {
    	return action;
    }
   
    public int getVisitCount() {
    	return this.visitCount;
    }
    
    public double getTotalValue() {
    	return this.totalValue;
    }
    
    public double getAverageValue()
    {
    	if(this.visitCount == 0 ) return 0.0;
    	return this.totalValue / this.visitCount;
    }
    
    public void addChild(TreeNode node) {
    	node.parent = this;
    	children.add(node);
    }
    
    public void removeChild(TreeNode node) throws Exception{
    	if(!children.remove(node)) {
    		throw new Exception(node + " NOT FOUND ");
    	}
    }
    
    public void VisitNode(Double value) { 
    	if(value != null) {
    		this.totalValue += value;
    	}
    	this.visitCount++;
    }
    

    public void ExpandNode(List<MoveResult> legalMoves, Game rules) {
    	if(this.isFullyExpanded()) return;
    	
    	for(MoveResult move : legalMoves) {
    		TreeNode child = new TreeNode(move.resultingState, this, move.action);
    		this.addChild(child);
    	}
    	
    	this.hasBeenExpanded = true;
    }

    public boolean isFullyExpanded() {
    	return hasBeenExpanded;
    }
    
    public boolean isTerminal() {
    	State.Turn turn = this.state.getTurn();
        return turn.equals(State.Turn.WHITEWIN) ||
               turn.equals(State.Turn.BLACKWIN) ||
               turn.equals(State.Turn.DRAW);
    }
    
    
    
}
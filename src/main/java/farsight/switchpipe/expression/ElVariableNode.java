package farsight.switchpipe.expression;

public abstract class ElVariableNode extends ElExpression {
	
	protected final String fId;

	public ElVariableNode(String id) {
		fId = id;
	}

	public boolean isConstant() {
		return false;
	}
	
	@Override
	public String getID() {
		return fId;
	}

}

package farsight.switchpipe.expression;

public class ElConstant extends ElExpression {
	
	private final String fConstant;

	public ElConstant(String constant) {
		fConstant = constant;
	}
	
	public boolean isConstant() {
		return true;
	};
	
	@Override
	public String getID() {
		return null;
	}

	@Override
	public String evaluate(Object value) {
		return fConstant;
	}
	
	public String toString() {
		return fConstant;
	}



}

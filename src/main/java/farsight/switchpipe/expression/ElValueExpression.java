package farsight.switchpipe.expression;

public class ElValueExpression extends ElVariableNode {
	
	public ElValueExpression(String id) {
		super(id);
	}

	@Override
	public String evaluate(Object value) {
		return String.valueOf(value);
	}
	
	public String toString() {
		return "${" + fId + "}";
	}

}

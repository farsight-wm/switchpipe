package farsight.switchpipe.expression;

public abstract class ElExpression {

	// default implementations
	
	public boolean isConstant() {
		return false;
	}
	
	// abstract template

	public abstract String getID();
	public abstract String evaluate(Object value);
	
	// expression API
	
	public static ElExpression parse(String expression) {
		ElExpression[] list = new ElParser(expression).parse();
		return list.length == 1 && list[0].isConstant() ? list[0] : new ElGroupExpression(list); 
	}	


}

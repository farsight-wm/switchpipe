package farsight.switchpipe.expression;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

public class ElGroupExpression extends ElExpression {
	
	
	private final ElExpression[] fChildren;

	public ElGroupExpression(ElExpression[] children) {
		fChildren = children;
	}
	
	@Override
	public String getID() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String evaluate(Object value) {
		if(value == null) {
			return evaluateWithMap(Collections.emptyMap());
		} else if(value instanceof ElLazyResolver) {
			return evaluateWithResolver((ElLazyResolver) value);
		} else if(value instanceof Map<?, ?>) {
			return evaluateWithMap((Map<Object, Object>) value);			
		} else {
			return evaluateWithObject(value);
		}
	}
	
	// private helper
	
	private String evaluateWithMap(Map<Object, Object> map) {
		StringBuilder buf = new StringBuilder();
		for(ElExpression child: fChildren) {
			Object value = child.isConstant() ? null : map.get(child.getID()); 
			buf.append(child.evaluate(value));
		}
		return buf.toString();
	}
	
	private String evaluateWithObject(Object object) {
		StringBuilder buf = new StringBuilder();
		for(ElExpression child: fChildren) {
			Object value = child.isConstant() ? null : getFromObject(child.getID(), object); 
			buf.append(child.evaluate(value));
		}
		return buf.toString();
	}

	private String evaluateWithResolver(ElLazyResolver resolver) {
		StringBuilder buf = new StringBuilder();
		for(ElExpression child: fChildren) {
			Object value = child.isConstant() ? null : resolver.resolve(child.getID()); 
			buf.append(child.evaluate(value));
		}
		return buf.toString();
	}
	
	
	private Object getFromObject(String name, Object src) {
		if(src == null)
			return null;
		Object value = getField(name, src);
		if(value != null)
			return value;
		return getMethod(name, src);
	}
	
	private Object getField(String name, Object src) {
		try {
			return src.getClass().getDeclaredField(name).get(src);
		} catch (NoSuchFieldException|SecurityException | IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}
	
	private Object getMethod(String name, Object src) {
		try {
			return src.getClass().getDeclaredMethod(name).invoke(src);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(ElExpression child: fChildren)
			buf.append(child.toString());
		return buf.toString();
	}


}

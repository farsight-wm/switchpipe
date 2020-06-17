package farsight.switchpipe.expression;

import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_WORD;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.LinkedList;

public class ElParser {
	
	private final StreamTokenizer fTok;
	private final StringBuilder fBuf = new StringBuilder();
	private final LinkedList<ElExpression> fParts = new LinkedList<>();

	public ElParser(String expression) {
		fTok = new StreamTokenizer(new StringReader(expression));
		fTok.resetSyntax();
		fTok.wordChars(0, 255);
		fTok.ordinaryChar('$');
		fTok.ordinaryChar('{');
		fTok.ordinaryChar('}');
	}
	
	private int next() throws IOException {
		if(fTok.ttype == TT_EOF) {
			throw new RuntimeException("Called next behind TT_EOF!");
		}
		return fTok.nextToken();
	}
	
	
	private void doParse() throws IOException {
		fTok.nextToken(); // set to first token
		while(fTok.ttype != TT_EOF) {
			if(fTok.ttype == '$') {
				parsePotentialExpresion();
			} else {
				parseConstant();
			}
		}	
		flushConstant();
	}
	
	private void parseConstant() throws IOException {
		while(fTok.ttype != '$' && fTok.ttype != TT_EOF) {
			append();
		}
	}
	
	private int append() throws IOException {
		if(fTok.ttype == TT_WORD)
			fBuf.append(fTok.sval);
		else
			fBuf.append((char) fTok.ttype);
		return next();
	}
	
	private void parsePotentialExpresion() throws IOException {
		switch(next()) {
		case '{':
			parseExpression();
			break;
		case '$':
			//encoded $$ -> $
			append();
			break;
		default:
			// implicit $
			fBuf.append('$');	
		}
	}
	
	private void parseExpression() throws IOException {
		flushConstant();
		next(); // remove '{'
		// only SimpleExpression supported for now: read all to buf until '}'
		int ttype;
		while((ttype = append()) != '}' && ttype != TT_EOF);
		
		String exp = fBuf.toString();
		fParts.add(new ElValueExpression(exp));

		fBuf.setLength(0);
		if(ttype != TT_EOF) next();
	}
	

	private void flushConstant() {
		String value = fBuf.toString();
		if(value.length() > 0)
			fParts.add(new ElConstant(value));
		fBuf.setLength(0);	
	}

	public ElExpression[] parse() {
		try {
			doParse();
		} catch(IOException e) {
			//ignore -- should not occure, we use a StringReader!
		}
		return fParts.toArray(new ElExpression[fParts.size()]);
	}

}

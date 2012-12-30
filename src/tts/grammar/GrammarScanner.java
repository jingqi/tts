package tts.grammar;

import tts.eval.IEvalValue;
import tts.eval.VoidValue;
import tts.scanner.*;
import tts.scanner.Token.TokenType;

/**
 * 语法细则: <br/>
 * 
 * expression = assignment | rvalue; <br/>
 * assignment = lvalue '=' rvalue; <br/>
 * lvalue = variable; <br/>
 * rvalue = part (rel-op part)?; <br/>
 * part = term ([\+\-] term)?; <br/>
 * term = factor ([\*\/\%] factor)?; <br/>
 * factor = (('+' | '-' | '++' | '--')? atom) | (atom ('++' | '--')); <br/>
 * atom = variable | constant | function | ('(' expression ')');
 * 
 * @author jingqi
 * 
 */
public class GrammarScanner {

	TokenStream tokenStream;

	public GrammarScanner(TokenStream tokenStream) {
		this.tokenStream = tokenStream;
	}

	public void evalAll() {
		while (true)
			expression();
	}

	private IEvalValue expression() {
		IEvalValue ret = assignment();
		if (ret == null)
			ret = rvalue();

		// 分号
		Token tok = tokenStream.nextToken();
		if (tok != null) {
			if (tok.type != TokenType.SEPARATOR || !tok.value.equals(";")) {
				tokenStream.putBack();
			}
		}

		return ret;
	}

	private IEvalValue assignment() {
		Token tok = tokenStream.nextToken();
		assert tok != null;
		if (tok.type != TokenType.IDENTIFIER) {
			tokenStream.putBack();
			return null;
		}

		Token t = tokenStream.nextToken();
		if (t.type != TokenType.SEPARATOR || !t.value.equals("=")) {
			tokenStream.putBack(2);
			return null;
		}

		// 如果是一个变量，尝试赋值
		IEvalValue v = rvalue();
		if (v == null) {
			tokenStream.putBack(2);
			return null;
		}

		// TODO
		// assign_var(tok, v);
		return v;
	}

	private IEvalValue rvalue() {
		IEvalValue v = part();
		if (v == null)
			return null;

		Token t = tokenStream.nextToken();
		if (t.type != TokenType.SEPARATOR) {
			tokenStream.putBack();
			return v;
		} else if (t.value.equals("==")) {
			IEvalValue vv = part();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals("!=")) {
			IEvalValue vv = part();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals(">=")) {
			IEvalValue vv = part();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals("<=")) {
			IEvalValue vv = part();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals(">")) {
			IEvalValue vv = part();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals("<")) {
			IEvalValue vv = part();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else {
			tokenStream.putBack();
			return v;
		}
	}

	private IEvalValue part() {
		IEvalValue v = term();
		if (v == null)
			return null;

		Token t = tokenStream.nextToken();
		if (t.type != TokenType.SEPARATOR) {
			tokenStream.putBack();
			return v;
		} else if (t.value.equals("+")) {
			IEvalValue vv = term();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals("-")) {
			IEvalValue vv = term();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else {
			tokenStream.putBack();
			return v;
		}
	}

	private IEvalValue term() {
		IEvalValue v = factor();
		if (v == null)
			return null;

		Token t = tokenStream.nextToken();
		if (t.type != TokenType.SEPARATOR) {
			tokenStream.putBack();
			return v;
		} else if (t.value.equals("*")) {
			IEvalValue vv = factor();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals("/")) {
			IEvalValue vv = factor();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals("%")) {
			IEvalValue vv = factor();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else {
			tokenStream.putBack();
			return v;
		}
	}

	private IEvalValue factor() {
		Token t = tokenStream.nextToken();

		int back = 0;
		if (t.type != TokenType.SEPARATOR) {
			tokenStream.putBack();
			t = null;
		} else if (t.value.equals("+")) {
			++back;
			// TODO
		} else if (t.value.equals("-")) {
			++back;
			// TODO
		} else if (t.value.equals("++")) {
			++back;
			// TODO
		} else if (t.value.equals("--")) {
			++back;
			// TODO
		} else {
			tokenStream.putBack();
			t = null;
		}

		IEvalValue v = atom();
		if (v == null) {
			tokenStream.putBack(back);
			return null;
		}

		if (t == null) {
			t = tokenStream.nextToken();
			if (t.type != TokenType.SEPARATOR) {
				tokenStream.putBack();
			} else if (t.value.equals("++")) {
				// TODO
			} else if (t.value.equals("--")) {
				// TODO
			} else {
				tokenStream.putBack();
			}
		}

		return v;
	}

	private IEvalValue atom() {
		Token t = tokenStream.nextToken();
		if (t.type == TokenType.IDENTIFIER) {
			// TODO return variable value;
			return VoidValue.instance;
		} else if (t.type == TokenType.BOOLEAN || t.type == TokenType.INTEGER
				|| t.type == TokenType.LONG_INT || t.type == TokenType.FLOAT
				|| t.type == TokenType.DOUBLE || t.type == TokenType.STRING) {
			// TODO
			return VoidValue.instance;
		} else if (t.type == TokenType.SEPARATOR && t.value.equals("(")) {
			IEvalValue v = expression();
			if (v == null)
				throw new GrammarException("");
			t = tokenStream.nextToken();
			if (t.type != TokenType.SEPARATOR || !t.value.equals(")"))
				throw new GrammarException("");
			return v;
		} else {
			throw new GrammarException("");
		}
	}
}

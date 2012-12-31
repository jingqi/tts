package tts.grammar.scanner;

import tts.eval.*;
import tts.grammar.tree.*;
import tts.token.scanner.*;
import tts.token.scanner.Token.TokenType;
import tts.vm.VarType;

/**
 * 语法分析，并构建出语法树
 * 
 * @author jingqi
 * 
 */
public class GrammarScanner {

	TokenStream tokenStream;

	public GrammarScanner(TokenStream tokenStream) {
		this.tokenStream = tokenStream;
	}

	/**
	 * all = sentence*;
	 */
	public IOp all() {
		OpList ret = new OpList();
		while (true) {
			IOp op = sentence();
			if (op == null)
				break;
			else
				ret.add(op);
		}
		return ret;
	}

	/**
	 * sentence = ((expression | defination)? ';') | statement | text_template |
	 * block;
	 */
	private IOp sentence() {
		int p = tokenStream.tell();
		IOp ret = expression();
		if (ret == null)
			ret = defination();
		Token t = tokenStream.nextToken();
		if (t != null && t.type == TokenType.SEPARATOR && t.value.equals(";")) {
			if (ret == null)
				return new OpList(); // 空语句
			return ret;
		}

		tokenStream.seek(p);
		ret = textTemplate();
		if (ret == null)
			ret = defination();
		if (ret == null)
			ret = block();
		if (ret == null)
			ret = statement();
		return ret;
	}

	/**
	 * block = '{' sentence* '}'
	 */
	private IOp block() {
		Token t = tokenStream.match(TokenType.SEPARATOR);
		if (t == null)
			return null;
		if (!t.value.equals("{")) {
			tokenStream.putBack();
			return null;
		}

		OpList ret = new OpList();
		while (true) {
			IOp op = sentence();
			if (op == null)
				break;
			else
				ret.add(op);
		}

		t = tokenStream.match(TokenType.SEPARATOR);
		if (t == null || !t.value.equals("}"))
			throw new GrammarException("");
		return ret;
	}

	private IOp textTemplate() {
		Token t = tokenStream.match(TokenType.TEXT_TEMPLATE);
		if (t == null)
			return null;
		return new TextTemplateOp((String) t.value);
	}

	/**
	 * statement = for_loop | do_while_loop | while_loop | if_else;
	 */
	private IOp statement() {
		IOp ret = forLoop();
		if (ret == null)
			ret = doWhileLoop();
		if (ret == null)
			ret = whileLoop();
		if (ret == null)
			ret = ifElse();
		return ret;
	}

	/**
	 * expression = assignment | rvalue;
	 */
	private IOp expression() {
		IOp ret = assignment();
		if (ret == null)
			ret = rvalue();
		return ret;
	}

	/**
	 * assignment = lvalue '=' rvalue; <br/>
	 * lvalue = varialbe;
	 */
	private IOp assignment() {
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
		IOp v = rvalue();
		if (v == null) {
			tokenStream.putBack(2);
			return null;
		}

		// TODO
		// assign_var(tok, v);
		return v;
	}

	/**
	 * rvalue = part (rel-op part)?;
	 */
	private IOp rvalue() {
		IOp v = part();
		if (v == null)
			return null;

		Token t = tokenStream.nextToken();
		if (t.type != TokenType.SEPARATOR) {
			tokenStream.putBack();
			return v;
		} else if (t.value.equals("==")) {
			IOp vv = part();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals("!=")) {
			IOp vv = part();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals(">=")) {
			IOp vv = part();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals("<=")) {
			IOp vv = part();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals(">")) {
			IOp vv = part();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals("<")) {
			IOp vv = part();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else {
			tokenStream.putBack();
			return v;
		}
	}

	/**
	 * part = term ([\+\-] term)?;
	 */
	private IOp part() {
		IOp v = term();
		if (v == null)
			return null;

		Token t = tokenStream.nextToken();
		if (t.type != TokenType.SEPARATOR) {
			tokenStream.putBack();
			return v;
		} else if (t.value.equals("+")) {
			IOp vv = term();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals("-")) {
			IOp vv = term();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else {
			tokenStream.putBack();
			return v;
		}
	}

	/**
	 * term = factor ([\*\/\%] factor)?;
	 */
	private IOp term() {
		IOp v = factor();
		if (v == null)
			return null;

		Token t = tokenStream.nextToken();
		if (t.type != TokenType.SEPARATOR) {
			tokenStream.putBack();
			return v;
		} else if (t.value.equals("*")) {
			IOp vv = factor();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals("/")) {
			IOp vv = factor();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else if (t.value.equals("%")) {
			IOp vv = factor();
			if (vv == null)
				throw new GrammarException("");
			// TODO
			return v;
		} else {
			tokenStream.putBack();
			return v;
		}
	}

	/**
	 * factor = (('+' | '-' | '++' | '--')? atom) | (atom ('++' | '--'));
	 */
	private IOp factor() {
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

		IOp v = atom();
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

	/**
	 * atom = variable | constant | function | ('(' expression ')');
	 */
	private IOp atom() {
		Token t = tokenStream.nextToken();
		switch (t.type) {
		case IDENTIFIER:
			return new Operand(new VariableEval((String) t.value));

		case BOOLEAN:
			return new Operand(BooleanEval.valueOf((Boolean) t.value));

		case INTEGER:
			return new Operand(new IntegerEval((Integer) t.value));

		case LONG_INT:
			return new Operand(new LongIntEval((Long) t.value));

		case FLOAT:
			return new Operand(new FloatEval((Float) t.value));

		case DOUBLE:
			return new Operand(new DoubleEval((Double) t.value));

		case STRING:
			return new Operand(new StringEval((String) t.value));

		case SEPARATOR:
			if (t.value.equals("(")) {
				IOp v = expression();
				if (v == null)
					throw new GrammarException("");
				t = tokenStream.nextToken();
				if (t.type != TokenType.SEPARATOR || !t.value.equals(")"))
					throw new GrammarException("");
				return v;
			}
		}
		throw new GrammarException("");
	}

	/**
	 * defination = type variable ('=' rvalue)? (',' varialbe ('=' rvalue)?)*;
	 */
	private IOp defination() {
		VarType vt;
		Token t = tokenStream.match(TokenType.KEY_WORD);
		if (t == null) {
			return null;
		} else if (t.value.equals("bool")) {
			vt = VarType.BOOLEAN;
		} else if (t.value.equals("int")) {
			vt = VarType.INTEGER;
		} else if (t.value.equals("long")) {
			vt = VarType.LONG_INT;
		} else if (t.value.equals("float")) {
			vt = VarType.FLOAT;
		} else if (t.value.equals("double")) {
			vt = VarType.DOUBLE;
		} else if (t.value.equals("string")) {
			vt = VarType.STRING;
		} else {
			tokenStream.putBack();
			return null;
		}

		OpList l = new OpList();
		do {
			t = tokenStream.match(TokenType.IDENTIFIER);
			if (t == null)
				throw new GrammarException("");

			String name = (String) t.value;
			if (tokenStream.match(TokenType.SEPARATOR, "=")) {
				IOp v = rvalue();
				if (v == null)
					throw new GrammarException("");
				l.add(new DefinationOp(vt, name, v));
			} else {
				l.add(new DefinationOp(vt, name, null));
			}
		} while (tokenStream.match(TokenType.SEPARATOR, ","));
		return l;
	}

	/**
	 * for_loop = 'for' '(' expression? ';' rvalue? ';' expression? ')'
	 * sentence;
	 */
	private IOp forLoop() {
		if (!tokenStream.match(TokenType.KEY_WORD, "for"))
			return null;
		if (!tokenStream.match(TokenType.SEPARATOR, "("))
			throw new GrammarException("");

		IOp init_exp = expression();
		if (!tokenStream.match(TokenType.SEPARATOR, ";"))
			throw new GrammarException("");

		IOp break_exp = rvalue();
		if (!tokenStream.match(TokenType.SEPARATOR, ";"))
			throw new GrammarException("");

		IOp fin_exp = expression();
		if (!tokenStream.match(TokenType.SEPARATOR, ")"))
			throw new GrammarException("");

		IOp body = sentence();
		if (body == null)
			throw new GrammarException("");

		return new ForLoopOp(init_exp, break_exp, fin_exp, body);
	}

	/**
	 * do_while_loop = 'do' block 'while' '(' rvalue ')' ';';
	 */
	private IOp doWhileLoop() {
		if (!tokenStream.match(TokenType.KEY_WORD, "do"))
			return null;

		IOp body = block();
		if (body == null)
			throw new GrammarException("");

		if (!tokenStream.match(TokenType.KEY_WORD, "while"))
			throw new GrammarException("");
		if (!tokenStream.match(TokenType.SEPARATOR, "("))
			throw new GrammarException("");

		IOp brk_exp = rvalue();
		if (brk_exp == null)
			throw new GrammarException("");

		if (!tokenStream.match(TokenType.SEPARATOR, ")"))
			throw new GrammarException("");
		if (!tokenStream.match(TokenType.SEPARATOR, ";"))
			throw new GrammarException("");

		return new DoWhileLoopOp(body, brk_exp);
	}

	/**
	 * while_loop = 'while' '(' rvalue ')' sentence;
	 */
	private IOp whileLoop() {
		if (!tokenStream.match(TokenType.KEY_WORD, "while"))
			return null;
		if (!tokenStream.match(TokenType.IDENTIFIER, "("))
			throw new GrammarException("");

		IOp brk_exp = rvalue();
		if (brk_exp == null)
			throw new GrammarException("");

		if (!tokenStream.match(TokenType.IDENTIFIER, ")"))
			throw new GrammarException("");

		IOp body = sentence();
		if (body == null)
			throw new GrammarException("");

		return new WhileLoop(brk_exp, body);
	}

	/**
	 * if_else = 'if' '(' rvalue ')' sentence ('else' sentence)?;
	 */
	private IOp ifElse() {
		if (!tokenStream.match(TokenType.KEY_WORD, "if"))
			return null;
		if (!tokenStream.match(TokenType.IDENTIFIER, "("))
			throw new GrammarException("");

		IOp cond = rvalue();
		if (cond == null)
			throw new GrammarException("");

		if (!tokenStream.match(TokenType.IDENTIFIER, ")"))
			throw new GrammarException("");

		IOp body = sentence();
		if (body == null)
			throw new GrammarException("");

		IOp else_body = null;
		if (tokenStream.match(TokenType.KEY_WORD, "else")) {
			else_body = sentence();
			if (else_body == null)
				throw new GrammarException("");
		}

		return new IfElseOp(cond, body, else_body);
	}
}

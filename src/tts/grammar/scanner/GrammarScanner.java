package tts.grammar.scanner;

import java.util.ArrayList;
import java.util.List;

import tts.eval.*;
import tts.grammar.tree.*;
import tts.grammar.tree.UnaryOp.OpType;
import tts.grammar.tree.binaryop.*;
import tts.token.scanner.*;
import tts.token.scanner.Token.TokenType;
import tts.vm.ScriptRuntimeException;
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

	// all = sentence*;
	public IOp all() {
		OpList ret = new OpList();
		while (true) {
			IOp op = sentence();
			if (op == null)
				break;
			else
				ret.add(op);
		}
		if (!tokenStream.eof())
			throw new GrammarException();
		return ret;
	}

	// sentence = ((expression | defination)? ';') | statement | text_template |
	// block;
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

	// block = '{' sentence* '}'
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
		return new FrameScopOp(ret);
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
	 * expression = assignment | rvalue; <br/>
	 * 
	 * 各个操作符的优先级顺序，参看
	 * http://www.cppblog.com/aqazero/archive/2012/06/02/8284.html
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
		Token tok = tokenStream.match(TokenType.IDENTIFIER);
		if (tok == null)
			return null;

		if (!tokenStream.match(TokenType.SEPARATOR, "=")) {
			tokenStream.putBack();
			return null;
		}

		// 如果是一个变量，尝试赋值
		IOp v = rvalue();
		if (v == null) {
			tokenStream.putBack(2);
			return null;
		}

		return new AssignOp((String) tok.value, v);
	}

	// rvalue = bin_switch | booleanOr;
	private IOp rvalue() {
		return binSwitch();
	}

	// bin_switch = or ('?' or ':' or)?;
	private IOp binSwitch() {
		int p = tokenStream.tell();
		IOp cond = booleanOr();
		if (cond == null)
			return null;

		if (!tokenStream.match(TokenType.SEPARATOR, "?")) {
			return cond;
		}

		IOp t = booleanOr();
		if (t == null) {
			tokenStream.seek(p);
			return null;
		}

		if (!tokenStream.match(TokenType.SEPARATOR, ":")) {
			tokenStream.seek(p);
			return null;
		}

		IOp f = booleanOr();
		if (f == null) {
			tokenStream.seek(p);
			return null;
		}

		return new BinSwitchOp(cond, t, f);
	}

	// or = and ('||' and)*;
	private IOp booleanOr() {
		IOp v = booleanAnd();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "||")) {
			IOp vv = booleanAnd();
			if (vv == null)
				throw new GrammarException();
			v = new BooleanOp(v, BooleanOp.OpType.OR, vv);
		}
		return v;
	}

	// and = bit_or ('&&' bit_or)*;
	private IOp booleanAnd() {
		IOp v = bitOr();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "||")) {
			IOp vv = bitOr();
			if (vv == null)
				throw new GrammarException();
			v = new BooleanOp(v, BooleanOp.OpType.AND, vv);
		}
		return v;
	}

	// bit_or = bit_xor ('|' bit_or)*;
	private IOp bitOr() {
		IOp v = bitXor();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "|")) {
			IOp vv = bitXor();
			if (vv == null)
				throw new GrammarException();
			v = new BitOp(v, BitOp.OpType.BIT_OR, vv);
		}
		return v;
	}

	// bit_xor = bit_and ('^' bit_and)*;
	private IOp bitXor() {
		IOp v = bitAnd();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "^")) {
			IOp vv = bitAnd();
			if (vv == null)
				throw new GrammarException();
			v = new BitOp(v, BitOp.OpType.BIT_XOR, vv);
		}
		return v;
	}

	// bit_and = eq_cmp ('&' eq_cmp)*;
	private IOp bitAnd() {
		IOp v = eqCmp();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "&")) {
			IOp vv = eqCmp();
			if (vv == null)
				throw new GrammarException();
			v = new BitOp(v, BitOp.OpType.BIT_AND, vv);
		}
		return v;
	}

	// eq_cmp = less_cmp (('==' | '!=') less_cmp)*;
	private IOp eqCmp() {
		IOp v = lessCmp();
		if (v == null)
			return null;

		while (true) {
			CompareOp.OpType op;
			if (tokenStream.match(TokenType.SEPARATOR, "=="))
				op = CompareOp.OpType.EQ;
			else if (tokenStream.match(TokenType.SEPARATOR, "!="))
				op = CompareOp.OpType.NOT_EQ;
			else
				break;

			IOp vv = lessCmp();
			if (vv == null)
				throw new GrammarException();
			v = new CompareOp(v, op, vv);
		}
		return v;
	}

	// less_cmp = bit_shift (('<' | '>' | '<=' | '>=') bit_shift)?;
	private IOp lessCmp() {
		IOp v = bitShift();
		if (v == null)
			return null;

		CompareOp.OpType op;
		if (tokenStream.match(TokenType.SEPARATOR, "<"))
			op = CompareOp.OpType.LESS;
		else if (tokenStream.match(TokenType.SEPARATOR, ">"))
			op = CompareOp.OpType.GREATER;
		else if (tokenStream.match(TokenType.SEPARATOR, "<="))
			op = CompareOp.OpType.LESS_EQ;
		else if (tokenStream.match(TokenType.SEPARATOR, ">="))
			op = CompareOp.OpType.GREATER_EQ;
		else
			return v;

		IOp vv = bitShift();
		if (vv == null)
			throw new GrammarException();
		return new CompareOp(v, op, vv);
	}

	// bit_shift = part (('<<' | '>>' | '>>>') part)*;
	private IOp bitShift() {
		IOp v = part();
		if (v == null)
			return null;

		while (true) {
			BitOp.OpType op;
			if (tokenStream.match(TokenType.SEPARATOR, "<<"))
				op = BitOp.OpType.SHIFT_LEFT;
			else if (tokenStream.match(TokenType.SEPARATOR, ">>"))
				op = BitOp.OpType.SHIFT_RIGHT;
			else if (tokenStream.match(TokenType.SEPARATOR, ">>>"))
				op = BitOp.OpType.CIRCLE_SHIFT_RIGHT;
			else
				break;

			IOp vv = part();
			if (vv == null)
				throw new GrammarException();
			v = new BitOp(v, op, vv);
		}
		return v;
	}

	// part = term (('+' | '-') term)*;
	private IOp part() {
		IOp v = term();
		if (v == null)
			return null;

		while (true) {
			Token t = tokenStream.nextToken();
			if (t == null) {
				return v;
			} else if (t.type != TokenType.SEPARATOR) {
				tokenStream.putBack();
				return v;
			} else if (t.value.equals("+")) {
				IOp vv = term();
				if (vv == null)
					throw new GrammarException("");
				v = new MathOp(v, MathOp.OpType.ADD, vv);
			} else if (t.value.equals("-")) {
				IOp vv = term();
				if (vv == null)
					throw new GrammarException("");
				v = new MathOp(v, MathOp.OpType.SUB, vv);
			} else {
				tokenStream.putBack();
				return v;
			}
		}
	}

	// term = factor (('*' | '/' | '%') factor)*;
	private IOp term() {
		IOp v = factor();
		if (v == null)
			return null;

		while (true) {
			Token t = tokenStream.nextToken();
			if (t == null) {
				return v;
			} else if (t.type != TokenType.SEPARATOR) {
				tokenStream.putBack();
				return v;
			} else if (t.value.equals("*")) {
				IOp vv = factor();
				if (vv == null)
					throw new GrammarException("");
				v = new MathOp(v, MathOp.OpType.MULTIPLY, vv);
			} else if (t.value.equals("/")) {
				IOp vv = factor();
				if (vv == null)
					throw new GrammarException("");
				v = new MathOp(v, MathOp.OpType.DIVID, vv);
			} else if (t.value.equals("%")) {
				IOp vv = factor();
				if (vv == null)
					throw new GrammarException("");
				v = new MathOp(v, MathOp.OpType.MOD, vv);
			} else {
				tokenStream.putBack();
				return v;
			}
		}
	}

	// factor = (('+' | '-' | '++' | '--' | '!' | '~')? function) | (function
	// ('++' |
	// '--'));
	private IOp factor() {
		int p = tokenStream.tell();

		UnaryOp.OpType op = null;
		if (tokenStream.match(TokenType.SEPARATOR, "+")) {
			op = OpType.POSITIVE;
		} else if (tokenStream.match(TokenType.SEPARATOR, "-")) {
			op = OpType.NEGATIEVE;
		} else if (tokenStream.match(TokenType.SEPARATOR, "!")) {
			op = OpType.NOT;
		} else if (tokenStream.match(TokenType.SEPARATOR, "~")) {
			op = OpType.BIT_NOT;
		}

		IOp v = function();
		if (v == null) {
			tokenStream.seek(p);
			return null;
		}
		if (op == null)
			return v;
		return new UnaryOp(op, v);
	}

	// function = index '(' (index (',' index)*)? ')';
	private IOp function() {
		IOp func = index();
		if (func == null)
			return null;
		if (!tokenStream.match(TokenType.SEPARATOR, "("))
			return func;

		List<IOp> args = new ArrayList<IOp>();
		while (true) {
			IOp arg = index();
			if (arg == null) {
				if (args.size() == 0)
					break;
				else
					throw new ScriptRuntimeException();
			}
			args.add(arg);

			if (!tokenStream.match(TokenType.SEPARATOR, ","))
				break;
		}
		if (!tokenStream.match(TokenType.SEPARATOR, ")"))
			throw new ScriptRuntimeException();
		return new FuncCallOp(func, args);
	}

	// index = member '[' member ']';
	private IOp index() {
		IOp body = member();
		if (body == null)
			return null;
		if (!tokenStream.match(TokenType.SEPARATOR, "["))
			return body;
		IOp i = member();
		if (i == null)
			throw new GrammarException();
		if (!tokenStream.match(TokenType.SEPARATOR, "]"))
			throw new GrammarException();
		return new IndexOp(body, i);
	}

	// member = atom '.' atom ;
	private IOp member() {
		IOp body = atom();
		if (body == null)
			return null;
		if (!tokenStream.match(TokenType.SEPARATOR, "."))
			return body;

		Token t = tokenStream.match(TokenType.IDENTIFIER);
		if (t == null)
			throw new GrammarException();
		return new MemberOp(body, (String) t.value);
	}

	// atom = variable | constant | function | ('(' expression ')') | array;
	private IOp atom() {
		if (tokenStream.eof())
			return null;

		Token t = tokenStream.nextToken();
		if (t == null)
			return null;
		switch (t.type) {
		case IDENTIFIER:
			return new Operand(new VariableEval((String) t.value));

		case BOOLEAN:
			return new Operand(BooleanEval.valueOf((Boolean) t.value));

		case INTEGER:
			return new Operand(new IntegerEval((Long) t.value));

		case DOUBLE:
			return new Operand(new DoubleEval((Double) t.value));

		case STRING:
			return new Operand(new StringEval((String) t.value));

		case SEPARATOR:
			if (t.value.equals("(")) {
				IOp v = expression();
				if (v == null)
					throw new GrammarException("");
				if (tokenStream.match(TokenType.SEPARATOR, ")"))
					throw new GrammarException("");
				return v;
			} else if (t.value.equals("[")) {
				tokenStream.putBack();
				IOp v = array();
				return v;
			}

		default:
			tokenStream.putBack();
			return null;
		}
	}

	// array = '[' ((expression ',')* expression ','?)? ']';
	private ArrayOp array() {
		if (!tokenStream.match(TokenType.SEPARATOR, "["))
			return null;

		ArrayList<IOp> l = new ArrayList<IOp>();
		while (true) {
			IOp v = expression();
			if (v == null)
				break;

			l.add(v);
			if (!tokenStream.match(TokenType.SEPARATOR, ","))
				break;
		}
		if (!tokenStream.match(TokenType.SEPARATOR, "]"))
			throw new GrammarException();

		return new ArrayOp(l);
	}

	// defination = type variable ('=' rvalue)? (',' varialbe ('=' rvalue)?)*;
	private IOp defination() {
		VarType vt;
		Token t = tokenStream.match(TokenType.KEY_WORD);
		if (t == null) {
			return null;
		} else if (t.value.equals("bool")) {
			vt = VarType.BOOLEAN;
		} else if (t.value.equals("int")) {
			vt = VarType.INTEGER;
		} else if (t.value.equals("double")) {
			vt = VarType.DOUBLE;
		} else if (t.value.equals("string")) {
			vt = VarType.STRING;
		} else if (t.value.equals("array")) {
			vt = VarType.ARRAY;
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
				IOp v = expression();
				if (v == null)
					throw new GrammarException("");
				l.add(new DefinationOp(vt, name, v));
			} else {
				l.add(new DefinationOp(vt, name, null));
			}
		} while (tokenStream.match(TokenType.SEPARATOR, ","));
		return l;
	}

	// for_loop = 'for' '(' expression? ';' rvalue? ';' expression? ')'
	// sentence;
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

	// do_while_loop = 'do' block 'while' '(' rvalue ')' ';';
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

		IOp brk_exp = expression();
		if (brk_exp == null)
			throw new GrammarException("");

		if (!tokenStream.match(TokenType.SEPARATOR, ")"))
			throw new GrammarException("");
		if (!tokenStream.match(TokenType.SEPARATOR, ";"))
			throw new GrammarException("");

		return new DoWhileLoopOp(body, brk_exp);
	}

	// while_loop = 'while' '(' rvalue ')' sentence;
	private IOp whileLoop() {
		if (!tokenStream.match(TokenType.KEY_WORD, "while"))
			return null;
		if (!tokenStream.match(TokenType.SEPARATOR, "("))
			throw new GrammarException("");

		IOp brk_exp = expression();
		if (brk_exp == null)
			throw new GrammarException("");

		if (!tokenStream.match(TokenType.SEPARATOR, ")"))
			throw new GrammarException("");

		IOp body = sentence();
		if (body == null)
			throw new GrammarException("");

		return new WhileLoop(brk_exp, body);
	}

	// if_else = 'if' '(' rvalue ')' sentence ('else' sentence)?;
	private IOp ifElse() {
		if (!tokenStream.match(TokenType.KEY_WORD, "if"))
			return null;
		if (!tokenStream.match(TokenType.SEPARATOR, "("))
			throw new GrammarException("");

		IOp cond = expression();
		if (cond == null)
			throw new GrammarException("");

		if (!tokenStream.match(TokenType.SEPARATOR, ")"))
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

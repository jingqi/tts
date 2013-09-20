package tts.grammar.scanner;

import java.util.ArrayList;

import tts.eval.*;
import tts.eval.UserFunctionEval.ParamInfo;
import tts.grammar.tree.*;
import tts.grammar.tree.binaryop.*;
import tts.lexer.scanner.*;
import tts.lexer.scanner.Token.TokenType;
import tts.lexer.stream.CharArrayScanReader;
import tts.lexer.stream.IScanReader;
import tts.util.SourceLocation;
import tts.vm.Variable.VarType;

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
	public Op all() {
		OpList ret = new OpList(tokenStream.getFile(), tokenStream.getLine());
		while (true) {
			Op op = sentence();
			if (op == null)
				break;
			ret.add(op);
		}
		if (!tokenStream.eof())
			throw new GrammarException("Next sentence not recognised", tokenStream);
		return ret;
	}

	// sentence = ((expression | defination)? ';') | statement | text_template |
	// block | include;
	private Op sentence() {
		final int p = tokenStream.tell();
		Op ret = expression();
		if (ret == null)
			ret = defination();

		Token t = tokenStream.match(TokenType.SEPARATOR, ";");
		if (t != null) {
			if (ret == null)
				return new OpList(t.file, t.line); // 空语句
			return ret;
		}

		tokenStream.seek(p);
		ret = textTemplate();
		if (ret == null)
			ret = block();
		if (ret == null)
			ret = statement();
		if (ret == null)
			ret = include();
		return ret;
	}

	// block = '{' sentence* '}'
	private Op block() {
		Token t = tokenStream.match(TokenType.SEPARATOR, "{");
		if (t == null)
			return null;

		OpList ret = new OpList(t.file, t.line);
		while (true) {
			Op op = sentence();
			if (op == null)
				break;
			else
				ret.add(op);
		}

		if (tokenStream.match(TokenType.SEPARATOR, "}") == null)
			throw new GrammarException("token '}' expected", tokenStream);

		return new ScopeOp(ret);
	}

	private Op textTemplate() {
		Token t = tokenStream.match(TokenType.TEXT_TEMPLATE);
		if (t == null)
			return null;

		String s = (String) t.value;
		ArrayList<Object> ret = new ArrayList<Object>();
		StringBuilder sb = new StringBuilder();
		int i = 0, line = t.line;
		while (i < s.length()) {
			char c = s.charAt(i);
			if (c == '\n') {
				++line;
			} else if (c == '$' && i + 1 < s.length()) {
				char cc = s.charAt(i + 1);
				if (cc == '{') {
					// 分析嵌入式代码
					IScanReader sr = new CharArrayScanReader(s.substring(i + 2).toCharArray());
					LexerScanner ts = new LexerScanner(sr, t.file, line, true);
					TokenStream tst = new TokenStream(ts);
					GrammarScanner gs = new GrammarScanner(tst);
					Op op = gs.expression();
					if (op == null)
						throw new GrammarException("Expression needed", tst);
					if (tst.match(TokenType.SEPARATOR, "}") == null)
						throw new GrammarException("Token '{' needed", tst);

					if (sb.length() > 0) {
						ret.add(sb.toString());
						sb.setLength(0);
					}
					ret.add(op);
					i = i + 2 + sr.tell();
					line = ts.getLine();
					continue;
				}
			}

			++i;
			sb.append(c);
		}
		if (sb.length() > 0)
			ret.add(sb.toString());

		return new TextTemplateOp(ret, t.file, t.line);
	}

	/**
	 * statement = 'break;' | 'continue;' | for_loop | do_while_loop |
	 * while_loop | if_else;
	 */
	private Op statement() {
		Token t = tokenStream.match(TokenType.KEY_WORD);
		if (t != null) {
			if (t.value.equals("break")) {
				if (tokenStream.match(TokenType.SEPARATOR, ";") == null)
					throw new GrammarException("Token ';' expected", tokenStream);
				return new BreakOp(t.file, t.line);
			} else if (t.value.equals("continue")) {
				if (tokenStream.match(TokenType.SEPARATOR, ";") == null)
					throw new GrammarException("Token ';' expected", tokenStream);
				return new ContinueOp(t.file, t.line);
			} else if (t.value.equals("return")) {
				Op r = expression();
				if (tokenStream.match(TokenType.SEPARATOR, ";") == null)
					throw new GrammarException("Token ';' expected", tokenStream);

				if (r == null)
					return new ReturnFuncOp(new OpList(t.file, t.line));
				return new ReturnFuncOp(r);
			} else {
				tokenStream.putBack();
			}
		}

		Op ret = forLoop();
		if (ret == null)
			ret = doWhileLoop();
		if (ret == null)
			ret = whileLoop();
		if (ret == null)
			ret = ifElse();
		if (ret == null)
			ret = functionDefination();
		return ret;
	}

	/**
	 * expression = assignment | rvalue; <br/>
	 *
	 * 各个操作符的优先级顺序，参看
	 * http://www.cppblog.com/aqazero/archive/2012/06/02/8284.html
	 */
	private Op expression() {
		Op ret = assignment();
		if (ret == null)
			ret = rvalue();
		return ret;
	}

	/**
	 * assignment = lvalue '=' rvalue; <br/>
	 * lvalue = varialbe;
	 */
	private Op assignment() {
		Token tok = tokenStream.match(TokenType.IDENTIFIER);
		if (tok == null)
			return null;
		SourceLocation sl = new SourceLocation(tok.file, tok.line);

		Object op = null;
		if (tokenStream.match(TokenType.SEPARATOR, "=") != null) {
			op = null;
		} else if (tokenStream.match(TokenType.SEPARATOR, "+=") != null) {
			op = MathOp.OpType.ADD;
		} else if (tokenStream.match(TokenType.SEPARATOR, "-=") != null) {
			op = MathOp.OpType.SUB;
		} else if (tokenStream.match(TokenType.SEPARATOR, "*=") != null) {
			op = MathOp.OpType.MULTIPLY;
		} else if (tokenStream.match(TokenType.SEPARATOR, "/=") != null) {
			op = MathOp.OpType.DIVID;
		} else if (tokenStream.match(TokenType.SEPARATOR, "%=") != null) {
			op = MathOp.OpType.MOD;
		} else if (tokenStream.match(TokenType.SEPARATOR, "&=") != null) {
			op = BitOp.OpType.BIT_AND;
		} else if (tokenStream.match(TokenType.SEPARATOR, "|=") != null) {
			op = BitOp.OpType.BIT_OR;
		} else if (tokenStream.match(TokenType.SEPARATOR, "^=") != null) {
			op = BitOp.OpType.BIT_XOR;
		} else if (tokenStream.match(TokenType.SEPARATOR, "<<=") != null) {
			op = BitOp.OpType.SHIFT_LEFT;
		} else if (tokenStream.match(TokenType.SEPARATOR, ">>=") != null) {
			op = BitOp.OpType.SHIFT_RIGHT;
		} else if (tokenStream.match(TokenType.SEPARATOR, ">>>=") != null) {
			op = BitOp.OpType.PRIMITIVE_SHIFT_RIGHT;
		} else {
			tokenStream.putBack();
			return null;
		}

		// 如果是一个变量，尝试赋值
		Op v = rvalue();
		if (v == null) {
			tokenStream.putBack(2);
			return null;
		}

		String name = (String) tok.value;
		if (op == null) {
			return new AssignOp(name, v, sl);
		} else if (op instanceof MathOp.OpType) {
			MathOp.OpType mop = (MathOp.OpType) op;
			return new AssignOp(name, new MathOp(new Operand(new VariableEval(name), sl), mop, v), sl);
		} else if (op instanceof BitOp.OpType) {
			BitOp.OpType bop = (BitOp.OpType) op;
			return new AssignOp(name, new BitOp(new Operand(new VariableEval(name), sl), bop, v), sl);
		} else {
			throw new RuntimeException();
		}
	}

	// rvalue = bin_switch | booleanOr;
	private Op rvalue() {
		return binSwitch();
	}

	// bin_switch = or ('?' or ':' or)?;
	private Op binSwitch() {
		int p = tokenStream.tell();
		Op cond = booleanOr();
		if (cond == null)
			return null;

		if (tokenStream.match(TokenType.SEPARATOR, "?") == null) {
			return cond;
		}

		Op t = booleanOr();
		if (t == null) {
			tokenStream.seek(p);
			return null;
		}

		if (tokenStream.match(TokenType.SEPARATOR, ":") == null) {
			tokenStream.seek(p);
			return null;
		}

		Op f = booleanOr();
		if (f == null) {
			tokenStream.seek(p);
			return null;
		}

		return new BinSwitchOp(cond, t, f);
	}

	// or = and ('||' and)*;
	private Op booleanOr() {
		Op v = booleanAnd();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "||") != null) {
			Op vv = booleanAnd();
			if (vv == null)
				throw new GrammarException("Expression expected", tokenStream);
			v = new BooleanOp(v, BooleanOp.OpType.OR, vv);
		}
		return v;
	}

	// and = bit_or ('&&' bit_or)*;
	private Op booleanAnd() {
		Op v = bitOr();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "&&") != null) {
			Op vv = bitOr();
			if (vv == null)
				throw new GrammarException("Expression expected", tokenStream);
			v = new BooleanOp(v, BooleanOp.OpType.AND, vv);
		}
		return v;
	}

	// bit_or = bit_xor ('|' bit_or)*;
	private Op bitOr() {
		Op v = bitXor();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "|") != null) {
			Op vv = bitXor();
			if (vv == null)
				throw new GrammarException("Expression expected", tokenStream);
			v = new BitOp(v, BitOp.OpType.BIT_OR, vv);
		}
		return v;
	}

	// bit_xor = bit_and ('^' bit_and)*;
	private Op bitXor() {
		Op v = bitAnd();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "^") != null) {
			Op vv = bitAnd();
			if (vv == null)
				throw new GrammarException("Expression expected", tokenStream);
			v = new BitOp(v, BitOp.OpType.BIT_XOR, vv);
		}
		return v;
	}

	// bit_and = eq_cmp ('&' eq_cmp)*;
	private Op bitAnd() {
		Op v = eqCmp();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "&") != null) {
			Op vv = eqCmp();
			if (vv == null)
				throw new GrammarException("Expression expected", tokenStream);
			v = new BitOp(v, BitOp.OpType.BIT_AND, vv);
		}
		return v;
	}

	// eq_cmp = less_cmp (('==' | '!=' | '===' | '!==') less_cmp)*;
	private Op eqCmp() {
		Op v = lessCmp();
		if (v == null)
			return null;

		while (true) {
			CompareOp.OpType op;
			if (tokenStream.match(TokenType.SEPARATOR, "==") != null)
				op = CompareOp.OpType.EQ;
			else if (tokenStream.match(TokenType.SEPARATOR, "!=") != null)
				op = CompareOp.OpType.NOT_EQ;
			else if (tokenStream.match(TokenType.SEPARATOR, "===") != null)
				op = CompareOp.OpType.REF_EQ;
			else if (tokenStream.match(TokenType.SEPARATOR, "!==") != null)
				op = CompareOp.OpType.REF_NOT_EQ;
			else
				break;

			Op vv = lessCmp();
			if (vv == null)
				throw new GrammarException("Expression expected", tokenStream);
			v = new CompareOp(v, op, vv);
		}
		return v;
	}

	// less_cmp = bit_shift (('<' | '>' | '<=' | '>=') bit_shift)?;
	private Op lessCmp() {
		Op v = bitShift();
		if (v == null)
			return null;

		CompareOp.OpType op;
		if (tokenStream.match(TokenType.SEPARATOR, "<") != null)
			op = CompareOp.OpType.LESS;
		else if (tokenStream.match(TokenType.SEPARATOR, ">") != null)
			op = CompareOp.OpType.GREATER;
		else if (tokenStream.match(TokenType.SEPARATOR, "<=") != null)
			op = CompareOp.OpType.LESS_EQ;
		else if (tokenStream.match(TokenType.SEPARATOR, ">=") != null)
			op = CompareOp.OpType.GREATER_EQ;
		else
			return v;

		Op vv = bitShift();
		if (vv == null)
			throw new GrammarException("Expression expected", tokenStream);
		return new CompareOp(v, op, vv);
	}

	// bit_shift = part (('<<' | '>>' | '>>>') part)*;
	private Op bitShift() {
		Op v = part();
		if (v == null)
			return null;

		while (true) {
			BitOp.OpType op;
			if (tokenStream.match(TokenType.SEPARATOR, "<<") != null)
				op = BitOp.OpType.SHIFT_LEFT;
			else if (tokenStream.match(TokenType.SEPARATOR, ">>") != null)
				op = BitOp.OpType.SHIFT_RIGHT;
			else if (tokenStream.match(TokenType.SEPARATOR, ">>>") != null)
				op = BitOp.OpType.PRIMITIVE_SHIFT_RIGHT;
			else
				break;

			Op vv = part();
			if (vv == null)
				throw new GrammarException("Expression expected", tokenStream);
			v = new BitOp(v, op, vv);
		}
		return v;
	}

	// part = term (('+' | '-') term)*;
	private Op part() {
		Op v = term();
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
				Op vv = term();
				if (vv == null)
					throw new GrammarException("Expression expected", tokenStream);
				v = new MathOp(v, MathOp.OpType.ADD, vv);
			} else if (t.value.equals("-")) {
				Op vv = term();
				if (vv == null)
					throw new GrammarException("Expression expected", tokenStream);
				v = new MathOp(v, MathOp.OpType.SUB, vv);
			} else {
				tokenStream.putBack();
				return v;
			}
		}
	}

	// term = factor (('*' | '/' | '%') factor)*;
	private Op term() {
		Op v = factor();
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
				Op vv = factor();
				if (vv == null)
					throw new GrammarException("Expression expected", tokenStream);
				v = new MathOp(v, MathOp.OpType.MULTIPLY, vv);
			} else if (t.value.equals("/")) {
				Op vv = factor();
				if (vv == null)
					throw new GrammarException("Expression expected", tokenStream);
				v = new MathOp(v, MathOp.OpType.DIVID, vv);
			} else if (t.value.equals("%")) {
				Op vv = factor();
				if (vv == null)
					throw new GrammarException("Expression expected", tokenStream);
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
	private Op factor() {
		int p = tokenStream.tell();

		UnaryOp.OpType op = null;
		if (tokenStream.match(TokenType.SEPARATOR, "+") != null)
			op = UnaryOp.OpType.POSITIVE;
		else if (tokenStream.match(TokenType.SEPARATOR, "-") != null)
			op = UnaryOp.OpType.NEGATIVE;
		else if (tokenStream.match(TokenType.SEPARATOR, "!") != null)
			op = UnaryOp.OpType.NOT;
		else if (tokenStream.match(TokenType.SEPARATOR, "~") != null)
			op = UnaryOp.OpType.BIT_NOT;
		else if (tokenStream.match(TokenType.SEPARATOR, "++") != null)
			op = UnaryOp.OpType.PRE_INCREMENT;
		else if (tokenStream.match(TokenType.SEPARATOR, "--") != null)
			op = UnaryOp.OpType.PRE_DECREMENT;

		Op v = postOp();
		if (v == null) {
			tokenStream.seek(p);
			return null;
		}

		if (op == null) {
			if (tokenStream.match(TokenType.SEPARATOR, "++") != null)
				op = UnaryOp.OpType.POST_INCREMENT;
			else if (tokenStream.match(TokenType.SEPARATOR, "--") != null)
				op = UnaryOp.OpType.POST_DECREMENT;
		}

		if (op == null)
			return v;
		return new UnaryOp(op, v, v.getSourceLocation());
	}

	// 后置操作符, ++/--/./[]/()
	// function = atom ('(' (expression (',' expression)*)? ')')*;
	// index = atom ('[' expression ']')*;
	// member = atom ('.' identifier)*;
	private Op postOp() {
		Op body = atom();
		if (body == null)
			return null;

		while (true) {
			if (tokenStream.match(TokenType.SEPARATOR, "(") != null) {
				// 函数调用
				ArrayList<Op> args = new ArrayList<Op>();
				while (true) {
					Op arg = expression();
					if (arg == null) {
						if (args.size() == 0)
							break;
						else
							throw new GrammarException("Function argument expected", tokenStream);
					}
					args.add(arg);

					if (tokenStream.match(TokenType.SEPARATOR, ",") == null)
						break;
				}
				if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
					throw new GrammarException("Token ')' expected", tokenStream);
				body = new FuncCallOp(body, args);
			} else if (tokenStream.match(TokenType.SEPARATOR, "[") != null) {
				// 数组索引
				Op i = expression();
				if (i == null)
					throw new GrammarException("Expression expected", tokenStream);
				if (tokenStream.match(TokenType.SEPARATOR, "]") == null)
					throw new GrammarException("Token ']' expected", tokenStream);
				body = new IndexOp(body, i);
			} else if (tokenStream.match(TokenType.SEPARATOR, ".") != null) {
				// 取成员
				Token t = tokenStream.match(TokenType.IDENTIFIER);
				if (t == null)
					throw new GrammarException("Identifier token expected", tokenStream);
				body = new MemberOp(body, (String) t.value);
			} else {
				break;
			}
		}
		return body;
	}

	// atom = variable | constant | function | ('(' expression ')') | array;
	private Op atom() {
		if (tokenStream.eof())
			return null;

		Op op = lambda();
		if (op != null)
			return op;

		Token t = tokenStream.nextToken();
		if (t == null)
			return null;
		switch (t.type) {
		case IDENTIFIER:
			return new Operand(new VariableEval((String) t.value), t.file,
					t.line);

		case KEY_WORD:
			if (t.value.equals("null")) {
				return new Operand(NullEval.instance, t.file, t.line);
			}
			tokenStream.putBack();
			return null;

		case BOOLEAN:
			return new Operand(BooleanEval.valueOf((Boolean) t.value), t.file,
					t.line);

		case INTEGER:
			return new Operand(new IntegerEval((Long) t.value), t.file, t.line);

		case DOUBLE:
			return new Operand(new DoubleEval((Double) t.value), t.file, t.line);

		case STRING:
			return new Operand(new StringEval((String) t.value), t.file, t.line);

		case SEPARATOR:
			if (t.value.equals("(")) {
				Op v = expression();
				if (v == null)
					throw new GrammarException("Expression expected", tokenStream);
				if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
					throw new GrammarException("Token ')' expected", tokenStream);
				return v;
			} else if (t.value.equals("[")) {
				tokenStream.putBack();
				Op v = array();
				return v;
			} else if (t.value.equals("{")) {
				tokenStream.putBack();
				Op v = map();
				return v;
			}
			tokenStream.putBack();
			return null;

		default:
			tokenStream.putBack();
			return null;
		}
	}

	// array = '[' ((expression ',')* expression ','?)? ']';
	private ArrayOp array() {
		Token t = tokenStream.match(TokenType.SEPARATOR, "[");
		if (t == null)
			return null;

		ArrayList<Op> l = new ArrayList<Op>();
		while (true) {
			Op v = expression();
			if (v == null)
				break;

			l.add(v);
			if (tokenStream.match(TokenType.SEPARATOR, ",") == null)
				break;
		}
		if (tokenStream.match(TokenType.SEPARATOR, "]") == null)
			throw new GrammarException("Token ']' expected", tokenStream);

		return new ArrayOp(l, t.file, t.line);
	}

	// defination = type variable ('=' rvalue)? (',' varialbe ('=' rvalue)?)*;
	private Op defination() {
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
		} else if (t.value.equals("map")) {
			vt = VarType.MAP;
		} else if (t.value.equals("var")) {
			vt = VarType.VAR;
		} else if (t.value.equals("function")) {
			vt = VarType.FUNCTION;
			if (tokenStream.match(TokenType.IDENTIFIER) == null) {
				tokenStream.putBack();
				return null;
			}
			if (tokenStream.match(TokenType.SEPARATOR, "=") == null
					&& tokenStream.match(TokenType.SEPARATOR, ",") == null
					&& tokenStream.match(TokenType.SEPARATOR, ";") == null) {
				tokenStream.putBack(2);
				return null;
			}
			tokenStream.putBack(2);
		} else {
			tokenStream.putBack();
			return null;
		}

		OpList l = new OpList(t.file, t.line);
		do {
			t = tokenStream.match(TokenType.IDENTIFIER);
			if (t == null)
				throw new GrammarException("Identifier token expected", tokenStream);

			String name = (String) t.value;
			if (tokenStream.match(TokenType.SEPARATOR, "=") != null) {
				Op v = expression();
				if (v == null)
					throw new GrammarException("Expression expected", tokenStream);
				l.add(new DefinationOp(vt, name, v, t.file, t.line));
			} else {
				l.add(new DefinationOp(vt, name, null, t.file, t.line));
			}
		} while (tokenStream.match(TokenType.SEPARATOR, ",") != null);
		return l;
	}

	// for_loop = 'for' '(' expression? ';' rvalue? ';' expression? ')'
	// sentence;
	private Op forLoop() {
		Token t = tokenStream.match(TokenType.KEY_WORD, "for");
		if (t == null)
			return null;
		if (tokenStream.match(TokenType.SEPARATOR, "(") == null)
			throw new GrammarException("Token '(' expected", tokenStream);

		Op init_exp = expression();
		if (init_exp == null)
			init_exp = defination();
		if (tokenStream.match(TokenType.SEPARATOR, ";") == null)
			throw new GrammarException("Token ';' expected", tokenStream);

		Op break_exp = rvalue();
		if (tokenStream.match(TokenType.SEPARATOR, ";") == null)
			throw new GrammarException("Token ';' expected", tokenStream);

		Op fin_exp = expression();
		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("Token ')' expected", tokenStream);

		Op body = sentence();
		if (body == null)
			throw new GrammarException("Expression expected", tokenStream);

		return new ForLoopOp(init_exp, break_exp, fin_exp, body, t.file, t.line);
	}

	// do_while_loop = 'do' block 'while' '(' rvalue ')' ';';
	private Op doWhileLoop() {
		Token t = tokenStream.match(TokenType.KEY_WORD, "do");
		if (t == null)
			return null;

		Op body = block();
		if (body == null)
			throw new GrammarException("Expression block expected", tokenStream);

		if (tokenStream.match(TokenType.KEY_WORD, "while") == null)
			throw new GrammarException("Keyword 'while' expected", tokenStream);
		if (tokenStream.match(TokenType.SEPARATOR, "(") == null)
			throw new GrammarException("Token '(' expected", tokenStream);

		Op brk_exp = expression();
		if (brk_exp == null)
			throw new GrammarException("Expression expected", tokenStream);

		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("Token ')' expected", tokenStream);
		if (tokenStream.match(TokenType.SEPARATOR, ";") == null)
			throw new GrammarException("Token ';' expected", tokenStream);

		return new DoWhileLoopOp(body, brk_exp, t.file, t.line);
	}

	// while_loop = 'while' '(' rvalue ')' sentence;
	private Op whileLoop() {
		Token t = tokenStream.match(TokenType.KEY_WORD, "while");
		if (t == null)
			return null;
		if (tokenStream.match(TokenType.SEPARATOR, "(") == null)
			throw new GrammarException("Token '(' expected", tokenStream);

		Op brk_exp = expression();
		if (brk_exp == null)
			throw new GrammarException("Expression expected", tokenStream);

		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("Token ')' expected", tokenStream);

		Op body = sentence();
		if (body == null)
			throw new GrammarException("Expression expected", tokenStream);

		return new WhileLoop(brk_exp, body, t.file, t.line);
	}

	// if_else = 'if' '(' rvalue ')' sentence ('else' sentence)?;
	private Op ifElse() {
		Token t = tokenStream.match(TokenType.KEY_WORD, "if");
		if (t == null)
			return null;
		if (tokenStream.match(TokenType.SEPARATOR, "(") == null)
			throw new GrammarException("Token '(' expected", tokenStream);

		Op cond = expression();
		if (cond == null)
			throw new GrammarException("Expression expected", tokenStream);

		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("Token ')' expected", tokenStream);

		Op body = sentence();
		if (body == null)
			throw new GrammarException("Expression expected", tokenStream);

		Op else_body = null;
		if (tokenStream.match(TokenType.KEY_WORD, "else") != null) {
			else_body = sentence();
			if (else_body == null)
				throw new GrammarException("Expression expected", tokenStream);
		}

		return new IfElseOp(cond, body, else_body, t.file, t.line);
	}

	// include = 'include' (('"' path '"') | ('<' path '>'));
	private Op include() {
		Token t = tokenStream.match(TokenType.KEY_WORD, "include");
		if (t == null)
			return null;
		Token f = tokenStream.match(TokenType.STRING);
		if (f == null)
			throw new GrammarException("File path expected", tokenStream);
		return new IncludeOp((String) f.value, t.file, t.line);
	}

	// function = 'function' name '(' (type param(',' type param)*)? ')'
	// block
	private Op functionDefination() {
		Token t = tokenStream.match(TokenType.KEY_WORD, "function");
		if (t == null)
			return null;

		// 函数名
		Token n = tokenStream.match(TokenType.IDENTIFIER);
		if (n == null) {
			tokenStream.putBack();
			return null;
		}
		String name = (String) n.value;

		if (tokenStream.match(TokenType.SEPARATOR, "(") == null) {
			tokenStream.putBack(2);
			return null;
		}

		// 形参列表
		ArrayList<ParamInfo> params = new ArrayList<ParamInfo>();
		while (true) {
			VarType vt;
			Token tt = tokenStream.match(TokenType.KEY_WORD);
			if (tt == null) {
				break;
			} else if (tt.value.equals("bool")) {
				vt = VarType.BOOLEAN;
			} else if (tt.value.equals("int")) {
				vt = VarType.INTEGER;
			} else if (tt.value.equals("double")) {
				vt = VarType.DOUBLE;
			} else if (tt.value.equals("string")) {
				vt = VarType.STRING;
			} else if (tt.value.equals("array")) {
				vt = VarType.ARRAY;
			} else if (tt.value.equals("map")) {
				vt = VarType.MAP;
			} else if (tt.value.equals("var")) {
				vt = VarType.VAR;
			} else {
				tokenStream.putBack();
				return null;
			}

			tt = tokenStream.match(TokenType.IDENTIFIER);
			if (tt == null)
				throw new GrammarException("Identifier token expected",
						tokenStream);

			String pname = (String) tt.value;
			params.add(new ParamInfo(pname, vt));
			if (tokenStream.match(TokenType.SEPARATOR, ",") == null)
				break;
		}

		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("Token ')' expected", tokenStream);

		// 函数体
		Op ops = block();
		if (ops == null)
			throw new GrammarException("Function body expected", tokenStream);

		SourceLocation sl = new SourceLocation(tokenStream.getFile(), t.line);
		FuncDefOp ufo = new FuncDefOp(name, ops, params, sl);
		return new DefinationOp(VarType.FUNCTION, name, ufo, t.file, t.line);
	}

	// lambda = 'function' '(' (type param(',' type param)*)? ')'
	// block
	private Op lambda() {
		Token t = tokenStream.match(TokenType.KEY_WORD, "function");
		if (t == null)
			return null;

		if (tokenStream.match(TokenType.SEPARATOR, "(") == null) {
			tokenStream.putBack();
			return null;
		}

		// 形参列表
		ArrayList<ParamInfo> params = new ArrayList<ParamInfo>();
		while (true) {
			VarType vt;
			Token tt = tokenStream.match(TokenType.KEY_WORD);
			if (tt == null) {
				break;
			} else if (tt.value.equals("bool")) {
				vt = VarType.BOOLEAN;
			} else if (tt.value.equals("int")) {
				vt = VarType.INTEGER;
			} else if (tt.value.equals("double")) {
				vt = VarType.DOUBLE;
			} else if (tt.value.equals("string")) {
				vt = VarType.STRING;
			} else if (tt.value.equals("array")) {
				vt = VarType.ARRAY;
			} else {
				tokenStream.putBack();
				return null;
			}

			tt = tokenStream.match(TokenType.IDENTIFIER);
			if (tt == null)
				throw new GrammarException("Identifier token expected", tokenStream);

			String pname = (String) tt.value;
			params.add(new ParamInfo(pname, vt));
			if (tokenStream.match(TokenType.SEPARATOR, ",") == null)
				break;
		}

		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("Token ')' expected", tokenStream);

		// 函数体
		Op ops = block();
		if (ops == null)
			throw new GrammarException("Function body expected", tokenStream);

		SourceLocation sl = new SourceLocation(tokenStream.getFile(), t.line);
		return new FuncDefOp("<lambda>", ops, params, sl);
	}

	// map_entry = rvalue ':' rvalue
	// map = '{' (map_entry (',' map_entry)*)? '}'
	private Op map() {
		int p = tokenStream.tell();
		Token t = tokenStream.match(TokenType.SEPARATOR, "{");
		if (t == null)
			return null;

		ArrayList<MapOp.Entry> l = new ArrayList<MapOp.Entry>();
		while (true) {
			Op k = expression();
			if (k == null)
				break;

			if (tokenStream.match(TokenType.SEPARATOR, ":") == null) {
				tokenStream.seek(p);
				return null;
			}

			Op v = expression();
			if (v == null)
				throw new GrammarException("Value of map entry expected", tokenStream);

			l.add(new MapOp.Entry(k, v));

			if (tokenStream.match(TokenType.SEPARATOR, ",") == null)
				break;
		}
		if (tokenStream.match(TokenType.SEPARATOR, "}") == null) {
			tokenStream.seek(p);
			return null;
		}

		return new MapOp(l, t.file, t.line);
	}
}

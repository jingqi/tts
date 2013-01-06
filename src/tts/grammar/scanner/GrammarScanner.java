package tts.grammar.scanner;

import java.util.ArrayList;

import tts.eval.*;
import tts.eval.UserFunctionEval.ParamInfo;
import tts.grammar.tree.*;
import tts.grammar.tree.binaryop.*;
import tts.token.scanner.*;
import tts.token.scanner.Token.TokenType;
import tts.util.SourceLocation;
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
		OpList ret = new OpList(tokenStream.getFile(), tokenStream.getLine());
		while (true) {
			IOp op = sentence();
			if (op == null)
				break;
			else
				ret.add(op);
		}
		if (!tokenStream.eof())
			throw new GrammarException("next sentence not recognised",
					tokenStream.getFile(), tokenStream.getLine());
		return ret;
	}

	// sentence = ((expression | defination)? ';') | statement | text_template |
	// block | include;
	private IOp sentence() {
		int p = tokenStream.tell();
		IOp ret = expression();
		if (ret == null)
			ret = defination();
		Token t = tokenStream.nextToken();
		if (t != null && t.type == TokenType.SEPARATOR && t.value.equals(";")) {
			if (ret == null)
				return new OpList(t.file, t.line); // 空语句
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
		if (ret == null)
			ret = include();
		return ret;
	}

	// block = '{' sentence* '}'
	private IOp block() {
		Token t = tokenStream.match(TokenType.SEPARATOR, "{");
		if (t == null)
			return null;

		OpList ret = new OpList(t.file, t.line);
		while (true) {
			IOp op = sentence();
			if (op == null)
				break;
			else
				ret.add(op);
		}

		if (tokenStream.match(TokenType.SEPARATOR, "}") == null)
			throw new GrammarException("token '}' expected",
					tokenStream.getFile(), tokenStream.getLine());

		return new FrameScopOp(ret);
	}

	private IOp textTemplate() {
		Token t = tokenStream.match(TokenType.TEXT_TEMPLATE);
		if (t == null)
			return null;
		return new TextTemplateOp((String) t.value, t.file, t.line);
	}

	/**
	 * statement = 'break;' | 'continue;' | for_loop | do_while_loop |
	 * while_loop | if_else;
	 */
	private IOp statement() {
		Token t = tokenStream.match(TokenType.KEY_WORD);
		if (t != null) {
			if (t.value.equals("break")) {
				if (tokenStream.match(TokenType.SEPARATOR, ";") == null)
					throw new GrammarException("token ';' expected",
							tokenStream.getFile(), tokenStream.getLine());
				return new BreakOp(t.file, t.line);
			} else if (t.value.equals("continue")) {
				if (tokenStream.match(TokenType.SEPARATOR, ";") == null)
					throw new GrammarException("token ';' expected",
							tokenStream.getFile(), tokenStream.getLine());
				return new ContinueOp(t.file, t.line);
			} else if (t.value.equals("return")) {
				IOp r = expression();
				if (tokenStream.match(TokenType.SEPARATOR, ";") == null)
					throw new GrammarException("token ';' expected",
							tokenStream.getFile(), tokenStream.getLine());

				if (r == null)
					return new ReturnFuncOp(new OpList(t.file, t.line));
				return new ReturnFuncOp(r);
			} else {
				tokenStream.putBack();
			}
		}

		IOp ret = forLoop();
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
		} else if (tokenStream.match(TokenType.SEPARATOR, "<<<=") != null) {
			op = BitOp.OpType.CIRCLE_SHIFT_LEFT;
		} else if (tokenStream.match(TokenType.SEPARATOR, ">>>=") != null) {
			op = BitOp.OpType.CIRCLE_SHIFT_RIGHT;
		} else {
			tokenStream.putBack();
			return null;
		}

		// 如果是一个变量，尝试赋值
		IOp v = rvalue();
		if (v == null) {
			tokenStream.putBack(2);
			return null;
		}

		String name = (String) tok.value;
		if (op == null) {
			return new AssignOp(name, v, sl);
		} else if (op instanceof MathOp.OpType) {
			MathOp.OpType mop = (MathOp.OpType) op;
			return new AssignOp(name, new MathOp(new Operand(new VariableEval(
					name), sl), mop, v), sl);
		} else if (op instanceof BitOp.OpType) {
			BitOp.OpType bop = (BitOp.OpType) op;
			return new AssignOp(name, new BitOp(new Operand(new VariableEval(
					name), sl), bop, v), sl);
		} else {
			throw new RuntimeException();
		}
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

		if (tokenStream.match(TokenType.SEPARATOR, "?") == null) {
			return cond;
		}

		IOp t = booleanOr();
		if (t == null) {
			tokenStream.seek(p);
			return null;
		}

		if (tokenStream.match(TokenType.SEPARATOR, ":") == null) {
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

		while (tokenStream.match(TokenType.SEPARATOR, "||") != null) {
			IOp vv = booleanAnd();
			if (vv == null)
				throw new GrammarException("expression expected",
						tokenStream.getFile(), tokenStream.getLine());
			v = new BooleanOp(v, BooleanOp.OpType.OR, vv);
		}
		return v;
	}

	// and = bit_or ('&&' bit_or)*;
	private IOp booleanAnd() {
		IOp v = bitOr();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "||") != null) {
			IOp vv = bitOr();
			if (vv == null)
				throw new GrammarException("expression expected",
						tokenStream.getFile(), tokenStream.getLine());
			v = new BooleanOp(v, BooleanOp.OpType.AND, vv);
		}
		return v;
	}

	// bit_or = bit_xor ('|' bit_or)*;
	private IOp bitOr() {
		IOp v = bitXor();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "|") != null) {
			IOp vv = bitXor();
			if (vv == null)
				throw new GrammarException("expression expected",
						tokenStream.getFile(), tokenStream.getLine());
			v = new BitOp(v, BitOp.OpType.BIT_OR, vv);
		}
		return v;
	}

	// bit_xor = bit_and ('^' bit_and)*;
	private IOp bitXor() {
		IOp v = bitAnd();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "^") != null) {
			IOp vv = bitAnd();
			if (vv == null)
				throw new GrammarException("expression expected",
						tokenStream.getFile(), tokenStream.getLine());
			v = new BitOp(v, BitOp.OpType.BIT_XOR, vv);
		}
		return v;
	}

	// bit_and = eq_cmp ('&' eq_cmp)*;
	private IOp bitAnd() {
		IOp v = eqCmp();
		if (v == null)
			return null;

		while (tokenStream.match(TokenType.SEPARATOR, "&") != null) {
			IOp vv = eqCmp();
			if (vv == null)
				throw new GrammarException("expression expected",
						tokenStream.getFile(), tokenStream.getLine());
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
			if (tokenStream.match(TokenType.SEPARATOR, "==") != null)
				op = CompareOp.OpType.EQ;
			else if (tokenStream.match(TokenType.SEPARATOR, "!=") != null)
				op = CompareOp.OpType.NOT_EQ;
			else
				break;

			IOp vv = lessCmp();
			if (vv == null)
				throw new GrammarException("expression expected",
						tokenStream.getFile(), tokenStream.getLine());
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

		IOp vv = bitShift();
		if (vv == null)
			throw new GrammarException("expression expected",
					tokenStream.getFile(), tokenStream.getLine());
		return new CompareOp(v, op, vv);
	}

	// bit_shift = part (('<<' | '>>' | '>>>') part)*;
	private IOp bitShift() {
		IOp v = part();
		if (v == null)
			return null;

		while (true) {
			BitOp.OpType op;
			if (tokenStream.match(TokenType.SEPARATOR, "<<") != null)
				op = BitOp.OpType.SHIFT_LEFT;
			else if (tokenStream.match(TokenType.SEPARATOR, ">>") != null)
				op = BitOp.OpType.SHIFT_RIGHT;
			else if (tokenStream.match(TokenType.SEPARATOR, "<<<") != null)
				op = BitOp.OpType.CIRCLE_SHIFT_LEFT;
			else if (tokenStream.match(TokenType.SEPARATOR, ">>>") != null)
				op = BitOp.OpType.CIRCLE_SHIFT_RIGHT;
			else
				break;

			IOp vv = part();
			if (vv == null)
				throw new GrammarException("expression expected",
						tokenStream.getFile(), tokenStream.getLine());
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
					throw new GrammarException("expression expected",
							tokenStream.getFile(), tokenStream.getLine());
				v = new MathOp(v, MathOp.OpType.ADD, vv);
			} else if (t.value.equals("-")) {
				IOp vv = term();
				if (vv == null)
					throw new GrammarException("expression expected",
							tokenStream.getFile(), tokenStream.getLine());
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
					throw new GrammarException("expression expected",
							tokenStream.getFile(), tokenStream.getLine());
				v = new MathOp(v, MathOp.OpType.MULTIPLY, vv);
			} else if (t.value.equals("/")) {
				IOp vv = factor();
				if (vv == null)
					throw new GrammarException("expression expected",
							tokenStream.getFile(), tokenStream.getLine());
				v = new MathOp(v, MathOp.OpType.DIVID, vv);
			} else if (t.value.equals("%")) {
				IOp vv = factor();
				if (vv == null)
					throw new GrammarException("expression expected",
							tokenStream.getFile(), tokenStream.getLine());
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
		if (tokenStream.match(TokenType.SEPARATOR, "+") != null)
			op = UnaryOp.OpType.POSITIVE;
		else if (tokenStream.match(TokenType.SEPARATOR, "-") != null)
			op = UnaryOp.OpType.NEGATIEVE;
		else if (tokenStream.match(TokenType.SEPARATOR, "!") != null)
			op = UnaryOp.OpType.NOT;
		else if (tokenStream.match(TokenType.SEPARATOR, "~") != null)
			op = UnaryOp.OpType.BIT_NOT;
		else if (tokenStream.match(TokenType.SEPARATOR, "++") != null)
			op = UnaryOp.OpType.PRE_INCREMENT;
		else if (tokenStream.match(TokenType.SEPARATOR, "--") != null)
			op = UnaryOp.OpType.PRE_DECREMENT;

		IOp v = functionCall();
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

	// function = index '(' (index (',' index)*)? ')';
	private IOp functionCall() {
		IOp func = index();
		if (func == null)
			return null;
		if (tokenStream.match(TokenType.SEPARATOR, "(") == null)
			return func;

		ArrayList<IOp> args = new ArrayList<IOp>();
		while (true) {
			IOp arg = expression();
			if (arg == null) {
				if (args.size() == 0)
					break;
				else
					throw new GrammarException("function argument expected",
							tokenStream.getFile(), tokenStream.getLine());
			}
			args.add(arg);

			if (tokenStream.match(TokenType.SEPARATOR, ",") == null)
				break;
		}
		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("token ')' expected",
					tokenStream.getFile(), tokenStream.getLine());
		return new FuncCallOp(func, args);
	}

	// index = member '[' member ']';
	private IOp index() {
		IOp body = member();
		if (body == null)
			return null;
		if (tokenStream.match(TokenType.SEPARATOR, "[") == null)
			return body;
		IOp i = expression();
		if (i == null)
			throw new GrammarException("expression expected",
					tokenStream.getFile(), tokenStream.getLine());
		if (tokenStream.match(TokenType.SEPARATOR, "]") == null)
			throw new GrammarException("token ']' expected",
					tokenStream.getFile(), tokenStream.getLine());
		return new IndexOp(body, i);
	}

	// member = atom '.' atom ;
	private IOp member() {
		IOp body = atom();
		if (body == null)
			return null;
		if (tokenStream.match(TokenType.SEPARATOR, ".") == null)
			return body;

		Token t = tokenStream.match(TokenType.IDENTIFIER);
		if (t == null)
			throw new GrammarException("identifier token expected",
					tokenStream.getFile(), tokenStream.getLine());
		return new MemberOp(body, (String) t.value);
	}

	// atom = variable | constant | function | ('(' expression ')') | array;
	private IOp atom() {
		if (tokenStream.eof())
			return null;

		IOp op = lambda();
		if (op != null)
			return op;

		Token t = tokenStream.nextToken();
		if (t == null)
			return null;
		switch (t.type) {
		case IDENTIFIER:
			return new Operand(new VariableEval((String) t.value), t.file,
					t.line);

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
				IOp v = expression();
				if (v == null)
					throw new GrammarException("expression expected",
							tokenStream.getFile(), tokenStream.getLine());
				if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
					throw new GrammarException("token ')' expected",
							tokenStream.getFile(), tokenStream.getLine());
				return v;
			} else if (t.value.equals("[")) {
				tokenStream.putBack();
				IOp v = array();
				return v;
			} else if (t.value.equals("{")) {
				tokenStream.putBack();
				IOp v = map();
				return v;
			}

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

		ArrayList<IOp> l = new ArrayList<IOp>();
		while (true) {
			IOp v = expression();
			if (v == null)
				break;

			l.add(v);
			if (tokenStream.match(TokenType.SEPARATOR, ",") == null)
				break;
		}
		if (tokenStream.match(TokenType.SEPARATOR, "]") == null)
			throw new GrammarException("token ']' expected",
					tokenStream.getFile(), tokenStream.getLine());

		return new ArrayOp(l, t.file, t.line);
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
				throw new GrammarException("identifier token expected",
						tokenStream.getFile(), tokenStream.getLine());

			String name = (String) t.value;
			if (tokenStream.match(TokenType.SEPARATOR, "=") != null) {
				IOp v = expression();
				if (v == null)
					throw new GrammarException("expression expected",
							tokenStream.getFile(), tokenStream.getLine());
				l.add(new DefinationOp(vt, name, v, t.file, t.line));
			} else {
				l.add(new DefinationOp(vt, name, null, t.file, t.line));
			}
		} while (tokenStream.match(TokenType.SEPARATOR, ",") != null);
		return l;
	}

	// for_loop = 'for' '(' expression? ';' rvalue? ';' expression? ')'
	// sentence;
	private IOp forLoop() {
		Token t = tokenStream.match(TokenType.KEY_WORD, "for");
		if (t == null)
			return null;
		if (tokenStream.match(TokenType.SEPARATOR, "(") == null)
			throw new GrammarException("token '(' expected",
					tokenStream.getFile(), tokenStream.getLine());

		IOp init_exp = expression();
		if (init_exp == null)
			init_exp = defination();
		if (tokenStream.match(TokenType.SEPARATOR, ";") == null)
			throw new GrammarException("token ';' expected",
					tokenStream.getFile(), tokenStream.getLine());

		IOp break_exp = rvalue();
		if (tokenStream.match(TokenType.SEPARATOR, ";") == null)
			throw new GrammarException("token ';' expected",
					tokenStream.getFile(), tokenStream.getLine());

		IOp fin_exp = expression();
		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("token ')' expected",
					tokenStream.getFile(), tokenStream.getLine());

		IOp body = sentence();
		if (body == null)
			throw new GrammarException("expression expected",
					tokenStream.getFile(), tokenStream.getLine());

		return new ForLoopOp(init_exp, break_exp, fin_exp, body, t.file, t.line);
	}

	// do_while_loop = 'do' block 'while' '(' rvalue ')' ';';
	private IOp doWhileLoop() {
		Token t = tokenStream.match(TokenType.KEY_WORD, "do");
		if (t == null)
			return null;

		IOp body = block();
		if (body == null)
			throw new GrammarException("expression block expected",
					tokenStream.getFile(), tokenStream.getLine());

		if (tokenStream.match(TokenType.KEY_WORD, "while") == null)
			throw new GrammarException("keyword 'while' expected",
					tokenStream.getFile(), tokenStream.getLine());
		if (tokenStream.match(TokenType.SEPARATOR, "(") == null)
			throw new GrammarException("token '(' expected",
					tokenStream.getFile(), tokenStream.getLine());

		IOp brk_exp = expression();
		if (brk_exp == null)
			throw new GrammarException("expression expected",
					tokenStream.getFile(), tokenStream.getLine());

		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("token ')' expected",
					tokenStream.getFile(), tokenStream.getLine());
		if (tokenStream.match(TokenType.SEPARATOR, ";") == null)
			throw new GrammarException("token ';' expected",
					tokenStream.getFile(), tokenStream.getLine());

		return new DoWhileLoopOp(body, brk_exp, t.file, t.line);
	}

	// while_loop = 'while' '(' rvalue ')' sentence;
	private IOp whileLoop() {
		Token t = tokenStream.match(TokenType.KEY_WORD, "while");
		if (t == null)
			return null;
		if (tokenStream.match(TokenType.SEPARATOR, "(") == null)
			throw new GrammarException("token '(' expected",
					tokenStream.getFile(), tokenStream.getLine());

		IOp brk_exp = expression();
		if (brk_exp == null)
			throw new GrammarException("expression expected",
					tokenStream.getFile(), tokenStream.getLine());

		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("token ')' expected",
					tokenStream.getFile(), tokenStream.getLine());

		IOp body = sentence();
		if (body == null)
			throw new GrammarException("expression expected",
					tokenStream.getFile(), tokenStream.getLine());

		return new WhileLoop(brk_exp, body, t.file, t.line);
	}

	// if_else = 'if' '(' rvalue ')' sentence ('else' sentence)?;
	private IOp ifElse() {
		Token t = tokenStream.match(TokenType.KEY_WORD, "if");
		if (t == null)
			return null;
		if (tokenStream.match(TokenType.SEPARATOR, "(") == null)
			throw new GrammarException("token '(' expected",
					tokenStream.getFile(), tokenStream.getLine());

		IOp cond = expression();
		if (cond == null)
			throw new GrammarException("expression expected",
					tokenStream.getFile(), tokenStream.getLine());

		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("token ')' expected",
					tokenStream.getFile(), tokenStream.getLine());

		IOp body = sentence();
		if (body == null)
			throw new GrammarException("expression expected",
					tokenStream.getFile(), tokenStream.getLine());

		IOp else_body = null;
		if (tokenStream.match(TokenType.KEY_WORD, "else") != null) {
			else_body = sentence();
			if (else_body == null)
				throw new GrammarException("expression expected",
						tokenStream.getFile(), tokenStream.getLine());
		}

		return new IfElseOp(cond, body, else_body, t.file, t.line);
	}

	// include = 'include' (('"' path '"') | ('<' path '>'));
	private IOp include() {
		Token t = tokenStream.match(TokenType.KEY_WORD, "include");
		if (t == null)
			return null;
		Token f = tokenStream.match(TokenType.STRING);
		if (f == null)
			throw new GrammarException("file path expected",
					tokenStream.getFile(), tokenStream.getLine());
		return new IncludeOp((String) f.value, t.file, t.line);
	}

	// function = 'function' name '(' (type param(',' type param)*)? ')'
	// block
	private IOp functionDefination() {
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
				throw new GrammarException("identifier token expected",
						tokenStream.getFile(), tokenStream.getLine());

			String pname = (String) tt.value;
			params.add(new ParamInfo(pname, vt));
			if (tokenStream.match(TokenType.SEPARATOR, ",") == null)
				break;
		}

		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("token ')' expected",
					tokenStream.getFile(), tokenStream.getLine());

		// 函数体
		IOp ops = block();
		if (ops == null)
			throw new GrammarException("function body expected",
					tokenStream.getFile(), tokenStream.getLine());

		UserFunctionEval ufe = new UserFunctionEval(name, ops, params);
		IOp fop = new Operand(ufe, t.file, t.line);
		return new DefinationOp(VarType.FUNCTION, name, fop, t.file, t.line);
	}

	// lambda = 'function' '(' (type param(',' type param)*)? ')'
	// block
	private IOp lambda() {
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
				throw new GrammarException("identifier token expected",
						tokenStream.getFile(), tokenStream.getLine());

			String pname = (String) tt.value;
			params.add(new ParamInfo(pname, vt));
			if (tokenStream.match(TokenType.SEPARATOR, ",") == null)
				break;
		}

		if (tokenStream.match(TokenType.SEPARATOR, ")") == null)
			throw new GrammarException("token ')' expected",
					tokenStream.getFile(), tokenStream.getLine());

		// 函数体
		IOp ops = block();
		if (ops == null)
			throw new GrammarException("function body expected",
					tokenStream.getFile(), tokenStream.getLine());

		UserFunctionEval ufe = new UserFunctionEval("<lambda>", ops, params);
		return new Operand(ufe, t.file, t.line);
	}

	// map_entry = rvalue ':' rvalue
	// map = '{' (map_entry (',' map_entry)*)? '}'
	private IOp map() {
		int p = tokenStream.tell();
		Token t = tokenStream.match(TokenType.SEPARATOR, "{");
		if (t == null)
			return null;

		ArrayList<MapOp.Entry> l = new ArrayList<MapOp.Entry>();
		while (true) {
			IOp k = expression();
			if (k == null)
				break;

			if (tokenStream.match(TokenType.SEPARATOR, ":") == null) {
				tokenStream.seek(p);
				return null;
			}

			IOp v = expression();
			if (v == null)
				throw new GrammarException("value of map entry expected",
						tokenStream.getFile(), tokenStream.getLine());

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

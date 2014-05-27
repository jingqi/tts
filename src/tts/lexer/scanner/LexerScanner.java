package tts.lexer.scanner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import tts.lexer.scanner.Token.TokenType;
import tts.lexer.stream.IScanReader;

/**
 * ragel 语法规则: <br/>
 *
 * Boolean = 'true' | 'false'; <br/>
 * Integer = [\+\-]? digit+; <br/>
 * Hex = '0x' xdigit+; <br/>
 * Double = [\+\-]? ((digit+ '.' digit*) | ('.' digit+)); <br/>
 * Sentic = (Integer | Float) 'E'i [\+\-]? digit+; <br/>
 *
 * String1 = ['] ([^\\\n\'] | ('\' any))* [']; <br/>
 * String2 = ["] ([^\\\n\"] | ('\' any))* ["]; <br/>
 * String3 = ['''] ([^\\\"] | ('\' any))* [''']; <br/>
 * String4 = ["""] ([^\\\"] | ('\' any))* ["""]; <br/>
 * String = String1 | String2 | String3 | String4; <br/>
 *
 * Keyword = 'if' | 'else' | 'while' ...; <br/>
 * Identifier = (('_' | alpha) ('_' | alnum)*) - Keyword; <br/>
 * Separator = '>>>' | '+=' | '>' ...; <br/>
 *
 * @author jingqi
 *
 */
public class LexerScanner {

	// 输入
	private IScanReader reader;

	// 源文件、行号
	private String file;
	private int line;

	// 是否在行代码模式
	private boolean inLineCode = false;

	// 是否在块代码模式
	private boolean inBlockCode = false;

	// 代码起止标记
	static final String BLOCK_CODE_START = "$${"; // 块代码起始标记
	static final String BLOCK_CODE_END = "}$$"; // 块代码结束标记
	static final String LINE_CODE_START = "$$"; // 行代码起始标记

	// 关键字
	static final Set<String> KEY_WORDS = new HashSet<String>();
	static {
		String[] keywords = {
				"if", "else", "for", "do", "while", "break",
				"continue", "bool", "int", "double", "string", "return",
				"array", "function", "new", "map", "var", "null",
				"throw", "catch", "include", "import", "as"};
		for (int i = 0, len = keywords.length; i < len; ++i)
			KEY_WORDS.add(keywords[i]);
	}

	static final String[] SEPARATORS = {
			// 字符从多到少排列:
			// 4个字符
			">>>=",
			// 3个字符
			">>>", "<<=",
			">>=", "===", "!==",
			// 2 个字符
			"==", ">=", "<=", "!=", "+=", "-=", "*=", "/=", "%=", "++", "--",
			"&=", "^=", "|=", ">>", "<<", "&&", "||",
			// 1 个字符
			"{", "}", ">", "<", "=", "+", "-", "*", "/", "%", "!", "~", "^",
			"&", "|", "(", ")", ";", ";", ".", ":", "[", "]", ",", "?" };

	public LexerScanner(IScanReader reader, String file) {
		this.reader = reader;
		this.file = file;
		this.line = 1;
	}

	public LexerScanner(IScanReader reader, String file, int line,
			boolean inBlockCode) {
		this.reader = reader;
		this.file = file;
		this.line = line;
		this.inBlockCode = inBlockCode;
	}

	public String getFile() {
		return file;
	}

	public int getLine() {
		return line;
	}

	public Token nextToken() throws IOException {

		int oldPos = -1;
		TEXT_CODE_LOOP: while (true) {
			if (reader.eof())
				return null;

			if (oldPos == reader.tell())
				throw new LexerException("Token not recognised", file, line);
			oldPos = reader.tell();

			if (!inLineCode && !inBlockCode) {
				if (reader.match(BLOCK_CODE_START)) {
					inBlockCode = true;
					continue;
				}
				if (reader.match(LINE_CODE_START)) {
					inLineCode = true;
					continue;
				}

				// 文本模板
				return getTextTemplate();
			} else if (inLineCode) {
				// 略过空白
				do {
					if (reader.match(" ") || reader.match("\t")
							|| reader.match("\f")) {
						continue;
					} else if (reader.match("\n\r") || reader.match("\r\n")
							|| reader.match("\n") || reader.match("\r")) {
						++line;
						inLineCode = false; // 行代码结束
						continue TEXT_CODE_LOOP;
					} else {
						break;
					}
				} while (!reader.eof());
				if (reader.eof())
					return null;

				Token tk = getToken();
				if (tk != null)
					return tk;
			} else if (inBlockCode) {
				// 略过空白
				do {
					if (reader.match(" ") || reader.match("\t")
							|| reader.match("\f")) {
						continue;
					} else if (reader.match("\n\r") || reader.match("\r\n")
							|| reader.match("\n") || reader.match("\r")) {
						++line;
						continue;
					} else {
						break;
					}
				} while (!reader.eof());
				if (reader.eof())
					return null;

				if (reader.match(BLOCK_CODE_END)) {
					// 略过块代码后面的空行，以免TextTemplate后面带很多空白
					do {
						if (reader.match(" ") || reader.match("\t")
								|| reader.match("\f")) {
							continue;
						} else if (reader.match("\n\r") || reader.match("\r\n")
								|| reader.match("\n") || reader.match("\r")) {
							++line;
							break;
						} else {
							break;
						}
					} while (!reader.eof());

					inBlockCode = false; // 块代码结束
					continue TEXT_CODE_LOOP;
				}

				Token tk = getToken();
				if (tk != null)
					return tk;
			} else {
				assert false;
			}
		}
	}

	public boolean eof() {
		return reader.eof();
	}

	private Token getToken() throws IOException {
		assert !reader.eof();
		char c = reader.read();

		// 字符串
		if (c == '\"' || c == '\'') {
			reader.putBack();
			return getString();
		}

		// 关键字，标识符
		if (c == '_' || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) {
			reader.putBack(1);
			return getIdentifierOrKeyword();
		}

		// 数字
		if ('0' <= c && c <= '9') {
			reader.putBack();
			return getNumber();
		} else if (c == '.' && !reader.eof()) {
			c = reader.read();
			if ('0' <= c && c <= '9') {
				reader.putBack(2);
				return getNumber();
			} else {
				reader.putBack(1);
			}
		}

		// 注释
		if (c == '/' && !reader.eof()) {
			c = reader.read();
			if (c == '/' || c == '*') {
				reader.putBack(2);
				skipComment();
				return null;
			} else {
				reader.putBack();
			}
		}

		// 分隔符：标点、运算符
		reader.putBack();
		return getSeparator();
	}

	// 警告信息
	void warning(String s) {
		System.err.println(s);
	}

	private Token getSeparator() {
		assert !reader.eof();
		for (int i = 0, len = SEPARATORS.length; i < len; ++i) {
			if (reader.match(SEPARATORS[i])) {
				return new Token(TokenType.SEPARATOR, SEPARATORS[i], file, line);
			}
		}
		return null;
	}

	private void skipComment() throws IOException {
		if (reader.match("//")) {
			// 行注释
			while (!reader.eof()) {
				char c = reader.read();
				if (c == '\n') {
					if (!reader.eof() && reader.read() != '\r')
						reader.putBack(1);
					++line;
					inLineCode = false; // 行代码结束
					return;
				} else if (c == '\r') {
					if (!reader.eof() && reader.read() != '\n')
						reader.putBack(1);
					++line;
					inLineCode = false; // 行代码结束
					return;
				}
			}
			return;
		} else if (reader.match("/*")) {
			// 块注释
			while (!reader.eof()) {
				if (reader.match("*/"))
					return;
				else if (reader.match("\n\r") || reader.match("\r\n")
						|| reader.match("\n") || reader.match("\r"))
					++line;
				else
					reader.skip(1);
			}
			throw new LexerException("need an end of block comment", file,
					line);
		}
	}

	// 处理整数部分
	private long getInteger() throws IOException {
		long v = 0;
		while (!reader.eof()) {
			char c = reader.read();
			if ('0' <= c && c <= '9') {
				v = v * 10 + (c - '0');
			} else {
				reader.putBack(1);
				break;
			}
		}
		return v;
	}

	// 处理小数点后面的数字
	private double getDecimal() throws IOException {
		double v = 0;
		int w = 0;
		while (!reader.eof()) {
			++w;
			char c = reader.read();
			if ('0' <= c && c <= '9') {
				v += (c - '0') / Math.pow(10, w);
			} else {
				reader.putBack(1);
				break;
			}
		}
		return v;
	}

	// 处理整数、浮点数、科学计数法
	private Token getNumber() throws IOException {
		assert !reader.eof();
		if (reader.preMatch("0x"))
			return getHexNumber();

		long i = 0;
		double d = 0;
		long e = 0;

		char c = reader.read();
		assert c == '.' || ('0' <= c && c <= '9');

		boolean float_value = false;
		if (c == '.') {
			d = getDecimal();
			float_value = true;
		} else {
			reader.putBack(1);
			i = getInteger();
			if (!reader.eof()) {
				c = reader.read();
				if (c != '.') {
					reader.putBack(1);
				} else {
					d = getDecimal();
					float_value = true;
				}
			}
		}

		if (!reader.eof()) {
			c = reader.read();
			if (c != 'e' && c != 'E') {
				reader.putBack(1);
			} else {
				e = getInteger();
				float_value = true;
			}
		}

		if (float_value) {
			double v = (i + d) * Math.pow(10, e);
			return new Token(TokenType.DOUBLE, Double.valueOf(v), file, line);
		} else {
			return new Token(TokenType.INTEGER, Long.valueOf(i), file, line);
		}
	}

	// 处理16进制数
	private Token getHexNumber() throws IOException {
		assert reader.preMatch("0x");
		reader.skip(2);

		if (reader.eof())
			throw new LexerException("Hex number expected", file, line);

		long v = 0;
		boolean over_flow = false;
		while (true) {
			if (reader.eof())
				break;

			char c = reader.read();
			int x = 0;
			if ('0' <= c && c <= '9') {
				x = c - '0';
			} else if ('a' <= c && c <= 'f') {
				x = c - 'a' + 10;
			} else if ('A' <= c && c <= 'F') {
				x = c - 'A' + 10;
			} else {
				reader.putBack();
				break;
			}
			if (((v << 4) >> 4) != v)
				over_flow = true;
			v = (v << 4) + x;
		}
		if (over_flow)
			warning("const hex number overflow");

		return new Token(TokenType.INTEGER, Long.valueOf(v), file, line);
	}

	// 处理转义字符
	private char convertChar() throws IOException {
		if (reader.eof())
			throw new LexerException("End of const string expected", file,
					line);
		char c = reader.read();
		switch (c) {
		case 'a':
			return 7; // \a

		case 'b':
			return '\b';

		case 'f':
			return '\f';

		case 'n':
			return '\n';

		case 'r':
			return '\r';

		case 't':
			return '\t';

		case 'v':
			return 11; // \v

		case '\\':
			return '\\';

		case '\"':
			return '\"';

		case '\'':
			return '\'';

		case 'x': // 16进制转义
			c = 0;
			for (int i = 0; i < 2 && !reader.eof(); ++i) {
				char cc = reader.read();
				if ('0' <= cc && cc <= '9') {
					c = (char) (c * 16 + cc - '0');
				} else if ('a' <= cc && cc <= 'f') {
					c = (char) (c * 16 + cc - 'a' + 10);
				} else if ('A' <= cc && cc <= 'F') {
					c = (char) (c * 16 + cc - 'A' + 10);
				} else {
					reader.putBack(1);
					break;
				}
			}
			return c;

		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
			c -= '0';
			for (int j = 0; j < 2 && !reader.eof(); ++j) {
				char cc = reader.read();
				if ('0' <= cc && cc <= '7') {
					c = (char) (c * 8 + cc - '0');
				} else {
					reader.putBack(1);
					break;
				}
			}
			return c;

		default:
			throw new LexerException("Unknow char convertion in string",
					file, line);
		}
	}

	// 处理字符串
	private Token getString() throws IOException {
		assert !reader.eof();
		final int start_line = line;
		char c = reader.read();
		assert c == '\"' || c == '\'';

		final char string_dec = c;
		if (reader.eof())
			throw new LexerException("End of const string expected", file,
					line);

		c = reader.read();
		StringBuilder sb = new StringBuilder();
		if (c == string_dec) { // 字符串块
			if (reader.eof()) { // 空字符串
				return new Token(TokenType.STRING, "", file, start_line);
			} else if (reader.read() != string_dec) {
				reader.putBack();
				return new Token(TokenType.STRING, "", file, start_line);
			}

			final String str_end = "" + string_dec + string_dec + string_dec;
			while (true) {
				if (reader.eof())
					throw new LexerException("String block expected", file,
							line);

				if (reader.match(str_end)) {
					break;
				} else if (reader.match("\n\r") || reader.match("\r\n")
						|| reader.match("\n") || reader.match("\r")) {
					sb.append('\n');
					++line;
					continue;
				}

				sb.append(reader.read());
			}
			String s = sb.toString();
			return new Token(TokenType.STRING, s, file, start_line);
		}

		// 单一字符串
		while (true) {
			if (c == '\\') {
				sb.append(convertChar());
			} else if (c == '\n' || c == '\r') {
				throw new LexerException(
						"Unexpected line end in const string", file, line);
			} else if (c == string_dec) {
				break;
			} else {
				sb.append(c);
			}

			if (reader.eof())
				throw new LexerException("End of string expected", file, line);
			c = reader.read();
		}

		String s = sb.toString();
		return new Token(TokenType.STRING, s, file, start_line);
	}

	private static boolean isFirstIdentifierChar(char c) {
		return c == '_' || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
	}

	private static boolean isIdentifierChar(char c) {
		return c == '_' || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')
				|| ('0' <= c && c <= '9');
	}

	// 处理标识符、关键字、布尔常量
	private Token getIdentifierOrKeyword() throws IOException {
		assert !reader.eof();
		char c = reader.read();
		assert isFirstIdentifierChar(c);
		StringBuilder sb = new StringBuilder();
		sb.append(c);

		while (true) {
			if (reader.eof())
				break;
			c = reader.read();
			if (isIdentifierChar(c)) {
				sb.append(c);
			} else {
				reader.putBack(1);
				break;
			}
		}

		String i = sb.toString();
		if (i.equals("true"))
			return new Token(TokenType.BOOLEAN, Boolean.TRUE, file, line);
		else if (i.equals("false"))
			return new Token(TokenType.BOOLEAN, Boolean.FALSE, file, line);
		else if (KEY_WORDS.contains(i))
			return new Token(TokenType.KEY_WORD, i, file, line);
		return new Token(TokenType.IDENTIFIER, i, file, line);
	}

	// 处理文本模板
	private Token getTextTemplate() throws IOException {
		final int start_line = line;
		StringBuilder sb = new StringBuilder();
		while (true) {
			if (reader.eof() || reader.preMatch(BLOCK_CODE_START)
					|| reader.preMatch(LINE_CODE_START)) {
				break;
			} else if (reader.match("\n\r") || reader.match("\r\n")
					|| reader.match("\n") || reader.match("\r")) {
				sb.append('\n');
				++line;
				continue;
			}

			sb.append(reader.read());
		}

		assert sb.length() > 0;
		String t = sb.toString();
		return new Token(TokenType.TEXT_TEMPLATE, t, file, start_line);
	}

}

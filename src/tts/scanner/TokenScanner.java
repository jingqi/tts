package tts.scanner;

import java.util.HashSet;
import java.util.Set;

import tts.scanner.Token.TokenType;
import tts.stream.IScanReader;

public class TokenScanner {

	IScanReader reader;

	boolean inLineCode = false;
	boolean inBlockCode = false;

	// 代码起止标记
	static final String BLOCK_CODE_START = "$${";
	static final String BLOCK_CODE_END = "}$$";
	static final String LINE_CODE_START = "$$";

	// 关键字
	static final Set<String> KEY_WORDS = new HashSet<String>();
	static {
		String[] keywords = { "if", "else", "void" };
		for (int i = 0, len = keywords.length; i < len; ++i)
			KEY_WORDS.add(keywords[i]);
	}

	static final String[] SEPARATORS1 = { "{", "}" };
	static final String[] SEPARATORS2 = { "==", };
	static final String[] SEPARATORS3 = { ">>>", };

	public Token nextToken() {

		int old_pos = -1;
		TEXT_CODE_LOOP: while (true) {
			if (reader.eof())
				return null;
			if (old_pos == reader.tell())
				throw new ScannerException("unknown token");
			old_pos = reader.tell();

			if (!inLineCode && !inBlockCode) {
				if (reader.preMatch(BLOCK_CODE_START)) {
					inBlockCode = true;
					continue;
				}
				if (reader.preMatch(LINE_CODE_START)) {
					inBlockCode = true;
					continue;
				}

				// 文本模板
				return getTextTemplate();
			} else if (inLineCode) {
				// 略过空白
				do {
					char c = reader.read();
					if (c == ' ' || c == '\t' || c == '\f') {
						continue;
					} else if (c == '\n') {
						if (!reader.eof() && reader.read() != '\r')
							reader.backward(1);
						inLineCode = false; // 代码行结束
						continue TEXT_CODE_LOOP;
					} else if (c == '\r') {
						if (!reader.eof() && reader.read() != '\n')
							reader.backward(1);
						inLineCode = false; // 代码行结束
						continue TEXT_CODE_LOOP;
					} else {
						reader.backward(1);
						break;
					}
				} while (!reader.eof());
				if (reader.eof())
					return null;

				Token tk = getToken();
				if (tk != null)
					return tk;
			} else if (inBlockCode) {// 略过空白
				do {
					char c = reader.read();
					if (c == ' ' || c == '\t' || c == '\f' || c == '\n'
							|| c == '\r') {
						continue;
					} else {
						reader.backward(1);
						break;
					}
				} while (!reader.eof());
				if (reader.eof())
					return null;

				if (reader.preMatch(BLOCK_CODE_END)) {
					inBlockCode = false; // 代码块结束
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

	private Token getToken() {
		assert !reader.eof();
		char c = reader.read();

		// 字符串
		if (c == '\"' || c == '\'') {
			reader.backward(1);
			return getString();
		}

		// 关键字，标识符
		if (c == '_' || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) {
			reader.backward(1);
			return getIdentifierOrKeyword();
		}

		// 数字
		if ('0' <= c && c <= '9') {
			reader.backward(1);
			return getNumber();
		} else if (c == '.' && !reader.eof()) {
			c = reader.read();
			if ('0' <= c && c <= '9') {
				reader.backward(2);
				return getNumber();
			} else {
				reader.backward(1);
			}
		}

		// 注释
		if (c == '/' && !reader.eof()) {
			c = reader.read();
			if (c == '/' || c == '*') {
				reader.backward(2);
				skipComment();
				return null;
			} else {
				reader.backward(1);
			}
		}

		// 分隔符：标点、运算符
		reader.backward(1);
		return getSeparator();
	}

	// 警告信息
	void warning(String s) {
		System.err.println(s);
	}

	private Token getSeparator() {
		assert !reader.eof();
		for (int i = 0, len = SEPARATORS3.length; i < len; ++i)
			if (reader.preMatch(SEPARATORS3[i]))
				return new Token(TokenType.SEPARATOR, SEPARATORS3[i]);

		for (int i = 0, len = SEPARATORS2.length; i < len; ++i)
			if (reader.preMatch(SEPARATORS2[i]))
				return new Token(TokenType.SEPARATOR, SEPARATORS2[i]);

		for (int i = 0, len = SEPARATORS1.length; i < len; ++i)
			if (reader.preMatch(SEPARATORS1[i]))
				return new Token(TokenType.SEPARATOR, SEPARATORS1[i]);

		return null;
	}

	private void skipComment() {
		if (reader.preMatch("//")) {
			// 行注释
			reader.skip(2);
			while (!reader.eof()) {
				char c = reader.read();
				if (c == '\n') {
					if (!reader.eof() && reader.read() != '\r')
						reader.backward(1);
					return;
				} else if (c == '\r') {
					if (!reader.eof() && reader.read() != '\n')
						reader.backward(1);
					return;
				}
			}
			return;
		} else if (reader.preMatch("/*")) {
			// 块注释
			reader.skip(2);
			while (!reader.eof()) {
				if (reader.preMatch("*/")) {
					reader.skip(2);
					return;
				}
				reader.skip(1);
			}
			throw new ScannerException("need an end of block comment");
		}
	}

	// 处理整数部分
	private long getInteger() {
		long v = 0;
		while (!reader.eof()) {
			char c = reader.read();
			if ('0' <= c && c <= '9') {
				v = v * 10 + (c - '0');
			} else {
				reader.backward(1);
				break;
			}
		}
		return v;
	}

	// 处理小数点后面的数字
	private double getDecimal() {
		double v = 0;
		int w = 0;
		while (!reader.eof()) {
			++w;
			char c = reader.read();
			if ('0' <= c && c <= '9') {
				v += (c - '0') / Math.pow(10, w);
			} else {
				reader.backward(1);
				break;
			}
		}
		return v;
	}

	// 处理整数、浮点数、科学计数法
	private Token getNumber() {
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
			reader.backward(1);
			i = getInteger();
			if (!reader.eof()) {
				c = reader.read();
				if (c != '.') {
					reader.backward(1);
				} else {
					d = getDecimal();
					float_value = true;
				}
			}
		}

		if (!reader.eof()) {
			c = reader.read();
			if (c != 'e' && c != 'E') {
				reader.backward(1);
			} else {
				e = getInteger();
				float_value = true;
			}
		}

		boolean short_value = !float_value;
		if (!reader.eof()) {
			c = reader.read();
			if (!float_value && (c == 'l' || c == 'L')) {
				short_value = false;
			} else if (float_value && (c == 'f' || c == 'F')) {
				short_value = true;
			} else {
				reader.backward(1);
			}
		}

		if (float_value) {
			double v = (i + d) * Math.pow(10, e);
			if (short_value)
				return new Token(TokenType.FLOAT, Float.valueOf((float) v));
			else
				return new Token(TokenType.DOUBLE, Double.valueOf(v));
		} else {
			if (short_value) {
				if (i != (int) i)
					warning("integer value overflow");
				return new Token(TokenType.INTEGER, Integer.valueOf((int) i));
			} else {
				return new Token(TokenType.LONG_INT, Long.valueOf(i));
			}
		}
	}

	// 处理16进制数
	private Token getHexNumber() {
		assert reader.preMatch("0x");
		reader.skip(2);

		if (reader.eof())
			throw new ScannerException("hex number expected");

		long v = 0;
		boolean long_value = false, over_flow = false;
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
			} else if (c == 'l' || c == 'L') {
				long_value = true;
				break;
			} else {
				break;
			}
			if (((v << 4) >> 4) != v)
				over_flow = true;
			v = (v << 4) + x;
		}
		if (!long_value && ((int) v) != v)
			over_flow = true;
		if (over_flow)
			warning("const hex number overflow");

		if (long_value)
			return new Token(TokenType.LONG_INT, Long.valueOf(v));
		else
			return new Token(TokenType.INTEGER, Integer.valueOf((int) v));
	}

	// 处理转义字符
	private char convertChar() {
		if (reader.eof())
			throw new ScannerException("end of string expected");
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
					reader.backward(1);
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
					reader.backward(1);
					break;
				}
			}
			return c;

		default:
			throw new ScannerException("unknow char convertion in string");
		}
	}

	// 处理字符串
	private Token getString() {
		assert !reader.eof();
		char c = reader.read();
		assert c == '\"' || c == '\'';

		final char string_dec = c;
		if (reader.eof())
			throw new ScannerException("end of string expected");

		c = reader.read();
		StringBuilder sb = new StringBuilder();
		if (c == string_dec) { // 字符串块
			if (reader.eof() || reader.read() != string_dec)
				throw new ScannerException("string block expected");

			final String str_end = "" + string_dec + string_dec + string_dec;
			while (true) {
				if (reader.eof())
					throw new ScannerException("string block expected");

				if (reader.preMatch(str_end)) {
					reader.skip(3);
					break;
				}

				c = reader.read();
				if (c == '\n') {
					if (!reader.eof() && reader.read() != '\r')
						reader.backward(1);
					sb.append('\n');
				} else if (c == '\r') {
					if (!reader.eof() && reader.read() != '\n')
						reader.backward(1);
					sb.append('\n');
				} else {
					sb.append(c);
				}
			}
			String s = sb.toString();
			return new Token(TokenType.STRING, s);
		}

		// 单一字符串
		while (true) {
			if (reader.eof())
				throw new ScannerException("end of string expected");

			c = reader.read();
			if (c == '\\') {
				sb.append(convertChar());
			} else if (c == '\n' || c == '\r') {
				throw new ScannerException("unexpected line end in string");
			} else if (c == string_dec) {
				break;
			} else {
				sb.append(c);
			}
		}

		String s = sb.toString();
		return new Token(TokenType.STRING, s);
	}

	private static boolean isFirstIdentifierChar(char c) {
		return c == '_' || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
	}

	private static boolean isIdentifierChar(char c) {
		return c == '_' || ('a' <= c && c <= 'Z') || ('A' <= c && c <= 'Z')
				|| ('0' <= c && c <= '9');
	}

	// 处理标识符、关键字、布尔常量
	private Token getIdentifierOrKeyword() {
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
				reader.backward(1);
				break;
			}
		}

		String i = sb.toString();
		if (i.equals("true"))
			return new Token(TokenType.BOOLEAN, Boolean.TRUE);
		else if (i.equals("false"))
			return new Token(TokenType.BOOLEAN, Boolean.FALSE);
		else if (KEY_WORDS.contains(i))
			return new Token(TokenType.KEY_WORD, i);
		return new Token(TokenType.IDENTIFIER, i);
	}

	// 处理文本模板
	private Token getTextTemplate() {
		StringBuilder sb = new StringBuilder();
		while (true) {
			if (reader.eof() || reader.preMatch(BLOCK_CODE_END)
					|| reader.preMatch(LINE_CODE_START))
				break;
			sb.append(reader.read());
		}

		assert sb.length() > 0;
		String t = sb.toString();
		return new Token(TokenType.TEXT_TEMPLATE, t);
	}
}

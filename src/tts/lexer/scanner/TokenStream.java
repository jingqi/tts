package tts.lexer.scanner;

import java.io.IOException;
import java.util.Stack;

import tts.lexer.scanner.Token.TokenType;

public class TokenStream {

	private TokenScanner scanner;
	private Stack<Token> readed = new Stack<Token>();
	private Stack<Token> unreaded = new Stack<Token>();

	public TokenStream(TokenScanner scanner) {
		this.scanner = scanner;
	}

	public Token nextToken() {
		Token ret = null;
		if (unreaded.size() > 0) {
			ret = unreaded.pop();
		} else {
			try {
				ret = scanner.nextToken();
			} catch (IOException e) {
			}
		}
		if (ret != null)
			readed.push(ret);
		return ret;
	}

	public String getFile() {
		Token t = nextToken();
		if (t == null)
			return scanner.getFile();
		putBack();
		return t.file;
	}

	public int getLine() {
		Token t = nextToken();
		if (t == null)
			return scanner.getLine();
		putBack();
		return t.line;
	}

	public int tell() {
		return readed.size();
	}

	public void seek(int pos) {
		if (pos < 0)
			throw new IndexOutOfBoundsException();
		if (pos < readed.size()) {
			for (int i = readed.size(); i > pos; --i)
				unreaded.push(readed.pop());
			return;
		} else if (pos <= readed.size() + unreaded.size()) {
			for (int i = readed.size(); i < pos; ++i)
				readed.push(unreaded.pop());
			return;
		}

		throw new IllegalStateException();
	}

	public boolean eof() {
		if (nextToken() == null)
			return true;
		putBack();
		return false;
	}

	public void putBack() {
		if (readed.size() == 0)
			throw new IllegalStateException();
		unreaded.push(readed.pop());
	}

	public void putBack(int count) {
		for (int i = 0; i < count; ++i)
			putBack();
	}

	public Token match(TokenType type) {
		Token t = nextToken();
		if (t == null)
			return null;

		if (t.type != type) {
			putBack();
			return null;
		}
		return t;
	}

	public Token match(TokenType type, Object value) {
		Token t = nextToken();
		if (t == null)
			return null;

		if (t.type != type || !t.value.equals(value)) {
			putBack();
			return null;
		}
		return t;
	}
}

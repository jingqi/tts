package tts.scanner;

import java.io.IOException;
import java.util.Stack;

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

	public void putBack() {
		if (readed.size() == 0)
			throw new IllegalStateException();
		unreaded.push(readed.pop());
	}

	public void putBack(int count) {
		for (int i = 0; i < count; ++i)
			putBack();
	}
}

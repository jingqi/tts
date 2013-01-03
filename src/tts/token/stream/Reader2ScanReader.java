package tts.token.stream;

import java.io.IOException;
import java.io.Reader;

import tts.util.CharStack;

public class Reader2ScanReader implements IScanReader {

	Reader reader;
	CharStack readed = new CharStack();
	CharStack unreaded = new CharStack();

	public Reader2ScanReader(Reader reader) {
		this.reader = reader;
	}

	@Override
	public char read() throws IOException {
		char c;
		if (unreaded.size() > 0) {
			c = unreaded.pop();
		} else {
			int r = reader.read();
			if (r < 0)
				throw new IOException();
			c = (char) r;
		}

		readed.push(c);
		return c;
	}

	@Override
	public char[] read(int len) throws IOException {
		char[] ret = new char[len];
		for (int i = 0; i < len; ++i)
			ret[i] = read();
		return ret;
	}

	@Override
	public void read(char[] buf, int buf_begin, int len) throws IOException {
		for (int i = 0; i < len; ++i)
			buf[buf_begin + i] = read();
	}

	@Override
	public String readString(int len) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; ++i)
			sb.append(read());
		return sb.toString();
	}

	@Override
	public boolean preMatch(String s) {
		if (!match(s))
			return false;

		try {
			putBack(s.length());
		} catch (IOException e) {
			throw new RuntimeException("不可能发生");
		}
		return true;
	}

	@Override
	public boolean match(String s) {
		for (int i = 0; i < s.length(); ++i) {
			char c;
			try {
				c = read();
			} catch (IOException e) {
				try {
					putBack(i);
				} catch (IOException e1) {
					throw new IllegalStateException("不可能发生");
				}
				return false;
			}

			if (c != s.charAt(i)) {
				try {
					putBack(i + 1);
				} catch (IOException e) {
					throw new IllegalStateException("不可能发生");
				}
				return false;
			}
		}
		return true;
	}

	@Override
	public void skip(int len) throws IOException {
		for (int i = 0; i < len; ++i)
			read();
	}

	@Override
	public boolean eof() {
		if (unreaded.size() > 0)
			return false;

		int i = 0;
		try {
			i = reader.read();
		} catch (IOException e) {
			return true;
		}

		if (i < 0)
			return true;

		unreaded.push((char) i);
		return false;
	}

	@Override
	public void putBack() throws IOException {
		putBack(1);
	}

	@Override
	public void putBack(int len) throws IOException {
		if (len > readed.size())
			throw new IOException();
		for (int i = 0; i < len; ++i)
			unreaded.push(readed.pop());
	}

	@Override
	public int tell() {
		return readed.size();
	}

	@Override
	public void seek(int pos) throws IOException {
		if (pos < 0)
			throw new IOException();

		if (pos < readed.size()) {
			for (int i = readed.size(); i > pos; --i) {
				unreaded.push(readed.pop());
			}
			return;
		} else if (pos <= readed.size() + unreaded.size()) {
			for (int i = readed.size(); i < pos; ++i) {
				readed.push(unreaded.pop());
			}
			return;
		}

		throw new IOException();

	}
}

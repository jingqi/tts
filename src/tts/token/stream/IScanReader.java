package tts.token.stream;

import java.io.IOException;

public interface IScanReader {

	char read() throws IOException;

	char[] read(int len) throws IOException;

	void read(char[] buf, int buf_begin, int len) throws IOException;

	String readString(int len) throws IOException;

	/**
	 * 前向匹配，指针不动
	 */
	boolean preMatch(String s);

	void skip(int len) throws IOException;

	int tell();

	void seek(int pos) throws IOException;

	boolean eof();

	/**
	 * 回退字符
	 */
	void backward(int len) throws IOException;
}

package tts.stream;

public interface IScanReader {

	char read();

	char[] read(int len);

	void read(char[] buf, int buf_begin, int len);

	String readString(int len);

	/**
	 * 前向匹配，指针不动
	 */
	boolean preMatch(String s);

	int available();

	int tell();

	void seek(int off);

	void skip(int len);

	boolean eof();

	/**
	 * 回退字符
	 */
	void backward(int len);
}

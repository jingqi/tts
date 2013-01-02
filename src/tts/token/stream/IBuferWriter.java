package tts.token.stream;

import java.io.IOException;

public interface IBuferWriter {

	void insert(char c);

	void insert(char[] s);

	void insert(String s);

	void insert(char[] s, int s_begin, int len);

	void insert(String s, int s_begin, int len);

	void insertEmpty(int len);

	void write(char c);

	void write(char[] s);

	void write(String s);

	void write(char[] s, int s_being, int len);

	void write(String s, int s_begin, int len);

	void erase(int len);

	int tell();

	void seek(int off) throws IOException;

	void skip(int len) throws IOException;

	boolean eof();

	void putback(int len);

	void setLength(int len);
}

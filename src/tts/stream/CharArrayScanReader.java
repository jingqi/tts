package tts.stream;

public class CharArrayScanReader implements IScanReader, IBuferWriter {

	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	private char[] buffer;
	private int size;
	private int index;

	public CharArrayScanReader() {
		this(DEFAULT_INITIAL_CAPACITY);
	}

	public CharArrayScanReader(int initialCapacity) {
		buffer = new char[initialCapacity];
		size = 0;
		index = 0;
	}

	public CharArrayScanReader(char[] s) {
		buffer = new char[s.length];
		System.arraycopy(s, 0, buffer, 0, s.length);
		size = s.length;
		index = 0;
	}

	private void ensureCap(int new_size) {
		if (new_size <= buffer.length)
			return;

		int new_cap = buffer.length * 3 / 2;
		if (new_cap < new_size)
			new_cap = new_size;

		char[] new_buf = new char[new_cap];
		System.arraycopy(buffer, 0, new_buf, 0, size);
		buffer = new_buf;
	}

	@Override
	public void insert(char c) {
		ensureCap(size + 1);
		System.arraycopy(buffer, index, buffer, index + 1, size - index);
		buffer[index++] = c;
		++size;
	}

	@Override
	public void insert(char[] s) {
		insert(s, 0, s.length);
	}

	@Override
	public void insert(String s) {
		insert(s, 0, s.length());
	}

	@Override
	public void insert(char[] s, int s_begin, int len) {
		if (s_begin < 0 || len < 0 || s_begin + len > s.length)
			throw new IllegalArgumentException();

		ensureCap(size + len);
		System.arraycopy(buffer, index, buffer, index + len, size - index);
		System.arraycopy(s, s_begin, buffer, index, len);
		index += len;
		size += len;
	}

	@Override
	public void insert(String s, int s_begin, int len) {
		if (s_begin < 0 || len < 0 || s_begin + len > s.length())
			throw new IllegalArgumentException();

		ensureCap(size + len);
		System.arraycopy(buffer, index, buffer, index + len, size - index);
		for (int i = 0; i < len; ++i)
			buffer[index + i] = s.charAt(s_begin + i);
		size += len;
		index += len;
	}

	@Override
	public void insertEmpty(int len) {
		if (len < 0)
			throw new IllegalArgumentException();
		else if (len == 0)
			return;

		ensureCap(size + len);
		System.arraycopy(buffer, index, buffer, index + len, size - index);
		for (int i = 0; i < len; ++i)
			buffer[index + i] = 0;
		size += len;
		index += len;
	}

	@Override
	public void write(char c) {
		ensureCap(index + 1);
		buffer[index++] = c;
		if (index > size)
			size = index;
	}

	@Override
	public void write(char[] s) {
		write(s, 0, s.length);
	}

	@Override
	public void write(String s) {
		write(s, 0, s.length());
	}

	@Override
	public void write(char[] s, int s_begin, int len) {
		if (s_begin < 0 || len < 0 || s_begin + len > s.length)
			throw new IllegalArgumentException();

		ensureCap(index + len);
		System.arraycopy(s, s_begin, buffer, index, len);
		index += len;
		if (index > size)
			size = index;
	}

	@Override
	public void write(String s, int s_begin, int len) {
		if (s_begin < 0 || len < 0 || s_begin + len > s.length())
			throw new IllegalArgumentException();

		ensureCap(index + len);
		for (int i = 0; i < len; ++i)
			buffer[index + i] = s.charAt(s_begin + i);
		index += len;
		if (index > size)
			size = index;
	}

	@Override
	public void erase(int len) {
		if (len < 0 || index + len > size)
			throw new IllegalArgumentException();

		System.arraycopy(buffer, index + len, buffer, index, size - index - len);
		size -= len;
	}

	@Override
	public void setLength(int len) {
		if (len < 0)
			throw new IllegalArgumentException();

		if (len > size)
			ensureCap(len);
		size = len;
		if (index > size)
			index = len;
	}

	private void checkAvailable(int toRead) {
		if (index + toRead > size)
			throw new IllegalStateException("no data available");
	}

	@Override
	public char read() {
		checkAvailable(1);
		return buffer[index++];
	}

	@Override
	public char[] read(int len) {
		if (len < 0)
			throw new IllegalArgumentException();
		checkAvailable(len);

		char[] ret = new char[len];
		System.arraycopy(buffer, index, ret, 0, len);
		index += len;
		return ret;
	}

	@Override
	public void read(char[] buf, int buf_begin, int len) {
		if (buf_begin < 0 || len < 0 || buf_begin + len > buf.length)
			throw new IllegalArgumentException();
		checkAvailable(len);

		System.arraycopy(buffer, index, buf, buf_begin, len);
		index += len;
	}

	@Override
	public String readString(int len) {
		return new String(read(len));
	}

	@Override
	public boolean preMatch(String s) {
		if (available() < s.length())
			return false;
		for (int i = 0, len = s.length(); i < len; ++i)
			if (s.charAt(i) != buffer[index + i])
				return false;
		return true;
	}

	@Override
	public void backward(int len) {
		if (len < 0 || len > index)
			throw new IllegalArgumentException();
		index -= len;
	}

	public int available() {
		return size - index;
	}

	@Override
	public int tell() {
		return index;
	}

	@Override
	public void seek(int off) {
		if (off < 0 || off > size)
			throw new IllegalArgumentException();
		index = off;
	}

	@Override
	public void skip(int len) {
		if (len < 0 || len > available())
			throw new IllegalArgumentException();
		index += len;
	}

	@Override
	public boolean eof() {
		return available() == 0;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(100);
		int offset = index - 50;
		if (offset <= 0)
			offset = 0;
		else
			sb.append("...");

		String ap = "";
		int count = 100;
		if (count >= size - offset)
			count = size - offset;
		else
			ap = "...";

		sb.append(buffer, offset, count);
		sb.append(ap);
		return sb.toString();
	}
}

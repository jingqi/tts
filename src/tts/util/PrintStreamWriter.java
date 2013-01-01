package tts.util;

import java.io.*;

/**
 * 因为 OutputStreamWriter 和 PrintWriter 来包装 System.out 都没有输出，这里自己弄一个
 * 
 * @author jingqi
 * 
 */
public class PrintStreamWriter extends Writer {

	PrintStream ps;

	public PrintStreamWriter(PrintStream ps) {
		this.ps = ps;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		for (int i = 0; i < len; ++i)
			ps.print(cbuf[off + i]);
	}

	@Override
	public void flush() throws IOException {
		ps.flush();
	}

	@Override
	public void close() throws IOException {
		if (ps != System.out && ps != System.err)
			ps.close();
	}

}

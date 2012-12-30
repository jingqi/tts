package tts;

import java.io.*;

import tts.grammar.GrammarScanner;
import tts.scanner.TokenScanner;
import tts.scanner.TokenStream;
import tts.stream.CharArrayScanReader;

public class Main {

	private static void run(CharArrayScanReader ss, Writer output) {
		TokenScanner tsn = new TokenScanner(ss);
		TokenStream ts = new TokenStream(tsn);
		GrammarScanner gs = new GrammarScanner(ts);
		gs.evalAll();
	}

	public static void main(String[] args) throws IOException {
		// 处理参数
		String input = null, output = null;
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("-o")) {
				if (i + 1 == args.length) {
					System.out.println("output file needed");
					return;
				}

				++i;
				output = args[i];
			} else {
				if (input != null) {
					System.out.println("too much args");
					return;
				}
				input = args[i];
			}
		}

		// debug
		input = "e:\\test.txt";

		if (input == null) {
			System.out.println("no input file");
			return;
		} else if (!new File(input).exists()) {
			System.out.println("file not exists");
			return;
		}

		FileReader fr = new FileReader(input);
		CharArrayScanReader ss = new CharArrayScanReader();
		char[] buf = new char[4096];
		int readed = 0;
		while ((readed = fr.read(buf)) > 0) {
			ss.write(buf, 0, readed);
		}
		fr.close();
		ss.seek(0);

		Writer writer = null;
		if (output != null)
			writer = new FileWriter(output);
		else
			writer = new OutputStreamWriter(System.out);

		run(ss, writer);
	}
}

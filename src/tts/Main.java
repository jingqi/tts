package tts;

import java.io.*;

import tts.token.stream.CharArrayScanReader;
import tts.vm.ScriptEngine;

public class Main {

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

		// 输入端
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

		// 启动脚本
		ScriptEngine engine = new ScriptEngine();
		engine.setScriptInput(ss);
		if (output != null)
			engine.setTextOutput(new FileWriter(output));
		engine.run();
	}
}

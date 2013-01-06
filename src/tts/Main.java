package tts;

import java.io.*;

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
					System.out.println("too much arguments");
					return;
				}
				input = args[i];
			}
		}
		if (input == null) {
			System.out.println("no input file");
			return;
		} else if (!new File(input).exists()) {
			System.out.println("file not exists");
			return;
		}

		// 启动脚本
		ScriptEngine en = new ScriptEngine();
		if (output != null)
			en.setTextOutput(new OutputStreamWriter(
					new FileOutputStream(output), "UTF-8"));
		en.run(new File(input));
	}
}

package tts;

import java.io.*;

import tts.vm.ScriptVM;

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

		// 启动脚本
		ScriptVM vm = new ScriptVM();
		if (output != null)
			vm.setTextOutput(new FileWriter(output));
		vm.run(new File(input));
	}
}

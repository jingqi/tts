package tts;

import java.io.*;

import tts.vm.ScriptVM;

public class Main {

	private static void printInfo() {
		System.out.println("tts InputFile [-o OutputFile]");
	}

	public static void main(String[] args) throws IOException {
		// 处理参数
		String input = null, output = null;
		for (int i = 0, len = args.length; i < len; ++i) {
			if (args[i].equals("-o")) {
				if (i + 1 == args.length) {
					System.out.println("ERROR: Output file needed");
					printInfo();
					return;
				}
				output = args[++i];
			} else {
				if (input != null) {
					System.out.println("ERROR: Too much arguments");
					printInfo();
					return;
				}
				input = args[i];
			}
		}
		if (input == null) {
			System.out.println("ERROR: No input file");
			printInfo();
			return;
		} else if (!new File(input).exists()) {
			printInfo();
			System.out.println("ERROR: Input file does not exists");
			return;
		}

		// 启动脚本
		ScriptVM vm = new ScriptVM();
		if (output != null)
			vm.setTextOutput(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
		vm.run(new File(input));
	}
}

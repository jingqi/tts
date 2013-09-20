package tts;

import java.io.*;

public class Main {

	private static void printInfo() {
		System.out.println("tts InputFile [-o OutputFile]");
	}

	public static void main1(String[] args) throws IOException {/*
		// 检测目录中的测试文件
		String dir = "./test";
		File[] fs = new File(dir).listFiles();
		for (int i = 0; i < fs.length; ++i) {
			File f = fs[i];
			if (f.getName().endsWith(".tts")) {
				ScriptEngine en = new ScriptEngine();
				en.run(f);
				System.out.print('.');
			}
		}*/


		// 单独跑某个文件
		String s = "E:\\data\\workspace\\java\\grape\\others\\primeval_stack.tts";
		ScriptEngine en = new ScriptEngine();
		en.run(new File(s));

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
		ScriptEngine en;
		if (output != null)
			en = new ScriptEngine(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
		else
			en = new ScriptEngine();
		en.run(new File(input));
	}
}

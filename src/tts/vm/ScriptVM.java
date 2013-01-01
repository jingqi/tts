package tts.vm;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * 脚本运行虚拟机
 */
public class ScriptVM {

	Writer textOutput;
	Map<String, Variable> globalVars = new HashMap<String, Variable>();

	static class Frame {
		Map<String, Variable> localValues = new HashMap<String, Variable>();
	}

	List<Frame> frames = new ArrayList<Frame>();

	public ScriptVM(Writer textOutput) {
		this.textOutput = textOutput;
	}

	// 进入帧
	public void enterFrame() {
		frames.add(new Frame());
	}

	// 退出帧
	public void leaveFrame() {
		frames.remove(frames.size() - 1);
	}

	// 添加变量定义
	public void addVariable(String name, Variable var) {
		Map<String, Variable> pool;
		if (frames.size() == 0)
			pool = globalVars;
		else
			pool = frames.get(frames.size() - 1).localValues;

		if (pool.containsKey(name))
			throw new RuntimeException("variable + " + name + " redefined");

		pool.put(name, var);
	}

	// 获取变量的值
	public Variable getVariable(String name) {
		for (int i = frames.size() - 1; i >= 0; --i) {
			Map<String, Variable> pool = frames.get(i).localValues;
			if (pool.containsKey(name))
				return pool.get(name);
		}

		if (globalVars.containsKey(name))
			return globalVars.get(name);

		throw new RuntimeException("variable " + name + " not found");
	}

	// 设置文本输出流
	public void setTextOutput(Writer w) {
		textOutput = w;
	}

	// 输出文本
	public void writeText(String s) {
		try {
			textOutput.write(s);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

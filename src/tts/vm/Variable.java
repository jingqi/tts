package tts.vm;

import tts.eval.IValueEval;

/**
 * 变量
 */
public class Variable {

	String name;
	VarType type;
	IValueEval value;

	public Variable(String name, VarType type, IValueEval value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public VarType getType() {
		return type;
	}

	public IValueEval getValue() {
		return value;
	}

	public void setValue(IValueEval value) {
		this.value = value;
	}
}

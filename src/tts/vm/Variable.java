package tts.vm;

import tts.eval.IValueEval;

/**
 * 变量
 */
public class Variable {

	/**
	 * 变量类型
	 */
	public enum VarType {
		BOOLEAN("bool"),
		INTEGER("int"),
		DOUBLE("double"),
		STRING("string"),
		ARRAY("array"),
		FUNCTION("function"),
		MAP("map"),
		VAR("var");

		public final String name;

		private VarType(String name) {
			this.name = name;
		}
	}

	private final String name;
	private final VarType type;
	private IValueEval value;

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

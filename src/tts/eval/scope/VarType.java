package tts.eval.scope;

/**
 * 变量类型
 */
public enum VarType {
	BOOLEAN("bool"),
	INTEGER("int"),
	DOUBLE("double"),
	STRING("string"),
	ARRAY("array"),
	MAP("map"),
	FUNCTION("function"),
	VAR("var");

	public final String name;

	private VarType(String name) {
		this.name = name;
	}
}

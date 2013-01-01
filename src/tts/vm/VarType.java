package tts.vm;

/**
 * 变量类型
 */
public enum VarType {
	BOOLEAN("bool"), INTEGER("int"), DOUBLE("double"), STRING("string"), ARRAY(
			"array");

	public final String name;

	VarType(String name) {
		this.name = name;
	}
}

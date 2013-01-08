package tts.vm;

import java.util.ArrayList;

import tts.util.SourceLocation;

/**
 * 调用栈有效范围
 */
class Frame {

	// 调用栈位置
	SourceLocation sl;
	String module;

	// 局部范围
	ArrayList<Scope> scopes = new ArrayList<Scope>();

	public Frame(SourceLocation sl, String module) {
		this.sl = sl;
		this.module = module;
	}

	public Variable getVariable(String name) {
		for (int i = scopes.size() - 1; i >= 0; --i) {
			Variable ret = scopes.get(i).getVariable(name);
			if (ret != null)
				return ret;
		}
		return null;
	}

	public void enterScope() {
		scopes.add(new Scope());
	}

	public void leaveScope() {
		scopes.remove(scopes.size()  - 1);
	}
}

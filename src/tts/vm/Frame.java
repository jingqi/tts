package tts.vm;

import java.util.ArrayList;

import tts.eval.scope.EvalSlot;
import tts.eval.scope.Scope;
import tts.trace.*;
import tts.util.*;

/**
 * 线程调用栈帧
 */
public final class Frame {

	// 虚拟机
	private final ScriptVM vm;

	// 作用域栈，存放用户变量
	private final ArrayList<Scope> scopeStack = new ArrayList<Scope>();

	// 调用栈，存放调用路径
	private final ArrayList<RuntimeLocation> callingStack = new ArrayList<RuntimeLocation>();

	public Frame(ScriptVM vm) {
		this.vm = vm;
	}

	public ScriptVM getVM() {
		return vm;
	}

	/**
	 * scope 压栈
	 */
	public void pushScope(Scope s) {
		scopeStack.add(s);
	}

	/**
	 * scope 弹出栈
	 */
	public void popScope() {
		scopeStack.remove(scopeStack.size() - 1);
	}

	/**
	 * 当前 scope (最后一个压栈的 scope)
	 */
	public Scope currentScope() {
		return scopeStack.get(scopeStack.size() - 1);
	}

	/**
	 * 函数调用路径压栈
	 */
	public void pushFuncCalling(SourceLocation from, String toFunc) {
		callingStack.add(new RuntimeLocation(from, toFunc));
	}

	/**
	 * 函数调用路径弹出栈
	 */
	public void popFuncCalling() {
		callingStack.remove(callingStack.size() - 1);
	}

	/**
	 * 当前调用栈
	 */
	public CallingStack currentCallingStack(SourceLocation sl) {
	    ArrayList<RuntimeLocation> ret = new ArrayList<RuntimeLocation>();
	    for (int i = 0, s = callingStack.size(); i < s; ++i) {
	        String func = callingStack.get(i).funcName;
	        SourceLocation source = null;
	        if (i < s - 1)
	            source = callingStack.get(i + 1).sl;
	        else
	            source = sl;

	        ret.add(new RuntimeLocation(source, func));
	    }
	    return new CallingStack(ret);
	}

	public EvalSlot getVariable(String name) {
		return currentScope().getVariableUpward(name);
	}

	public void addVariable(String name, EvalSlot var, SourceLocation sl) {
		currentScope().addVariable(name, var, sl);
	}
}

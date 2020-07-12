/**
 * 
 */
package com.lndb.dwtool.erm.util;

import java.util.EmptyStackException;
import java.util.Stack;

/**
 * This stop watch allows nested time recordings based on the context it prints
 * the elapsed time
 * 
 * @author harsha07
 */
public final class StopWatch {
    private static class Context {
	private String context;
	private long startTime;

	public String getContext() {
	    return this.context;
	}

	public void setContext(String context) {
	    this.context = context;
	}

	public long getStartTime() {
	    return this.startTime;
	}

	public void setStartTime(long startTime) {
	    this.startTime = startTime;
	}

	private Context(String context, long startTime) {
	    super();
	    this.context = context;
	    this.startTime = startTime;
	}
    }

    private static final Stack<Context> stack = new Stack<Context>();

    public static void start() {
	long now = System.nanoTime();
	Throwable stackTrace = new Throwable().fillInStackTrace();
	StackTraceElement invoker = stackTrace.getStackTrace()[1];
	String contexKey = getContextKey(invoker);
	stack.push(new Context(contexKey, now));
    }

    public static void stop() {
	long now = System.nanoTime();
	Throwable stackTrace = new Throwable().fillInStackTrace();
	StackTraceElement invoker = stackTrace.getStackTrace()[1];
	String contexKey = getContextKey(invoker);

	try {
	    Context peek = stack.pop();
	    if (peek != null) {
		System.out.println("Time elapsed between " + peek.getContext() + " and " + contexKey + " is " + ((now - peek.getStartTime()) * 1E-6));
	    }
	} catch (EmptyStackException e) {
	    System.out.println("Starting context not found for " + contexKey);
	}

    }

    /**
     * @param invoker
     * @return
     */
    private static String getContextKey(StackTraceElement invoker) {
	String contexKey = invoker.getClassName().substring(invoker.getClassName().lastIndexOf('.') + 1) + ":" + invoker.getMethodName() + ":" + invoker.getLineNumber();
	return contexKey;
    }

    public static void stopAll() {
	while (!stack.isEmpty()) {
	    stop();
	}
    }
}

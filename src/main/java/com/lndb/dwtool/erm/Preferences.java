package com.lndb.dwtool.erm;

public final class Preferences {
    private static final ThreadLocal<Boolean> IGNORE_VIEWS = new ThreadLocal<Boolean>();

    public static void setIgnoreViews(boolean ignoreView) {
	IGNORE_VIEWS.set(ignoreView);
    }

    /**
     * By default Views are ignored
     * 
     * @return
     */
    public static boolean isIgnoreDBViews() {
	Boolean ignoreView = IGNORE_VIEWS.get();
	// default is true
	if (ignoreView == null) {
	    return true;
	}
	return ignoreView.booleanValue();
    }
}

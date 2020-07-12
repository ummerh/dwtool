package com.lndb.dwtool.erm.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

public final class StatementPool {
    private static ThreadLocal<HashMap<String, PreparedStatement>> pool = new ThreadLocal<HashMap<String, PreparedStatement>>();

    public static PreparedStatement get(String key) {
	if (pool.get() == null) {
	    return null;
	}
	return pool.get().get(key);
    }

    public static void release() throws SQLException {
	if (pool.get() == null) {
	    return;
	}
	Collection<PreparedStatement> pstmts = pool.get().values();
	for (PreparedStatement preparedStatement : pstmts) {
	    preparedStatement.close();
	}
	pool.get().clear();
    }

    public static void add(String key, PreparedStatement pstmt) {
	if (pool.get() == null) {
	    pool.set(new HashMap<String, PreparedStatement>());
	}
	pool.get().put(key, pstmt);
    }
}

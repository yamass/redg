package com.btc.redg.generator.testutil;

import org.h2.jdbcx.JdbcConnectionPool;

import javax.sql.DataSource;

public class DatabaseTestUtil {

	public static DataSource createH2DataSource(final String url,
												final String username,
												final String password) {
		return JdbcConnectionPool.create(url, username, password);
	}

}

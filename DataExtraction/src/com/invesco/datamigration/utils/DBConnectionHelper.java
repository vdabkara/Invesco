package com.invesco.datamigration.utils;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

import org.apache.log4j.Logger;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;


public class DBConnectionHelper {
	static Logger logger = Logger.getLogger(DBConnectionHelper.class);

	/**
	 * Function will establish Connection with Local Database and will return
	 * connection Object.
	 * 
	 * @return
	 */
	public static Connection getSourceConnection() throws Exception {
		Connection connection = null;
		try {
			//			System.setProperty("oracle.net.tsn_admin", "C:/ORACLE/PRODUCT/12.1.0/client_1/network/admin/sample/");
			/*
			 * Load all the required parameters from properties file DataBase
			 * Driver ClassName Database URL Database UserName Database Password
			 */
			String databaseURL = ApplicationProperties.getProperty("jdbc.databaseurl");
			String databaseUserName = ApplicationProperties.getProperty("jdbc.databaseUserName");
			String databasePassword = ApplicationProperties.getProperty("jdbc.databasePassword");
			/*
			 * Load Database Driver Class
			 */
			Driver myDriver = new oracle.jdbc.driver.OracleDriver();
			DriverManager.registerDriver( myDriver );
			//			Class.forName("oracle.jdbc.OracleDriver");
			// getSourceConnection Object
			connection = DriverManager.getConnection(databaseURL,databaseUserName, databasePassword);
			// set driver to null
			//			myDriver= null;
			// set databaseURL to null
			databaseURL = null;
			// set userName & password to null
			databaseUserName = null;
			databasePassword = null;
		} catch (Exception e) {
			Utilities.printStackTraceToLogs(DBConnectionHelper.class.getName(), "getSourceConnection()", e);
			// set connection to null
			connection = null;
		}
		return connection;
	}

	public static Connection getDestinationConnection()
	{
		Connection conn = null;
		try
		{
			/*
			 * Load all the required parameters from properties file DataBase
			 * Driver ClassName Database URL Database UserName Database Password
			 */
			String databaseURL = ApplicationProperties.getProperty("jdbc.dest.databaseurl");
			String databaseUserName= ApplicationProperties.getProperty("jdbc.dest.databaseUserName");
			String databasePassword = ApplicationProperties.getProperty("jdbc.dest.databasePassword");
			/*
			 * Load Database Driver Class
			 */
//			Driver myDriver = new com.microsoft.sqlserver.jdbc.SQLServerDriver();
//			DriverManager.registerDriver(myDriver);
			// getConnection Object
			if(null!=databaseUserName && !"".equals(databaseUserName) && null!=databasePassword && !"".equals(databasePassword))
			{
				logger.info("getDestinationConnection :: When DB Credentials are Provided.");
				conn = DriverManager.getConnection(databaseURL,databaseUserName, databasePassword);
			}
			else
			{
				logger.info("getDestinationConnection :: Only with DB URL.");
				conn = DriverManager.getConnection(databaseURL);
			}
			// set databaseURL to null
			databaseURL = null;
			databaseUserName = null;
			databasePassword = null;
//			myDriver = null;
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("getDestinationConnection() :: Exception :: >" + e.getMessage());
			// set connection to null
			conn = null;
			Utilities.printStackTraceToLogs(DBConnectionHelper.class.getName(), "getDestinationConnection()", e);
		}
		return conn;
	}
}
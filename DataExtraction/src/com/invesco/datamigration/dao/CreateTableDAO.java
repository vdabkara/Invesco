package com.invesco.datamigration.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.invesco.datamigration.utils.DBConnectionHelper;
import com.invesco.datamigration.utils.Utilities;



public class CreateTableDAO extends DBConnectionHelper{

	private static Logger logger = Logger.getLogger(CreateTableDAO.class);

	private static String tableNamePlaceholder="TABLE_NAME_PLACEHOLDER";

	/**
	 * Function will check whether DB Table Exists or not, if not will create table at run time
	 * @param tableName
	 * @param tableType
	 * @return
	 */
	public static boolean checkTableExists(String tableName, String tableType)
	{
		boolean bool = true;
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try
		{
			if(null!=tableName && !"".equals(tableName))
			{
				conn = getDestinationConnection();
				int tableStatus=0;
				String sql = "IF OBJECT_ID (N'"+tableName+"', N'U') IS NOT NULL \n" + 
						"   SELECT 1 AS res ELSE SELECT 0 AS res ";
				//                                                            logger.info("checkTableExists :: Sql :: >"+ sql);
				stmt = conn.createStatement();
				rs=stmt.executeQuery(sql);
				if(rs.next())
				{
					tableStatus = rs.getInt("res");
				}
				sql = null;
				stmt.close();stmt=null;
				rs.close();rs=null;

				if(tableStatus==0)
				{
					logger.info("checkTableExists :: "+tableName+" does not exists. Proceed for creating Table.");
					// read vehicle Table create query
					InputStream is = null;
					String query="";
					try
					{
						is = CreateTableDAO.class.getResourceAsStream("/com/invesco/datamigration/sqls/"+tableType+"_TABLE.sql");
						if(null!=is)
						{
							// convert to string
							query = Utilities.readInputStramToString(is);
						}
					}
					catch(Exception e)
					{
						Utilities.printStackTraceToLogs(CreateTableDAO.class.getName(), "checkTableExists()", e);
					}
					finally
					{
						if(null!=is)
						{
							is.close();
						}
					}
					is = null;

					if(null!=query && !"".equals(query))
					{
						query=query.replace(tableNamePlaceholder, tableName);
						stmt = conn.createStatement();
						stmt.executeUpdate(query);
						logger.info("checkTableExists :: "+tableName+" Created Successfully. Proceed for dumping data in Table.");
					}
					else
					{
						logger.info("checkTableExists :: Failed to Read Create Table query for "+tableType+"_TABLE.sql. Cannot create table.");
						bool = false;
					}
					query = null;
				}
				else
				{
					logger.info("checkTableExists :: "+tableName+" already exists. Proceed for dumping data in Table.");
				}
			}
			else
			{
				logger.info("checkTableExists :: Table Name as parameter is null.");
				bool = false;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CreateTableDAO.class.getName(), "checkTableExists()", e);
			bool = false;
		}
		finally
		{
			try
			{
				if(null!=stmt)
					stmt.close();
				if(null!=rs)
					rs.close();
				if(null!=conn)
					conn.close();
				stmt=null;
				rs=null;
				conn = null;
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(CreateTableDAO.class.getName(), "checkTableExists()", e);
			}
		}
		return bool;
	}
}

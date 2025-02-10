//this is the previous one for the sqlite database
//package fr.isen.java2.db.daos;
//
//import javax.sql.DataSource;
//
//import org.sqlite.SQLiteDataSource;
//
//public class DataSourceFactory {
//
//	private static SQLiteDataSource dataSource;
//
//	private DataSourceFactory() {
//		// This is a static class that should not be instantiated.
//		// Here's a way to remember it when this class will have 2K lines and you come
//		// back to it in 2 years
//		throw new IllegalStateException("This is a static class that should not be instantiated");
//	}
//
//	/**
//	 * @return a connection to the SQLite Database
//	 *
//	 */
//	public static DataSource getDataSource() {
//		if (dataSource == null) {
//			dataSource = new SQLiteDataSource();
//			dataSource.setUrl("jdbc:sqlite:sqlite.db");
//		}
//		return dataSource;
//	}
//}



//tried the bonus side

package fr.isen.java2.db.daos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DataSourceFactory {


	public static Connection getConnection() throws SQLException {
		String url = "jdbc:sqlite:sqlite.db";
		return DriverManager.getConnection(url);
	}
}
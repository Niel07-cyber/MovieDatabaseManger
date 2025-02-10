package fr.isen.java2.db.daos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import fr.isen.java2.db.entities.Genre;

public class GenreDao {
	/**
	 * Retrieves a list of all genres from the database.
	 *
	 * @return a list of {@link Genre} objects.
	 * @throws RuntimeException if there is an error during database access.
	 */

	public List<Genre> listGenres() {
		List<Genre> genres = new ArrayList<>();
		String sqlStatement = "SELECT * FROM genre";
			 try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = connection.prepareStatement(sqlStatement);
			 ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				genres.add(new Genre(resultSet.getInt("idgenre"), resultSet.getString("name")));
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listing genre into database");
		}

		return genres;
	}




	/**
	 * Retrieves a genre by its name.
	 *
	 * @param name the name of the genre to retrieve.
	 * @return a {@link Genre} object if found, otherwise null.
	 * @throws RuntimeException if there is an error during database access.
	 */


	public Genre getGenre(String name) {
		String sqlStatement = "SELECT * FROM genre WHERE name = ?";

		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = connection.prepareStatement("SELECT * FROM genre WHERE name = ?")) {
			statement.setString(1, name);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					Genre genre = new Genre(resultSet.getInt("idgenre"), resultSet.getString("name"));
					return genre;
				}
			}
		}   catch (SQLException e) {
			 throw new RuntimeException(e);
		}
		return null;
	}



	/**
	 * Adds a new genre to the database.
	 *
	 * @param name the name of the genre to add.
	 * @throws RuntimeException if there is an error during database access.
	 */



	public void addGenre(String name) {
		String sqlStatement = "INSERT INTO genre(name) VALUES(?)";
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = connection.prepareStatement(sqlStatement, PreparedStatement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, name);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error adding genre into database" );
		}
	}
}
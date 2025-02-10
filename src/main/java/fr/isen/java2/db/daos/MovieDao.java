package fr.isen.java2.db.daos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import fr.isen.java2.db.entities.Genre;
import fr.isen.java2.db.entities.Movie;

public class MovieDao {
	/**
	 * Retrieves a list of all movies from the database, including their associated genres.
	 *
	 * @return a list of {@link Movie} objects.
	 * @throws RuntimeException if there is an error during database access.
	 */

	public List<Movie> listMovies() {
		List<Movie> movies = new ArrayList<>();
		String sqlStatement = "SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre";
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = connection.prepareStatement(sqlStatement);
			 ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				Genre genre = new Genre(resultSet.getInt("idgenre"), resultSet.getString("name"));
				movies.add(new Movie(
						resultSet.getInt("idmovie"),
						resultSet.getString("title"),
						resultSet.getTimestamp("release_date").toLocalDateTime().toLocalDate(),
						genre,
						resultSet.getInt("duration"),
						resultSet.getString("director"),
						resultSet.getString("summary")
				));

			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listing movies into database", e);
		}
		return movies;
	}




	/**
	 * Retrieves a list of movies filtered by a specific genre.
	 *
	 * @param genreName the name of the genre to filter movies by.
	 * @return a list of {@link Movie} objects that belong to the specified genre.
	 * @throws RuntimeException if there is an error during database access.
	 */


	public List<Movie> listMoviesByGenre(String genreName) {
		List<Movie> movies = new ArrayList<>();
		String sqlStatement = "SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre WHERE genre.name = ?";
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
			statement.setString(1, genreName);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					Genre genre = new Genre(resultSet.getInt("idgenre"), resultSet.getString("name"));
					movies.add(new Movie(
							resultSet.getInt("idmovie"),
							resultSet.getString("title"),
							resultSet.getTimestamp("release_date").toLocalDateTime().toLocalDate(),
							genre,
							resultSet.getInt("duration"),
							resultSet.getString("director"),
							resultSet.getString("summary")

					));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listing movies into database", e);
		}
		return movies;
	}



	/**
	 * Adds a new movie to the database.
	 *
	 * @param movie the {@link Movie} object to be added.
	 * @return the added {@link Movie} object with its generated ID.
	 * @throws RuntimeException if there is an error during database access.
	 */


	public Movie addMovie(Movie movie) {
		String sqlStatement = "INSERT INTO movie(title, release_date, genre_id, duration, director, summary) VALUES(?, ?, ?, ?, ?, ?)";
		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = connection.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, movie.getTitle());
			statement.setDate(2, java.sql.Date.valueOf(movie.getReleaseDate()));
			statement.setInt(3, movie.getGenre().getId());
			statement.setInt(4, movie.getDuration());
			statement.setString(5, movie.getDirector());
			statement.setString(6, movie.getSummary());
			statement.executeUpdate();


			int affectedRows = statement.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating movie failed, no rows affected.");
			}

			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					movie.setId(generatedKeys.getInt(1));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error adding movie into database", e);
		}
		return movie;
	}
}

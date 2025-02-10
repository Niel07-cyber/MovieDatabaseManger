package fr.isen.java2.db.daos;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import fr.isen.java2.db.entities.Genre;
import fr.isen.java2.db.entities.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MovieDaoTestCase {
	@BeforeEach
	public void initDb() throws Exception {
		try (Connection connection = DataSourceFactory.getConnection();
			 Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS genre (idgenre INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name VARCHAR(50) NOT NULL);");
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS movie ("
							+ "idmovie INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
							+ "title VARCHAR(100) NOT NULL, "
							+ "release_date DATETIME NULL, "
							+ "genre_id INT NOT NULL, "
							+ "duration INT NULL, "
							+ "director VARCHAR(100) NOT NULL, "
							+ "summary MEDIUMTEXT NULL, "
							+ "CONSTRAINT genre_fk FOREIGN KEY (genre_id) REFERENCES genre (idgenre));");
			stmt.executeUpdate("DELETE FROM movie");
			stmt.executeUpdate("DELETE FROM genre");
			stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='movie'");
			stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='genre'");

			try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO genre(idgenre,name) VALUES (?,?)")) {
				pstmt.setInt(1, 1);
				pstmt.setString(2, "Drama");
				pstmt.executeUpdate();
				pstmt.setInt(1, 2);
				pstmt.setString(2, "Comedy");
				pstmt.executeUpdate();
			}

			try (PreparedStatement pstmt = connection.prepareStatement(
					"INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) VALUES (?,?,?,?,?,?,?)")) {
				pstmt.setInt(1, 1);
				pstmt.setString(2, "Title 1");
				pstmt.setString(3, "2015-11-26 12:00:00.000");
				pstmt.setInt(4, 1);
				pstmt.setInt(5, 120);
				pstmt.setString(6, "director 1");
				pstmt.setString(7, "summary of the first movie");
				pstmt.executeUpdate();

				pstmt.setInt(1, 2);
				pstmt.setString(2, "My Title 2");
				pstmt.setString(3, "2015-11-14 12:00:00.000");
				pstmt.setInt(4, 2);
				pstmt.setInt(5, 114);
				pstmt.setString(6, "director 2");
				pstmt.setString(7, "summary of the second movie");
				pstmt.executeUpdate();

				pstmt.setInt(1, 3);
				pstmt.setString(2, "Third title");
				pstmt.setString(3, "2015-12-12 12:00:00.000");
				pstmt.setInt(4, 2);
				pstmt.setInt(5, 176);
				pstmt.setString(6, "director 3");
				pstmt.setString(7, "summary of the third movie");
				pstmt.executeUpdate();
			}
		}
	}

	@Test
	public void shouldListMovies() {
		// Given: A MovieDao instance with pre-populated database
		MovieDao movieDao = new MovieDao();

		// When: Retrieving all movies from the database
		List<Movie> movies = movieDao.listMovies();

		// Then: The list should contain exactly 3 movies with expected titles
		assertThat(movies)
				.isNotEmpty()
				.hasSize(3)
				.extracting(Movie::getTitle)
				.containsExactlyInAnyOrder("Title 1", "My Title 2", "Third title");

		// Additional assertions to ensure correct movie details
		assertThat(movies)
				.extracting(Movie::getDirector)
				.contains("director 1", "director 2", "director 3");
	}

	@Test
	public void shouldListMoviesByGenre() {
		// Arrange: Set up MovieDao instance
		MovieDao movieDao = new MovieDao();

		// Act: Retrieve movies of the "Comedy" genre
		List<Movie> movies = movieDao.listMoviesByGenre("Comedy");

		// Assert: Check the size and expected movie titles
		assertThat(movies)
				.isNotEmpty()
				.hasSize(2)
				.extracting(Movie::getTitle)
				.containsExactlyInAnyOrder("My Title 2", "Third title");

		// Additional assertions to ensure genre correctness
		assertThat(movies)
				.allSatisfy(movie -> assertThat(movie.getGenre().getName()).isEqualTo("Comedy"));
	}

	@Test
	public void shouldAddMovie() throws Exception {
		MovieDao movieDao = new MovieDao();
		Genre genre = new Genre(1, "Drama");

		// Create a new movie instance
		Movie movie = new Movie(null, "The Test Movie", LocalDate.of(2024, 3, 15), genre, 135, "Test Director", "A dramatic test movie");

		// Add movie to database
		Movie savedMovie = movieDao.addMovie(movie);

		// Check that the movie was correctly added
		assertThat(savedMovie).isNotNull();
		assertThat(savedMovie.getId()).isNotNull();
		assertThat(savedMovie.getTitle()).isEqualTo("The Test Movie");
		assertThat(savedMovie.getReleaseDate()).isEqualTo(LocalDate.of(2024, 3, 15));
		assertThat(savedMovie.getGenre().getId()).isEqualTo(1);
		assertThat(savedMovie.getDuration()).isEqualTo(135);
		assertThat(savedMovie.getDirector()).isEqualTo("Test Director");
		assertThat(savedMovie.getSummary()).isEqualTo("A dramatic test movie");

		// Verify in the database that it was persisted
		try (Connection connection = DataSourceFactory.getConnection();
			 PreparedStatement statement = connection.prepareStatement("SELECT * FROM movie WHERE idmovie = ?")) {
			statement.setInt(1, savedMovie.getId());

			try (ResultSet resultSet = statement.executeQuery()) {
				assertThat(resultSet.next()).isTrue();
				assertThat(resultSet.getString("title")).isEqualTo("The Test Movie");
				assertThat(resultSet.getString("director")).isEqualTo("Test Director");
				assertThat(resultSet.getString("summary")).isEqualTo("A dramatic test movie");
				assertThat(resultSet.getInt("duration")).isEqualTo(135);
				assertThat(resultSet.getInt("genre_id")).isEqualTo(1);
				assertThat(resultSet.next()).isFalse(); // Ensures no duplicate records
			}
		}
	}
}
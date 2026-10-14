package com.cinewave.repository;

import com.cinewave.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByActiveTrue();

    Optional<Movie> findByTmdbId(Long tmdbId);

    Optional<Movie> findByTitleIgnoreCase(String title);

    boolean existsByTmdbId(Long tmdbId);

    @Query("SELECT m FROM Movie m WHERE m.active = true AND " +
           "(LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.genre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.language) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.castInfo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.director) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Movie> searchMovies(@Param("search") String search);

    @Query("SELECT m FROM Movie m WHERE m.active = true " +
           "AND (:genre IS NULL OR m.genre = :genre) " +
           "AND (:language IS NULL OR m.language = :language)")
    List<Movie> findByFilters(@Param("genre") String genre,
                              @Param("language") String language);

    @Query("SELECT DISTINCT m.genre FROM Movie m WHERE m.active = true ORDER BY m.genre")
    List<String> findDistinctGenres();

    @Query("SELECT DISTINCT m.language FROM Movie m WHERE m.active = true ORDER BY m.language")
    List<String> findDistinctLanguages();

    List<Movie> findByTagAndActiveTrue(String tag);

    List<Movie> findByReleaseDateGreaterThanAndActiveTrueOrderByReleaseDateAsc(java.time.LocalDate date);

    List<Movie> findByReleaseDateLessThanEqualAndActiveTrueOrderByReleaseDateDesc(java.time.LocalDate date);

    List<Movie> findByOriginalLanguageIgnoreCaseAndActiveTrue(String originalLanguage);

    List<Movie> findByLanguageContainingIgnoreCaseAndActiveTrue(String language);

    List<Movie> findAllByActiveTrueOrderByVoteAverageDescVoteCountDesc();

    long countByActiveTrue();
}

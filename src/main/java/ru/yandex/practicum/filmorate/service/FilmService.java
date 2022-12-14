package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.films.FilmStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final DirectorService directorService;
    private final FeedService feedService;


    public Film getFilmById(long id) {
        return filmStorage.loadFilm(id)
                .orElseThrow(() -> new NotFoundException("**Film** #" + id + " not found."));
    }

    public Film createNewFilm(Film film) {
        long filmId = filmStorage.saveFilm(film);
        if (film.getGenres() != null && film.getGenres().size() > 0) {
            genreService.addGenresToFilm(filmId, film.getGenres());
        }
        if (film.getDirectors() != null && film.getDirectors().size() > 0) {
            directorService.addDirectorsToFilm(filmId, film.getDirectors());
        }
        Film savedFilm = getFilmById(filmId);
        log.debug("Creating new film {}.", savedFilm);
        return savedFilm;
    }

    public Film updateFilm(Film film) {
        Film updatedFilm = getFilmById(film.getId());
        if (film.getDescription() == null) {
            film.setDescription(updatedFilm.getDescription());
        }
        if (film.getReleaseDate() == null) {
            film.setReleaseDate(updatedFilm.getReleaseDate());
        }
        if (film.getDuration() == 0L) {
            film.setDuration(updatedFilm.getDuration());
        }
        if (film.getName() == null || film.getName().isBlank()) {
            film.setName(updatedFilm.getName());
        }
        if (film.getMpa() == null) {
            film.setMpa(updatedFilm.getMpa());
        }
        if (film.getGenres() == null) {
            film.setGenres(updatedFilm.getGenres());
        } else if (film.getGenres().size() == 0) {
            genreService.deleteFilmGenres(film.getId());
        } else {
            genreService.updateFilmGenres(film.getId(), film.getGenres());
        }
        if (film.getDirectors() == null || film.getDirectors().size() == 0) {
            directorService.deleteFilmDirectors(film.getId());
        } else {
            directorService.updateFilmDirectors(film.getId(), film.getDirectors());
        }
        filmStorage.updateFilm(film);
        Film savedFilm = getFilmById(film.getId());
        log.debug("Updating film {}.", savedFilm);
        return savedFilm;
    }

    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.loadFilms();
        log.debug("Loading {} films.", films.size());
        return films;
    }

    public void addLikeFromUser(long filmId, long userId) {
        getFilmById(filmId);
        userService.getUserById(userId);
        if (filmStorage.hasFilmLikeFromUser(filmId, userId)) {
            log.debug("Attempting to create an existing like for film #{} from user #{}.", filmId, userId);
        } else {
            filmStorage.saveLikeFromUser(filmId, userId);
            log.debug("Creating like for film #{} from user #{}.", filmId, userId);
            feedService.saveFeed(userId, filmId, EventType.LIKE, Operation.ADD);
        }
    }

    public void deleteLikeFromUser(long filmId, long userId) {
        getFilmById(filmId);
        userService.getUserById(userId);
        if (filmStorage.hasFilmLikeFromUser(filmId, userId)) {
            filmStorage.deleteLikeFromUser(filmId, userId);
            log.debug("Deleting like from film #{} from user #{}.", filmId, userId);
            feedService.saveFeed(userId, filmId, EventType.LIKE, Operation.REMOVE);
        } else {
            log.debug("Attempting to delete a non-existent like for film #{} from user #{}", filmId, userId);
        }
    }

    public List<Film> getPopularFilms(long count, Long genreId, Integer year) {
        List<Film> popular = filmStorage.loadPopularFilms(count, genreId, year);
        log.debug("Returning {} popular films.", popular.size());
        return popular;
    }

    public void deleteFilm(long filmId) {
        filmStorage.deleteFilm(filmId);
        log.debug("Deleting {} film.", filmId);
    }

    public List<Film> getSortedFilmsOfDirector(long directorId, String sortBy) {
        directorService.getDirectorById(directorId);
        switch (sortBy) {
            case "YEAR":
                List<Film> films = filmStorage.loadFilmsOfDirectorSortedByYears(directorId);
                log.debug("Returning {} films sorted by years.", films.size());
                return films;
            case "LIKES":
                films = filmStorage.loadFilmsOfDirectorSortedByLikes(directorId);
                log.debug("Returning {} films sorted by likes.", films.size());
                return films;
            default:
                throw new NotFoundException("Sorting not found.");
        }
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        List<Film> common = filmStorage.getCommonFilms(userId, friendId);
        log.debug("Returning {} common films.", common.size());
        return common;
    }

    public List<Film> searchFilm(String query, String by) {
        return filmStorage.searchFilm(query, by);

    }

    public List<Film> getRecommendation(long id) {
        userService.getUserById(id);
        List<Film> recommendationFilm = filmStorage.getRecommendation(id);
        log.debug("Recommendation {} films.", recommendationFilm.size());
        return recommendationFilm;
    }
}
package com.youngariy.mopick.network;

import com.youngariy.mopick.network.movies.Movie;
import com.youngariy.mopick.network.movies.MovieCastsOfPersonResponse;
import com.youngariy.mopick.network.movies.MovieCreditsResponse;
import com.youngariy.mopick.network.movies.NowShowingMoviesResponse;
import com.youngariy.mopick.network.movies.PopularMoviesResponse;
import com.youngariy.mopick.network.movies.SimilarMoviesResponse;
import com.youngariy.mopick.network.movies.TopRatedMoviesResponse;
import com.youngariy.mopick.network.movies.UpcomingMoviesResponse;
import com.youngariy.mopick.network.people.Person;
import com.youngariy.mopick.network.tvshows.AiringTodayTVShowsResponse;
import com.youngariy.mopick.network.tvshows.OnTheAirTVShowsResponse;
import com.youngariy.mopick.network.tvshows.PopularTVShowsResponse;
import com.youngariy.mopick.network.tvshows.SimilarTVShowsResponse;
import com.youngariy.mopick.network.tvshows.TVCastsOfPersonResponse;
import com.youngariy.mopick.network.tvshows.TVShow;
import com.youngariy.mopick.network.tvshows.TVShowCreditsResponse;
import com.youngariy.mopick.network.tvshows.TopRatedTVShowsResponse;
import com.youngariy.mopick.network.videos.VideosResponse;
import com.youngariy.mopick.network.watchproviders.WatchProvidersResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by hitanshu on 27/7/17.
 */

public interface ApiInterface {

    //MOVIES

    @GET("movie/now_playing")
    Call<NowShowingMoviesResponse> getNowShowingMovies(@Query("api_key") String apiKey, @Query("page") Integer page, @Query("region") String region, @Query("language") String language);

    @GET("movie/popular")
    Call<PopularMoviesResponse> getPopularMovies(@Query("api_key") String apiKey, @Query("page") Integer page, @Query("region") String region, @Query("language") String language);

    @GET("movie/upcoming")
    Call<UpcomingMoviesResponse> getUpcomingMovies(@Query("api_key") String apiKey, @Query("page") Integer page, @Query("region") String region, @Query("language") String language);

    @GET("movie/top_rated")
    Call<TopRatedMoviesResponse> getTopRatedMovies(@Query("api_key") String apiKey, @Query("page") Integer page, @Query("region") String region, @Query("language") String language);

    @GET("movie/{id}")
    Call<Movie> getMovieDetails(@Path("id") Integer movieId, @Query("api_key") String apiKey, @Query("language") String language);

    @GET("movie/{id}/videos")
    Call<VideosResponse> getMovieVideos(@Path("id") Integer movieId, @Query("api_key") String apiKey, @Query("language") String language);

    @GET("movie/{id}/credits")
    Call<MovieCreditsResponse> getMovieCredits(@Path("id") Integer movieId, @Query("api_key") String apiKey);

    @GET("movie/{id}/similar")
    Call<SimilarMoviesResponse> getSimilarMovies(@Path("id") Integer movieId, @Query("api_key") String apiKey, @Query("page") Integer page, @Query("language") String language);

    @GET("movie/{id}/watch/providers")
    Call<WatchProvidersResponse> getMovieWatchProviders(@Path("id") Integer movieId, @Query("api_key") String apiKey);

    @GET("genre/movie/list")
    Call<com.youngariy.mopick.network.movies.GenresList> getMovieGenresList(@Query("api_key") String apiKey, @Query("language") String language);

    //TV SHOWS

    @GET("tv/airing_today")
    Call<AiringTodayTVShowsResponse> getAiringTodayTVShows(@Query("api_key") String apiKey, @Query("page") Integer page, @Query("language") String language);

    @GET("tv/on_the_air")
    Call<OnTheAirTVShowsResponse> getOnTheAirTVShows(@Query("api_key") String apiKey, @Query("page") Integer page, @Query("language") String language);

    @GET("tv/popular")
    Call<PopularTVShowsResponse> getPopularTVShows(@Query("api_key") String apiKey, @Query("page") Integer page, @Query("language") String language);

    @GET("tv/top_rated")
    Call<TopRatedTVShowsResponse> getTopRatedTVShows(@Query("api_key") String apiKey, @Query("page") Integer page, @Query("language") String language);

    @GET("tv/{id}")
    Call<TVShow> getTVShowDetails(@Path("id") Integer tvShowId, @Query("api_key") String apiKey, @Query("language") String language);

    @GET("tv/{id}/videos")
    Call<VideosResponse> getTVShowVideos(@Path("id") Integer movieId, @Query("api_key") String apiKey, @Query("language") String language);

    @GET("tv/{id}/credits")
    Call<TVShowCreditsResponse> getTVShowCredits(@Path("id") Integer movieId, @Query("api_key") String apiKey);

    @GET("tv/{id}/similar")
    Call<SimilarTVShowsResponse> getSimilarTVShows(@Path("id") Integer movieId, @Query("api_key") String apiKey, @Query("page") Integer page, @Query("language") String language);

    @GET("tv/{id}/watch/providers")
    Call<WatchProvidersResponse> getTVShowWatchProviders(@Path("id") Integer tvShowId, @Query("api_key") String apiKey);

    @GET("genre/tv/list")
    Call<com.youngariy.mopick.network.tvshows.GenresList> getTVShowGenresList(@Query("api_key") String apiKey, @Query("language") String language);

    //PERSON

    @GET("person/{id}")
    Call<Person> getPersonDetails(@Path("id") Integer personId, @Query("api_key") String apiKey, @Query("language") String language);

    @GET("person/{id}/movie_credits")
    Call<MovieCastsOfPersonResponse> getMovieCastsOfPerson(@Path("id") Integer personId, @Query("api_key") String apiKey, @Query("language") String language);

    @GET("person/{id}/tv_credits")
    Call<TVCastsOfPersonResponse> getTVCastsOfPerson(@Path("id") Integer personId, @Query("api_key") String apiKey, @Query("language") String language);

    //SEARCH

    @GET("search/multi")
    Call<com.youngariy.mopick.network.search.SearchResponse> search(@Query("api_key") String apiKey, @Query("query") String query, @Query("page") Integer page, @Query("language") String language);

}





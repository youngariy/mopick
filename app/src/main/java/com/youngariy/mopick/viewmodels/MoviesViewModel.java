package com.youngariy.mopick.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.youngariy.mopick.network.ApiClient;
import com.youngariy.mopick.network.ApiInterface;
import com.youngariy.mopick.network.movies.Genre;
import com.youngariy.mopick.network.movies.GenresList;
import com.youngariy.mopick.network.movies.MovieBrief;
import com.youngariy.mopick.network.movies.NowShowingMoviesResponse;
import com.youngariy.mopick.network.movies.PopularMoviesResponse;
import com.youngariy.mopick.network.movies.TopRatedMoviesResponse;
import com.youngariy.mopick.network.movies.UpcomingMoviesResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoviesViewModel extends ViewModel {

    private final MutableLiveData<List<Genre>> genresList = new MutableLiveData<>();
    private final MutableLiveData<List<MovieBrief>> nowShowingMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MovieBrief>> popularMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MovieBrief>> upcomingMovies = new MutableLiveData<>();
    private final MutableLiveData<List<MovieBrief>> topRatedMovies = new MutableLiveData<>();
    private final MutableLiveData<Boolean> error = new MutableLiveData<>();

    public LiveData<List<Genre>> getGenresList() {
        return genresList;
    }

    public LiveData<List<MovieBrief>> getNowShowingMovies() {
        return nowShowingMovies;
    }

    public LiveData<List<MovieBrief>> getPopularMovies() {
        return popularMovies;
    }

    public LiveData<List<MovieBrief>> getUpcomingMovies() {
        return upcomingMovies;
    }

    public LiveData<List<MovieBrief>> getTopRatedMovies() {
        return topRatedMovies;
    }

    public LiveData<Boolean> getError() {
        return error;
    }

    public void loadAllMovies(String apiKey, String region) {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        apiService.getMovieGenresList(apiKey).enqueue(new Callback<GenresList>() {
            @Override
            public void onResponse(Call<GenresList> call, Response<GenresList> response) {
                if (response.isSuccessful()) {
                    genresList.postValue(response.body().getGenres());
                } else {
                    error.postValue(true);
                }
            }

            @Override
            public void onFailure(Call<GenresList> call, Throwable t) {
                error.postValue(true);
            }
        });

        apiService.getNowShowingMovies(apiKey, 1, region).enqueue(new Callback<NowShowingMoviesResponse>() {
            @Override
            public void onResponse(Call<NowShowingMoviesResponse> call, Response<NowShowingMoviesResponse> response) {
                if (response.isSuccessful()) {
                    nowShowingMovies.postValue(response.body().getResults());
                } else {
                    error.postValue(true);
                }
            }

            @Override
            public void onFailure(Call<NowShowingMoviesResponse> call, Throwable t) {
                error.postValue(true);
            }
        });

        apiService.getPopularMovies(apiKey, 1, region).enqueue(new Callback<PopularMoviesResponse>() {
            @Override
            public void onResponse(Call<PopularMoviesResponse> call, Response<PopularMoviesResponse> response) {
                if (response.isSuccessful()) {
                    popularMovies.postValue(response.body().getResults());
                } else {
                    error.postValue(true);
                }
            }

            @Override
            public void onFailure(Call<PopularMoviesResponse> call, Throwable t) {
                error.postValue(true);
            }
        });

        apiService.getUpcomingMovies(apiKey, 1, region).enqueue(new Callback<UpcomingMoviesResponse>() {
            @Override
            public void onResponse(Call<UpcomingMoviesResponse> call, Response<UpcomingMoviesResponse> response) {
                if (response.isSuccessful()) {
                    upcomingMovies.postValue(response.body().getResults());
                } else {
                    error.postValue(true);
                }
            }

            @Override
            public void onFailure(Call<UpcomingMoviesResponse> call, Throwable t) {
                error.postValue(true);
            }
        });

        apiService.getTopRatedMovies(apiKey, 1, region).enqueue(new Callback<TopRatedMoviesResponse>() {
            @Override
            public void onResponse(Call<TopRatedMoviesResponse> call, Response<TopRatedMoviesResponse> response) {
                if (response.isSuccessful()) {
                    topRatedMovies.postValue(response.body().getResults());
                } else {
                    error.postValue(true);
                }
            }

            @Override
            public void onFailure(Call<TopRatedMoviesResponse> call, Throwable t) {
                error.postValue(true);
            }
        });
    }
}





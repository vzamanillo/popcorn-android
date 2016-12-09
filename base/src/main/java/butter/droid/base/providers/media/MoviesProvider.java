/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.base.providers.media;

import android.support.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butter.droid.base.BuildConfig;
import butter.droid.base.R;
import butter.droid.base.providers.media.callback.MediaProviderCallback;
import butter.droid.base.providers.media.filters.Sort;
import butter.droid.base.providers.media.models.Genre;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.response.MovieResponse;
import butter.droid.base.providers.media.response.models.movies.Movie;
import butter.droid.base.providers.media.type.MediaProviderType;
import butter.droid.base.providers.subs.SubsProvider;
import okhttp3.Call;
import okhttp3.OkHttpClient;

public class MoviesProvider extends MediaProvider {

    public MoviesProvider(OkHttpClient client, ObjectMapper mapper, @Nullable SubsProvider subsProvider) {
        super(client, mapper, subsProvider, BuildConfig.MOVIE_URLS, "movies/", "", 0);
    }

    @Override
    public ArrayList<Media> getResponseFormattedList(String responseStr, ArrayList<Media> currentList) throws IOException {
        ArrayList<Media> formattedData = currentList;
        List<Movie> list = mapper.readValue(responseStr, mapper.getTypeFactory().constructCollectionType(List.class, Movie.class));
        if (!list.isEmpty()) {
            formattedData = new MovieResponse(list).formatListForPopcorn(currentList, this, getSubsProvider());
        }
        return formattedData;
    }

    @Override
    public Call getDetail(ArrayList<Media> currentList, Integer index, MediaProviderCallback callback) {
        ArrayList<Media> returnList = new ArrayList<>();
        returnList.add(currentList.get(index));
        callback.onSuccess(null, returnList, true);
        return null;
    }

    @Override
    public int getLoadingMessage() {
        return R.string.loading_movies;
    }

    @Override
    public List<Sort> getSortAvailable() {
        List<Sort> sortList = new ArrayList<>();
        sortList.add(Sort.TRENDING);
        sortList.add(Sort.POPULARITY);
        sortList.add(Sort.RATING);
        sortList.add(Sort.DATE);
        sortList.add(Sort.YEAR);
        sortList.add(Sort.ALPHABET);
        return sortList;
    }

    @Override
    public List<Genre> getGenres() {
        List<Genre> returnList = new ArrayList<>();
        returnList.add(new Genre("all", R.string.genre_all));
        returnList.add(new Genre("action", R.string.genre_action));
        returnList.add(new Genre("adventure", R.string.genre_adventure));
        returnList.add(new Genre("animation", R.string.genre_animation));
        returnList.add(new Genre("comedy", R.string.genre_comedy));
        returnList.add(new Genre("crime", R.string.genre_crime));
        returnList.add(new Genre("disaster", R.string.genre_disaster));
        returnList.add(new Genre("documentary", R.string.genre_documentary));
        returnList.add(new Genre("drama", R.string.genre_drama));
        returnList.add(new Genre("eastern", R.string.genre_eastern));
        returnList.add(new Genre("family", R.string.genre_family));
        returnList.add(new Genre("fantasy", R.string.genre_fantasy));
        returnList.add(new Genre("fan-film", R.string.genre_fan_film));
        returnList.add(new Genre("film-noir", R.string.genre_film_noir));
        returnList.add(new Genre("history", R.string.genre_history));
        returnList.add(new Genre("holiday", R.string.genre_holiday));
        returnList.add(new Genre("horror", R.string.genre_horror));
        returnList.add(new Genre("indie", R.string.genre_indie));
        returnList.add(new Genre("music", R.string.genre_music));
        returnList.add(new Genre("mystery", R.string.genre_mystery));
        returnList.add(new Genre("road", R.string.genre_road));
        returnList.add(new Genre("romance", R.string.genre_romance));
        returnList.add(new Genre("science-fiction", R.string.genre_sci_fi));
        returnList.add(new Genre("short", R.string.genre_short));
        returnList.add(new Genre("sports", R.string.genre_sport));
        returnList.add(new Genre("suspense", R.string.genre_suspense));
        returnList.add(new Genre("thriller", R.string.genre_thriller));
        returnList.add(new Genre("tv-movie", R.string.genre_tv_movie));
        returnList.add(new Genre("war", R.string.genre_war));
        returnList.add(new Genre("western", R.string.genre_western));
        return returnList;
    }

    @Override
    public MediaProviderType getProviderType() {
        return MediaProviderType.MOVIE;
    }
}

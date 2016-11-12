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

import android.accounts.NetworkErrorException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butter.droid.base.BuildConfig;
import butter.droid.base.ButterApplication;
import butter.droid.base.R;
import butter.droid.base.providers.media.models.Genre;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.providers.media.models.response.movies.Language;
import butter.droid.base.providers.media.models.response.movies.Quality;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.providers.subs.YSubsProvider;
import butter.droid.base.utils.StringUtils;
import timber.log.Timber;

public class MoviesProvider extends MediaProvider {

    private static Integer CURRENT_API = 0;
    private static final String[] API_URLS = BuildConfig.MOVIE_URLS;
    private static final MoviesProvider sMediaProvider = new MoviesProvider();
    private static final SubsProvider sSubsProvider = new YSubsProvider();

    @Override
    public Call getList(final ArrayList<Media> existingList, Filters filters, final Callback callback) {
        final ArrayList<Media> currentList;
        if (existingList == null) {
            currentList = new ArrayList<>();
        } else {
            currentList = new ArrayList<Media>(existingList);
        }

        ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
        params.add(new AbstractMap.SimpleEntry<String, String>("limit", "30"));

        if (filters == null) {
            filters = new Filters();
        }

        if (filters.keywords != null) {
            params.add(new AbstractMap.SimpleEntry<String, String>("keywords", filters.keywords));
        }

        if (filters.genre != null) {
            params.add(new AbstractMap.SimpleEntry<String, String>("genre", filters.genre));
        }

        if (filters.order == Filters.Order.ASC) {
            params.add(new AbstractMap.SimpleEntry<String, String>("order", "1"));
        } else {
            params.add(new AbstractMap.SimpleEntry<String, String>("order", "-1"));
        }

        if(filters.langCode != null) {
            params.add(new AbstractMap.SimpleEntry<String, String>("lang", filters.langCode));
        }

        String sort;
        switch (filters.sort) {
            default:
            case POPULARITY:
                sort = "popularity";
                break;
            case YEAR:
                sort = "year";
                break;
            case DATE:
                sort = "last added";
                break;
            case RATING:
                sort = "rating";
                break;
            case ALPHABET:
                sort = "name";
                break;
            case TRENDING:
                sort = "trending";
                break;
        }

        params.add(new AbstractMap.SimpleEntry<String, String>("sort", sort));

        String url = API_URLS[CURRENT_API] + "movies/";
        if (filters.page != null) {
            url += filters.page;
        } else {
            url += "1";
        }

        Request.Builder requestBuilder = new Request.Builder();
        String query = buildQuery(params);
        url = url + "?" + query;
        requestBuilder.url(url);
        requestBuilder.tag(MEDIA_CALL);

        Timber.d("MoviesProvider", "Making request to: " + url);

        return fetchList(currentList, requestBuilder, filters, callback);
    }

    /**
     * Fetch the list of movies from API
     *
     * @param currentList    Current shown list to be extended
     * @param requestBuilder Request to be executed
     * @param callback       Network callback
     * @return Call
     */
    private Call fetchList(final ArrayList<Media> currentList, final Request.Builder requestBuilder, final Filters filters, final Callback callback) {
        return enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                String url = requestBuilder.build().urlString();
                if (CURRENT_API >= API_URLS.length - 1) {
                    callback.onFailure(e);
                } else {
                    if(url.contains(API_URLS[CURRENT_API])) {
                        url = url.replace(API_URLS[CURRENT_API], API_URLS[CURRENT_API + 1]);
                        CURRENT_API++;
                    } else {
                        url = url.replace(API_URLS[CURRENT_API - 1], API_URLS[CURRENT_API]);
                    }
                    requestBuilder.url(url);
                    fetchList(currentList, requestBuilder, filters, callback);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {

                        ObjectMapper mapper = new ObjectMapper();

                        List<butter.droid.base.providers.media.models.response.movies.Movie> list = mapper.readValue(response.body().string(), mapper.getTypeFactory().constructCollectionType(List.class, butter.droid.base.providers.media.models.response.movies.Movie.class));

                        if (list == null) {
                            callback.onFailure(new NetworkErrorException("Empty response"));
                        } else {
                            MovieResp result = new MovieResp(list);
                            ArrayList<Media> formattedData = result.formatListForPopcorn(currentList);
                            callback.onSuccess(filters, formattedData, list.size() > 0);
                            return;
                        }
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
                callback.onFailure(new NetworkErrorException("Couldn't connect to MovieAPI"));
            }
        });
    }

    @Override
    public Call getDetail(ArrayList<Media> currentList, Integer index, Callback callback) {
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
    public List<NavInfo> getNavigation() {
        List<NavInfo> tabs = new ArrayList<>();
        tabs.add(new NavInfo(R.id.movie_filter_trending,Filters.Sort.TRENDING, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.trending),R.drawable.movie_filter_trending));
        tabs.add(new NavInfo(R.id.movie_filter_popular_now,Filters.Sort.POPULARITY, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.popular),R.drawable.movie_filter_popular_now));
        tabs.add(new NavInfo(R.id.movie_filter_top_rated,Filters.Sort.RATING, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.top_rated),R.drawable.movie_filter_top_rated));
        tabs.add(new NavInfo(R.id.movie_filter_release_date,Filters.Sort.DATE, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.release_date),R.drawable.movie_filter_release_date));
        tabs.add(new NavInfo(R.id.movie_filter_year,Filters.Sort.YEAR, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.year),R.drawable.movie_filter_year));
        tabs.add(new NavInfo(R.id.movie_filter_a_to_z,Filters.Sort.ALPHABET, Filters.Order.ASC, ButterApplication.getAppContext().getString(R.string.a_to_z),R.drawable.movie_filter_a_to_z));
        return tabs;
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

    static private class MovieResp {
        List<butter.droid.base.providers.media.models.response.movies.Movie> movieList;

        public MovieResp(List<butter.droid.base.providers.media.models.response.movies.Movie> movieList) {
            this.movieList = movieList;
        }

        public ArrayList<Media> formatListForPopcorn(ArrayList<Media> existingList) {
            for (butter.droid.base.providers.media.models.response.movies.Movie item: movieList) {

                Movie movie = new Movie(sMediaProvider, sSubsProvider);


                movie.videoId = item.getImdbId();
                movie.imdbId = movie.videoId;

                movie.title = item.getTitle();
                movie.year = item.getYear();

                List<String> genres = item.getGenres();
                movie.genre = "";
                if (genres.size() > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String genre : genres) {
                        if (stringBuilder.length() > 0) {
                            stringBuilder.append(", ");
                        }
                        stringBuilder.append(StringUtils.capWords(genre));
                    }
                    movie.genre = stringBuilder.toString();
                }

                movie.rating = Double.toString(item.getRating().getPercentage() / 10);
                movie.trailer = item.getTrailer();
                movie.runtime = item.getRuntime();
                movie.synopsis = item.getSynopsis();
                movie.certification = item.getCertification();

                if(!item.getImages().getPoster().contains("images/posterholder.png")) {
                    movie.image = item.getImages().getPoster().replace("/original/", "/medium/");
                    movie.fullImage = item.getImages().getPoster();
                    movie.headerImage = item.getImages().getFanart().replace("/original/", "/medium/");
                }

                if (item.getTorrents() != null) {
                    for (Map.Entry<String, Language> language : item.getTorrents().getLanguages().entrySet()) {
                        Map<String, Media.Torrent> torrentMap = new HashMap<>();
                        for (Map.Entry<String, Quality> torrentQuality : language.getValue().getQualities().entrySet()) {
                            if (torrentQuality == null) continue;
                            Media.Torrent torrent = new Media.Torrent();
                            torrent.seeds = torrentQuality.getValue().getSeed();
                            torrent.peers = torrentQuality.getValue().getPeer();
                            torrent.url = torrentQuality.getValue().getUrl();

                            torrentMap.put(torrentQuality.getKey(), torrent);
                        }
                        movie.torrents.put(language.getKey(), torrentMap);
                    }
                }

                existingList.add(movie);
            }
            return existingList;
        }
    }

}

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import butter.droid.base.BuildConfig;
import butter.droid.base.ButterApplication;
import butter.droid.base.R;
import butter.droid.base.providers.media.models.Genre;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.response.TVDetailsReponse;
import butter.droid.base.providers.media.response.TVResponse;
import butter.droid.base.providers.media.response.models.shows.ShowDetails;
import butter.droid.base.providers.meta.MetaProvider;
import butter.droid.base.providers.meta.TraktProvider;
import butter.droid.base.providers.subs.OpenSubsProvider;
import butter.droid.base.providers.subs.SubsProvider;
import timber.log.Timber;

public class TVProvider extends MediaProvider {

    private static final String[] API_URLS = BuildConfig.TV_URLS;
    private static final SubsProvider sSubsProvider = new OpenSubsProvider();
    private static final MetaProvider sMetaProvider = new TraktProvider();
    private static final MediaProvider sMediaProvider = new TVProvider();
    private static Integer CURRENT_API = 0;

    @Override
    public Call getList(final ArrayList<Media> existingList, Filters filters, final Callback callback) {
        final ArrayList<Media> currentList;
        if (existingList == null) {
            currentList = new ArrayList<>();
        } else {
            currentList = new ArrayList<>(existingList);
        }

        ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
        params.add(new AbstractMap.SimpleEntry<>("limit", "30"));

        if (filters == null) {
            filters = new Filters();
        }

        if (filters.keywords != null) {
            params.add(new AbstractMap.SimpleEntry<>("keywords", filters.keywords));
        }

        if (filters.genre != null) {
            params.add(new AbstractMap.SimpleEntry<>("genre", filters.genre));
        }

        if (filters.order == Filters.Order.ASC) {
            params.add(new AbstractMap.SimpleEntry<>("order", "1"));
        } else {
            params.add(new AbstractMap.SimpleEntry<>("order", "-1"));
        }

        String sort;
        switch (filters.sort) {
            default:
            case POPULARITY:
                sort = "popularity";
                break;
            case TRENDING:
                sort = "trending";
                break;
            case YEAR:
                sort = "year";
                break;
            case DATE:
                sort = "updated";
                break;
            case RATING:
                sort = "rating";
                break;
            case ALPHABET:
                sort = "name";
                break;
        }

        params.add(new AbstractMap.SimpleEntry<>("sort", sort));

        String url = API_URLS[CURRENT_API] + "shows/";
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

        Timber.d("TVProvider", "Making request to: " + url);

        return fetchList(currentList, requestBuilder, filters, callback);
    }

    /**
     * Fetch the list of movies from EZTV
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
                    if (url.contains(API_URLS[CURRENT_API])) {
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

                        String responseStr = response.body().string();

                        if (responseStr.isEmpty()) {
                            callback.onFailure(new NetworkErrorException("Empty response"));
                        }

                        ObjectMapper mapper = new ObjectMapper();
                        List<butter.droid.base.providers.media.response.models.shows.Show> list = mapper.readValue(responseStr, mapper.getTypeFactory().constructCollectionType(List.class, butter.droid.base.providers.media.response.models.shows.Show.class));

                        if (!list.isEmpty()) {
                            TVResponse result = new TVResponse(list);
                            ArrayList<Media> formattedData = result.formatListForPopcorn(currentList, sMediaProvider, sSubsProvider);
                            callback.onSuccess(filters, formattedData, list.size() > 0);
                            return;
                        }
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
                callback.onFailure(new NetworkErrorException("Couldn't connect to TVAPI"));
            }
        });
    }

    @Override
    public Call getDetail(ArrayList<Media> currentList, Integer index, final Callback callback) {
        Request.Builder requestBuilder = new Request.Builder();
        String url = API_URLS[CURRENT_API] + "show/" + currentList.get(index).videoId;
        requestBuilder.url(url);
        requestBuilder.tag(MEDIA_CALL);

        Timber.d("TVProvider", "Making request to: " + url);

        return enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {

                        String responseStr = response.body().string();

                        if (responseStr.isEmpty()) {
                            callback.onFailure(new NetworkErrorException("Empty response"));
                        }

                        ObjectMapper mapper = new ObjectMapper();
                        ShowDetails detail = mapper.readValue(responseStr, ShowDetails.class);

                        if (detail != null) {
                            TVDetailsReponse result = new TVDetailsReponse();
                            ArrayList<Media> formattedData = result.formatDetailForPopcorn(detail, sMediaProvider, sSubsProvider, sMetaProvider);
                            if (formattedData.size() > 0) {
                                callback.onSuccess(null, formattedData, true);
                                return;
                            }
                            callback.onFailure(new IllegalStateException("Empty list"));
                            return;
                        }
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
                callback.onFailure(new NetworkErrorException("Couldn't connect to TVAPI"));
            }
        });
    }

    @Override
    public int getLoadingMessage() {
        return R.string.loading_shows;
    }

    @Override
    public List<NavInfo> getNavigation() {
        List<NavInfo> tabs = new ArrayList<>();

        tabs.add(new NavInfo(R.id.tvshow_filter_trending, Filters.Sort.TRENDING, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.trending), R.drawable.tvshow_filter_trending));
        tabs.add(new NavInfo(R.id.tvshow_filter_popular_now, Filters.Sort.POPULARITY, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.popular), R.drawable.tvshow_filter_popular_now));
        tabs.add(new NavInfo(R.id.tvshow_filter_top_rated, Filters.Sort.RATING, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.top_rated), R.drawable.tvshow_filter_top_rated));
        tabs.add(new NavInfo(R.id.tvshow_filter_last_updated, Filters.Sort.DATE, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.last_updated), R.drawable.tvshow_filter_last_updated));
        tabs.add(new NavInfo(R.id.tvshow_filter_year, Filters.Sort.YEAR, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.year), R.drawable.tvshow_filter_year));
        tabs.add(new NavInfo(R.id.tvshow_filter_a_to_z, Filters.Sort.ALPHABET, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.a_to_z), R.drawable.tvshow_filter_a_to_z));
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
}

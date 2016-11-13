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
import android.annotation.SuppressLint;

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
import butter.droid.base.providers.media.response.AnimeDetailsReponse;
import butter.droid.base.providers.media.response.AnimeResponse;
import butter.droid.base.providers.media.response.models.anime.AnimeDetails;
import timber.log.Timber;

@SuppressLint("ParcelCreator")
public class AnimeProvider extends MediaProvider {

    private static final String[] API_URLS = BuildConfig.ANIME_URLS;
    private static final MediaProvider sMediaProvider = new AnimeProvider();
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
            case YEAR:
                sort = "year";
                break;
            case RATING:
                sort = "rating";
                break;
            case ALPHABET:
                sort = "name";
                break;
        }

        params.add(new AbstractMap.SimpleEntry<>("sort", sort));

        String url = API_URLS[CURRENT_API] + "animes/";
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

        Timber.d("AnimeProvider", "Making request to: " + url);

        return fetchList(currentList, requestBuilder, filters, callback);
    }

    /**
     * Fetch the list of movies from Haruhichan
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
                        List<butter.droid.base.providers.media.response.models.anime.Anime> list = mapper.readValue(responseStr, mapper.getTypeFactory().constructCollectionType(List.class, butter.droid.base.providers.media.response.models.anime.Anime.class));

                        if (!list.isEmpty()) {
                            AnimeResponse result = new AnimeResponse(list);
                            ArrayList<Media> formattedData = result.formatListForPopcorn(currentList, sMediaProvider, null);
                            callback.onSuccess(filters, formattedData, list.size() > 0);
                            return;
                        }
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
                callback.onFailure(new NetworkErrorException("Couldn't connect to AnimeAPI"));
            }
        });
    }

    @Override
    public Call getDetail(ArrayList<Media> currentList, Integer index, final Callback callback) {
        Request.Builder requestBuilder = new Request.Builder();
        String url = API_URLS[CURRENT_API] + "anime/" + currentList.get(index).videoId;
        requestBuilder.url(url);
        requestBuilder.tag(MEDIA_CALL);

        Timber.d("AnimeProvider", "Making request to: " + url);

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
                        AnimeDetails detail = mapper.readValue(responseStr, AnimeDetails.class);

                        if (detail != null) {
                            AnimeDetailsReponse result = new AnimeDetailsReponse();
                            ArrayList<Media> formattedData = result.formatDetailForPopcorn(detail, sMediaProvider, null, null);
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
                callback.onFailure(new NetworkErrorException("Couldn't connect to AnimeAPI"));
            }
        });
    }

    @Override
    public int getLoadingMessage() {
        return R.string.loading_data;
    }

    @Override
    public List<NavInfo> getNavigation() {
        List<NavInfo> tabs = new ArrayList<>();
        tabs.add(new NavInfo(R.id.anime_filter_popular, Filters.Sort.POPULARITY, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.popular), R.drawable.anime_filter_popular));
        tabs.add(new NavInfo(R.id.anime_filter_year, Filters.Sort.YEAR, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.year), R.drawable.anime_filter_year));
        tabs.add(new NavInfo(R.id.anime_filter_a_to_z, Filters.Sort.ALPHABET, Filters.Order.DESC, ButterApplication.getAppContext().getString(R.string.a_to_z), R.drawable.anime_filter_a_to_z));
        return tabs;
    }

    @Override
    public List<Genre> getGenres() {
        List<Genre> returnList = new ArrayList<>();
        returnList.add(new Genre("All", R.string.genre_all));
        returnList.add(new Genre("Action", R.string.genre_action));
        returnList.add(new Genre("Adventure", R.string.genre_adventure));
        returnList.add(new Genre("Racing", R.string.genre_cars));
        returnList.add(new Genre("Comedy", R.string.genre_comedy));
        returnList.add(new Genre("Dementia", R.string.genre_dementia));
        returnList.add(new Genre("Demons", R.string.genre_demons));
        returnList.add(new Genre("Drama", R.string.genre_drama));
        returnList.add(new Genre("Ecchi", R.string.genre_ecchi));
        returnList.add(new Genre("Fantasy", R.string.genre_fantasy));
        returnList.add(new Genre("Game", R.string.genre_game));
        returnList.add(new Genre("Gender Bender", R.string.gender_bender));
        returnList.add(new Genre("Gore", R.string.gore));
        returnList.add(new Genre("Harem", R.string.genre_harem));
        returnList.add(new Genre("Historical", R.string.genre_history));
        returnList.add(new Genre("Horror", R.string.genre_horror));
        returnList.add(new Genre("Kids", R.string.genre_kids));
        returnList.add(new Genre("Magic", R.string.genre_magic));
        returnList.add(new Genre("Mahou Shoujo", R.string.mahou_shoujo));
        returnList.add(new Genre("Mahou Shounen", R.string.mahou_shounen));
        returnList.add(new Genre("Martial Arts", R.string.genre_martial_arts));
        returnList.add(new Genre("Mecha", R.string.genre_mecha));
        returnList.add(new Genre("Military", R.string.genre_military));
        returnList.add(new Genre("Music", R.string.genre_music));
        returnList.add(new Genre("Mystery", R.string.genre_mystery));
        returnList.add(new Genre("Parody", R.string.genre_parody));
        returnList.add(new Genre("Police", R.string.genre_police));
        returnList.add(new Genre("Psychological", R.string.genre_psychological));
        returnList.add(new Genre("Romance", R.string.genre_romance));
        returnList.add(new Genre("Samurai", R.string.genre_samurai));
        returnList.add(new Genre("School", R.string.genre_school));
        returnList.add(new Genre("Sci-Fi", R.string.genre_sci_fi));
        returnList.add(new Genre("Shoujo Ai", R.string.genre_shoujo_ai));
        returnList.add(new Genre("Shounen Ai", R.string.genre_shounen_ai));
        returnList.add(new Genre("Slice of Life", R.string.genre_slice_of_life));
        returnList.add(new Genre("Space", R.string.genre_space));
        returnList.add(new Genre("Sports", R.string.genre_sport));
        returnList.add(new Genre("Super Power", R.string.genre_super_power));
        returnList.add(new Genre("Supernatural", R.string.genre_supernatural));
        returnList.add(new Genre("Thriller", R.string.genre_thriller));
        returnList.add(new Genre("Vampire", R.string.genre_vampire));
        returnList.add(new Genre("Yuri", R.string.genre_yuri));
        return returnList;
    }
}

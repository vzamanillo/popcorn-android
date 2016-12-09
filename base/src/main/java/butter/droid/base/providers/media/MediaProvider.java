/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.base.providers.media;

import android.accounts.NetworkErrorException;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import butter.droid.base.R;
import butter.droid.base.providers.BaseProvider;
import butter.droid.base.providers.media.callback.MediaProviderCallback;
import butter.droid.base.providers.media.filters.Filters;
import butter.droid.base.providers.media.filters.Order;
import butter.droid.base.providers.media.filters.Sort;
import butter.droid.base.providers.media.models.Genre;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.type.MediaProviderType;
import butter.droid.base.providers.subs.SubsProvider;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * MediaProvider.java
 * <p/>
 * Base class for all media providers. Any media providers has to extend this class and use the callback defined here.
 */
public abstract class MediaProvider extends BaseProvider {

    @Nullable
    private final SubsProvider subsProvider;

    private String[] apiUrls = new String[0];
    private String itemsPath = "";
    private String itemDetailsPath = "";
    private Integer currentApi = 0;

    public MediaProvider(OkHttpClient client, ObjectMapper mapper, @Nullable SubsProvider subsProvider, String[] apiUrls, String itemsPath, String itemDetailsPath, Integer currentApi) {
        super(client, mapper);
        this.subsProvider = subsProvider;
        this.apiUrls = apiUrls;
        this.itemsPath = itemsPath;
        this.itemDetailsPath = itemDetailsPath;
        this.currentApi = currentApi;
    }

    /**
     * Get a list of Media items from the provider
     *
     * @param filters  Filters the provider can use to sort or search
     * @param callback MediaProvider callback
     */
    public Call getList(Filters filters, MediaProviderCallback callback) {
        return getList(null, filters, callback);
    }

    /**
     * Get a list of Media items from the provider
     *
     * @param existingList Input the current list so it can be extended
     * @param filters      Filters the provider can use to sort or search
     * @param callback     MediaProvider callback
     * @return Call
     */
    public Call getList(final ArrayList<Media> existingList, Filters filters, final MediaProviderCallback callback) {
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

        if (filters.getKeywords() != null) {
            params.add(new AbstractMap.SimpleEntry<>("keywords", filters.getKeywords()));
        }

        if (filters.getGenre() != null) {
            params.add(new AbstractMap.SimpleEntry<>("genre", filters.getGenre()));
        }

        params.add(new AbstractMap.SimpleEntry<>("order", filters.getOrder().getValue()));

        if (filters.getLangCode() != null) {
            params.add(new AbstractMap.SimpleEntry<>("lang", filters.getLangCode()));
        }

        params.add(new AbstractMap.SimpleEntry<>("sort", filters.getSort().getParamName()));

        String url = apiUrls[currentApi] + itemsPath;
        if (filters.getPage() != null) {
            url += filters.getPage();
        } else {
            url += "1";
        }

        Request.Builder requestBuilder = new Request.Builder();
        String query = buildQuery(params);
        url = url + "?" + query;
        requestBuilder.url(url);

        Timber.d(this.getClass().getSimpleName(), "Making request to: " + url);

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
    private Call fetchList(final ArrayList<Media> currentList, final Request.Builder requestBuilder, final Filters filters, final MediaProviderCallback callback) {
        return enqueue(requestBuilder.build(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String url = requestBuilder.build().url().toString();
                if (currentApi >= apiUrls.length - 1) {
                    callback.onFailure(e);
                } else {
                    if (url.contains(apiUrls[currentApi])) {
                        url = url.replace(apiUrls[currentApi], apiUrls[currentApi + 1]);
                        currentApi++;
                    } else {
                        url = url.replace(apiUrls[currentApi - 1], apiUrls[currentApi]);
                    }
                    requestBuilder.url(url);
                    fetchList(currentList, requestBuilder, filters, callback);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    String responseStr = response.body().string();

                    if (responseStr.isEmpty()) {
                        onFailure(call, new IOException("Empty response"));
                    }
                    int actualSize = currentList.size();
                    ArrayList<Media> responseItems = getResponseFormattedList(responseStr, currentList);
                    callback.onSuccess(filters, responseItems, responseItems.size() > actualSize);
                    return;
                }
                onFailure(call, new IOException("Couldn't connect to API"));
            }
        });
    }

    public Call getDetail(ArrayList<Media> currentList, Integer index, final MediaProviderCallback callback) {
        Request.Builder requestBuilder = new Request.Builder();
        String url = apiUrls[currentApi] + itemDetailsPath + currentList.get(index).videoId;
        requestBuilder.url(url);

        Timber.d(this.getClass().getSimpleName(), "Making request to: " + url);

        return enqueue(requestBuilder.build(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {

                        String responseStr = response.body().string();

                        if (responseStr.isEmpty()) {
                            callback.onFailure(new NetworkErrorException("Empty response"));
                        }

                        ArrayList<Media> formattedData = getResponseDetailsFormattedList(responseStr);
                        if (formattedData.size() > 0) {
                            callback.onSuccess(null, formattedData, true);
                            return;
                        }
                        callback.onFailure(new IllegalStateException("Empty list"));
                        return;
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
                callback.onFailure(new NetworkErrorException("Couldn't connect to API"));
            }
        });
    }

    public int getLoadingMessage() {
        return R.string.loading;
    }

    public ArrayList<Media> getResponseFormattedList(String responseStr, ArrayList<Media> currentList) throws IOException {
        return new ArrayList<>();
    }

    public ArrayList<Media> getResponseDetailsFormattedList(String responseStr) throws IOException {
        return new ArrayList<>();
    }

    public List<Sort> getSortAvailable() {
        return new ArrayList<>();
    }

    public Sort getSortDefault() {
        return Sort.TRENDING;
    }

    public Order getOrderDefault() {
        return Order.ASC;
    }

    public List<Genre> getGenres() {
        return new ArrayList<>();
    }

    @Nullable
    public SubsProvider getSubsProvider() {
        return subsProvider;
    }

    public boolean hasSubsProvider() {
        return subsProvider != null;
    }

    public abstract MediaProviderType getProviderType();


}

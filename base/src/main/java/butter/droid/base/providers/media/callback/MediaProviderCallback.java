package butter.droid.base.providers.media.callback;

import java.util.ArrayList;

import butter.droid.base.providers.media.filters.Filters;
import butter.droid.base.providers.media.models.Media;

public interface MediaProviderCallback {
    void onSuccess(Filters filters, ArrayList<Media> items, boolean changed);

    void onFailure(Exception e);
}
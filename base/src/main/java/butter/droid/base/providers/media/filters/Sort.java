package butter.droid.base.providers.media.filters;

import butter.droid.base.ButterApplication;
import butter.droid.base.R;

public enum Sort {

    POPULARITY(R.string.popular, "popularity"),
    YEAR(R.string.year, "year"),
    DATE(R.string.release_date, "last added"),
    RATING(R.string.top_rated, "rating"),
    ALPHABET(R.string.a_to_z, "name"),
    TRENDING(R.string.trending, "trending");

    private int title;
    private String paramName;

    Sort(int title, String paramName) {
        this.title = title;
        this.paramName = paramName;
    }

    public String getTitle() {
        return ButterApplication.getAppContext().getString(title);
    }

    public String getParamName() {
        return paramName;
    }
}
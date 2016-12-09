package butter.droid.base.providers.media.type;

import butter.droid.base.R;

public enum MediaProviderType {

    MOVIE(R.string.title_movies, R.drawable.ic_nav_movies, 0),
    SHOW(R.string.title_shows, R.drawable.ic_nav_tv, 1),
    ANIME(R.string.title_anime, R.drawable.ic_nav_anime, 2);

    private int title;
    private int icon;
    private int position;

    MediaProviderType(int title, int icon, int position) {
        this.title = title;
        this.icon = icon;
        this.position = position;
    }

    public int getIcon() {
        return icon;
    }

    public int getTitle() {
        return title;
    }

    public int getPosition() {
        return position;
    }
}

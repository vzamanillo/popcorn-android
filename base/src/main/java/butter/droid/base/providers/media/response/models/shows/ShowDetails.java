package butter.droid.base.providers.media.response.models.shows;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import butter.droid.base.providers.media.response.models.ResponseItem;
import butter.droid.base.providers.media.response.models.common.Images;
import butter.droid.base.providers.media.response.models.common.Rating;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class ShowDetails extends ResponseItem {

    @JsonProperty("_id")
    private String id;
    @JsonProperty("imdb_id")
    private String imdbId;
    @JsonProperty("tvdb_id")
    private String tvdbId;
    @JsonProperty("title")
    private String title;
    @JsonProperty("year")
    private String year;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("synopsis")
    private String synopsis;
    @JsonProperty("runtime")
    private String runtime;
    @JsonProperty("country")
    private String country;
    @JsonProperty("network")
    private String network;
    @JsonProperty("air_day")
    private String airDay;
    @JsonProperty("air_time")
    private String airTime;
    @JsonProperty("status")
    private String status;
    @JsonProperty("num_seasons")
    private int numSeasons;
    @JsonProperty("last_updated")
    private long lastUpdated;
    @JsonProperty("__v")
    private int v;
    @JsonProperty("episodes")
    private List<Episode> episodes = new ArrayList<>();
    @JsonProperty("genres")
    private List<String> genres = new ArrayList<>();
    @JsonProperty("images")
    private Images images;
    @JsonProperty("rating")
    private Rating rating;

    /**
     * @return The id
     */
    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    /**
     * @param id The _id
     */
    @JsonProperty("_id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The imdbId
     */
    @JsonProperty("imdb_id")
    public String getImdbId() {
        return imdbId;
    }

    /**
     * @param imdbId The imdb_id
     */
    @JsonProperty("imdb_id")
    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    /**
     * @return The tvdbId
     */
    @JsonProperty("tvdb_id")
    public String getTvdbId() {
        return tvdbId;
    }

    /**
     * @param tvdbId The tvdb_id
     */
    @JsonProperty("tvdb_id")
    public void setTvdbId(String tvdbId) {
        this.tvdbId = tvdbId;
    }

    /**
     * @return The title
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The year
     */
    @JsonProperty("year")
    public String getYear() {
        return year;
    }

    /**
     * @param year The year
     */
    @JsonProperty("year")
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * @return The slug
     */
    @JsonProperty("slug")
    public String getSlug() {
        return slug;
    }

    /**
     * @param slug The slug
     */
    @JsonProperty("slug")
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * @return The synopsis
     */
    @JsonProperty("synopsis")
    public String getSynopsis() {
        return synopsis;
    }

    /**
     * @param synopsis The synopsis
     */
    @JsonProperty("synopsis")
    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    /**
     * @return The runtime
     */
    @JsonProperty("runtime")
    public String getRuntime() {
        return runtime;
    }

    /**
     * @param runtime The runtime
     */
    @JsonProperty("runtime")
    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    /**
     * @return The country
     */
    @JsonProperty("country")
    public String getCountry() {
        return country;
    }

    /**
     * @param country The country
     */
    @JsonProperty("country")
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return The network
     */
    @JsonProperty("network")
    public String getNetwork() {
        return network;
    }

    /**
     * @param network The network
     */
    @JsonProperty("network")
    public void setNetwork(String network) {
        this.network = network;
    }

    /**
     * @return The airDay
     */
    @JsonProperty("air_day")
    public String getAirDay() {
        return airDay;
    }

    /**
     * @param airDay The air_day
     */
    @JsonProperty("air_day")
    public void setAirDay(String airDay) {
        this.airDay = airDay;
    }

    /**
     * @return The airTime
     */
    @JsonProperty("air_time")
    public String getAirTime() {
        return airTime;
    }

    /**
     * @param airTime The air_time
     */
    @JsonProperty("air_time")
    public void setAirTime(String airTime) {
        this.airTime = airTime;
    }

    /**
     * @return The status
     */
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status
     */
    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The numSeasons
     */
    @JsonProperty("num_seasons")
    public int getNumSeasons() {
        return numSeasons;
    }

    /**
     * @param numSeasons The num_seasons
     */
    @JsonProperty("num_seasons")
    public void setNumSeasons(int numSeasons) {
        this.numSeasons = numSeasons;
    }

    /**
     * @return The lastUpdated
     */
    @JsonProperty("last_updated")
    public long getLastUpdated() {
        return lastUpdated;
    }

    /**
     * @param lastUpdated The last_updated
     */
    @JsonProperty("last_updated")
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * @return The v
     */
    @JsonProperty("__v")
    public int getV() {
        return v;
    }

    /**
     * @param v The __v
     */
    @JsonProperty("__v")
    public void setV(int v) {
        this.v = v;
    }

    /**
     * @return The episodes
     */
    @JsonProperty("episodes")
    public List<Episode> getEpisodes() {
        return episodes;
    }

    /**
     * @param episodes The episodes
     */
    @JsonProperty("episodes")
    public void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes;
    }

    /**
     * @return The genres
     */
    @JsonProperty("genres")
    public List<String> getGenres() {
        return genres;
    }

    /**
     * @param genres The genres
     */
    @JsonProperty("genres")
    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    /**
     * @return The images
     */
    @JsonProperty("images")
    public Images getImages() {
        return images;
    }

    /**
     * @param images The images
     */
    @JsonProperty("images")
    public void setImages(Images images) {
        this.images = images;
    }

    /**
     * @return The rating
     */
    @JsonProperty("rating")
    public Rating getRating() {
        return rating;
    }

    /**
     * @param rating The rating
     */
    @JsonProperty("rating")
    public void setRating(Rating rating) {
        this.rating = rating;
    }

}

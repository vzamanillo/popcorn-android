package butter.droid.base.providers.media.models.response.movies;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Quality {

    @JsonProperty("provider")
    private String provider;
    @JsonProperty("filesize")
    private String filesize;
    @JsonProperty("size")
    private long size;
    @JsonProperty("peer")
    private int peer;
    @JsonProperty("seed")
    private int seed;
    @JsonProperty("url")
    private String url;

    /**
     * @return The provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @param provider The provider
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * @return The filesize
     */
    public String getFilesize() {
        return filesize;
    }

    /**
     * @param filesize The filesize
     */
    public void setFilesize(String filesize) {
        this.filesize = filesize;
    }

    /**
     * @return The size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size The size
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return The peer
     */
    public int getPeer() {
        return peer;
    }

    /**
     * @param peer The peer
     */
    public void setPeer(int peer) {
        this.peer = peer;
    }

    /**
     * @return The seed
     */
    public int getSeed() {
        return seed;
    }

    /**
     * @param seed The seed
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

}

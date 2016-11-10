
package butter.droid.base.updater.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Architecture {

    @SerializedName("versionCode")
    @Expose
    private int versionCode;
    @SerializedName("versionName")
    @Expose
    private String versionName;
    @SerializedName("checksum")
    @Expose
    private String checksum;
    @SerializedName("updateUrl")
    @Expose
    private String updateUrl;

    public Architecture() {
    }

    public Architecture(int versionCode, String versionName, String checksum, String updateUrl) {
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.checksum = checksum;
        this.updateUrl = updateUrl;
    }

    /**
     * @return The versionCode
     */
    public int getVersionCode() {
        return versionCode;
    }

    /**
     * @return The versionName
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * @return The checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * @return The updateUrl
     */
    public String getUpdateUrl() {
        return updateUrl;
    }

    /**
     *
     * @param versionCode
     */
    public void setVersionCode(int versionCode) {
      this.versionCode = versionCode;
    }

    /**
     *
     * @param versionName
     */
    public void setVersionName(String versionName) {
      this.versionName = versionName;
    }

    /**
     *
     * @param checksum
     */
    public void setChecksum(String checksum) {
      this.checksum = checksum;
    }

    /**
     *
     * @param updateUrl
     */
    public void setUpdateUrl(String updateUrl) {
      this.updateUrl = updateUrl;
    }
}

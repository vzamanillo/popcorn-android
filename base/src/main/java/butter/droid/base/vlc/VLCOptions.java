/*****************************************************************************
 * VLCOptions.java
 *****************************************************************************
 * Copyright Â© 2015 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package butter.droid.base.vlc;

import android.content.Context;
import android.util.Log;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.util.VLCUtil;

import java.util.ArrayList;

import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.utils.PrefUtils;


public class VLCOptions {

    private static final String TAG = "VLCConfig";

    @SuppressWarnings("unused")
    public static final int HW_ACCELERATION_AUTOMATIC = -1;
    public static final int HW_ACCELERATION_DISABLED = 0;
    public static final int HW_ACCELERATION_DECODING = 1;
    public static final int HW_ACCELERATION_FULL = 2;

    public final static int MEDIA_VIDEO = 0x01;
    public final static int MEDIA_NO_HWACCEL = 0x02;
    private final static int MEDIA_PAUSED = 0x4;

    public final static int DEFAULT_NETWORK_CACHING = 60000;

    public static ArrayList<String> getLibOptions(Context context, String subtitlesEncoding, String chroma, boolean verboseMode) {

        ArrayList<String> options = new ArrayList<>(50);

        int deblocking = getDeblocking(-1);

        int networkCaching = PrefUtils.get(context, Prefs.NETWORK_CACHING, DEFAULT_NETWORK_CACHING);

        if (chroma != null && chroma.equals("YV12"))
            chroma = "";

        options.add("--avcodec-skiploopfilter");
        options.add("" + deblocking);
        options.add("--subsdec-encoding");
        options.add(subtitlesEncoding);
        options.add("--stats");
        options.add("--network-caching=" + networkCaching);
        /* XXX: why can't the default be fine ? #7792 */
        options.add("--androidwindow-chroma");
        options.add(chroma != null ? chroma : "RV32");
        options.add("--audio-resampler");
        options.add(getResampler());

        options.add(verboseMode ? "-vv" : "-v");
        return options;
    }

    private static String getResampler() {
        final VLCUtil.MachineSpecs m = VLCUtil.getMachineSpecs();
        return (m == null || m.processors > 2) ? "soxr" : "ugly";
    }

    private static int getDeblocking(int deblocking) {
        int ret = deblocking;
        if (deblocking < 0) {
            /**
             * Set some reasonable sDeblocking defaults:
             *
             * Skip all (4) for armv6 and MIPS by default
             * Skip non-ref (1) for all armv7 more than 1.2 Ghz and more than 2 cores
             * Skip non-key (3) for all devices that don't meet anything above
             */
            VLCUtil.MachineSpecs m = VLCUtil.getMachineSpecs();
            if (m == null)
                return ret;
            if ((m.hasArmV6 && !(m.hasArmV7)) || m.hasMips)
                ret = 4;
            else if (m.frequency >= 1200 && m.processors > 2)
                ret = 1;
            else if (m.bogoMIPS >= 1200 && m.processors > 2) {
                ret = 1;
                Log.d(TAG, "Used bogoMIPS due to lack of frequency info");
            } else
                ret = 3;
        } else if (deblocking > 4) { // sanity check
            ret = 3;
        }
        return ret;
    }

    public static void setMediaOptions(Media media, Context context, int flags) {
        boolean noHardwareAcceleration = (flags & MEDIA_NO_HWACCEL) != 0;
        boolean noVideo = (flags & MEDIA_VIDEO) == 0;
        final boolean paused = (flags & MEDIA_PAUSED) != 0;
        int hardwareAcceleration = HW_ACCELERATION_DISABLED;

        if (!noHardwareAcceleration) {
            try {
                hardwareAcceleration = PrefUtils.get(context, Prefs.HW_ACCELERATION, HW_ACCELERATION_AUTOMATIC);
            } catch (NumberFormatException ignored) {}
        }
        if (hardwareAcceleration == HW_ACCELERATION_DISABLED)
            media.setHWDecoderEnabled(false, false);
        else if (hardwareAcceleration == HW_ACCELERATION_FULL || hardwareAcceleration == HW_ACCELERATION_DECODING) {
            media.setHWDecoderEnabled(true, true);
            if (hardwareAcceleration == HW_ACCELERATION_DECODING) {
                media.addOption(":no-mediacodec-dr");
                media.addOption(":no-omxil-dr");
            }
        } /* else automatic: use default options */

        if (noVideo)
            media.addOption(":no-video");
        if (paused)
            media.addOption(":start-paused");
    }
}
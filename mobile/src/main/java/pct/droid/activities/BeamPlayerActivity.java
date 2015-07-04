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

package pct.droid.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import pct.droid.R;
import pct.droid.base.beaming.BeamManager;
import pct.droid.base.beaming.server.BeamServer;
import pct.droid.base.beaming.server.BeamServerService;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.torrent.TorrentService;
import pct.droid.dialogfragments.OptionDialogFragment;
import pct.droid.fragments.BeamPlayerFragment;
import pct.droid.fragments.VideoPlayerFragment;

public class BeamPlayerActivity extends PopcornBaseActivity implements VideoPlayerFragment.Callback {

    private BeamPlayerFragment mFragment;
    private BeamManager mBeamManager = BeamManager.getInstance(this);
    private StreamInfo mStreamInfo;
    private String mTitle;

    public static Intent startActivity(Context context, StreamInfo info) {
        return startActivity(context, info, 0);
    }

    public static Intent startActivity(Context context, StreamInfo info, long resumePosition) {
        Intent i = new Intent(context, BeamPlayerActivity.class);
        i.putExtra(INFO, info);
        //todo: resume position
        context.startActivity(i);
        return i;
    }

    public final static String INFO = "stream_info";
    public final static String RESUME_POSITION = "resume_position";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState, R.layout.activity_beamplayer);

        BeamServerService.getServer().start();

        mStreamInfo = getIntent().getParcelableExtra(INFO);

        mTitle = mStreamInfo.getTitle() == null ? getString(R.string.the_video) : mStreamInfo.getTitle();

        String location = mStreamInfo.getVideoLocation();
        if (!location.startsWith("http://") && !location.startsWith("https://")) {
            BeamServer.setCurrentVideo(location);
            location = BeamServer.getVideoURL();
        }
        mStreamInfo.setVideoLocation(location);

        /*
        File subsLocation = new File(SubsProvider.getStorageLocation(context), media.videoId + "-" + subLanguage + ".srt");
        BeamServer.setCurrentSubs(subsLocation);
         */

        mFragment = (BeamPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.beam_fragment);
    }

    @Override
    protected void onStop() {
        if(null != mService)
            mService.removeListener(mFragment);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showExitDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        OptionDialogFragment.show(getSupportFragmentManager(), getString(R.string.leave_videoplayer_title), String.format(getString(R.string.leave_videoplayer_message), mTitle), getString(android.R.string.yes), getString(android.R.string.no), new OptionDialogFragment.Listener() {
            @Override
            public void onSelectionPositive() {
                mBeamManager.stopVideo();
                BeamServerService.getServer().stop();
                if (mService != null)
                    mService.stopStreaming();
                finish();
            }

            @Override
            public void onSelectionNegative() {
            }
        });
    }

    @Override
    public StreamInfo getInfo() {
          return mStreamInfo;
    }

    @Override
    public TorrentService getService() {
        return mService;
    }

    @Override
    public void onTorrentServiceConnected() {
        super.onTorrentServiceConnected();
        mService.addListener(mFragment);
    }
}

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

package butter.droid.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import butter.droid.R;
import butter.droid.base.ButterApplication;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.fragments.MediaGenreSelectionFragment;
import butter.droid.fragments.MediaListFragment;

public class MediaPagerAdapter extends FragmentStatePagerAdapter {

    //private String mGenre;
    private int mHasGenreTabs = 0;
    private Fragment mGenreFragment;
    private Fragment mSortFragment;
    private List<MediaListFragment> mFragments;

    public MediaPagerAdapter(FragmentManager fm, List<MediaListFragment> fragments) {
        super(fm);
        mFragments = fragments;
        //mHasGenreTabs = (getmMediaProvider().getGenres() != null && provider.getGenres().size() > 0 ? 1 : 0);

    }

    @Override
    public int getCount() {
        return mFragments.size() + mHasGenreTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mHasGenreTabs > 0 && position == 0) {
            return ButterApplication.getAppContext().getString(R.string.genres).toUpperCase(LocaleUtils.getCurrent());
        }
        position -= mHasGenreTabs;
        return ButterApplication.getAppContext()
                .getString(mFragments.get(position).getMediaProvider().getProviderType().getTitle())
                .toUpperCase(LocaleUtils.getCurrent());
    }

    @Override
    public Fragment getItem(int position) {
        if (mHasGenreTabs > 0 && position == 0) {
            if (mGenreFragment == null) {
                mGenreFragment = MediaGenreSelectionFragment.newInstance(mMediaGenreSelectionFragmentListener);
            }
            return mGenreFragment;
        }

        position -= mHasGenreTabs;

        return mFragments.get(position);
    }

    private MediaGenreSelectionFragment.Listener mMediaGenreSelectionFragmentListener = new MediaGenreSelectionFragment.Listener() {
        @Override
        public void onGenreSelected(String genre) {
            //mGenre = genre;
            for (int i = 0; i < getCount(); i++) {
                MediaListFragment mediaListFragment = mFragments.get(i);
                if (mediaListFragment != null) {
                    mediaListFragment.getMediaProvider().cancel();
                    mediaListFragment.changeGenre(genre);
                }
            }
        }
    };
}
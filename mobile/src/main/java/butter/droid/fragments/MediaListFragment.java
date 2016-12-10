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

package butter.droid.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import javax.inject.Inject;

import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.activities.MediaDetailActivity;
import butter.droid.adapters.MediaGridAdapter;
import butter.droid.base.ButterApplication;
import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.providers.media.MediaProvider;
import butter.droid.base.providers.media.callback.MediaProviderCallback;
import butter.droid.base.providers.media.filters.Filters;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.NetworkUtils;
import butter.droid.base.utils.PrefUtils;
import butter.droid.base.utils.ThreadUtils;
import butter.droid.fragments.dialog.LoadingDetailDialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;
import okhttp3.OkHttpClient;

/**
 * This fragment is the main screen for viewing a collection of media items.
 * <p/>
 * LOADING
 * <p/>
 * This fragment has 2 ways of representing a loading state; If the data is being loaded for the first time, or the media detail for the
 * detail screen is being loaded,a progress layout is displayed with a message.
 * <p/>
 * If a page is being loaded, the adapter will display a progress item.
 * <p/>
 * MODE
 * <p/>
 * This fragment can be instantiated with ether a SEARCH mode, or a NORMAL mode. SEARCH mode simply does not load any initial data.
 */
public class MediaListFragment extends Fragment implements LoadingDetailDialogFragment.Callback {

    public static final String EXTRA_GENRE = "extra_genre";
    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_PROVIDER = "extra_provider";
    public static final String DIALOG_LOADING_DETAIL = "DIALOG_LOADING_DETAIL";

    public static final int LOADING_DIALOG_FRAGMENT = 1;

    private MediaProvider mMediaProvider;

    @Inject
    OkHttpClient client;

    private Context mContext;
    private MediaGridAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private Integer mColumns = 2, mRetries = 0;

    //overrides the default loading message
    private int mLoadingMessage = R.string.loading_data;

    private State mState = State.UNINITIALISED;

    public enum Mode {
        NORMAL, SEARCH
    }

    private enum State {
        UNINITIALISED, LOADING, SEARCHING, LOADING_PAGE, LOADED, LOADING_DETAIL
    }

    private ArrayList<Media> mItems = new ArrayList<>();

    private boolean mEndOfListReached = false;

    private int mTotalItemCount = 0, mLoadingTreshold = mColumns * 3, mPreviousTotal = 0;

    private int mPage = 1;
    private Filters mFilters = new Filters();

    View mRootView;
    @BindView(R.id.progressOverlay)
    LinearLayout mProgressOverlay;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.emptyView)
    TextView mEmptyView;
    @BindView(R.id.progress_textview)
    TextView mProgressTextView;
    private MediaProviderCallback mCallback = new MediaProviderCallback() {
        @Override
        @DebugLog
        public void onSuccess(Filters filters, final ArrayList<Media> items) {
            items.removeAll(mItems);
            mEndOfListReached = items.size() == 0;
            if (!mEndOfListReached) {
                mItems.addAll(items);
                if (isAdded()) {
                    mPage = mPage + 1;
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.setItems(mItems);
                            mPreviousTotal = mTotalItemCount = mAdapter.getItemCount();
                        }
                    });
                }
            }
            setState(State.LOADED);
        }

        @Override
        @DebugLog
        public void onFailure(Exception e) {
            if (!isDetached() || !e.getMessage().equals("Canceled")) {
                e.printStackTrace();
                if (mRetries > 1) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(mRootView, R.string.unknown_error, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    mMediaProvider.getList(mItems, mFilters, this);
                }
                mRetries++;
            }
            mEndOfListReached = true;
            if (mAdapter != null) {
                mAdapter.removeLoading();
            }
            setState(State.LOADED);
        }
    };
    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int visibleItemCount = mLayoutManager.getChildCount();
            mTotalItemCount = mLayoutManager.getItemCount() - (mAdapter.isLoading() ? 1 : 0);
            int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

            if (mState == State.LOADING_PAGE) {
                if (mTotalItemCount > mPreviousTotal) {
                    mPreviousTotal = mTotalItemCount;
                    mPreviousTotal = mTotalItemCount = mLayoutManager.getItemCount();
                    setState(State.LOADED);
                }
            }

            if (!mEndOfListReached && mState != State.SEARCHING && mState != State.LOADING_PAGE && mState != State.LOADING && (mTotalItemCount - visibleItemCount) <= (firstVisibleItem +
                    mLoadingTreshold)) {

                mFilters.setPage(mPage);
                mMediaProvider.getList(mItems, mFilters, mCallback);

                mPreviousTotal = mTotalItemCount = mLayoutManager.getItemCount();
                setState(State.LOADING_PAGE);
            }
        }
    };

    //TODO igual tenemos que volver al modelo anterior del ProviderManager
    public static MediaListFragment newInstance(MediaProvider mediaProvider, Mode mode) {
        return newInstance(mediaProvider, mode, null);
    }

    public static MediaListFragment newInstance(MediaProvider mediaProvider, Mode mode, String genre) {
        MediaListFragment frag = new MediaListFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_MODE, mode);
        args.putString(EXTRA_GENRE, genre);
        args.putSerializable(EXTRA_PROVIDER, mediaProvider);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();

        mRootView = inflater.inflate(R.layout.fragment_media, container, false);
        ButterKnife.bind(this, mRootView);

        mColumns = getResources().getInteger(R.integer.overview_cols);
        mLoadingTreshold = mColumns * 3;

        mLayoutManager = new GridLayoutManager(mContext, mColumns);
        mRecyclerView.setLayoutManager(mLayoutManager);

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(mScrollListener);
        //adapter should only ever be created once on fragment initialise.
        mAdapter = new MediaGridAdapter(mContext, mItems, mColumns);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
    }

    public void changeGenre(String genre) {
        if (!(mFilters.getGenre() == null ? "" : mFilters.getGenre()).equals(genre == null ? "" : genre)) {

            setState(State.LOADING);

            mItems.clear();
            mAdapter.clearItems();

            mFilters.setGenre(genre);
            mFilters.setPage(1);
            mMediaProvider.getList(mFilters, mCallback);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileButterApplication.getAppContext()
                .getComponent()
                .inject(this);
    }

    private void updateLoadingMessage() {
        mProgressTextView.setText(mLoadingMessage);
    }

    @DebugLog
    private void setState(State state) {
        if (mState == state) return;//do nothing
        mState = state;
        updateUI();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMediaProvider = (MediaProvider) getArguments().getSerializable(EXTRA_PROVIDER);

        mFilters.setSort(mMediaProvider.getSortDefault());
        mFilters.setOrder(mMediaProvider.getOrderDefault());
        mFilters.setGenre(getArguments().getString(EXTRA_GENRE));
        String language = PrefUtils.get(getActivity(), Prefs.LOCALE, ButterApplication.getSystemLanguage());
        mFilters.setLangCode(LocaleUtils.toLocale(language).getLanguage());

        Mode mode = (Mode) getArguments().getSerializable(EXTRA_MODE);
        if (mode == Mode.SEARCH) mEmptyView.setText(getString(R.string.no_search_results));

        //don't load initial data in search mode
        if (mode != Mode.SEARCH && mAdapter.getItemCount() == 0) {
            mMediaProvider.getList(mFilters, mCallback);/* fetch new items */
            setState(State.LOADING);
        } else updateUI();
    }

    /**
     * Responsible for updating the UI based on the state of this fragment
     */
    private void updateUI() {
        if (!isAdded()) return;

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (mState) {
                    case LOADING_DETAIL:
                        mLoadingMessage = R.string.loading_details;
                        break;
                    case SEARCHING:
                        mLoadingMessage = R.string.searching;
                        break;
                    case LOADING:
                        if (mAdapter.isLoading()) mAdapter.removeLoading();
                        //show the progress bar
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mRecyclerView.animate().alpha(0.5f).setDuration(500).start();
                        mEmptyView.setVisibility(View.GONE);
                        mProgressOverlay.setVisibility(View.VISIBLE);
                        break;
                    case LOADED:
                        if (mAdapter.isLoading()) mAdapter.removeLoading();
                        mProgressOverlay.setVisibility(View.GONE);
                        boolean hasItems = mItems.size() > 0;
                        //show either the recyclerview or the empty view
                        mRecyclerView.animate().alpha(1.0f).setDuration(100).start();
                        mRecyclerView.setVisibility(hasItems ? View.VISIBLE : View.INVISIBLE);
                        mEmptyView.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                        break;
                    case LOADING_PAGE:
                        //add a loading view to the adapter
                        if (!mAdapter.isLoading()) mAdapter.addLoading();
                        mEmptyView.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        break;
                    default:
                        int providerMessage = getMediaProvider().getLoadingMessage();
                        mLoadingMessage = providerMessage > 0 ? providerMessage : R.string.loading_data;
                        break;
                }
                updateLoadingMessage();
            }
        });
    }

    private MediaGridAdapter.OnItemClickListener mOnItemClickListener = new MediaGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(final View view, final Media item, final int position) {
            /**
             * We shouldn't really be doing the palette loading here without any ui feedback,
             * but it should be really quick
             */
            RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
            if (holder instanceof MediaGridAdapter.ViewHolder) {
                ImageView coverImage = ((MediaGridAdapter.ViewHolder) holder).getCoverImage();

                if (coverImage.getDrawable() == null) {
                    showLoadingDialog(position);
                    return;
                }

                Bitmap cover = ((BitmapDrawable) coverImage.getDrawable()).getBitmap();
                new Palette.Builder(cover).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        int vibrantColor = palette.getVibrantColor(-1);
                        int paletteColor;
                        if (vibrantColor == -1) {
                            paletteColor = palette.getMutedColor(ContextCompat.getColor(getContext(), R.color.primary));
                        } else {
                            paletteColor = vibrantColor;
                        }
                        item.color = paletteColor;
                        showLoadingDialog(position);
                    }
                });
            } else {
                showLoadingDialog(position);
            }

        }
    };

    private void showLoadingDialog(Integer position) {
        LoadingDetailDialogFragment loadingFragment = LoadingDetailDialogFragment.newInstance(position);
        loadingFragment.setTargetFragment(MediaListFragment.this, LOADING_DIALOG_FRAGMENT);
        loadingFragment.show(getFragmentManager(), DIALOG_LOADING_DETAIL);
    }

    public void triggerSearch(String searchQuery) {
        if (!isAdded()) return;
        if (null == mAdapter) return;
        if (!NetworkUtils.isNetworkConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.network_message, Toast.LENGTH_SHORT).show();
            return;
        }

        getMediaProvider().cancel();

        mEndOfListReached = false;

        mItems.clear();
        mAdapter.clearItems();//clear out adapter

        if (searchQuery.equals("")) {
            setState(State.LOADED);
            return; //don't do a search for empty queries
        }

        setState(State.SEARCHING);
        mFilters.setKeywords(searchQuery);
        mFilters.setPage(1);
        mPage = 1;
        mMediaProvider.getList(mFilters, mCallback);
    }


    /**
     * Called when loading media details fails
     */
    @Override
    public void onDetailLoadFailure() {
        Snackbar.make(mRootView, R.string.unknown_error, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Called when media details have been loaded. This should be called on a background thread.
     *
     * @param item The media item
     */
    @Override
    public void onDetailLoadSuccess(final Media item) {
        MediaDetailActivity.startActivity(mContext, item);
    }

    /**
     * Called when loading media details
     * @return mItems
     */
    @Override
    public ArrayList<Media> getCurrentList() {
        return mItems;
    }

    public MediaProvider getMediaProvider() {
        return (MediaProvider) getArguments().getSerializable(EXTRA_PROVIDER);
    }
}

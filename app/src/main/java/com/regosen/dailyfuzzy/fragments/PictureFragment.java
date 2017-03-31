package com.regosen.dailyfuzzy.fragments;


import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.regosen.dailyfuzzy.R;
import com.regosen.dailyfuzzy.activities.PictureActivity;
import com.regosen.dailyfuzzy.models.Post;
import com.regosen.dailyfuzzy.models.PostFetcher;

import static com.bumptech.glide.load.engine.DiskCacheStrategy.SOURCE;

public class PictureFragment extends Fragment {
    public static final String KEY_POST_ID = "KEY_POST_ID";
    public static final String KEY_CAPTION = "KEY_CAPTION";
    public static final String KEY_URL = "KEY_URL";
    public static final String TAG = "PictureFragment";

    public String mImageURL;
    public String mCaption;
    public String mPostId;

    public enum PictureType {
        PIC_NONE,
        PIC_IMAGE,
        PIC_GIF,
        PIC_VIDEO
    }
    public PictureType mPicType;

    ImageView mImageView;
    VideoView mVideoView;
    ProgressBar mProgressBar;

    public Drawable getDrawable() {
        return mImageView.getDrawable();
    }

    private static PictureType getPictureType(String url)
    {
        String ext = Post.getExtension(url);
        if (ext == null || ext.isEmpty())
        {
            return PictureType.PIC_NONE;
        }
        else if (ext.equals("gifv") || ext.equals("mp4") || ext.equals("webm")) {
            return PictureType.PIC_VIDEO;
        }
        else if (ext.equals("gif")) {
            return PictureType.PIC_GIF;
        }
        return PictureType.PIC_IMAGE;
    }

    public static PictureFragment newInstance(Post post) {
        PictureFragment fragment = new PictureFragment();
        Bundle args = new Bundle();

        if (post != null) {
            args.putString(KEY_CAPTION, post.title);
            args.putString(KEY_URL, post.link);
            args.putString(KEY_POST_ID, post.id);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public PictureFragment() {
    }

    private void fragmentVisible() {
        if (mImageView == null) {
            return;
        }
        if (mImageURL == null) {
            mImageView.setImageResource(R.drawable.missing_image);
            mImageView.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.GONE);
            Log.d(TAG, "mImageURL is null");
            return;
        }
        Log.d(TAG, "fragmentVisible " + mImageURL);

        if (mCaption != null) {
            getActivity().setTitle(mCaption);
        }
        PictureActivity.sVisibleFragment = this;
        PostFetcher.getInstance().viewedPost(mPostId);

        if (mPicType == PictureType.PIC_VIDEO) {
            mVideoView.start();
        }
    }

    private void fragmentHidden() {
        if (mPicType == PictureType.PIC_VIDEO) {
            mVideoView.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            fragmentVisible();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!getUserVisibleHint()) {
            fragmentHidden();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            fragmentVisible();
        }
        else {
            fragmentHidden();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_picture, container, false);

        mImageURL = getArguments().getString(KEY_URL);
        mCaption = getArguments().getString(KEY_CAPTION);
        mPostId = getArguments().getString(KEY_POST_ID);
        mPicType = getPictureType(mImageURL);
        mImageView = (ImageView) rootView.findViewById(R.id.img_fuzzy);
        mVideoView = (VideoView) rootView.findViewById(R.id.vid_fuzzy);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

        if (mPicType != PictureType.PIC_NONE)
        {
            boolean isVideo = (mPicType == PictureType.PIC_VIDEO);
            mImageView.setVisibility(isVideo ? View.GONE : View.VISIBLE);
            mVideoView.setVisibility(isVideo ? View.VISIBLE : View.GONE);

            if (mImageURL != null && mImageURL.length() > 0) {
                if (isVideo)
                {
                    try {
                        Uri link = Uri.parse(mImageURL);
                        mProgressBar.setVisibility(View.VISIBLE);
                        mVideoView.setVideoURI(link);
                        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {

                                mProgressBar.setVisibility(View.GONE);
                                mp.setLooping(true);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (mPicType == PictureType.PIC_GIF) {
                    Glide.with(getContext()).load(mImageURL).asGif().diskCacheStrategy(SOURCE).into(mImageView);
                }
                else {
                    Glide.with(getContext()).load(mImageURL).into(mImageView);
                }
            }
            registerForContextMenu(rootView);
        }
        return rootView;
    }
}
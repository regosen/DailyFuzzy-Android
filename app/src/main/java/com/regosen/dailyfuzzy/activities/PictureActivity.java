package com.regosen.dailyfuzzy.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.regosen.dailyfuzzy.R;
import com.regosen.dailyfuzzy.cache.FuzzyCache;
import com.regosen.dailyfuzzy.fragments.PictureFragment;
import com.regosen.dailyfuzzy.models.Post;
import com.regosen.dailyfuzzy.models.PostFetcher;
import com.regosen.dailyfuzzy.utils.FuzzyHelper;
import com.regosen.dailyfuzzy.utils.NotificationUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Calendar;

import static com.regosen.dailyfuzzy.R.id.pager;
import static com.regosen.dailyfuzzy.fragments.PictureFragment.PictureType.PIC_VIDEO;
import static com.regosen.dailyfuzzy.models.PostFetcher.PostType.POST_NEW;

public class PictureActivity extends AppCompatActivity {

    public static final String FIRST_LAUNCH = "FIRST_LAUNCH";

    public static final String KEY_POST_POS = "KEY_POST_POS";
    public static final String KEY_POST_TYPE = "KEY_POST_TYPE";
    public static final String TAG = "PictureActivity";
    public static final int MIN_API_FOR_STORAGE_PERMISSIONS = 23;
    private static final int MAX_PAGES = 1000;
    private PostFetcher mPostFetcher;
    private AlertDialog mInstructionsDialog = null;

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    PostFetcher.PostType mPostType = POST_NEW;

    public static PictureFragment sVisibleFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FuzzyCache.setContext(getApplicationContext());
        setContentView(R.layout.activity_picture);

        Bundle extras = getIntent().getExtras();
        mPostFetcher = PostFetcher.getInstance();
        if (mPostFetcher.needsNetworkFetch()) {
                final ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.popup_loading_title),
                        getString(R.string.popup_loading_message));
                new Thread() {
                    public void run() {
                        mPostFetcher.fetchFromNetwork(); // make sure past fuzzies are retrieved
                        (new Handler(Looper.getMainLooper())).post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                setupPager(PostFetcher.numPastPosts(), POST_NEW);
                            }
                        });
                    }
                }.start();
        } else {
            PostFetcher.PostType postType = POST_NEW;

            int position = 0;
            if (extras != null) {
                position = extras.getInt(KEY_POST_POS);
                postType = (PostFetcher.PostType) extras.getSerializable(KEY_POST_TYPE);
            }
            else if(savedInstanceState != null) {
                position = savedInstanceState.getInt(KEY_POST_POS);
                postType = (PostFetcher.PostType) savedInstanceState.getSerializable(KEY_POST_TYPE);
            }
            setupPager(position, postType);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!sharedPref.getBoolean(FIRST_LAUNCH, false))
        {
            showFirstTimeIntro();

            // setup daily notifications for current time
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            NotificationUtil.setDailyNotifications(getApplicationContext(), true,
                    hour, minute, true);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(FIRST_LAUNCH, true);
            editor.commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        TextView textView = (TextView) toolbar.getChildAt(0);

        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog aboutDialog = new AlertDialog.Builder(PictureActivity.this).create();
                aboutDialog.setMessage(sVisibleFragment.mCaption);
                aboutDialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mInstructionsDialog != null) {
            mInstructionsDialog.dismiss();
            mInstructionsDialog = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mViewPager != null) {
            outState.putInt(KEY_POST_POS, mViewPager.getCurrentItem());
            outState.putSerializable(KEY_POST_TYPE, mPostType);
        }
    }

    private void showFirstTimeIntro() {
        Resources res = getResources();
        mInstructionsDialog = new AlertDialog.Builder(PictureActivity.this).create();
        mInstructionsDialog.setTitle(res.getString(R.string.title_instructions));
        mInstructionsDialog.setMessage(res.getString(R.string.message_instructions));
        mInstructionsDialog.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mInstructionsDialog = null;
                    }
                });
        mInstructionsDialog.show();
    }

    public void setupPager(int startPosition, PostFetcher.PostType postType) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mPostType = postType;

        mViewPager = (ViewPager) findViewById(pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(startPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        switch (mPostType) {
            case POST_NEW:
                menu.findItem(R.id.action_today).setEnabled(false);
                break;
            case POST_PAST:
                //always let the user go back to the list view
                //menu.findItem(R.id.action_past).setEnabled(false);
                break;
            case POST_FAVORITES:
                //always let the user go back to the list view
                //menu.findItem(R.id.action_favorites).setEnabled(false);
                break;
            default:
                break;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (Arrays.asList(permissions).contains(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            saveCurrentImageToGallery();
        }
    }

    // TODO: re-downloading video.  Is there a way to read this directly from VideoView?
    private boolean saveVideoToGallery(Post post) {
        boolean success = false;
        ContentValues values = new ContentValues(1);
        String ext = Post.getExtension(post.link);
        if (ext.equals("gifv"))
        {
            ext = "mp4";
        }
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/" + ext);

        Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

        try {
            URL url = new URL(post.link);
            URLConnection ucon = url.openConnection();
            InputStream is = ucon.getInputStream();
            OutputStream os = getContentResolver().openOutputStream(uri);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
            is.close();
            os.close();
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "exception while writing video: ", e);
        }
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        return success;
    }

    private void saveCurrentImageToGallery() {
        if (sVisibleFragment == null) {
            return;
        }
        Post post = mPostFetcher.getPostById(sVisibleFragment.mPostId);
        if (post == null) {
            return;
        }

        boolean success = true;
        switch (sVisibleFragment.mPicType)
        {
            case PIC_GIF:
            {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/gif");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                try {
                    OutputStream fos = getContentResolver().openOutputStream(uri);
                    GifDrawable gif = (GifDrawable)sVisibleFragment.getDrawable();
                    fos.write(gif.getData());
                    fos.flush();
                    fos.close();
                } catch (IOException ioe) {
                    success = false;
                    ioe.printStackTrace();
                }
            }
            break;

            case PIC_IMAGE:
            {
                Bitmap bitmap = ((GlideBitmapDrawable)sVisibleFragment.getDrawable()).getBitmap();
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, post.title, null);
            }
            break;

            case PIC_VIDEO:
                success = saveVideoToGallery(post);
                break;

            default:
                break;
        }

        if (success) {
            this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void shareOnFacebook(String caption, String imageURL) {
        if (!ShareDialog.canShow(ShareLinkContent.class)) {
            Toast.makeText(getApplicationContext(), getString(R.string.share_error), Toast.LENGTH_SHORT).show();
            return;
        }

        // replace gif/video with still image
        String stillImageURL = imageURL.replace(".gifv", "l.gif");
        if (sVisibleFragment.mPicType == PIC_VIDEO)
        {
            Post post = mPostFetcher.getPostById(sVisibleFragment.mPostId);
            if (post == null) {
                return;
            }
            stillImageURL = post.thumb;
        }

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentTitle(caption)
                .setContentDescription(getString(R.string.share_description))
                .setContentUrl(Uri.parse(imageURL))
                .setImageUrl(Uri.parse(stillImageURL))
                .build();
        ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.show(content, ShareDialog.Mode.FEED);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_save: {
                boolean hasPermissions = true;
                if (Build.VERSION.SDK_INT >= MIN_API_FOR_STORAGE_PERMISSIONS) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        hasPermissions = false;
                        ActivityCompat.requestPermissions(this, new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                0);
                    }
                }
                if (hasPermissions) {
                    new Thread() {
                        public void run() {
                            saveCurrentImageToGallery();
                        }
                    }.start();
                }
            }
                return true;

            case R.id.action_favorites_add:
                mPostFetcher.addToFavorites(sVisibleFragment.mPostId);
                Toast.makeText(getApplicationContext(), getString(R.string.toast_favorites_added), Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_favorites_del:
                mPostFetcher.removeFromFavorites(sVisibleFragment.mPostId);
                Toast.makeText(getApplicationContext(), getString(R.string.toast_favorites_removed), Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_share:
                shareOnFacebook(sVisibleFragment.mCaption, sVisibleFragment.mImageURL);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_picture, menu);
        if (mPostFetcher.isInFavorites(sVisibleFragment.mPostId))
        {
            MenuItem itemAdd = menu.findItem(R.id.action_favorites_add);
            itemAdd.setVisible(false);
        }
        else
        {
            MenuItem itemDel = menu.findItem(R.id.action_favorites_del);
            itemDel.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = FuzzyHelper.handleMenuNavigation(item.getItemId(), getApplicationContext());
        if (intent != null) {
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Post post = mPostFetcher.getPostAt(mPostType, position);
            return PictureFragment.newInstance(post);
        }

        @Override
        public int getCount() {
            return (mPostType == PostFetcher.PostType.POST_FAVORITES) ? PostFetcher.numFavorites() : MAX_PAGES;
        }
    }
}

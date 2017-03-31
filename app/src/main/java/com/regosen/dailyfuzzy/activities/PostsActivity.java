package com.regosen.dailyfuzzy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.regosen.dailyfuzzy.R;
import com.regosen.dailyfuzzy.fragments.PostsFragment;
import com.regosen.dailyfuzzy.models.PostFetcher;
import com.regosen.dailyfuzzy.utils.FuzzyHelper;

/**
 * based on template code provided by Hathy
 */
public class PostsActivity extends AppCompatActivity {
     
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem itemPast = menu.findItem(R.id.action_past);
        itemPast.setEnabled(false);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = FuzzyHelper.handleMenuNavigation(item.getItemId(), getApplicationContext());
        if (intent != null) {
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);
        addFragment();
        setTitle(getString(R.string.action_past));
    }
     
    void addFragment(){
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.fragments_holder
                 , PostsFragment.newInstance(PostFetcher.PostType.POST_PAST))
            .commit();
    }
}
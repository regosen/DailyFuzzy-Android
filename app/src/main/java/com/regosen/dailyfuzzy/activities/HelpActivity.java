package com.regosen.dailyfuzzy.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.regosen.dailyfuzzy.R;
import com.regosen.dailyfuzzy.utils.FuzzyHelper;

public class HelpActivity extends AppCompatActivity {

    private static final String TAG = HelpActivity.class.getSimpleName();

    View mButtonAbout;
    View mButtonShowInstructionPage;
    View mButtonHowToPost;
    View mButtonEmailDeveloper;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem itemPast = menu.findItem(R.id.action_help);
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

    private void showAboutMessage()
    {
        Resources res = getResources();
        String title = res.getString(R.string.title_about);

        try {
            String packageName = getApplicationContext().getPackageName();
            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(packageName, 0);
            String version = pInfo.versionName;
            title = String.format(title, version);
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting PackageInfo " + e.getMessage());
        }

        AlertDialog aboutDialog = new AlertDialog.Builder(HelpActivity.this).create();
        aboutDialog.setTitle(title);
        aboutDialog.setMessage(res.getString(R.string.desc_about));
        aboutDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        aboutDialog.show();
    }

    private void showInstructionPage()
    {
        Resources res = getResources();
        AlertDialog instructionsDialog = new AlertDialog.Builder(HelpActivity.this).create();
        instructionsDialog.setTitle(res.getString(R.string.title_instructions));
        instructionsDialog.setMessage(res.getString(R.string.message_instructions));
        instructionsDialog.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        instructionsDialog.show();
    }

    private void showHowToPost()
    {
        Resources res = getResources();
        AlertDialog postDialog = new AlertDialog.Builder(HelpActivity.this).create();
        postDialog.setTitle(res.getString(R.string.title_how_to_post));
        postDialog.setMessage(res.getString(R.string.message_how_to_post));
        postDialog.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        postDialog.setButton(AlertDialog.BUTTON_POSITIVE, res.getString(R.string.button_see_reddit),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.reddit.com"));
                        startActivity(browserIntent);
                    }
                });
        postDialog.show();
    }

    private void emailDeveloper()
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"rego@dailyfuzzy.com"});
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setTitle(getString(R.string.action_help));

        mButtonAbout = findViewById(R.id.layout_about);
        mButtonShowInstructionPage = findViewById(R.id.layout_show_instruction);
        mButtonHowToPost = findViewById(R.id.layout_how_to_post);
        mButtonEmailDeveloper = findViewById(R.id.layout_email_dev);

        mButtonAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showAboutMessage(); } });

        mButtonShowInstructionPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showInstructionPage(); } });

        mButtonHowToPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showHowToPost(); } });

        mButtonEmailDeveloper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { emailDeveloper(); } });
    }

}
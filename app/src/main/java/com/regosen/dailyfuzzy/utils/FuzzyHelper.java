package com.regosen.dailyfuzzy.utils;

import android.content.Context;
import android.content.Intent;

import com.regosen.dailyfuzzy.R;
import com.regosen.dailyfuzzy.activities.FavoritesActivity;
import com.regosen.dailyfuzzy.activities.HelpActivity;
import com.regosen.dailyfuzzy.activities.PictureActivity;
import com.regosen.dailyfuzzy.activities.PostsActivity;
import com.regosen.dailyfuzzy.activities.SettingsActivity;
import com.regosen.dailyfuzzy.models.PostFetcher;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FuzzyHelper {

    public static Intent handleMenuNavigation(int id, Context context) {
        Class intentClass = null;
        Intent intent = null;

        switch (id) {

            case R.id.action_today:
                intent = new Intent(context, PictureActivity.class);
                intent.putExtra(PictureActivity.KEY_POST_POS, PostFetcher.numPastPosts() - 1);
                intent.putExtra(PictureActivity.KEY_POST_TYPE, PostFetcher.PostType.POST_NEW);
                break;

            case R.id.action_past:
                intentClass = PostsActivity.class;
                break;

            case R.id.action_favorites:
                intentClass = FavoritesActivity.class;
                break;

            case R.id.action_settings:
                intentClass = SettingsActivity.class;
                break;

            case R.id.action_help:
                intentClass = HelpActivity.class;
                break;

            default:
                break;
        }

        if (intentClass != null) {
            intent = new Intent(context, intentClass);
        }
        return intent;
    }


    // based on http://www.splinter.com.au/2014/09/16/storing-secret-keys/
    private static byte[] getSHA512Hash(byte[] stringBytes){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] obfuscator = md.digest(FuzzyHelper.class.getName().getBytes(Charset.forName( "UTF-8" ) ));
            byte[] obfuscated = stringBytes.clone();

            assert(stringBytes.length <= obfuscator.length);
            for(int i=0; i< stringBytes.length ;i++){
                obfuscated[i] = (byte)(stringBytes[i] ^ obfuscator[i]);
            }
            return obfuscated;
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }

    // use this to generate new byte array to deobfuscate later
    public static byte[] obfuscate(String stringToHash){
        byte[] stringBytes = stringToHash.getBytes(Charset.forName("UTF-8"));
        return getSHA512Hash(stringBytes);
    }

    public static String deobfuscate(byte[] obfuscatedData){
        byte[] deobfuscatedData = getSHA512Hash(obfuscatedData);
        return new String(deobfuscatedData, Charset.forName("UTF-8"));
    }
}
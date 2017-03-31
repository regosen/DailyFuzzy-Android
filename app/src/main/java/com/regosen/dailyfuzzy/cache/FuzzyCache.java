package com.regosen.dailyfuzzy.cache;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * based on template code provided by Hathy
 */
public abstract class FuzzyCache {

    private String cacheFullPath = null;

    protected String cachePrefix = "fuzzy";

    // HACK: I don't know how to Android
    protected static Context ctx = null;

    public static void setContext(Context context) {
        ctx = context;
    }

    public FuzzyCache(String prefix) {
        cachePrefix = prefix;
    }

    public String convertToCacheName(String url){
        try {            
            MessageDigest digest=MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            byte[] b=digest.digest();
            BigInteger bi=new BigInteger(b);
            return cachePrefix + "_" + bi.toString(16) + ".cac";
        } catch (Exception e) {
            Log.d("ERROR", e.toString());
            return null;
        }
    }
     
    public byte[] read(String url){
        try{
            FileInputStream fis = ctx.openFileInput(convertToCacheName(url));
            byte data[]=new byte[fis.available()];
            fis.read(data);
            fis.close();
            return data;
        }catch(Exception e) {
            Log.d("ERROR", e.toString());
            return null;
        }
    }
     
    public void write(String url, String data){
        try{
            FileOutputStream fos = ctx.openFileOutput(convertToCacheName(url), Context.MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
        }catch(Exception e) {
            Log.d("ERROR", e.toString());
        }
    }
}
package com.regosen.dailyfuzzy.models;

import android.util.Log;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a class that holds the data of the JSON objects
 * returned by the Reddit API.
 * 
 * original author Hathy, modified by Rego Sen
 */
public class Post {

    private static final String IMAGE_URL_BASE = "http://i.imgur.com/";

    private static final String JSON_TAG_TITLE = "title";
    private static final String JSON_TAG_ID = "id";
    private static final String JSON_TAG_URL_PRE = "url";
    private static final String JSON_TAG_URL_POST = "link";
    private static final String JSON_TAG_THUMB = "thumbnail";

    private static List<String> ALLOWED_EXTENSIONS;

    public String title;
    public String id;
    public String thumb;
    public String link;

    public static String getExtension(String url)
    {
        if (url == null)
        {
            return null;
        }
        return url.substring(url.lastIndexOf(".") + 1, url.length()).toLowerCase();
    }

    private static boolean fileAllowed(String fileName) {
        String ext = getExtension(fileName);
        if (ALLOWED_EXTENSIONS == null)
        {
            // TODO: is there a cleaner way to do this?
            String[] temp = {"jpg","jpeg","png","gif","gifv","mp4","webm"};
            ALLOWED_EXTENSIONS = Arrays.asList(temp);
        }
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();
        try {
            json.put(JSON_TAG_TITLE, title);
            json.put(JSON_TAG_ID, id);
            json.put(JSON_TAG_THUMB, thumb);
            json.put(JSON_TAG_URL_POST, link);
        } catch(Exception e){
            Log.e("Post.toJSON",e.toString());
            return null;
        }
        return json;
    }

    public Post(JSONObject json, boolean cached)
    {
        title = json.optString(JSON_TAG_TITLE).replace("&amp;","&");
        id=json.optString(JSON_TAG_ID);
        thumb = json.optString(JSON_TAG_THUMB);
        if (cached) {
            link = json.optString(JSON_TAG_URL_POST);
        }
        else if ((title != null) && (id != null))
        {
            String url = json.optString(JSON_TAG_URL_PRE);
            Pattern pattern = Pattern.compile(".*imgur\\.com/(\\w+)\\.(\\w+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.matches()) {
                String imgurId = matcher.group(1);
                String base = IMAGE_URL_BASE + imgurId;
                String ext = matcher.group(2).toLowerCase();
                if (ext.equals("gifv")) {
                    link = base + ".mp4";
                }
                else {
                    link = base + "l." + ext;
                }
                thumb = base + "s.jpg";
            }
            /*
            else if (url.contains("/gfycat.com/"))
            {
                link = url.replace("/gfycat.com/","/zippy.gfycat.com/") + ".webm";
            }
            */
            else if (fileAllowed(url))
            {
                link = url;
            }
        }
    }

    public boolean isValid() { return (link != null); }

}
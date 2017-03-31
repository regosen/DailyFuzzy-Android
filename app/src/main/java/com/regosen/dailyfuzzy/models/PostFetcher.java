package com.regosen.dailyfuzzy.models;

import android.util.Log;

import com.regosen.dailyfuzzy.cache.PostsCache;
import com.regosen.dailyfuzzy.utils.PostFilter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.regosen.dailyfuzzy.models.PostFetcher.PostType.POST_PAST;
import static com.regosen.dailyfuzzy.models.PostFetcher.PostType.POST_FAVORITES;

/**
 * This is the class that creates Post objects out of the Reddit
 * API, and maintains a list of these posts for other classes
 * to use.
 *
 * original author Hathy, modified by Rego Sen
 */
public class PostFetcher {

    private static Integer maxPostsPerQuery = 20;
    private static Integer maxPostsBeforeQueryingMore = 3;
    private static final String JSON_POSTS_ROOT_NAME = "posts";

    private static PostFetcher mInstance = null;

    public static PostFetcher getInstance(){
        if(mInstance == null) {
            mInstance = new PostFetcher();
        }
        return mInstance;
    }

    public enum PostType {
        POST_PAST,
        POST_NEW,
        POST_FAVORITES
    }

    private List<Post> jsonToPostList(String json)
    {
        List<Post> postList = new ArrayList<>();
        try {
            JSONArray posts = new JSONObject(json).getJSONArray(JSON_POSTS_ROOT_NAME);
            for(int i=0;i<posts.length();i++){
                JSONObject cur=posts.getJSONObject(i);
                Post post = new Post(cur, true);
                if (post != null) {
                    postList.add(post);
                }
            }
        } catch(Exception e){
            Log.e("jsonToPostList()",e.toString());
        }
        return postList;
    }

    private final String URL_TEMPLATE=
                "https://www.reddit.com/r/aww/hot/"
               +".json"
               +"?limit=LIMIT";

    // we want to cache past fuzzies (but only the ones we've viewed)
    List<Post> pastFuzzies = new ArrayList<>();
    List<Post> favorites = new ArrayList<>();
    List<Post> newPosts = new ArrayList<>();
    HashMap<String, Post> postsById = new HashMap<String, Post>();

    private PostFilter mPostFilter;

    String limit;
    String after;


    PostFetcher(){
        limit=maxPostsPerQuery.toString();
        after="";
        mPostFilter = PostFilter.getInstance();

        byte[] pastCache= PostsCache.getInstance().read(POST_PAST.toString());
        if(pastCache!=null) {
            String jsonString = new String(pastCache);
            pastFuzzies = jsonToPostList(jsonString);
        }

        byte[] favoritesCache= PostsCache.getInstance().read(POST_FAVORITES.toString());
        if(favoritesCache!=null) {
            String jsonString = new String(favoritesCache);
            favorites = jsonToPostList(jsonString);
        }
        for (Post post : pastFuzzies) {
            postsById.put(post.id, post);
        }
        for (Post post : favorites) {
            postsById.put(post.id, post);
        }

    }

    public void fetchFromNetwork()
    {
        while (needsNetworkFetch())
        {
            fetchInternal();
        }
    }

    public boolean needsNetworkFetch()
    {
        return (newPosts.size() < maxPostsBeforeQueryingMore);
    }

    public List<Post> getPosts(PostType postType)
    {
        switch (postType)
        {
            case POST_NEW:
                return newPosts;

            case POST_FAVORITES:
                return favorites;

            case POST_PAST:
            default:
                return pastFuzzies;
        }
    }

    public static int numPastPosts()
    {
        return PostFetcher.getInstance().getPosts(POST_PAST).size();
    }

    public static int numFavorites()
    {
        return PostFetcher.getInstance().getPosts(POST_FAVORITES).size();
    }

    public void viewedPost(String id)
    {
        Post post = getPostById(id);
        if (newPosts.contains(post)) {
            newPosts.remove(post);
            pastFuzzies.add(post);

            JSONArray jsonArray = new JSONArray();
            for (Post curPost : pastFuzzies) {
                jsonArray.put(curPost.toJSON());
            }
            JSONObject jsonRoot = new JSONObject();
            try {
                jsonRoot.put(JSON_POSTS_ROOT_NAME, jsonArray);
            } catch(Exception e){
                Log.e("fetchPosts()",e.toString());
            }
            PostsCache.getInstance().write(POST_PAST.toString(), jsonRoot.toString());

            if (newPosts.size() < maxPostsBeforeQueryingMore)
            {
                new Thread() {
                    public void run() {
                        fetchFromNetwork();
                    }
                }.start();
            }
        }
    }

    public Post getPostAt(PostType postType, int position)
    {
        List<Post> posts = newPosts;

        if (postType == POST_FAVORITES) {
            posts = favorites;
        }
        else if (position < pastFuzzies.size()) {
            posts = pastFuzzies;
        }
        else {
            // past and future fuzzies are listed together, hence this position continuum
            position -= pastFuzzies.size();
        }
        return (position >= 0 && position < posts.size()) ? posts.get(position) : null;
    }

    public Post getPostById(String id)
    {
        return postsById.get(id);
    }

    public boolean isInFavorites(String id)
    {
        Post post = getPostById(id);
        return favorites.contains(post);
    }

    public void addToFavorites(String id)
    {
        Post post = getPostById(id);
        if (!favorites.contains(post)) {
            favorites.add(post);
            updateFavorites();
        }
    }

    public void removeFromFavorites(String id)
    {
        Post post = getPostById(id);
        favorites.remove(post);
        updateFavorites();
    }

    private void updateFavorites()
    {
        JSONArray jsonArray = new JSONArray();
        for (Post curPost : favorites) {
            jsonArray.put(curPost.toJSON());
        }
        JSONObject jsonRoot = new JSONObject();
        try {
            jsonRoot.put(JSON_POSTS_ROOT_NAME, jsonArray);
        } catch(Exception e){
            Log.e("fetchPosts()",e.toString());
        }
        PostsCache.getInstance().write(POST_FAVORITES.toString(), jsonRoot.toString());
    }

    private void fetchInternal(){
        String url=URL_TEMPLATE.replace("LIMIT", limit);
        if (!after.isEmpty())
        {
            url += "&after=" + after;
        }
        String raw= QueryFetcher.readContents(url);
        try{
            JSONObject data=new JSONObject(raw)
                                .getJSONObject("data");
            JSONArray children=data.getJSONArray("children");

            //Using this property we can fetch the next set of
            //posts from the same subreddit
            after=data.getString("after");
            for(int i=0;i<children.length();i++){
                JSONObject cur=children.getJSONObject(i)
                                    .getJSONObject("data");
                Post p = new Post(cur, false);
                if (!p.isValid()) {
                    continue;
                }
                if (postsById.containsKey(p.id)) {
                    continue;
                }
                if (!mPostFilter.allowPostTitle(p.title.toLowerCase())) {
                    continue;
                }

                if (p.isValid() && !postsById.containsKey(p.id)) {
                    newPosts.add(p);
                    postsById.put(p.id, p);
                }
            }
        }catch(Exception e){
            Log.e("fetchPosts()",e.toString());
        }
    }
}
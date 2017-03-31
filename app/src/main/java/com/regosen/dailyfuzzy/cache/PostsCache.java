package com.regosen.dailyfuzzy.cache;

/**
 * Created by regosen on 1/26/17.
 */

public class PostsCache extends FuzzyCache {

	public PostsCache() {
		super("posts");
	}

	// using singleton just to override functionality (static methods cannot be overridden), is there a better way?
	private static PostsCache mInstance = null;

	public static PostsCache getInstance() {
		if(mInstance == null)
		{
			mInstance = new PostsCache();
		}
		return mInstance;
	}
}

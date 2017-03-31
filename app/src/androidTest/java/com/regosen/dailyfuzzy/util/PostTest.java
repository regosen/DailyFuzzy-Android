package com.regosen.dailyfuzzy.util;

import android.util.Log;

import com.regosen.dailyfuzzy.models.Post;
import com.regosen.dailyfuzzy.utils.FuzzyHelper;

import junit.framework.TestCase;

import org.json.JSONObject;

/**
 * Created by regosen on 3/19/17.
 */
public class PostTest extends TestCase {

	public void testValidPosts() {
		assertEquals(true, createTestPost("http://example.com/test.jpeg").isValid());
		assertEquals(true, createTestPost("http://example.com/test.jpg").isValid());
		assertEquals(true, createTestPost("http://example.com/test.gif").isValid());
		assertEquals(true, createTestPost("http://example.com/test.png").isValid());
		assertEquals(true, createTestPost("http://example.com/test.gifv").isValid());
		assertEquals(true, createTestPost("http://example.com/test.mp4").isValid());
		assertEquals(true, createTestPost("http://example.com/test.webm").isValid());
	}

	public void testInvalidPosts() {
		assertEquals(false, createTestPost("http://example.com/test.html").isValid());
		assertEquals(false, createTestPost("http://example.com/test.bmp").isValid());
		assertEquals(false, createTestPost("http://example.com/test").isValid());
	}

	public void testConversion() {
		Post testPost = createTestPost("http://example.com/test.jpeg");
		Post reconvertedPost = new Post(testPost.toJSON(), false);

		assertEquals(false, reconvertedPost.equals(testPost));
	}

	public void testExtension() {
		assertEquals("jpeg", Post.getExtension("http://example.com/test.jpeg"));
		assertEquals("gif", Post.getExtension("http://example.com/test.gotcha.GIF"));
		assertEquals(null, Post.getExtension(null));
	}

	public void testObfuscation() {
		String testString = "a,b,c,d,e";
		byte[] obfuscatedString = FuzzyHelper.obfuscate(testString);
		String reconvertedString = FuzzyHelper.deobfuscate(obfuscatedString);
		assertEquals(testString, reconvertedString);

		// copy console output to generate a new obfuscated variable
		StringBuilder sb = new StringBuilder();
		sb.append("new byte[] {");
		for (byte num : obfuscatedString) {
			sb.append(String.format("(byte)0x%02X,", num));
		}
		sb.append("};");
		Log.d(PostTest.class.getName(), sb.toString());
	}

	// ------------------------------------

	private Post createTestPost(String url)
	{
		try
		{
			JSONObject postJSON = new JSONObject();
			postJSON.put("title", "test");
			postJSON.put("url", url);
			postJSON.put("id", "test");
			postJSON.put("thumbnail", url);
			Post post = new Post(postJSON, false);
			return post;
		}catch(Exception e){
			Log.e("createTestPost()",e.toString());
		}
		return null;
	}

}


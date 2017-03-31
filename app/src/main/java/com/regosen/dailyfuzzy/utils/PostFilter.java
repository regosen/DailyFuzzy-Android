package com.regosen.dailyfuzzy.utils;

import android.content.res.Resources;

import com.regosen.dailyfuzzy.DailyFuzzyApp;
import com.regosen.dailyfuzzy.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PostFilter {

    private static PostFilter mInstance = null;

    public static PostFilter getInstance(){
        if(mInstance == null) {
            mInstance = new PostFilter();
        }
        return mInstance;
    }

    // filter posts by caption text
    private List<String> toxicSubstrings; // swear words that children should not see
    private List<Pattern> whitelistRegexes; // word prefixes that indicate this is a picture of animals
    private List<Pattern> overridableBlacklistRegexes; // substrings that indicate this is a picture of non-animals
    private List<Pattern> nonOverridableBlacklistRegexes; // substrings to avoid, even if it's a picture of animals

    // obfuscating toxic words, to make sure they can't be seen even when inspecting app data
    static private byte[] getToxicSubstringData() {
        return new byte[] {
                (byte)0x10,
                (byte)0x33,
                (byte)0x19,
                (byte)0x5C,
                (byte)0xED,
                (byte)0x20,
                (byte)0xE9,
                (byte)0xA2,
                (byte)0xD3,
                (byte)0xF9,
                (byte)0x21,
                (byte)0x53,
                (byte)0x9B,
                (byte)0xFF,
                (byte)0xBC,
                (byte)0xD2,
                (byte)0x03,
                (byte)0xB1,
                (byte)0x90,
                (byte)0x62,
                (byte)0xE7,
                (byte)0x41,
                (byte)0xCE,
                (byte)0xF2,
                (byte)0x63,
                (byte)0xA9,
                (byte)0x38,
                (byte)0x52,
                (byte)0x87,
                (byte)0x06,
                (byte)0xB2,
                (byte)0x4C,
        };
    }

    private List<Pattern> regexArrayFromPrefixes(String[] prefixes) {
        List<Pattern> patterns = new ArrayList<Pattern>(prefixes.length);
        for (String prefix : prefixes)
        {
            // prepend \b to match any word that starts with a keyword
            patterns.add(Pattern.compile("\\b" + prefix));
        }
        return patterns;
    }

    PostFilter(){
        Resources res = DailyFuzzyApp.getAppResources();
        whitelistRegexes = regexArrayFromPrefixes(res.getStringArray(R.array.whitelist_prefixes));
        overridableBlacklistRegexes = regexArrayFromPrefixes(res.getStringArray(R.array.overridable_blacklist_prefixes));
        nonOverridableBlacklistRegexes = regexArrayFromPrefixes(res.getStringArray(R.array.non_overridable_blacklist_prefixes));
        toxicSubstrings = Arrays.asList(FuzzyHelper.deobfuscate(getToxicSubstringData()).split(","));
    }

    public boolean allowPostTitle(String titleLower) {

        for (String keyword : toxicSubstrings) {
            if (titleLower.contains(keyword)) {
                return false;
            }
        }

        for (Pattern pattern : nonOverridableBlacklistRegexes) {
            if (pattern.matcher(titleLower).find()) {
                return false;
            }
        }

        for (Pattern pattern : whitelistRegexes) {
            if (pattern.matcher(titleLower).find()) {
                return true;
            }
        }

        for (Pattern pattern : overridableBlacklistRegexes) {
            if (pattern.matcher(titleLower).find()) {
                return false;
            }
        }

        return true;
    }

}
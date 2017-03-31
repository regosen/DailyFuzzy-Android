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
        return new byte[] {(byte)0xC4,(byte)0x0D,(byte)0xAA,(byte)0x11,(byte)0xC8,(byte)0x5B,(byte)0x7A,(byte)0x0F,(byte)0xAD,(byte)0xD4,(byte)0x9C,(byte)0xAD,(byte)0x09,(byte)0xD6,(byte)0xCD,(byte)0x29,(byte)0xA7,(byte)0x6F,(byte)0x7B,(byte)0xB6,(byte)0xE9,(byte)0x34,(byte)0x8E,(byte)0x4C,(byte)0x8E,(byte)0x77,(byte)0x8C,(byte)0x05,(byte)0x5A,(byte)0x87,(byte)0x0E,(byte)0x89};
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
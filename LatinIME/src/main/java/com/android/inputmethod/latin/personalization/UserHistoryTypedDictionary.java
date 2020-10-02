package com.android.inputmethod.latin.personalization;

import android.content.Context;
import android.util.Log;

import com.android.inputmethod.annotations.ExternallyReferenced;
import com.android.inputmethod.annotations.UsedForTesting;
import com.android.inputmethod.latin.BinaryDictionary;
import com.android.inputmethod.latin.Dictionary;
import com.android.inputmethod.latin.ExpandableBinaryDictionary;
import com.android.inputmethod.latin.NgramContext;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.define.ProductionFlags;
import com.android.inputmethod.latin.makedict.DictionaryHeader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

/**
 * AnhNDd: từ điển lưu trữ kiểu gõ telex mà người dùng đã nhập để gõ tiếng việt
 * Từ điền này giống y hệt với từ điển UserHistory, chỉ khác nội dung
 */
public class UserHistoryTypedDictionary extends ExpandableBinaryDictionary {

    static final String NAME = UserHistoryTypedDictionary.class.getSimpleName();

    UserHistoryTypedDictionary(Context context, Locale locale, @Nullable String account) {
        super(context, getUserHistoryTypedDictName(NAME, locale, null , account), locale, Dictionary.TYPE_USER_HISTORY_TYPED, null);
        if (mLocale != null && mLocale.toString().length() > 1) {
            reloadDictionaryIfRequired();
        }
    }

    /**
     * @returns the name of the {@link UserHistoryTypedDictionary}.
     */
    @UsedForTesting
    protected static String getUserHistoryTypedDictName(final String name, final Locale locale,
                                                        @Nullable final File dictFile, @Nullable final String account) {
        if (!ProductionFlags.ENABLE_PER_ACCOUNT_USER_HISTORY_DICTIONARY) {
            return getDictName(name, locale, dictFile);
        }
        return getUserHistoryTypedDictNamePerAccount(name, locale, dictFile, account);
    }

    /**
     * Uses the currently signed in account to determine the dictionary name.
     */
    private static String getUserHistoryTypedDictNamePerAccount(final String name, final Locale locale,
                                                                @Nullable final File dictFile, @Nullable final String account) {
        if (dictFile != null) {
            return dictFile.getName();
        }
        String dictName = name + "." + locale.toString();
        if (account != null) {
            dictName += "." + account;
        }
        return dictName;
    }

    // Note: This method is called by {@link DictionaryFacilitator} using Java reflection.
    @SuppressWarnings("unused")
    @ExternallyReferenced
    public static UserHistoryTypedDictionary getDictionary(final Context context, final Locale locale,
                                                           final File dictFile, final String dictNamePrefix, @Nullable final String account) {
        return BkavPersonalizationHelper.getUserHistoryTypedDictionary(context, locale, account);
    }

    /**
     * Add a word to the user history dictionary.
     *
     * @param UserHistoryTypedDictionary the user history dictionary
     * @param ngramContext               the n-gram context
     * @param word                       the word the user inputted
     * @param isValid                    whether the word is valid or not
     * @param timestamp                  the timestamp when the word has been inputted
     */
    public static void addToDictionary(final ExpandableBinaryDictionary UserHistoryTypedDictionary,
                                       @Nonnull final NgramContext ngramContext, final String word, final boolean isValid,
                                       final int timestamp) {
        if (word.length() > BinaryDictionary.DICTIONARY_MAX_WORD_LENGTH) {
            return;
        }
        UserHistoryTypedDictionary.updateEntriesForWord(ngramContext, word,
                isValid, 1 /* count */, timestamp);
    }

    @Override
    public void close() {
        // Flush pending writes.
        asyncFlushBinaryDictionary();
        super.close();
    }

    @Override
    protected Map<String, String> getHeaderAttributeMap() {
        final Map<String, String> attributeMap = super.getHeaderAttributeMap();
        attributeMap.put(DictionaryHeader.HAS_HISTORICAL_INFO_KEY,
                DictionaryHeader.ATTRIBUTE_VALUE_TRUE);
        return attributeMap;
    }

    @Override
    protected void loadInitialContentsLocked() {
        // No initial contents.
    }

    @Override
    public boolean isValidWord(final String word) {
        // Strings out of this dictionary should not be considered existing words.
        return false;
    }
}

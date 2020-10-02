package com.bkav;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.UserDictionary;

import com.android.inputmethod.annotations.ExternallyReferenced;
import com.android.inputmethod.latin.BinaryDictionary;
import com.android.inputmethod.latin.UserBinaryDictionary;

import java.io.File;
import java.util.Locale;

import javax.annotation.Nullable;

import static com.android.inputmethod.latin.define.DecoderSpecificConstants.DICTIONARY_MAX_WORD_SHORT_CUT_LENGTH;

/**
 * AnhNDd: quản lý việc lưu từ điển gõ tắt
 */
public class BkavUserBinaryDictionary extends UserBinaryDictionary {
    protected BkavUserBinaryDictionary(Context context, Locale locale, boolean alsoUseMoreRestrictiveLocales, File dictFile, String name) {
        super(context, locale, alsoUseMoreRestrictiveLocales, dictFile, name);
    }

    @Override
    protected void addWordsLocked(Cursor cursor) {
        final boolean hasShortcutColumn = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
        if (cursor == null) return;
        if (cursor.moveToFirst()) {
            final int indexWord = cursor.getColumnIndex(UserDictionary.Words.WORD);
            final int indexShortcut = hasShortcutColumn ? cursor.getColumnIndex(UserDictionary.Words.SHORTCUT) : 0;
            final int indexFrequency = cursor.getColumnIndex(UserDictionary.Words.FREQUENCY);
            while (!cursor.isAfterLast()) {
                final String word = cursor.getString(indexWord);
                final String shortcut = hasShortcutColumn ? cursor.getString(indexShortcut) : null;
                final int frequency = cursor.getInt(indexFrequency);
                final int adjustedFrequency = scaleFrequencyFromDefaultToLatinIme(frequency);
                // Safeguard against adding really long words.
                if (word.length() <= MAX_WORD_LENGTH) {
                    runGCIfRequiredLocked(true);
                    addUnigramLocked(word, adjustedFrequency, false,
                            false,
                            BinaryDictionary.NOT_A_VALID_TIMESTAMP);
                    if (null != shortcut && shortcut.length() <= MAX_WORD_LENGTH) {
                        runGCIfRequiredLocked(true);
                        addUnigramLocked(shortcut, adjustedFrequency, word,
                                USER_DICT_SHORTCUT_FREQUENCY, true,
                                false, BinaryDictionary.NOT_A_VALID_TIMESTAMP);
                    }
                }
                cursor.moveToNext();
            }
        }
    }

    // Note: This method is called by {@link DictionaryFacilitator} using Java reflection.
    @ExternallyReferenced
    public static UserBinaryDictionary getDictionary(
            final Context context, final Locale locale, final File dictFile,
            final String dictNamePrefix, @Nullable final String account) {
        return new BkavUserBinaryDictionary(
                context, locale, false /* alsoUseMoreRestrictiveLocales */,
                dictFile, dictNamePrefix + NAME);
    }
}

package com.bkav;

import android.content.Context;
import androidx.annotation.Nullable;

import com.android.inputmethod.annotations.ExternallyReferenced;
import com.android.inputmethod.latin.Dictionary;
import com.android.inputmethod.latin.ExpandableBinaryDictionary;
import com.android.inputmethod.latin.common.Constants;

import java.io.*;
import java.util.Locale;

/**
 * AnhNDd: Một từ điển dùng để tự động sửa từ theo kiểu gõ telex của tiếng việt
 * từ điển này có word là kiểu gõ telex phổ biến giống như kiểu viết chữ bình thường, còn shortcut là từ tiếng việt
 * được tạo ra từ đó, lựa chọn cách này để thuận tiện trong việc lấy ra đúng từ tiếng việt để tự động sửa từ
 */
public class BkavAutoCorrectBinaryDictionnary extends ExpandableBinaryDictionary {

    private static final String NAME = BkavAutoCorrectBinaryDictionnary.class.getName();

    /**
     * Creates a new expandable binary dictionary.
     *
     * @param context  The application context of the parent.
     * @param dictName The name of the dictionary. Multiple instances with the same
     *                 name is supported.
     * @param locale   the dictionary locale.
     * @param dictType the dictionary type, as a human-readable string
     * @param dictFile dictionary file path. if null, use default dictionary path based on
     */
    public BkavAutoCorrectBinaryDictionnary(Context context, String dictName, Locale locale, String dictType, File dictFile) {
        super(context, dictName, locale, dictType, dictFile);
    }

    // Note: This method is called by {@link DictionaryFacilitator} using Java reflection.
    @ExternallyReferenced
    public static BkavAutoCorrectBinaryDictionnary getDictionary(
            final Context context, final Locale locale, final File dictFile,
            final String dictNamePrefix, @Nullable final String account) {
        //AnhNDd: Cái này là mình đã có sẵn file của từ điển autocorrect
        File dict = getDictFileAutoCorrect(context);
        return new BkavAutoCorrectBinaryDictionnary(context, getDictName(NAME, locale, dict),
                locale, Dictionary.TYPE_MAIN_VI_AUTOCORRECT, dict);
    }

    private static File getDictFileAutoCorrect(Context context) {
        //AnhNDd: file này đã được khởi tạo ở BkavDictionaryFactory.java
        return new File(context.getFilesDir(), Constants.DICT_VI_TELEX_AUTOCORRECT_NAME);
    }

    @Override
    protected void loadInitialContentsLocked() {

    }

    @Override
    public void close() {
        // Flush pending writes.
        asyncFlushBinaryDictionary();
        super.close();
    }
}

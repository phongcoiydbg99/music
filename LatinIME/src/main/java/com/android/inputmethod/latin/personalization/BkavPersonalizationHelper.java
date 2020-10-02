package com.android.inputmethod.latin.personalization;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.Log;
import com.android.inputmethod.latin.common.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.SoftReference;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AnhNDd: class này giống y hệt với PersonalizationHelper
 */
public class BkavPersonalizationHelper {
    private static final String TAG = BkavPersonalizationHelper.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final ConcurrentHashMap<String, SoftReference<UserHistoryTypedDictionary>>
            sLangUserHistoryTypedDictCache = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, SoftReference<BkavCacheUserHistoryDictionary>>
            sLangBkavCacheUserHistoryDictionary = new ConcurrentHashMap<>();

    @Nonnull
    public static UserHistoryTypedDictionary getUserHistoryTypedDictionary(
            final Context context, final Locale locale, @Nullable final String accountName) {
        String lookupStr = locale.toString();
        if (accountName != null) {
            lookupStr += "." + accountName;
        }
        synchronized (sLangUserHistoryTypedDictCache) {
            if (sLangUserHistoryTypedDictCache.containsKey(lookupStr)) {
                final SoftReference<UserHistoryTypedDictionary> ref =
                        sLangUserHistoryTypedDictCache.get(lookupStr);
                final UserHistoryTypedDictionary dict = ref == null ? null : ref.get();
                if (dict != null) {
                    if (DEBUG) {
                        Log.d(TAG, "Use cached UserHistoryTypedDictionary with lookup: " + lookupStr);
                    }
                    dict.reloadDictionaryIfRequired();
                    return dict;
                }
            }
            final UserHistoryTypedDictionary dict = new UserHistoryTypedDictionary(
                    context, locale, accountName);
            sLangUserHistoryTypedDictCache.put(lookupStr, new SoftReference<>(dict));
            return dict;
        }
    }

    @Nonnull
    public static BkavCacheUserHistoryDictionary getBkavCacheUserHistoryDictionary(
            final Context context, final Locale locale, @Nullable final String accountName) {
        String lookupStr = locale.toString();
        if (accountName != null) {
            lookupStr += "." + accountName;
        }
        synchronized (sLangBkavCacheUserHistoryDictionary) {
            if (sLangBkavCacheUserHistoryDictionary.containsKey(lookupStr)) {
                final SoftReference<BkavCacheUserHistoryDictionary> ref =
                        sLangBkavCacheUserHistoryDictionary.get(lookupStr);
                final BkavCacheUserHistoryDictionary dict = ref == null ? null : ref.get();
                if (dict != null) {
                    if (DEBUG) {
                        Log.d(TAG, "Use cached BkavCacheUserHistoryDictionary with lookup: " + lookupStr);
                    }
                    dict.reloadDictionaryIfRequired();
                    return dict;
                }
            }
            final BkavCacheUserHistoryDictionary dict = new BkavCacheUserHistoryDictionary(
                    context, locale, accountName);
            sLangBkavCacheUserHistoryDictionary.put(lookupStr, new SoftReference<>(dict));
            return dict;
        }
    }

    public static void removeAllUserHistoryTypedDictionaries(final Context context) {
        synchronized (sLangUserHistoryTypedDictCache) {
            for (final ConcurrentHashMap.Entry<String, SoftReference<UserHistoryTypedDictionary>> entry
                    : sLangUserHistoryTypedDictCache.entrySet()) {
                if (entry.getValue() != null) {
                    final UserHistoryTypedDictionary dict = entry.getValue().get();
                    if (dict != null) {
                        dict.clear();
                    }
                }
            }
            sLangUserHistoryTypedDictCache.clear();
            final File filesDir = context.getFilesDir();
            if (filesDir == null) {
                Log.e(TAG, "context.getFilesDir() returned null.");
                return;
            }
            final boolean filesDeleted = FileUtils.deleteFilteredFiles(
                    filesDir, new DictFilter(UserHistoryTypedDictionary.NAME));
            if (!filesDeleted) {
                Log.e(TAG, "Cannot remove dictionary files. filesDir: " + filesDir.getAbsolutePath()
                        + ", dictNamePrefix: " + UserHistoryTypedDictionary.NAME);
            }
        }
    }

    private static class DictFilter implements FilenameFilter {
        private final String mName;

        DictFilter(final String name) {
            mName = name;
        }

        @Override
        public boolean accept(final File dir, final String name) {
            return name.startsWith(mName);
        }
    }
}

package com.bkav;

import com.android.inputmethod.latin.Suggest;

/**
 * AnhNDd: class này là tĩnh dùng để xử lý có thể lấy gõ tiếp theo ở bất kì đâu
 */
public class NextNodeUtil implements Suggest.OnGetNextNodeSuggestedCallback{

    private NextNodeResults mNextNodeResults;

    private static NextNodeUtil sInstance;

    public static NextNodeUtil getInstance() {
        if (sInstance == null) return sInstance = new NextNodeUtil();
        return sInstance;
    }

    @Override
    public void onGetNextNodeSuggested(NextNodeResults nextNodes) {
        mNextNodeResults = nextNodes;
    }

    public NextNodeResults getNextNodeResults(){
        return mNextNodeResults;
    }
}

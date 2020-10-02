package com.bkav;

/**
 * AnhNDd: Thông tin về node có thể gõ tiếp theo
 */
public class Node {
    private char mCodePoint;
    private int mScore;

    public Node(char key, int score) {
        mCodePoint = key;
        mScore = score;
    }

    public char getCodePoint() {
        return mCodePoint;
    }

    public int getScore() {
        return mScore;
    }

    @Override
    public String toString() {
        return mCodePoint + ": " + mScore;
    }
}

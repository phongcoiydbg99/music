package com.bkav;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * AnhNDd: Lưu trữ thông tin về node có thể gõ tiếp theo
 */
public class NextNodeResults extends TreeSet<Node>{

    public static final int MAX_NODE_RESULTS = 18;

    private final int mCapacity;

    public NextNodeResults(int capacity){
        super(sNodeComparator);
        mCapacity = capacity;
    }

    @Override
    public boolean add(Node node) {
        if (size() < mCapacity) return super.add(node);
        if (comparator().compare(node, last()) > 0) return false;
        super.add(node);
        pollLast(); // removes the last element
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Node> e) {
        return null != e && super.addAll(e);
    }

    static final class NodeComparator implements Comparator<Node> {
        //AnhNDd: so sánh các node được thêm vào
        @Override
        public int compare(final Node o1, final Node o2) {
            if (o1.getScore() > o2.getScore()) return -1;
            if (o1.getScore() < o2.getScore()) return 1;
            return (o1.getCodePoint()+"").compareTo((o2.getCodePoint()+""));
        }
    }

    private static final NodeComparator sNodeComparator =
            new NodeComparator();
}

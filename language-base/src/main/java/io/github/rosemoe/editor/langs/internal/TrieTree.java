/*
 *   Copyright 2020-2021 Rosemoe
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package io.github.rosemoe.editor.langs.internal;

/**
 * @author Rose
 * TrieTree to query values quickly
 */
public class TrieTree<T> {

    public final Node<T> root;
    private int maxLen;

    public TrieTree() {
        root = new Node<>();
        maxLen = 0;
    }

    public void put(String v, T token) {
        maxLen = Math.max(v.length(), maxLen);
        addInternal(root, v, 0, v.length(), token);
    }

    public void put(CharSequence v, int off, int len, T token) {
        maxLen = Math.max(maxLen, len);
        addInternal(root, v, off, len, token);
    }

    public T get(CharSequence s, int offset, int len) {
        if (len > maxLen) {
            return null;
        }
        return getInternal(root, s, offset, len);
    }

    private T getInternal(Node<T> node, CharSequence s, int offset, int len) {
        if (len == 0) {
            return node.token;
        }
        char point = s.charAt(offset);
        Node<T> sub = node.map.get(point);
        if (sub == null) {
            return null;
        }
        return getInternal(sub, s, offset + 1, len - 1);
    }

    private void addInternal(Node<T> node, CharSequence v, int i, int len, T token) {
        char point = v.charAt(i);
        Node<T> sub = node.map.get(point);
        if (sub == null) {
            sub = new Node<>();
            node.map.put(point, sub);
        }
        if (len == 1) {
            sub.token = token;
        } else {
            addInternal(sub, v, i + 1, len - 1, token);
        }
    }

    public static class Node<T> {

        public final HashCharMap<Node<T>> map;

        public T token;

        public Node() {
            this.map = new HashCharMap<>();
        }

    }

    /**
     * 固定长度哈希表
     *
     * @author Rose
     */
    public static class HashCharMap<V> {

        private final LinkedPair<V>[] columns;

        private final LinkedPair<V>[] ends;

        private final static int CAPACITY = 64;

        @SuppressWarnings("unchecked")
        public HashCharMap() {
            columns = new LinkedPair[CAPACITY];
            ends = new LinkedPair[CAPACITY];
        }


        private static int position(int first) {
            return Math.abs(first ^ (first << 6) * ((first & 1) != 0 ? 3 : 1)) % CAPACITY;
        }

        public V get(char first) {
            int position = position(first);
            LinkedPair<V> pair = columns[position];
            while (pair != null) {
                if (pair.first == first) {
                    return pair.second;
                }
                pair = pair.next;
            }
            return null;
        }

        private LinkedPair<V> get(char first, int position) {
            LinkedPair<V> pair = columns[position];
            while (pair != null) {
                if (pair.first == first) {
                    return pair;
                }
                pair = pair.next;
            }
            return null;
        }

        public void put(char first, V second) {
            int position = position(first);
            if (ends[position] == null) {
                columns[position] = ends[position] = new LinkedPair<>();
                ends[position].first = first;
                ends[position].second = second;
                return;
            }
            LinkedPair<V> p = get(first, position);
            if (p == null) {
                p = ends[position].next = new LinkedPair<>();
                ends[position] = p;
            }
            p.first = first;
            p.second = second;
        }


    }

    /**
     * 数据节点
     *
     * @author Rose
     */
    public static class LinkedPair<V> {

        public LinkedPair<V> next;

        public char first;

        public V second;

    }

}


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
package io.github.rosemoe.editor.langs;

import io.github.rosemoe.editor.text.TextAnalyzeResult;
import io.github.rosemoe.editor.interfaces.AutoCompleteProvider;
import io.github.rosemoe.editor.struct.CompletionItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Identifier auto-completion
 * You can use it to provide identifiers
 * <strong>Note:</strong> To use this, you must use {@link Identifiers} as {@link TextAnalyzeResult#mExtra}
 */
public class IdentifierAutoComplete implements AutoCompleteProvider {

    private String[] mKeywords;
    private boolean mKeywordsAreLowCase;

    public void setKeywordsAreLowCase(boolean mKeywordsAreLowCase) {
        this.mKeywordsAreLowCase = mKeywordsAreLowCase;
    }

    public IdentifierAutoComplete() {

    }

    public IdentifierAutoComplete(String[] keywords) {
        setKeywords(keywords);
    }

    public void setKeywords(String[] keywords) {
        mKeywords = keywords;
        mKeywordsAreLowCase = true;
    }

    public String[] getKeywords() {
        return mKeywords;
    }

    public static class Identifiers {

        private final List<String> identifiers = new ArrayList<>();
        private HashMap<String, Object> cache;
        private final static Object SIGN = new Object();

        public void addIdentifier(String identifier) {
            if (cache == null) {
                throw new IllegalStateException("begin() has not been called");
            }
            if (cache.put(identifier, SIGN) == SIGN) {
                return;
            }
            identifiers.add(identifier);
        }

        public void begin() {
            cache = new HashMap<>();
        }

        public void finish() {
            cache.clear();
            cache = null;
        }

        public List<String> getIdentifiers() {
            return identifiers;
        }

    }

    @Override
    public List<CompletionItem> getAutoCompleteItems(String prefix, boolean isInCodeBlock, TextAnalyzeResult colors, int line) {
        List<CompletionItem> keywords = new ArrayList<>();
        final String[] keywordArray = mKeywords;
        final boolean lowCase = mKeywordsAreLowCase;
        String match = prefix.toLowerCase();
        if (keywordArray != null) {
            if (lowCase) {
                for (String kw : keywordArray) {
                    if (kw.startsWith(match)) {
                        keywords.add(new CompletionItem(kw, "Keyword"));
                    }
                }
            } else {
                for (String kw : keywordArray) {
                    if (kw.toLowerCase().startsWith(match)) {
                        keywords.add(new CompletionItem(kw, "Keyword"));
                    }
                }
            }
        }
        Collections.sort(keywords, CompletionItem.COMPARATOR_BY_NAME);
        Object extra = colors.mExtra;
        Identifiers userIdentifiers = (extra instanceof Identifiers) ? (Identifiers) extra : null;
        if (userIdentifiers != null) {
            List<CompletionItem> words = new ArrayList<>();
            for (String word : userIdentifiers.getIdentifiers()) {
                if (word.toLowerCase().startsWith(match)) {
                    words.add(new CompletionItem(word, "Identifier"));
                }
            }
            Collections.sort(words, CompletionItem.COMPARATOR_BY_NAME);
            keywords.addAll(words);
        }
        return keywords;
    }


}

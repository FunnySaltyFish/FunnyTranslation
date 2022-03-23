package com.funny.translation.codeeditor.ui.editor;

import static io.github.rosemoe.editor.langs.universal.UniversalTokens.EOF;

import com.funny.translation.trans.Language;

import java.util.Stack;

import io.github.rosemoe.editor.interfaces.AutoCompleteProvider;
import io.github.rosemoe.editor.langs.IdentifierAutoComplete;
import io.github.rosemoe.editor.langs.universal.LanguageDescription;
import io.github.rosemoe.editor.langs.universal.UniversalLanguage;
import io.github.rosemoe.editor.langs.universal.UniversalTokenizer;
import io.github.rosemoe.editor.langs.universal.UniversalTokens;
import io.github.rosemoe.editor.struct.BlockLine;
import io.github.rosemoe.editor.text.LineNumberCalculator;
import io.github.rosemoe.editor.text.TextAnalyzeResult;
import io.github.rosemoe.editor.text.TextAnalyzer;
import io.github.rosemoe.editor.widget.EditorColorScheme;

public class PluginLanguage extends UniversalLanguage {
    LineNumberCalculator helper;
    IdentifierAutoComplete autoComplete;
    UniversalTokenizer tokenizer;

    public PluginLanguage(LanguageDescription languageDescription) {
        super(languageDescription);
        autoComplete = new IdentifierAutoComplete();
        autoComplete.setKeywords(mLanguage.getKeywords());
        autoComplete.setKeywordsAreLowCase(false);
//        identifiers.begin();
//        for (Language language : Language.values()){
//            identifiers.addIdentifier("LANGUAGE_"+language.name());
//        }
    }

    @Override
    public AutoCompleteProvider getAutoCompleteProvider() {
        return autoComplete;
    }

    @Override
    public void analyze(CharSequence content, TextAnalyzeResult colors, TextAnalyzer.AnalyzeThread.Delegate delegate) {
        StringBuilder text = content instanceof StringBuilder ? (StringBuilder) content : new StringBuilder(content);
        tokenizer = getTokenizer();
        tokenizer.setInput(text);
        helper = new LineNumberCalculator(text);

        IdentifierAutoComplete.Identifiers identifiers = new IdentifierAutoComplete.Identifiers();
        identifiers.begin();
        int maxSwitch = 0;
        int layer = 0;
        int currSwitch = 0;
        try {
            UniversalTokens token;
            Stack<BlockLine> stack = new Stack<>();
            while ((token = tokenizer.nextToken()) != EOF) {
                int index = tokenizer.getOffset();
                int line = helper.getLine();
                int column = helper.getColumn();
                switch (token) {
                    case KEYWORD:
                        colors.addIfNeeded(line, column, EditorColorScheme.KEYWORD);
                        break;
                    case IDENTIFIER:
                        identifiers.addIdentifier(text.substring(index, index + tokenizer.getTokenLength()));
                        colors.addIfNeeded(line, column, EditorColorScheme.TEXT_NORMAL);
                        break;
                    case LITERAL:
                        colors.addIfNeeded(line, column, EditorColorScheme.LITERAL);
                        break;
                    case LINE_COMMENT:
                    case LONG_COMMENT:
                        colors.addIfNeeded(line, column, EditorColorScheme.COMMENT);
                        break;
                    case OPERATOR:
                        colors.addIfNeeded(line, column, EditorColorScheme.OPERATOR);
                        if (mLanguage.isSupportBlockLine()) {
                            String op = text.substring(index, index + tokenizer.getTokenLength());
                            if (mLanguage.isBlockStart(op)) {
                                BlockLine blockLine = colors.obtainNewBlock();
                                blockLine.startLine = line;
                                blockLine.startColumn = column;
                                stack.add(blockLine);
                                if (layer == 0) {
                                    currSwitch = 1;
                                } else {
                                    currSwitch++;
                                }
                                layer++;
                            } else if (mLanguage.isBlockEnd(op)) {
                                if (!stack.isEmpty()) {
                                    BlockLine blockLine = stack.pop();
                                    blockLine.endLine = line;
                                    blockLine.endColumn = column;
                                    colors.addBlockLine(blockLine);
                                    if (layer == 1) {
                                        if (currSwitch > maxSwitch) {
                                            maxSwitch = currSwitch;
                                        }
                                    }
                                    layer--;
                                }
                            }
                        }
                        break;
                    case WHITESPACE:
                    case NEWLINE:
                        colors.addNormalIfNull();
                        break;
                    case UNKNOWN:
                        colors.addIfNeeded(line, column, EditorColorScheme.ANNOTATION);
                        break;
                }
                helper.update(tokenizer.getTokenLength());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        colors.determine(helper.getLine());
        identifiers.finish();
        colors.mExtra = identifiers;
        tokenizer.setInput(null);
        if (currSwitch > maxSwitch) {
            maxSwitch = currSwitch;
        }
        colors.setSuppressSwitch(maxSwitch + 50);
    }
}

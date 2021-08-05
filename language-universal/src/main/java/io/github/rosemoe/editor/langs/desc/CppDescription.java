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
package io.github.rosemoe.editor.langs.desc;

/**
 * @author Rose
 */
@SuppressWarnings("SpellCheckingInspection")
public class CppDescription extends CDescription {

    @Override
    public String[] getKeywords() {
        return new String[]{
                "asm", "auto", "bool", "break", "case", "catch", "char", "class",
                "const", "const_cast", "continue", "default", "delete", "do",
                "double", "dynamic_cast", "else", "enum", "explicit", "export",
                "extern", "false", "float", "for", "friend", "goto", "if", "inline",
                "int", "long", "mutable", "namespace", "new", "operator",
                "private", "protected", "public", "register", "reinterpret_cast",
                "return", "short", "signed", "sizeof", "static", "static_cast",
                "struct", "switch", "template", "this", "throw", "true", "try",
                "typedef", "typeid", "typename", "unsigned", "union",
                "using", "virtual", "void", "volatile", "wchar_t", "while",
        };
    }
}

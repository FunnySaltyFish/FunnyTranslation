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
package io.github.rosemoe.editor.widget;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.rosemoe.editor.R;
import io.github.rosemoe.editor.struct.CompletionItem;

/**
 * Adapter to display results
 *
 * @author Rose
 */
@SuppressWarnings("CanBeFinal")
class DefaultCompletionItemAdapter extends EditorCompletionAdapter {

    @Override
    public int getItemHeight() {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, Resources.getSystem().getDisplayMetrics());
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent, boolean isCurrentCursorPosition) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.default_completion_result_item, parent, false);
        }
        CompletionItem item = getItem(pos);
        TextView tv = (TextView) view.findViewById(R.id.result_item_label);
        tv.setText(item.label);
        tv = (TextView) view.findViewById(R.id.result_item_desc);
        tv.setText(item.desc);
        view.setTag(pos);
        if (isCurrentCursorPosition) {
            view.setBackgroundColor(0xffdddddd);
        } else {
            view.setBackgroundColor(0xffffffff);
        }
        ImageView iv = (ImageView) view.findViewById(R.id.result_item_image);
        iv.setImageDrawable(item.icon);
        return view;
    }

}

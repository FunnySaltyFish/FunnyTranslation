/*
 *   Copyright 2021 FunnySaltyFish
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

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import io.github.rosemoe.editor.R;
import io.github.rosemoe.editor.struct.CompletionItem;

public class EditorAutoCompletionAdapter extends BaseQuickAdapter<CompletionItem, BaseViewHolder> {
    private int selectIndex = 0;
    public EditorAutoCompletionAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, CompletionItem completionItem) {
        baseViewHolder.setText(R.id.result_item_label,completionItem.label);
        baseViewHolder.setText(R.id.result_item_desc,completionItem.desc);
        baseViewHolder.setImageDrawable(R.id.result_item_image,completionItem.icon);
        int color = selectIndex == getItemPosition(completionItem) ? 0xffdddddd : 0xffffffff;
        baseViewHolder.setBackgroundColor(R.id.result_item,color);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, CompletionItem item, @NonNull List<?> payloads) {
        super.convert(holder, item, payloads);
        if(payloads==null)convert(holder, item);//没传payloads，说明是直接全局刷新
        else{//仅刷新背景
            int color = selectIndex == getItemPosition(item) ? 0xffdddddd : 0xffffffff;
            holder.setBackgroundColor(R.id.result_item,color);
        }
    }

    public void setSelectIndex(int selectIndex) {
        this.selectIndex = selectIndex;
    }
}

/*
 * Copyright 2018 Kaushik N. Sanji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kaushiknsanji.storeapp.ui.common;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.kaushiknsanji.storeapp.R;


public class ListItemSpacingDecoration extends RecyclerView.ItemDecoration {
    //Stores the spacing to be applied between the items in the List
    private final int mVerticalOffsetSize;
    //Stores the spacing to be applied between the items and its parent in the List
    private final int mHorizontalOffsetSize;
    //Boolean Flag to add bottom offset for the Bottom Fab when true
    private final boolean mOffsetBottomFab;


    public ListItemSpacingDecoration(int verticalOffsetSize, int horizontalOffsetSize) {
        this(verticalOffsetSize, horizontalOffsetSize, false);
    }


    public ListItemSpacingDecoration(int verticalOffsetSize, int horizontalOffsetSize, boolean offsetBottomFab) {
        mVerticalOffsetSize = verticalOffsetSize;
        mHorizontalOffsetSize = horizontalOffsetSize;
        mOffsetBottomFab = offsetBottomFab;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //Get the Child View position in the adapter
        int position = parent.getChildAdapterPosition(view);

        //Evaluates to first item when position is 0
        boolean isFirstItem = (position == 0);

        //Evaluates to last item when position equals the list size
        boolean isLastItem = (position == (parent.getAdapter().getItemCount() - 1));

        //Set full spacing to the top when the Item is the First Item in the list
        if (isFirstItem) {
            outRect.top = mVerticalOffsetSize;
        }

        //Set full spacing to bottom
        outRect.bottom = mVerticalOffsetSize;
        //Set full spacing to left
        outRect.left = mHorizontalOffsetSize;
        //Set full spacing to right
        outRect.right = mHorizontalOffsetSize;

        if (isLastItem && mOffsetBottomFab) {
            //When it is the last item and Bottom Fab offset is required

            //Get the Resources from Context
            Resources resources = view.getContext().getResources();
            //Add the required offset for the Bottom Fab of normal size (56dp)
            //along with top and bottom margin of 16dp
            outRect.bottom += resources.getDimensionPixelSize(R.dimen.fab_material_margin) * 2
                    + resources.getDimensionPixelSize(R.dimen.fab_material_normal_size);
        }
    }
}
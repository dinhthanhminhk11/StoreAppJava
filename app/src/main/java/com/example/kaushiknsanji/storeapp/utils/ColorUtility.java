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

package com.example.kaushiknsanji.storeapp.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;


public final class ColorUtility {


    private ColorUtility() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    public static int[] obtainColorsFromTypedArray(Context context, @ArrayRes int colorArrayRes, @ColorRes int defaultColorRes) {
        //Obtaining the Typed Array of Colors from the Resources
        TypedArray typedArrayColors = context.getResources().obtainTypedArray(colorArrayRes);
        //Get the number of Colors
        int noOfColors = typedArrayColors.length();
        //Creating an integer array for the size
        int[] colors = new int[noOfColors];
        //Retrieving the default color from the resources
        int defaultColorInt = ContextCompat.getColor(context, defaultColorRes);
        //Iterating over the TypedArray to get the colors
        for (int index = 0; index < noOfColors; index++) {
            colors[index] = typedArrayColors.getColor(index, defaultColorInt);
        }
        //Release the TypedArray resource
        typedArrayColors.recycle();
        //Returning the colors read from the TypedArray
        return colors;
    }
}

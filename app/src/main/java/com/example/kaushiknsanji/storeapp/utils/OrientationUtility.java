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
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v4.app.FragmentActivity;


public final class OrientationUtility {

    private OrientationUtility() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }


    public static boolean isDeviceInPortraitMode(Context context) {
        //Retrieving the current orientation
        int screenOrientation = context.getResources().getConfiguration().orientation;

        //Return the evaluation of screenOrientation
        return (screenOrientation == Configuration.ORIENTATION_PORTRAIT);
    }

    public static void lockCurrentScreenOrientation(FragmentActivity activity) {
        //Retrieving the current orientation
        int screenOrientation = activity.getResources().getConfiguration().orientation;

        //Locking the screen orientation to current orientation
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            //For Portrait Mode
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            //For Landscape Mode
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    public static void unlockScreenOrientation(FragmentActivity activity) {
        //Setting the screen orientation to sensor's orientation for unlocking
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
}

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

import com.example.kaushiknsanji.storeapp.data.StoreRepository;
import com.example.kaushiknsanji.storeapp.data.local.StoreFileRepository;
import com.example.kaushiknsanji.storeapp.data.local.StoreLocalRepository;


public final class InjectorUtility {

    private InjectorUtility() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    private static StoreLocalRepository provideLocalRepository(Context context) {
        return StoreLocalRepository.getInstance(context.getContentResolver(), AppExecutors.getInstance());
    }


    private static StoreFileRepository provideFileRepository(Context context) {
        return StoreFileRepository.getInstance(context.getContentResolver(), AppExecutors.getInstance());
    }

    public static StoreRepository provideStoreRepository(Context context) {
        return StoreRepository.getInstance(provideLocalRepository(context), provideFileRepository(context));
    }

}

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

package com.example.kaushiknsanji.storeapp.data;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.List;


public interface FileRepository {


    void saveImageToFile(Context context, Uri fileContentUri, FileOperationsCallback<Uri> operationsCallback);


    void takePersistablePermissions(Uri fileContentUri, int intentFlags);

    void deleteImageFiles(List<String> fileContentUriList, FileOperationsCallback<Boolean> operationsCallback);

    void deleteImageFilesSilently(List<String> fileContentUriList);

    interface FileOperationsCallback<T> {

        void onSuccess(T results);


        default void onFailure(@StringRes int messageId, @Nullable Object... args) {
        }
    }

}

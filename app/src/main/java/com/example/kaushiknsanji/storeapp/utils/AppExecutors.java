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

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public final class AppExecutors {

    //Singleton instance
    private static volatile AppExecutors INSTANCE;

    //Executors for various needs
    private final Executor diskIO;
    private final Executor mainThread;

    private AppExecutors(Executor diskIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.mainThread = mainThread;
    }


    public static AppExecutors getInstance() {
        if (INSTANCE == null) {
            //When instance is not available
            synchronized (AppExecutors.class) {
                //Apply lock and check for the instance again
                if (INSTANCE == null) {
                    //When there is no instance, create a new one
                    INSTANCE = new AppExecutors(
                            //Single Thread Executor for Database/Disk operations
                            Executors.newSingleThreadExecutor(),
                            //MainThreadExecutor for UI Thread
                            new MainThreadExecutor()
                    );
                }
            }
        }
        //Returning the instance of AppExecutors
        return INSTANCE;
    }


    public Executor getDiskIO() {
        return diskIO;
    }

    public Executor getMainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        //Main Thread Handler attached to the Main Looper
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());


        @Override
        public void execute(@NonNull Runnable command) {
            //Posts runnables to the message queue of the Main Thread Handler
            mainThreadHandler.post(command);
        }
    }
}
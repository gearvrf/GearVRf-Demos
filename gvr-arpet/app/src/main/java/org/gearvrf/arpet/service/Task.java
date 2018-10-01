/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.arpet.service;

public abstract class Task implements Thread.UncaughtExceptionHandler {

    private TaskException mError;

    public TaskException getError() {
        return mError;
    }

    public void setError(TaskException error) {
        this.mError = error;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        mError = new TaskException(e);
    }

    protected void notifyExecuted() {
        synchronized (this) {
            notify();
        }
    }

    public void start() {
        Thread thread = new Thread(() -> {
            execute();
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    mError = new TaskException(e);
                }
            }
        });
        thread.setUncaughtExceptionHandler(this);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            mError = new TaskException(e);
        }
    }

    public abstract void execute();
}

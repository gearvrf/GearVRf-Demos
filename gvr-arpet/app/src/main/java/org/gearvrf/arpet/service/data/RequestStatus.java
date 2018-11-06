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

package org.gearvrf.arpet.service.data;

import android.support.annotation.StringDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RequestStatus implements Serializable {

    public static final String STATUS_UNDEFINED = "STATUS_UNDEFINED";
    public static final String STATUS_OK = "STATUS_OK";
    public static final String STATUS_ERROR = "STATUS_ERROR";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            STATUS_UNDEFINED,
            STATUS_OK,
            STATUS_ERROR
    })
    public @interface Status {
    }

    private int requestId;
    @Status
    private String status = STATUS_UNDEFINED;
    private Throwable error;

    public RequestStatus(int requestId) {
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }

    @Status
    public String getStatus() {
        return status;
    }

    public void setStatus(@Status String status) {
        this.status = status;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "RequestStatus{" +
                "requestId=" + requestId +
                ", status='" + status + '\'' +
                ", error=" + error +
                '}';
    }
}

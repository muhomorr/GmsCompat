/*
 * Copyright (C) 2013-2017 microG Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.common.api;

import android.app.PendingIntent;

import org.microg.safeparcel.AutoSafeParcelable;

// https://developers.google.com/android/reference/com/google/android/gms/common/api/Status
public final class Status extends AutoSafeParcelable implements Result {
    public static final Status INTERNAL_ERROR = new Status(CommonStatusCodes.INTERNAL_ERROR);
    public static final Status CANCELED = new Status(CommonStatusCodes.CANCELED);
    public static final Status SUCCESS = new Status(CommonStatusCodes.SUCCESS);

    @Field(1) public int statusCode;
    @Field(2) public String statusMessage;
    @Field(3) public PendingIntent resolution;

    public Status(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public Status getStatus() {
        return this;
    }

    public boolean isSuccess() {
        return statusCode <= CommonStatusCodes.SUCCESS;
    }

    public static final Creator<Status> CREATOR = new AutoCreator<>(Status.class);
}

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

package com.google.android.gms.location;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;

import org.microg.safeparcel.AutoSafeParcelable;

// https://developers.google.com/android/reference/com/google/android/gms/location/LocationSettingsResult
public class LocationSettingsResult extends AutoSafeParcelable implements Result {
    @Field(1) public Status status;
    @Field(2) public LocationSettingsStates settings;

    @Override
    public Status getStatus() {
        return status;
    }

    public LocationSettingsResult(LocationSettingsStates settings, Status status) {
        this.settings = settings;
        this.status = status;
    }

    public static final Creator<LocationSettingsResult> CREATOR = new AutoCreator<>(LocationSettingsResult.class);
}

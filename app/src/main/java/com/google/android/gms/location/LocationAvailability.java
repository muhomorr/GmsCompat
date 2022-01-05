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

import org.microg.safeparcel.AutoSafeParcelable;

// https://developers.google.com/android/reference/com/google/android/gms/location/LocationAvailability
public class LocationAvailability extends AutoSafeParcelable {

    @Deprecated
    @Field(1) public int cellStatus;
    @Deprecated
    @Field(2) public int wifiStatus;
    @Field(3) public long elapsedRealtimeNs;
    @Field(4) public int locationStatus;
    // @Deprecated @Field(5) public NetworkLocationStatus[]

    private static final int STATUS_AVAILABLE = 0;
    private static final int STATUS_UNAVAILABLE = 1000;

    private LocationAvailability(int status) {
        locationStatus = status;
    }

    private static final LocationAvailability AVAILABLE = new LocationAvailability(STATUS_AVAILABLE);
    private static final LocationAvailability UNAVAILABLE = new LocationAvailability(STATUS_UNAVAILABLE);

    public static LocationAvailability get(boolean available) {
        return available? AVAILABLE : UNAVAILABLE;
    }

    public boolean isLocationAvailable() {
        return locationStatus < STATUS_UNAVAILABLE;
    }

    public static final Creator<LocationAvailability> CREATOR = new AutoCreator<>(LocationAvailability.class);
}

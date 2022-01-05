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

// https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
public class LocationRequest extends AutoSafeParcelable {
    public static final int PRIORITY_BALANCED_POWER_ACCURACY = 102;
    public static final int PRIORITY_HIGH_ACCURACY = 100;
    public static final int PRIORITY_LOW_POWER = 104;
    public static final int PRIORITY_NO_POWER = 105;

    @Field(1) public int priority;
    @Field(2) public long interval;
    @Field(3) public long fastestInterval;
    @Field(4) public boolean explicitFastestInterval;
    @Field(5) public long expirationTime;
    @Field(6) public int numUpdates;
    @Field(7) public float smallestDesplacement;
    @Field(8) public long maxWaitTime;
    @Field(9) public boolean waitForAccurateLocation;

    public static final Creator<LocationRequest> CREATOR = new AutoCreator<>(LocationRequest.class);
}

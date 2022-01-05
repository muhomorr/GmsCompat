/*
 * SPDX-FileCopyrightText: 2015, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package com.google.android.gms.location;

import org.microg.safeparcel.AutoSafeParcelable;

import java.util.List;

// https://developers.google.com/android/reference/com/google/android/gms/location/LocationSettingsRequest.Builder
public class LocationSettingsRequest extends AutoSafeParcelable {
    @Field(value = 1, subClass = LocationRequest.class)
    public List<LocationRequest> requests;
    @Field(2) public boolean alwaysShow;
    @Field(3) public boolean needBle;

    public static final Creator<LocationSettingsRequest> CREATOR = new AutoCreator<>(LocationSettingsRequest.class);
}

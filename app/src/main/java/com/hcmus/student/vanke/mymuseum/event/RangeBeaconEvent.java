package com.hcmus.student.vanke.mymuseum.event;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import java.util.Collection;

/**
 * Created by vanke on 16/06/2018.
 */

public class RangeBeaconEvent {

    public Collection<Beacon> beacons;
    public Region region;

    public RangeBeaconEvent(Collection<Beacon> beacons, Region region) {
        this.beacons = beacons;
        this.region = region;
    }
}

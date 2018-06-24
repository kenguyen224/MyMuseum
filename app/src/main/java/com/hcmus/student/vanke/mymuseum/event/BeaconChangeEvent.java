package com.hcmus.student.vanke.mymuseum.event;

import org.altbeacon.beacon.Beacon;

/**
 * Created by vanke on 16/06/2018.
 */

public class BeaconChangeEvent {
    public Beacon beacon;

    public BeaconChangeEvent(Beacon beacon) {
        this.beacon = beacon;
    }
}

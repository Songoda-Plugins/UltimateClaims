package com.songoda.ultimateclaims.claim.region;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RegionCorners {
    private final Set<ClaimCorners> claimCorners = new HashSet<>();

    public void addCorners(ClaimCorners claimCorners) {
        this.claimCorners.add(claimCorners);
    }

    public Set<ClaimCorners> getClaimCorners() {
        return Collections.unmodifiableSet(this.claimCorners);
    }
}

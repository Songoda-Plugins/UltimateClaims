package com.songoda.ultimateclaims.invite;

import com.songoda.ultimateclaims.claim.Claim;

import java.util.UUID;

public class Invite {
    private final UUID inviter;
    private final UUID invited;

    private final Claim claim;

    private final long created;

    private boolean accepted = false;

    public Invite(UUID inviter, UUID invited, Claim claim) {
        this.inviter = inviter;
        this.invited = invited;
        this.claim = claim;
        this.created = System.currentTimeMillis();
    }

    public UUID getInviter() {
        return this.inviter;
    }

    public UUID getInvited() {
        return this.invited;
    }

    public Claim getClaim() {
        return this.claim;
    }

    public long getCreated() {
        return this.created;
    }

    public void accepted() {
        this.accepted = true;
    }

    public boolean isAccepted() {
        return this.accepted;
    }
}

package com.songoda.ultimateclaims.claim;

public enum ClaimDeleteReason {
    /**
     * When an admin deletes a claim through the admin command
     */
    ADMIN,

    /**
     * When a player deletes a claim
     */
    PLAYER,

    /**
     * When the power cell runs out and deletes the claim
     */
    POWERCELL_TIMEOUT
}

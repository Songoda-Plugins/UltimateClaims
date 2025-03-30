package com.songoda.ultimateclaims.member;

public enum ClaimRole {
    VISITOR(1, "interface.role.visitor"),
    MEMBER(2, "interface.role.member"),
    OWNER(3, "interface.role.owner");

    private final int index;
    private final String localePath;

    ClaimRole(int index, String localePath) {
        this.index = index;
        this.localePath = localePath;
    }

    public int getIndex() {
        return this.index;
    }

    public String getLocalePath() {
        return this.localePath;
    }

    public static ClaimRole fromIndex(int index) {
        for (ClaimRole role : values()) {
            if (role.getIndex() == index) {
                return role;
            }
        }
        return null;
    }
}

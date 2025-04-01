package com.songoda.ultimateclaims.claim;

import com.songoda.core.locale.Locale;
import com.songoda.ultimateclaims.UltimateClaims;

import java.util.HashSet;
import java.util.Set;

public class ClaimSettings {
    private final Set<ClaimSetting> settings = new HashSet<>();

    public ClaimSettings setEnabled(ClaimSetting setting, boolean enabled) {
        if (enabled) {
            this.settings.add(setting);
        } else {
            this.settings.remove(setting);
        }

        return this;
    }

    public boolean isEnabled(ClaimSetting setting) {
        return this.settings.contains(setting);
    }

    public String getStatus(ClaimSetting setting) {
        Locale locale = UltimateClaims.getInstance().getLocale();
        return isEnabled(setting) ? locale.getMessage("general.status.true").toText() : locale.getMessage("general.status.false").toText();
    }
}

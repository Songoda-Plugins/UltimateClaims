package com.songoda.ultimateclaims.claim;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.settings.Settings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AuditManager {
    private final UltimateClaims plugin;
    private final Cache<Claim, Deque<Audit>> auditCache;

    public AuditManager(UltimateClaims plugin) {
        this.plugin = plugin;
        this.auditCache = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build();
    }

    public void getAudits(Claim claim, Consumer<Deque<Audit>> callback) {
        Deque<Audit> cached = this.auditCache.getIfPresent(claim);
        if (cached != null) {
            callback.accept(cached);
            return;
        }

        this.plugin.getDataHelper().getAuditLog(claim, audits -> {
            this.auditCache.put(claim, audits);
            callback.accept(audits);
        });
    }


    public void addToAuditLog(Claim claim, UUID who, long when) {
        if (!Settings.ENABLE_AUDIT_LOG.getBoolean()) {
            return;
        }

        Audit audit = new Audit(who, when);
        getAudits(claim, auditLog -> {
            if (auditLog.isEmpty()
                    || auditLog.getFirst().getWho() != audit.getWho()
                    || System.currentTimeMillis() - auditLog.getFirst().getWhen() > 5 * 1000 * 60) {
                this.plugin.getDataHelper().addAudit(claim, audit);
                auditLog.addFirst(audit);
            }
        });
    }
}

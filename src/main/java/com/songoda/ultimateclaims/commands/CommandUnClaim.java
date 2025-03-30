package com.songoda.ultimateclaims.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimChunkUnclaimEvent;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimDeleteReason;
import com.songoda.ultimateclaims.claim.PowerCell;
import com.songoda.ultimateclaims.claim.region.ClaimedChunk;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandUnClaim extends AbstractCommand {
    private final UltimateClaims plugin;

    public CommandUnClaim(UltimateClaims plugin) {
        super(CommandType.PLAYER_ONLY, "unclaim");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        Chunk chunk = player.getLocation().getChunk();
        Claim claim = this.plugin.getClaimManager().getClaim(chunk);

        if (claim == null) {
            this.plugin.getLocale().getMessage("command.general.notclaimed").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (!claim.getOwner().getUniqueId().equals(player.getUniqueId())) {
            this.plugin.getLocale().getMessage("command.general.notyourclaim").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (claim.getPowerCell().hasLocation()) {
            PowerCell powerCell = claim.getPowerCell();
            if (powerCell.getLocation().getChunk().equals(chunk)) {
                this.plugin.getLocale().getMessage("command.unclaim.powercell").sendPrefixedMessage(sender);
                return ReturnType.FAILURE;
            }
        }

        ClaimChunkUnclaimEvent event = new ClaimChunkUnclaimEvent(claim, chunk);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return ReturnType.FAILURE;
        }

        // we've just unclaimed the chunk we're in, so we've "moved" out of the claim
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().getChunk().equals(chunk)) {
                ClaimMember member = claim.getMember(p);
                if (member != null) {
                    if (member.getRole() == ClaimRole.VISITOR) {
                        claim.removeMember(member);
                    } else {
                        member.setPresent(false);
                    }
                    this.plugin.getTrackerTask().toggleFlyOff(p);
                }
                if (Settings.CLAIMS_BOSSBAR.getBoolean()) {
                    claim.getVisitorBossBar().removePlayer(p);
                    claim.getMemberBossBar().removePlayer(p);
                }
            }
        }

        // Remove chunk from claim
        ClaimedChunk removedChunk = claim.removeClaimedChunk(chunk, player);

        if (this.plugin.getDynmapManager() != null) {
            this.plugin.getDynmapManager().refresh();
        }

        if (claim.getClaimSize() == 0) {
            this.plugin.getLocale().getMessage("general.claim.dissolve")
                    .processPlaceholder("claim", claim.getName())
                    .sendPrefixedMessage(player);

            claim.destroy(ClaimDeleteReason.PLAYER);
        } else {
            this.plugin.getDataHelper().deleteClaimedChunk(removedChunk);

            this.plugin.getLocale().getMessage("command.unclaim.success").sendPrefixedMessage(sender);
        }

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateclaims.unclaim";
    }

    @Override
    public String getSyntax() {
        return "unclaim";
    }

    @Override
    public String getDescription() {
        return "Unclaim land from your claim.";
    }
}

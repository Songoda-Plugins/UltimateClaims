package com.songoda.ultimateclaims.listeners;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimManager;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimPerm;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.Optional;

public class InteractListeners implements Listener {
    private final UltimateClaims plugin;

    public InteractListeners(UltimateClaims plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        ClaimManager claimManager = UltimateClaims.getInstance().getClaimManager();

        Chunk chunk = event.getClickedBlock().getChunk();

        boolean hasClaim = claimManager.hasClaim(chunk);
        if (event.getAction() == Action.PHYSICAL && hasClaim) {
            Claim claim = claimManager.getClaim(chunk);

            boolean canRedstone = isRedstone(event.getClickedBlock()) && claim.playerHasPerms(event.getPlayer(), ClaimPerm.REDSTONE);
            if (canRedstone) {
                return;
            } else if (isRedstone(event.getClickedBlock()) && !claim.playerHasPerms(event.getPlayer(), ClaimPerm.REDSTONE)) {
                this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(event.getPlayer());
                event.setCancelled(true);
                return;
            }

            if (!claim.playerHasPerms(event.getPlayer(), ClaimPerm.PLACE)) {
                this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage((Player) event.getPlayer());
                event.setCancelled(true);
            }
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !hasClaim) {
            return;
        }

        Claim claim = claimManager.getClaim(chunk);

        boolean canDoors = isDoor(event.getClickedBlock()) && claim.playerHasPerms(event.getPlayer(), ClaimPerm.DOORS);
        boolean canRedstone = isRedstone(event.getClickedBlock()) && claim.playerHasPerms(event.getPlayer(), ClaimPerm.REDSTONE);

        if (canRedstone || canDoors) {
            return;
        } else if (isRedstone(event.getClickedBlock()) && !claim.playerHasPerms(event.getPlayer(), ClaimPerm.REDSTONE)
                || isDoor(event.getClickedBlock()) && !claim.playerHasPerms(event.getPlayer(), ClaimPerm.DOORS)) {
            this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(event.getPlayer());
            event.setCancelled(true);
            return;
        }

        ClaimMember member = claim.getMember(event.getPlayer());

        if (claim.getPowerCell().hasLocation()
                && claim.getPowerCell().getLocation().equals(event.getClickedBlock().getLocation())
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && !event.getPlayer().isSneaking()) {

            // Make sure all items in the powercell are stacked.
            claim.getPowerCell().stackItems();
            if (member != null && member.getRole() != ClaimRole.VISITOR || event.getPlayer().hasPermission("ultimateclaims.powercell.view")) {
                this.plugin.getGuiManager().showGUI(event.getPlayer(), claim.getPowerCell().getGui(event.getPlayer()));
            } else {
                this.plugin.getLocale().getMessage("event.powercell.failopen").sendPrefixedMessage(event.getPlayer());
            }
            event.setCancelled(true);
            return;
        }

        if (!claim.playerHasPerms(event.getPlayer(), ClaimPerm.INTERACT)) {
            this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Chunk chunk = event.getBlockClicked().getRelative(event.getBlockFace()).getChunk();

        onBucket(chunk, event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketFillEvent event) {
        Chunk chunk = event.getBlockClicked().getRelative(event.getBlockFace()).getChunk();

        onBucket(chunk, event.getPlayer(), event);
    }

    // New method for handling interactions with entities (including leashed entities)
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        ClaimManager claimManager = this.plugin.getClaimManager();
        Chunk chunk = entity.getLocation().getChunk();

        if (!claimManager.hasClaim(chunk)) {
            return;
        }

        Claim claim = claimManager.getClaim(chunk);

        // Prevent non-members from interacting with boats
        if (entity.getType().name().contains("BOAT")) {
            if (!claim.playerHasPerms(player, ClaimPerm.INTERACT)) {
                this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(player);
                event.setCancelled(true);
                return;
            }
        }

        // Prevent non-members from interacting with leash hitches
        if (entity instanceof LeashHitch) {
            if (!claim.playerHasPerms(player, ClaimPerm.INTERACT)) {
                this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(player);
                event.setCancelled(true);
                return;
            }
        }

        // Check if this is a leash interaction
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;

            // If the entity has a leash holder, this might be an attempt to break the leash
            if (livingEntity.isLeashed()) {
                if (!claim.playerHasPerms(player, ClaimPerm.INTERACT)) {
                    this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(player);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Check if this is an attempt to ride
        if (isRideable(entity)) {
            if (!claim.playerHasPerms(player, ClaimPerm.INTERACT)) {
                this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(player);
                event.setCancelled(true);
            }
        }
    }

    // Add event handler for leash break by entity
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getRemover();
        Entity entity = event.getEntity();

        // Check if this is a leash hitch
        if (entity instanceof LeashHitch) {
            ClaimManager claimManager = this.plugin.getClaimManager();
            Chunk chunk = entity.getLocation().getChunk();

            if (!claimManager.hasClaim(chunk)) {
                return;
            }

            Claim claim = claimManager.getClaim(chunk);

            if (!claim.playerHasPerms(player, ClaimPerm.INTERACT)) {
                this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(player);
                event.setCancelled(true);
            }
        }
    }

    // New method to prevent riding any entities (mobs, boats, etc.)
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityMount(EntityMountEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Entity vehicle = event.getMount();
        ClaimManager claimManager = this.plugin.getClaimManager();
        Chunk chunk = vehicle.getLocation().getChunk();

        if (!claimManager.hasClaim(chunk)) {
            return;
        }

        Claim claim = claimManager.getClaim(chunk);

        // Prevent non-claim members from riding any entities
        if (!claim.playerHasPerms(player, ClaimPerm.INTERACT)) {
            this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(player);
            event.setCancelled(true);
        }
    }

    // Helper method to check if an entity is rideable
    private boolean isRideable(Entity entity) {
        switch (entity.getType().name()) {
            case "HORSE":
            case "DONKEY":
            case "MULE":
            case "LLAMA":
            case "PIG":
            case "STRIDER":
            case "CAMEL":
            case "SKELETON_HORSE":
            case "ZOMBIE_HORSE":
            case "TRADER_LLAMA":
            case "RAVAGER":
            case "BOAT":
            case "CHEST_BOAT":
                return true;
            default:
                return false;
        }
    }

    private void onBucket(Chunk chunk, Player player, Cancellable event) {
        ClaimManager claimManager = this.plugin.getClaimManager();

        if (!claimManager.hasClaim(chunk)) {
            return;
        }

        Claim claim = claimManager.getClaim(chunk);

        if (!claim.playerHasPerms(player, ClaimPerm.PLACE)) {
            this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(player);
            event.setCancelled(true);
        }
    }

    private boolean isDoor(Block block) {
        if (block == null) {
            return false;
        }

        switch (block.getType().name()) {
            // Doors
            case "DARK_OAK_DOOR":
            case "ACACIA_DOOR":
            case "BIRCH_DOOR":
            case "JUNGLE_DOOR":
            case "OAK_DOOR":
            case "SPRUCE_DOOR":
            case "CRIMSON_DOOR":
            case "WARPED_DOOR":
            case "MANGROVE_DOOR":
            case "CHERRY_DOOR":
            case "BAMBOO_DOOR":
            case "COPPER_DOOR":
            case "EXPOSED_COPPER_DOOR":
            case "WEATHERED_COPPER_DOOR":
            case "OXIDIZED_COPPER_DOOR":
            case "IRON_DOOR":
                // Trapdoors
            case "ACACIA_TRAPDOOR":
            case "BIRCH_TRAPDOOR":
            case "DARK_OAK_TRAPDOOR":
            case "IRON_TRAPDOOR":
            case "JUNGLE_TRAPDOOR":
            case "OAK_TRAPDOOR":
            case "SPRUCE_TRAPDOOR":
            case "CRIMSON_TRAPDOOR":
            case "WARPED_TRAPDOOR":
            case "MANGROVE_TRAPDOOR":
            case "CHERRY_TRAPDOOR":
            case "BAMBOO_TRAPDOOR":
            case "COPPER_TRAPDOOR":
            case "EXPOSED_COPPER_TRAPDOOR":
            case "WEATHERED_COPPER_TRAPDOOR":
            case "OXIDIZED_COPPER_TRAPDOOR":

                // Fence Gates
            case "OAK_FENCE_GATE":
            case "ACACIA_FENCE_GATE":
            case "BIRCH_FENCE_GATE":
            case "DARK_OAK_FENCE_GATE":
            case "JUNGLE_FENCE_GATE":
            case "SPRUCE_FENCE_GATE":
            case "CRIMSON_FENCE_GATE":
            case "WARPED_FENCE_GATE":
            case "MANGROVE_FENCE_GATE":
            case "CHERRY_FENCE_GATE":
            case "BAMBOO_FENCE_GATE":
            case "COPPER_FENCE_GATE":
            case "EXPOSED_COPPER_FENCE_GATE":
            case "WEATHERED_COPPER_FENCE_GATE":
            case "OXIDIZED_COPPER_FENCE_GATE":

                // Legacy values (for compatibility with older versions)
            case "WOODEN_DOOR":
            case "WOOD_DOOR":
            case "TRAP_DOOR":
            case "FENCE_GATE":
                return true;
            default:
                return false;
        }
    }


    private boolean isRedstone(Block block) {
        if (block == null) {
            return false;
        }

        Optional<XMaterial> material = CompatibleMaterial.getMaterial(block.getType());
        if (!material.isPresent()) {
            return false;
        }
        switch (material.get()) {
            // Buttons
            case LEVER:
            case BIRCH_BUTTON:
            case ACACIA_BUTTON:
            case DARK_OAK_BUTTON:
            case JUNGLE_BUTTON:
            case OAK_BUTTON:
            case SPRUCE_BUTTON:
            case STONE_BUTTON:
            case CRIMSON_BUTTON:
            case WARPED_BUTTON:
            case MANGROVE_BUTTON:
            case CHERRY_BUTTON:
            case BAMBOO_BUTTON:
                // Pressure Plates
            case ACACIA_PRESSURE_PLATE:
            case BIRCH_PRESSURE_PLATE:
            case DARK_OAK_PRESSURE_PLATE:
            case HEAVY_WEIGHTED_PRESSURE_PLATE:
            case JUNGLE_PRESSURE_PLATE:
            case LIGHT_WEIGHTED_PRESSURE_PLATE:
            case OAK_PRESSURE_PLATE:
            case SPRUCE_PRESSURE_PLATE:
            case STONE_PRESSURE_PLATE:
            case CRIMSON_PRESSURE_PLATE:
            case WARPED_PRESSURE_PLATE:
            case MANGROVE_PRESSURE_PLATE:
            case CHERRY_PRESSURE_PLATE:
            case BAMBOO_PRESSURE_PLATE:
                return true;
            default:
                return false;
        }
    }
}

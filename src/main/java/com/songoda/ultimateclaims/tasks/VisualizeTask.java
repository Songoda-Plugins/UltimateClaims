package com.songoda.ultimateclaims.tasks;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.utils.ReflectionUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class VisualizeTask extends BukkitRunnable {
    private static VisualizeTask instance;
    private static UltimateClaims plugin;
    private static final Map<OfflinePlayer, Boolean> ACTIVE = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();

    int radius;

    public VisualizeTask(UltimateClaims plug) {
        plugin = plug;
        this.radius = Bukkit.getServer().getViewDistance();
    }

    public static VisualizeTask startTask(UltimateClaims plug) {
        plugin = plug;
        if (instance == null) {
            instance = new VisualizeTask(plugin);
            instance.runTaskTimerAsynchronously(plugin, 60, 10);
        }

        return instance;
    }

    public static boolean togglePlayer(Player p) {
        Boolean isActive = ACTIVE.get(p);
        ACTIVE.put(p, isActive = (isActive == null || isActive == false));
        return isActive;
    }

    public static void removePlayer(Player p) {
        ACTIVE.remove(p);
    }

    @Override
    public void run() {
        ACTIVE.entrySet().stream()
                .filter(e -> e.getValue() && e.getKey().isOnline())
                .forEach(e -> particleTick((Player) e.getKey()));
    }

    void particleTick(Player player) {
        final ClaimManager claimManager = plugin.getClaimManager();
        final Location playerLocation = player.getLocation();
        final World world = playerLocation.getWorld();
        // start and stop chunk coordinates
        int startY = playerLocation.getBlockY() + 1;
        int cxi = (playerLocation.getBlockX() >> 4) - this.radius, cxn = cxi + this.radius * 2;
        int czi = (playerLocation.getBlockZ() >> 4) - this.radius, czn = czi + this.radius * 2;
        // loop through the chunks to find applicable ones
        for (int cx = cxi; cx < cxn; cx++) {
            for (int cz = czi; cz < czn; cz++) {
                // sanity check
                if (!world.isChunkLoaded(cx, cz)) {
                    continue;
                }

                // so! Is this a claimed chunk?
                Claim claim = claimManager.getClaim(world.getName(), cx, cz);
                if (claim != null) {
                    // we found one!
                    // now we get to spawn the silly particles for the player
                    showChunkParticles(player, world.getChunkAt(cx, cz), startY, claim.isOwnerOrMember(player));
                }
            }
        }
    }

    void showChunkParticles(Player player, Chunk chunk, int startY, boolean canBuild) {
        // loop through the chunk
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // show about 1/5 of the blocks per tick
                boolean show = RANDOM.nextFloat() < .2;
                if (!show) {
                    continue;
                }

                // Exclude everything over max height
                if (startY >= chunk.getWorld().getMaxHeight()) {
                    continue;
                }

                // only show if there is a space to show above a solid block
                Block b = chunk.getBlock(x, startY, z);
                int maxDown = 8;
                do {
                    show = b.getType().isTransparent() && !(b = b.getRelative(BlockFace.DOWN)).getType().isTransparent();
                } while (--maxDown > 0 && !show);

                // can we do this?
                if (show) {
                    final Location loc = b.getLocation().add(.5, 1.5, .5);

                    if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
                        if (canBuild) {
                            player.spawnParticle(Particle.VILLAGER_HAPPY, loc, 0, 0, 0, 0, 1);
                        } else {
                            player.spawnParticle(Particle.REDSTONE, loc, 0, 0, 0, 0, 1, new Particle.DustOptions(canBuild ? Color.LIME : Color.RED, 2F));
                        }
                    } else if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
                        if (canBuild) {
                            player.spawnParticle(Particle.VILLAGER_HAPPY, loc, 0, 0, 0, 0, 1);
                        } else {
                            player.spawnParticle(Particle.REDSTONE, loc, 0, 1.0F, 0.1F, 0.1F, 1.0); // xyz = r b g
                        }
                    } else if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_8)) {
                        try {
                            Object[] data;
                            if (canBuild) {
                                data = new Object[]{loc, Effect.valueOf("HAPPY_VILLAGER"), 20, 2, 0F, 0F, 0F, 0F, 1, 16};
                            } else {
                                data = new Object[]{loc, Effect.valueOf("COLOURED_DUST"), 30, 2, 1.0F, 0.1F, 0.1F, 0F, 1, 16};
                            }
                            ReflectionUtils.invokePrivateMethod(player.spigot().getClass(), "playEffect", player.spigot(),
                                    new Class[]{Location.class,
                                            Effect.class,
                                            int.class, //id
                                            int.class, //data
                                            float.class, //offset x
                                            float.class, //offset y
                                            float.class, //offset z
                                            float.class, //speed
                                            int.class, //count
                                            int.class}, //radius, how far the player can be away from loc to see the particles
                                    data);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
//                         1.8 requires PacketPlayOutWorldParticles for this. todo?
                }
            }
        }
    }
}

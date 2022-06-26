package de.epiceric.shopchest.external.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import de.epiceric.shopchest.hook.UseShopHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collections;

public class WGUseHook extends WGHook implements UseShopHook {

    public WGUseHook(WGFlagRegistry registry) {
        super(registry);
    }

    @Override
    public boolean canUseShop(Block block, Player player, boolean admin) {
        final Location location = getLocation(BukkitAdapter.adapt(block.getWorld()), block);
        return test(
                Collections.singletonList(location),
                player,
                admin ? registry.getUseAdmin().get() : registry.getUse().get()
        );
    }

}

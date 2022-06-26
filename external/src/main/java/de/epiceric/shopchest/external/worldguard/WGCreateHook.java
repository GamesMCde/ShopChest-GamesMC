package de.epiceric.shopchest.external.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.flags.Flags;
import de.epiceric.shopchest.hook.CreateShopHook;
import de.epiceric.shopchest.hook.ExtendShopHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public class WGCreateHook extends WGHook implements CreateShopHook, ExtendShopHook {

    public WGCreateHook(WGFlagRegistry registry) {
        super(registry);
    }

    @Override
    public boolean canCreate(Block clickedBlock, List<Block> shopBlocks, Player player) {
        final World world = BukkitAdapter.adapt(clickedBlock.getWorld());
        final List<Location> locations = getLocations(world, shopBlocks);
        return test(locations, player, registry.getCreate().get());
    }

    @Override
    public boolean canExtend(Block newestBlock, List<Block> currentShopBlocks, Player player) {
        final World world = BukkitAdapter.adapt(newestBlock.getWorld());
        final List<Location> locations = getLocations(world, currentShopBlocks);
        locations.add(getLocation(world, newestBlock));
        return test(locations, player, Flags.CHEST_ACCESS, registry.getCreate().get());
    }

}

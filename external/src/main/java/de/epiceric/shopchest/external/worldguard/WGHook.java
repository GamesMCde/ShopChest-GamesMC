package de.epiceric.shopchest.external.worldguard;

import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public abstract class WGHook {

    protected final WGFlagRegistry registry;

    public WGHook(WGFlagRegistry registry) {
        this.registry = registry;
    }


    protected List<Location> getLocations(World world, List<Block> blocks) {
        final List<Location> locations = new LinkedList<>();
        for (Block block : blocks) {
            locations.add(getLocation(world, block));
        }
        return locations;
    }

    protected Location getLocation(World world, Block block) {
        return new Location(world, Vector3.at(block.getX(), block.getY(), block.getZ()));
    }

    protected boolean test(List<Location> locations, Player player, StateFlag... flag) {
        final LocalPlayer wgPlayer = player == null ? null : WorldGuardPlugin.inst().wrapPlayer(player);
        final RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        for (Location location : locations) {
            final ApplicableRegionSet set = query.getApplicableRegions(location);
            if (!set.testState(wgPlayer, flag)) {
                return false;
            }
        }
        return true;
    }

}

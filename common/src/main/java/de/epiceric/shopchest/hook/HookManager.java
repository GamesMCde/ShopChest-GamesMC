package de.epiceric.shopchest.hook;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class HookManager {

    private final List<UseShopHook> useShopHooks;

    public HookManager() {
        useShopHooks = new LinkedList<>();
    }

    public void registerUseShopHook(UseShopHook hook) {
        useShopHooks.add(hook);
    }

    public boolean canUseShop(Block block, Player player, boolean admin) {
        for(UseShopHook useShopHook : useShopHooks) {
            if(!useShopHook.canUseShop(block, player, admin)) {
                return false;
            }
        }
        return true;
    }

}

package de.epiceric.shopchest.hook;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class HookManager {

    private final List<InteractHook> interactHooks;
    private final List<UseShopHook> useShopHooks;

    public HookManager() {
        interactHooks = new LinkedList<>();
        useShopHooks = new LinkedList<>();
    }

    public void registerInteractHook(InteractHook hook) {
        interactHooks.add(hook);
    }

    public void registerUseShopHook(UseShopHook hook) {
        useShopHooks.add(hook);
    }

    public boolean canInteract(Player player) {
        for (InteractHook interactHook : interactHooks) {
            if (!interactHook.canInteract(player)) {
                return false;
            }
        }
        return true;
    }

    public boolean canUseShop(Block block, Player player, boolean admin) {
        for (UseShopHook useShopHook : useShopHooks) {
            if (!useShopHook.canUseShop(block, player, admin)) {
                return false;
            }
        }
        return true;
    }

}

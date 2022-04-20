package de.epiceric.shopchest.hook;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class HookManager {

    private final List<InteractHook> interactHooks;
    private final List<UseShopHook> useShopHooks;
    private final List<CreateShopHook> createShopHooks;
    private final List<ExtendShopHook> extendShopHooks;

    public HookManager() {
        interactHooks = new LinkedList<>();
        useShopHooks = new LinkedList<>();
        createShopHooks = new LinkedList<>();
        extendShopHooks = new LinkedList<>();
    }

    public void registerInteractHook(InteractHook hook) {
        interactHooks.add(hook);
    }

    public void registerUseShopHook(UseShopHook hook) {
        useShopHooks.add(hook);
    }

    public void registerCreateShopHook(CreateShopHook hook) {
        createShopHooks.add(hook);
    }

    public void registerExtendShopHook(ExtendShopHook hook) {
        extendShopHooks.add(hook);
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

    public boolean canCreateShop(Block clickedBlock, List<Block> shopBlocks, Player player) {
        for (CreateShopHook createShopHook : createShopHooks) {
            if (!createShopHook.canCreate(clickedBlock, shopBlocks, player)) {
                return false;
            }
        }
        return true;
    }

    public boolean canExtendShop(Block newestBlock, List<Block> currentShopBlocks, Player player) {
        for (ExtendShopHook extendShopHook : extendShopHooks) {
            if (!extendShopHook.canExtend(newestBlock, currentShopBlocks, player)) {
                return false;
            }
        }
        return true;
    }

}

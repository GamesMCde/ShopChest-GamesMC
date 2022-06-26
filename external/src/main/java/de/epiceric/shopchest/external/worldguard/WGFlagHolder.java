package de.epiceric.shopchest.external.worldguard;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import de.epiceric.shopchest.external.FlagHolder;

public class WGFlagHolder implements FlagHolder<StateFlag> {

    private StateFlag flag;

    public WGFlagHolder(StateFlag flag) {
        this.flag = flag;
    }

    @Override
    public StateFlag get() {
        return flag;
    }

    @Override
    public void register() {
        final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(flag);
        } catch (Exception e) {
            // Maybe the flag is already registered (Strange plugin loading manipulation)
            final Flag<?> registeredFlag = registry.get(flag.getName());
            if (registeredFlag == null) {
                throw new IllegalStateException("Can not register the '" + flag.getName() + "' flag in WorldGuard and it's not present");
            }
            if (!(registeredFlag instanceof StateFlag)) {
                throw new IllegalArgumentException("The registered '" + flag.getName() + "' flag of WorldGuard is not a 'state flag'");
            }

            // Get the registered flag
            // Not perfect as it may not have the same default value as the one specified in the ShopChest's config
            this.flag = (StateFlag) registeredFlag;
        }
    }
}

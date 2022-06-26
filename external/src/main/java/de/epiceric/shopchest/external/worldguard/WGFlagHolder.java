package de.epiceric.shopchest.external.worldguard;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.epiceric.shopchest.external.FlagHolder;

public class WGFlagHolder implements FlagHolder<StateFlag> {

    private final StateFlag flag;

    public WGFlagHolder(StateFlag flag) {
        this.flag = flag;
    }

    @Override
    public StateFlag get() {
        return flag;
    }

    @Override
    public void register() {
        WorldGuard.getInstance().getFlagRegistry().register(flag);
    }
}

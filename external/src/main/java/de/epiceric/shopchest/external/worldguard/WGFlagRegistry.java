package de.epiceric.shopchest.external.worldguard;

import com.sk89q.worldguard.protection.flags.StateFlag;
import de.epiceric.shopchest.external.ExternalLoadData;
import de.epiceric.shopchest.external.FlagRegistry;

public class WGFlagRegistry extends FlagRegistry<StateFlag> {
    @Override
    protected void initializeFlag(ExternalLoadData loadData) {
        create = new WGFlagHolder(
                new StateFlag(
                        "create-shop",
                        loadData.isFlag(ExternalLoadData.Flags.CREATE)
                )
        );
        use = new WGFlagHolder(
                new UseFlag(
                        "use-shop",
                        loadData.isFlag(ExternalLoadData.Flags.USE)
                )
        );
        useAdmin = new WGFlagHolder(
                new UseFlag(
                        "use-admin-shop",
                        loadData.isFlag(ExternalLoadData.Flags.USE_ADMIN)
                )
        );
    }
}

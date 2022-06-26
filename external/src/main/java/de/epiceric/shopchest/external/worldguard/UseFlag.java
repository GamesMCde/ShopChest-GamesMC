package de.epiceric.shopchest.external.worldguard;

import com.sk89q.worldguard.protection.flags.StateFlag;

// Custom class to use specificities of Flags.BUILD
// It's the cleanest way to allow use shop flags for member by default
class UseFlag extends StateFlag {
    public UseFlag(String name, boolean def) {
        super(name, def);
    }

    @Override
    public boolean implicitlySetWithMembership() {
        return true;
    }

    @Override
    public boolean usesMembershipAsDefault() {
        return true;
    }

    @Override
    public boolean preventsAllowOnGlobal() {
        return true;
    }

    @Override
    public boolean requiresSubject() {
        return true;
    }

}

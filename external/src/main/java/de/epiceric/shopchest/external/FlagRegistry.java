package de.epiceric.shopchest.external;

public abstract class FlagRegistry<T> {

    protected FlagHolder<T> create, use, useAdmin;
    private boolean initialized, registered;

    public FlagRegistry() {
    }

    public FlagHolder<T> getCreate() {
        return create;
    }

    public FlagHolder<T> getUse() {
        return use;
    }

    public FlagHolder<T> getUseAdmin() {
        return useAdmin;
    }

    public void initialize(ExternalLoadData loadData) {
        if (initialized) {
            throw new IllegalStateException("Flags are already initialized");
        }

        initializeFlag(loadData);

        initialized = true;
    }

    protected abstract void initializeFlag(ExternalLoadData loadData);

    public void register() {
        if (registered) {
            throw new IllegalStateException("Flags are already registered");
        }
        if (!initialized) {
            throw new IllegalStateException("Flags are not initialized");
        }
        create.register();
        use.register();
        useAdmin.register();

        registered = true;
    }

}

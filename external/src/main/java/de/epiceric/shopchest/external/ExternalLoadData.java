package de.epiceric.shopchest.external;

public class ExternalLoadData {

    private int flags;

    public ExternalLoadData() {
        flags = 0;
    }

    public void setFlag(Flags flag) {
        flags = flag.setFlag(flags);
    }

    public boolean isFlag(Flags flag) {
        return flag.isFlag(flags);
    }

    public enum Flags {
        CREATE, USE, USE_ADMIN;

        private int setFlag(int data) {
            return data | 1 << ordinal();
        }

        private boolean isFlag(int data) {
            return (data >>> ordinal() & 1) == 1;
        }
    }

}

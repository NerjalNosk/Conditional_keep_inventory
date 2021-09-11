package net.nerjal.keepInventory;

public enum Validation {
    WHITELIST ("Blacklist"),
    BLACKLIST ("Whitelist"),
    NONE ("None");

    public final String name;

    Validation(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public boolean compare(Object o) {
        if (!(o instanceof Validation v)) return false;
        return v.name.equals(this.name);
    }
}

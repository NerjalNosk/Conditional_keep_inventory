package net.nerjal.keepInventory.config;

public enum ListComparator {
    Whitelist (1),
    Blacklist (0);

    public final int check;

    ListComparator(int check) {
        this.check = check;
    }

    public static ListComparator test(String test) {
        if (test.equals("Whitelist")) return Whitelist;
        else if (test.equals("Blacklist")) return Blacklist;
        return null;
    }
}

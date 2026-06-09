package panetina.elarion.addons.worlds.service;

public final class AbundanceSelector {
    private AbundanceSelector() {
    }

    public static boolean keep(long seed, long position, int salt, double chance) {
        if (chance >= 1) return true;
        if (chance <= 0) return false;
        return unit(seed ^ position ^ ((long) salt << 32)) < chance;
    }

    public static double unit(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdl;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53l;
        value ^= value >>> 33;
        return (value >>> 11) * 0x1.0p-53;
    }
}

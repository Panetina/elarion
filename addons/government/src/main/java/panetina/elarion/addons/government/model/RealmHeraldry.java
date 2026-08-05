package panetina.elarion.addons.government.model;

import panetina.elarion.core.model.ElarionPixelAsset32;

/** Government-owned, revisioned Realm heraldry; Core supplies only the shared pixel contract. */
public record RealmHeraldry(long revision, byte[] paletteIndices) {
    public RealmHeraldry {
        ElarionPixelAsset32 asset = new ElarionPixelAsset32(revision, paletteIndices);
        revision = asset.revision();
        paletteIndices = asset.paletteIndices();
    }
    @Override public byte[] paletteIndices() { return paletteIndices.clone(); }
    public static RealmHeraldry blank() { return new RealmHeraldry(0L, new byte[0]); }
    public RealmHeraldry revised(byte[] pixels) {
        ElarionPixelAsset32 asset = new ElarionPixelAsset32(revision, paletteIndices).revised(pixels);
        return new RealmHeraldry(asset.revision(), asset.paletteIndices());
    }
}

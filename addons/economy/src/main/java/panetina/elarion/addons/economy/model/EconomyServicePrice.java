package panetina.elarion.addons.economy.model;

public record EconomyServicePrice(String id, long base, long minimum, long maximum) {
    public EconomyServicePrice {
        if (id == null || !id.matches("[a-z0-9_.\\-]+")) {
            throw new IllegalArgumentException("Invalid Economy service price id: " + id);
        }
        if (minimum < 0 || base < minimum || maximum < base) {
            throw new IllegalArgumentException("Invalid price bounds for " + id);
        }
    }
}

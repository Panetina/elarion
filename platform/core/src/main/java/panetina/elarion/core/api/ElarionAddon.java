package panetina.elarion.core.api;

/**
 * Stable bootstrap contract for every Elarion addon.
 *
 * Addons use the "elarion:addon" Fabric entrypoint. Core invokes this contract
 * only after the canonical API and all Core services have been initialized.
 */
@FunctionalInterface
public interface ElarionAddon {
    void initialize(ElarionApi api);
}

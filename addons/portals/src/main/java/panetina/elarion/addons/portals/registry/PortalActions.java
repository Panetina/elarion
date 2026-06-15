package panetina.elarion.addons.portals.registry;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.portals.PortalContent;
import panetina.elarion.addons.portals.service.PortalDefinitionService;
import panetina.elarion.addons.portals.service.PortalRouteService;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.registry.ActionHandler;
import panetina.elarion.core.registry.ActionType;
import panetina.elarion.core.registry.RegistryExecutionResult;

public final class PortalActions {
    public static final String UNLOCK = "elarion:portal_unlock";
    public static final String LOCK = "elarion:portal_lock";
    public static final String STATUS = "elarion:portal_status";
    public static final String BUY_TICKET = "elarion:portal_buy_ticket";

    private PortalActions() {
    }

    public static void register(
            ElarionApi api, PortalDefinitionService definitions, PortalRouteService routes
    ) {
        register(api, UNLOCK, "Unlocks a configured portal route.", context -> {
            String route = context.parameters().getOrDefault(
                    "route", context.execution().actorRealmId());
            routes.unlock(route, context.execution().actorId());
            return RegistryExecutionResult.ok("Portal route unlocked.");
        });
        register(api, LOCK, "Locks a configured portal route.", context -> {
            String route = context.parameters().getOrDefault("route", "");
            routes.lock(route, context.execution().actorId());
            return RegistryExecutionResult.ok("Portal route locked.");
        });
        register(api, STATUS, "Shows the current status of a portal route.", context -> {
            var status = routes.snapshot(context.parameters().getOrDefault("route", ""));
            var definition = definitions.require(status.routeId());
            return RegistryExecutionResult.ok(status.displayName() + ": "
                    + (!status.complete() ? "Not fully linked."
                    : status.active() && !definition.mode().usesSchedule() ? "Always open."
                    : status.active() ? "Open until " + status.windowEnd()
                    : status.unlocked() ? "Closed. Next opening " + status.windowStart() : "Locked."));
        });
        register(api, BUY_TICKET, "Purchases one physical portal ticket.", context -> {
            ServerPlayerEntity player = context.execution().actor();
            if (player == null) return RegistryExecutionResult.failure("This action requires a player.");
            String routeId = context.parameters().getOrDefault("route", "");
            var definition = definitions.require(routeId);
            if (!definition.enabled()) {
                return RegistryExecutionResult.failure("That gate is disabled.");
            }
            if (!definition.mode().requiresTicket()) {
                return RegistryExecutionResult.failure("That gate does not use tickets.");
            }
            if (!routes.snapshot(routeId).unlocked()) {
                return RegistryExecutionResult.failure("That gate has not been unlocked.");
            }
            ElarionEconomyApi economy = ElarionEconomyApi.get();
            long price = economy.servicePrice(definition.ticketPriceKey());
            var payment = economy.sink(EconomyAccount.player(player.getUuid()), price,
                    player.getUuid(), "Portal ticket purchase", "elarion:portals");
            if (!payment.successful()) return RegistryExecutionResult.failure(payment.message());
            if (!player.getInventory().insertStack(PortalContent.TICKET.create(
                    definition.ticketId(), definition.ticketName(), definition.ticketLore()))) {
                economy.reward(EconomyAccount.player(player.getUuid()), price,
                        player.getUuid(), "Portal ticket delivery refund", "elarion:portals");
                return RegistryExecutionResult.failure("Your inventory is full; payment was refunded.");
            }
            routes.recordTicketPurchase(player.getUuid(), routeId, price);
            return RegistryExecutionResult.ok("Purchased " + definition.ticketName() + " for "
                    + api.serverIdentity().currencyAmount(price) + ".");
        });
    }

    private static void register(ElarionApi api, String id, String description, ActionHandler handler) {
        api.registries().actions().register(new ActionType(id, "elarion_portals", description));
        api.registries().registerActionHandler(id, handler);
    }
}

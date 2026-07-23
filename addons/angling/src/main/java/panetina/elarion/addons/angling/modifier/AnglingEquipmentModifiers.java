package panetina.elarion.addons.angling.modifier;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingModifierComponent;
import panetina.elarion.addons.angling.component.AnglingSingleStackComponent;
import panetina.elarion.addons.angling.domainmap.AnglingDomainMapSnapshot;
import panetina.elarion.addons.angling.domainmap.AnglingDomainMaps;
import panetina.elarion.addons.angling.domainmap.AnglingRegistrySelector;
import panetina.elarion.addons.angling.registry.AnglingItems;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Resolves one immutable, bounded modifier list at cast time; bobber ticks never rescan equipment. */
public final class AnglingEquipmentModifiers {
    public static final int MAX_RESOLVED_MODIFIERS = 512;

    public Resolved resolve(ServerPlayerEntity player, ItemStack rod) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(rod, "rod");
        return resolve(player, rod, AnglingDomainMaps.snapshot());
    }

    Resolved resolve(ServerPlayerEntity player, ItemStack rod, AnglingDomainMapSnapshot snapshot) {
        ArrayList<AnglingCompiledModifier> result = new ArrayList<>();
        appendItem(result, rod, snapshot);
        appendItem(result, rod.getOrDefault(AnglingDataComponents.HOOK, AnglingSingleStackComponent.EMPTY).stack(),
                snapshot);
        appendItem(result, rod.getOrDefault(AnglingDataComponents.BAIT, AnglingSingleStackComponent.EMPTY).stack(),
                snapshot);
        appendItem(result, rod.getOrDefault(AnglingDataComponents.BOBBER, AnglingSingleStackComponent.EMPTY).stack(),
                snapshot);
        for (ItemStack armor : player.getInventory().armor) appendItem(result, armor, snapshot);
        player.getStatusEffects().stream()
                .sorted(Comparator.comparing(effect -> effect.getEffectType().getIdAsString()))
                .forEach(effect -> appendEffect(result, effect, snapshot));
        appendItem(result, new ItemStack(AnglingItems.require("default_catch")), snapshot);
        appendItem(result, new ItemStack(AnglingItems.require("default_minigame")), snapshot);
        return new Resolved(result);
    }

    private static void appendItem(
            List<AnglingCompiledModifier> result,
            ItemStack stack,
            AnglingDomainMapSnapshot snapshot
    ) {
        if (stack.isEmpty()) return;
        AnglingModifierComponent stored = stack.get(AnglingDataComponents.MODIFIERS);
        if (stored != null) append(result, stored.compiled());
        snapshot.itemModifiers().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey(Comparator.comparing(AnglingRegistrySelector::toString)))
                .filter(value -> matchesItem(stack, value.getKey()))
                .forEach(value -> append(result, value.getValue()));
    }

    private static void appendEffect(
            List<AnglingCompiledModifier> result,
            StatusEffectInstance effect,
            AnglingDomainMapSnapshot snapshot
    ) {
        snapshot.effectModifiers().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey(Comparator.comparing(AnglingRegistrySelector::toString)))
                .filter(value -> matchesEffect(effect, value.getKey()))
                .forEach(value -> append(result, value.getValue()));
    }

    private static boolean matchesItem(ItemStack stack, AnglingRegistrySelector selector) {
        return selector.tag()
                ? stack.isIn(TagKey.of(RegistryKeys.ITEM, selector.id()))
                : Registries.ITEM.getId(stack.getItem()).equals(selector.id());
    }

    private static boolean matchesEffect(StatusEffectInstance effect, AnglingRegistrySelector selector) {
        return selector.tag()
                ? effect.getEffectType().isIn(TagKey.of(RegistryKeys.STATUS_EFFECT, selector.id()))
                : effect.getEffectType().getKey().map(key -> key.getValue().equals(selector.id())).orElse(false);
    }

    private static void append(List<AnglingCompiledModifier> result, List<AnglingCompiledModifier> additions) {
        if (result.size() + additions.size() > MAX_RESOLVED_MODIFIERS) {
            throw new IllegalStateException("Resolved Angling equipment modifiers exceed " + MAX_RESOLVED_MODIFIERS);
        }
        result.addAll(additions);
    }

    public record Resolved(List<AnglingCompiledModifier> modifiers) {
        public Resolved {
            modifiers = List.copyOf(Objects.requireNonNull(modifiers, "modifiers"));
            if (modifiers.size() > MAX_RESOLVED_MODIFIERS) {
                throw new IllegalArgumentException("Resolved modifier list is unbounded");
            }
        }

        public boolean has(String path) {
            return modifiers.stream().anyMatch(value -> value.type().getPath().equals(path));
        }

        public float multiplier(String path) {
            float result = 1.0F;
            for (AnglingCompiledModifier modifier : modifiers) {
                if (modifier.type().getPath().equals(path)
                        && modifier.value() instanceof AnglingModifierValue.Multiplier multiplier) {
                    result *= multiplier.multiplier();
                }
            }
            if (!Float.isFinite(result)) throw new IllegalStateException("Angling modifier product is non-finite");
            return result;
        }

        public LureTiming lureTiming(int minimumTicks, int maximumTicks, double chancePerTick) {
            double minimum = minimumTicks;
            double maximum = maximumTicks;
            double chance = chancePerTick;
            for (AnglingCompiledModifier modifier : modifiers) {
                if (modifier.value() instanceof AnglingModifierValue.AdjustLureTime lure) {
                    minimum *= lure.minimumMultiplier();
                    maximum *= lure.maximumMultiplier();
                    chance *= lure.chanceMultiplier();
                }
            }
            return new LureTiming(Math.max(1, boundedInt(minimum)), Math.max(1, boundedInt(maximum)),
                    Math.clamp(chance, 0.0D, 1.0D));
        }

        public double throwSpeedMultiplier() {
            return multiplier("bost_thrown_speed");
        }

        private static int boundedInt(double value) {
            if (!Double.isFinite(value) || value > 999_999) {
                throw new IllegalStateException("Angling lure timing is outside its bounded range");
            }
            return (int) value;
        }
    }

    public record LureTiming(int minimumTicks, int maximumTicks, double chancePerTick) {
        public LureTiming {
            if (minimumTicks < 1 || maximumTicks < 1 || minimumTicks > maximumTicks
                    || !Double.isFinite(chancePerTick) || chancePerTick < 0 || chancePerTick > 1) {
                throw new IllegalArgumentException("invalid resolved lure timing");
            }
        }
    }
}

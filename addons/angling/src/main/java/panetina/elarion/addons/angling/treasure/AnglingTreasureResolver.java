package panetina.elarion.addons.angling.treasure;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;
import panetina.elarion.addons.angling.domainmap.AnglingDomainMaps;
import panetina.elarion.addons.angling.domainmap.AnglingRegistrySelector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/** Bounded server-only treasure unpacker matching the frozen combined weighted pool. */
public final class AnglingTreasureResolver {
    public Optional<ItemStack> select(
            ServerPlayerEntity player,
            Vec3d origin,
            ItemStack rod,
            AnglingCatchSnapshot.NativeCatch selected,
            RandomGenerator random
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(rod, "rod");
        Objects.requireNonNull(selected, "selected");
        Objects.requireNonNull(random, "random");
        Optional<AnglingTreasureDefinition> definition = definition(selected);
        if (definition.isEmpty()) return Optional.empty();
        AnglingTreasureDefinition value = definition.orElseThrow();
        int total = 0;
        for (var table : value.lootTables()) total = Math.addExact(total, table.weight());
        for (var stack : value.stacks()) total = Math.addExact(total, stack.weight());
        if (total == 0) return Optional.empty();
        int roll = random.nextInt(total);
        for (var table : value.lootTables()) {
            if (roll < table.weight()) return loot(player, origin, rod, table.lootTable(), value.blacklist(), random);
            roll -= table.weight();
        }
        for (var stack : value.stacks()) {
            if (roll < stack.weight()) {
                var reference = stack.stack();
                if (!Registries.ITEM.containsId(reference.id())) return Optional.empty();
                return Optional.of(new ItemStack(Registries.ITEM.get(reference.id()), reference.count()));
            }
            roll -= stack.weight();
        }
        throw new IllegalStateException("Angling treasure weighted selection did not resolve");
    }

    public Optional<ItemStack> fromLootTable(
            ServerPlayerEntity player,
            Vec3d origin,
            ItemStack rod,
            Identifier lootTable,
            RandomGenerator random
    ) {
        return loot(player, origin, rod, lootTable, List.of(), random);
    }

    private static Optional<AnglingTreasureDefinition> definition(AnglingCatchSnapshot.NativeCatch selected) {
        var entries = AnglingDomainMaps.snapshot().treasures().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey(Comparator.comparing(AnglingRegistrySelector::toString)))
                .toList();
        for (var entry : entries) {
            if (!entry.getKey().tag() && entry.getKey().id().equals(selected.id())) {
                return Optional.of(entry.getValue());
            }
        }
        Identifier rarityTag = Identifier.of("elarion_angling", selected.rarity().serializedName() + "_entries");
        for (var entry : entries) {
            if (entry.getKey().tag() && entry.getKey().id().equals(rarityTag)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    private static Optional<ItemStack> loot(
            ServerPlayerEntity player,
            Vec3d origin,
            ItemStack rod,
            Identifier lootTableId,
            List<AnglingTreasureDefinition.IngredientSelector> blacklist,
            RandomGenerator random
    ) {
        ServerWorld world = player.getServerWorld();
        LootContextParameterSet parameters = new LootContextParameterSet.Builder(world)
                .add(LootContextParameters.ORIGIN, origin)
                .add(LootContextParameters.TOOL, rod)
                .add(LootContextParameters.THIS_ENTITY, player)
                .luck(player.getLuck())
                .build(LootContextTypes.FISHING);
        LootTable table = player.getServer().getReloadableRegistries().getLootTable(
                RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableId));
        ArrayList<ItemStack> valid = new ArrayList<>();
        for (ItemStack stack : table.generateLoot(parameters)) {
            if (!stack.isEmpty() && blacklist.stream().noneMatch(value -> matches(stack, value))) {
                valid.add(stack);
            }
        }
        return valid.isEmpty() ? Optional.empty() : Optional.of(valid.get(random.nextInt(valid.size())).copy());
    }

    private static boolean matches(ItemStack stack, AnglingTreasureDefinition.IngredientSelector selector) {
        if (selector.item().isPresent()) {
            return Registries.ITEM.getId(stack.getItem()).equals(selector.item().orElseThrow());
        }
        return stack.isIn(TagKey.of(RegistryKeys.ITEM, selector.tag().orElseThrow()));
    }
}

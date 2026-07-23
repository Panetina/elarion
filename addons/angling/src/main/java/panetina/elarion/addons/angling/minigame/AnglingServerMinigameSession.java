package panetina.elarion.addons.angling.minigame;

import panetina.elarion.addons.angling.network.AnglingMinigameInputAction;
import panetina.elarion.addons.angling.network.AnglingMinigameInputPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * One bobber-owned deterministic server simulation. The owning bobber ticks
 * this object; there is no global session scan and no client-provided result.
 */
public final class AnglingServerMinigameSession {
    public static final int HOLDING_DELAY_TICKS = 6;
    public static final int MAX_CATCH_UP_TICKS = 20;
    public static final long DEFAULT_LIFETIME_TICKS = 12_000L;

    private static final AnglingServerMinigameSpec.Sweetspot TREASURE_SPOT =
            new AnglingServerMinigameSpec.Sweetspot(
                    AnglingSweetspotBehaviorType.TREASURE,
                    Identifier.of("elarion_angling", "textures/gui/minigame/spots/treasure.png"),
                    20, 15, false,
                    0.0F, 0.0F, 0xffFFD700, List.of());
    private static final AnglingServerMinigameSpec.Sweetspot LEAF_SPOT =
            new AnglingServerMinigameSpec.Sweetspot(
                    AnglingSweetspotBehaviorType.LEAF,
                    Identifier.of("elarion_angling", "textures/gui/minigame/spots/leaf.png"),
                    15, 15, false, 0.0F, 0.0F, 0xff00ff00, List.of());

    private final UUID sessionId;
    private final UUID actorId;
    private final int bobberEntityId;
    private final long openedAtTick;
    private final long expiresAtTick;
    private final AnglingServerMinigameSpec spec;
    private final AnglingMinigameInputGate inputGate;
    private final Random random;
    private final List<ActiveSweetspot> sweetspots = new ArrayList<>();
    private final List<AnglingNativeModifier> activeModifiers = new ArrayList<>();

    private AnglingServerMinigameStatus status = AnglingServerMinigameStatus.ACTIVE;
    private long lastTick;
    private long revision;
    private long elapsedTicks;
    private float pointerPosition;
    private float pointerSpeed;
    private int pointerRotation = 1;
    private float progress;
    private float smoothedProgress;
    private int treasureProgress;
    private boolean perfect = true;
    private int consecutiveHits;
    private int totalHits;
    private int holdingTicks;
    private int gracePeriod = Integer.MAX_VALUE;
    private int darkness;
    private int pointerLayer;
    private int maximumLayers;
    private int nextSweetspotIndex;
    private float teleportReturnPosition;
    private SpeedEffect speedEffect;
    private boolean bounced;
    private int bounceBackTicks;

    public AnglingServerMinigameSession(
            UUID sessionId,
            UUID actorId,
            int bobberEntityId,
            long openedAtTick,
            long lifetimeTicks,
            long seed,
            AnglingServerMinigameSpec spec
    ) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.actorId = Objects.requireNonNull(actorId, "actorId");
        this.spec = Objects.requireNonNull(spec, "spec");
        if (bobberEntityId < 0 || openedAtTick < 0) {
            throw new IllegalArgumentException("Invalid bobber identity or opening tick");
        }
        if (lifetimeTicks < 1 || lifetimeTicks > AnglingMinigameInputGate.MAX_LIFETIME_TICKS) {
            throw new IllegalArgumentException("Minigame lifetime is outside the bounded range");
        }
        this.bobberEntityId = bobberEntityId;
        this.openedAtTick = openedAtTick;
        this.expiresAtTick = Math.addExact(openedAtTick, lifetimeTicks);
        this.lastTick = openedAtTick;
        this.random = new Random(seed);
        this.inputGate = new AnglingMinigameInputGate(
                sessionId, actorId, bobberEntityId, openedAtTick, lifetimeTicks);
        this.pointerSpeed = spec.pointerSpeed();
        this.progress = spec.initialProgress();
        this.smoothedProgress = spec.initialProgress();
        this.teleportReturnPosition = random.nextInt(360);
        activeModifiers.addAll(spec.modifiers());
        for (AnglingNativeModifier modifier : activeModifiers) {
            if (modifier instanceof AnglingNativeModifier.MultiLayer layers) {
                maximumLayers = Math.max(maximumLayers, layers.maximumLayers());
            }
        }
        for (AnglingServerMinigameSpec.Sweetspot sweetspot : spec.sweetspots()) {
            addSweetspot(sweetspot);
        }
    }

    public AnglingMinigameInputGate.Result acceptInput(
            UUID senderId,
            AnglingMinigameInputPayload payload,
            long serverTick
    ) {
        tick(serverTick);
        if (status.terminal()) return AnglingMinigameInputGate.Result.CLOSED;
        AnglingMinigameInputGate.Result result = inputGate.accept(senderId, payload, serverTick);
        if (result != AnglingMinigameInputGate.Result.ACCEPTED) {
            if (result == AnglingMinigameInputGate.Result.EXPIRED) finish(AnglingServerMinigameStatus.EXPIRED);
            return result;
        }
        switch (payload.action()) {
            case PRESS -> press();
            case RELEASE -> holdingTicks = 0;
            case LAYER_PREVIOUS -> pointerLayer = Math.max(0, pointerLayer - 1);
            case LAYER_NEXT -> pointerLayer = Math.min(maximumLayers, pointerLayer + 1);
            case ABANDON -> finish(AnglingServerMinigameStatus.ABANDONED);
            case INVALID -> throw new IllegalStateException("Input gate accepted an invalid action");
        }
        revision++;
        return result;
    }

    /** Advances only this bobber's session and bounds lag catch-up work. */
    public void tick(long serverTick) {
        if (status.terminal() || serverTick <= lastTick) return;
        if (serverTick > expiresAtTick) {
            finish(AnglingServerMinigameStatus.EXPIRED);
            lastTick = serverTick;
            return;
        }
        long catchUp = serverTick - lastTick;
        if (catchUp > MAX_CATCH_UP_TICKS) {
            finish(AnglingServerMinigameStatus.EXPIRED);
            lastTick = serverTick;
            return;
        }
        while (lastTick < serverTick && !status.terminal()) {
            lastTick++;
            tickOnce();
        }
    }

    private void tickOnce() {
        elapsedTicks++;
        if (inputGate.isPressed()) {
            holdingTicks++;
            if (holdingTicks > HOLDING_DELAY_TICKS) press();
        }

        tickSpeedEffect();
        tickSpawningModifiers();
        tickPersistentModifiers();
        for (ActiveSweetspot spot : sweetspots) spot.tick();
        sweetspots.removeIf(ActiveSweetspot::removed);

        pointerPosition = normalize(pointerPosition + pointerSpeed * pointerRotation);
        gracePeriod--;
        smoothedProgress += (progress - smoothedProgress) / 6.0F;
        if (elapsedTicks % 5L == 0L && gracePeriod < 0) progress -= spec.decay();

        if (smoothedProgress < 0.0F) finish(AnglingServerMinigameStatus.FAILED);
        else if (smoothedProgress > spec.hitPoints()) finish(AnglingServerMinigameStatus.SUCCEEDED);
        revision++;
    }

    private void press() {
        if (status.terminal()) return;
        if (gracePeriod > 0) gracePeriod = 0;
        float hitPosition = normalize(pointerPosition + spec.hitDelayTicks() * pointerSpeed * pointerRotation);
        ActiveSweetspot hit = null;
        for (ActiveSweetspot spot : sweetspots) {
            if (spot.layer == pointerLayer && overlaps(hitPosition, spot.position, spot.spec.hitboxSizePixels() / 2)) {
                hit = spot;
                break;
            }
        }
        if (hit == null) miss();
        else hit(hit);
    }

    private void hit(ActiveSweetspot spot) {
        consecutiveHits++;
        totalHits++;
        maybeAddTreasure();
        for (AnglingNativeModifier modifier : activeModifiers) {
            if (modifier instanceof AnglingNativeModifier.DeepDark
                    && spot.spec.behavior() != AnglingSweetspotBehaviorType.GLOWING) {
                darkness = Math.min(255, darkness + 40);
            } else if (modifier instanceof AnglingNativeModifier.Teleport) {
                float oldSpot = spot.position;
                pointerPosition = teleportReturnPosition;
                teleportReturnPosition = oldSpot;
            } else if (modifier instanceof AnglingNativeModifier.FlipEveryHit) {
                pointerRotation *= -1;
                for (ActiveSweetspot active : sweetspots) active.rotation *= -1;
            } else if (modifier instanceof AnglingNativeModifier.StopDecayOnHit stopDecay) {
                gracePeriod = stopDecay.graceTicks();
            } else if (modifier instanceof AnglingNativeModifier.SpawnTreasureOnHit threshold
                    && consecutiveHits == threshold.hits() && treasureProgress == 0
                    && sweetspots.stream().noneMatch(active ->
                    active.spec.behavior() == AnglingSweetspotBehaviorType.TREASURE)) {
                addSweetspot(TREASURE_SPOT);
                addSweetspot(TREASURE_SPOT);
            }
        }

        switch (spot.spec.behavior()) {
            case TREASURE -> {
                if (spot.spec.flip()) pointerRotation *= -1;
                treasureProgress = Math.min(100, treasureProgress + spot.spec.reward());
                if (treasureProgress >= 100) spot.removed = true;
                else reposition(spot);
            }
            case TNT -> {
                progress -= spot.spec.reward();
                spot.removed = true;
            }
            case LEAF -> {
                normalHit(spot, true, false);
            }
            case CLOUD -> normalHit(spot, false, true);
            case FREEZE -> {
                normalHit(spot, false, false);
                startFreeze(40, 10);
            }
            case GLOWING -> {
                normalHit(spot, false, false);
                darkness = Math.max(0, darkness - 10);
            }
            case NORMAL, AQUA, DEEP_OCEAN -> normalHit(spot, false, false);
        }
        addOnHitModifiers(spot.spec.onHitModifiers());
    }

    private void normalHit(ActiveSweetspot spot, boolean remove, boolean preservePosition) {
        float priorPosition = spot.position;
        progress += spot.spec.reward();
        if (spot.spec.flip()) pointerRotation *= -1;
        if (remove) spot.removed = true;
        else if (!preservePosition) reposition(spot);
        else spot.position = priorPosition;
        spot.alpha = 1.0F;
    }

    private void miss() {
        if (has(AnglingNativeModifier.BounceBack.class)) {
            if (bounceBackTicks > 0) progress += spec.missPenalty();
            if (progress <= spec.missPenalty() && !bounced) {
                progress += spec.missPenalty();
                bounceBackTicks = Math.max(1, spec.hitPoints() / 5);
                bounced = true;
            }
        }
        consecutiveHits = 0;
        perfect = false;
        progress -= spec.missPenalty();
        for (ActiveSweetspot spot : sweetspots) {
            spot.alpha = 1.0F;
            if (spot.spec.behavior() == AnglingSweetspotBehaviorType.DEEP_OCEAN) spot.removed = true;
        }
        if (has(AnglingNativeModifier.MoveSweetspotsOnMiss.class)) {
            for (ActiveSweetspot spot : sweetspots) reposition(spot);
        }
        for (AnglingNativeModifier modifier : activeModifiers) applyMissModifier(modifier);
    }

    private void addOnHitModifiers(List<AnglingNativeModifier> modifiers) {
        for (AnglingNativeModifier modifier : modifiers) {
            if (activeModifiers.size() >= AnglingServerMinigameSpec.MAX_MODIFIERS) return;
            activeModifiers.add(modifier);
            if (modifier instanceof AnglingNativeModifier.MultiLayer layers
                    && layers.maximumLayers() > maximumLayers) {
                maximumLayers = layers.maximumLayers();
                for (ActiveSweetspot spot : sweetspots) {
                    spot.layer = random.nextInt(maximumLayers + 1);
                }
            }
        }
    }

    private void applyMissModifier(AnglingNativeModifier modifier) {
        if (modifier instanceof AnglingNativeModifier.DeepDark) {
            darkness = Math.max(0, darkness - 10);
        } else if (modifier instanceof AnglingNativeModifier.FlipSweetspotsOnMiss flip) {
            for (ActiveSweetspot spot : sweetspots) {
                if (random.nextFloat() < flip.chance()) spot.rotation *= -1;
            }
        } else if (modifier instanceof AnglingNativeModifier.FreezeOnMiss freeze) {
            startFreeze(freeze.length(), freeze.rampTime());
        } else if (modifier instanceof AnglingNativeModifier.BurnOnMiss burn) {
            startBurn(burn.length(), burn.rampTime(), burn.extraSpeed());
        } else if (modifier instanceof AnglingNativeModifier.Teleport) {
            pointerPosition = random.nextFloat() * 360.0F;
        }
    }

    private void startFreeze(int length, int rampTime) {
        if (has(AnglingNativeModifier.PreventFrozen.class)) return;
        speedEffect = new SpeedEffect(SpeedEffectType.FREEZE, length, rampTime, 0.0F, 0);
    }

    private void startBurn(int length, int rampTime, int extraSpeed) {
        speedEffect = new SpeedEffect(SpeedEffectType.BURN, length, rampTime, extraSpeed, 0);
    }

    private void tickSpeedEffect() {
        if (speedEffect == null) {
            pointerSpeed = spec.pointerSpeed();
            return;
        }
        speedEffect = speedEffect.next();
        int elapsed = speedEffect.elapsed;
        int ramp = Math.max(1, speedEffect.rampTime);
        float phaseIn = Math.min(1.0F, elapsed / (float) ramp);
        float phaseOut = Math.min(1.0F, Math.max(0, speedEffect.length - elapsed) / (float) ramp);
        float strength = Math.min(phaseIn, phaseOut);
        if (speedEffect.type == SpeedEffectType.FREEZE) {
            pointerSpeed = spec.pointerSpeed() * (1.0F - strength);
        } else {
            pointerSpeed = spec.pointerSpeed() + speedEffect.extraSpeed * strength;
        }
        if (elapsed >= speedEffect.length) {
            speedEffect = null;
            pointerSpeed = spec.pointerSpeed();
        }
    }

    /** Mirrors modifier tick order before smoothing/decay, including the low-progress bounce trigger. */
    private void tickPersistentModifiers() {
        if (has(AnglingNativeModifier.BounceBack.class)) {
            if (bounced) bounceBackTicks--;
            if (bounceBackTicks > 0) {
                progress += 1;
                if (smoothedProgress < 5) smoothedProgress = 5;
            }
            if (smoothedProgress < 2 && !bounced) {
                bounced = true;
                bounceBackTicks = Math.max(1, spec.hitPoints() / 5);
            }
        }
        if (has(AnglingNativeModifier.NeverLose.class) && smoothedProgress < 5) {
            smoothedProgress += spec.hitPoints() / 5.0F;
            progress = spec.hitPoints() / 5.0F;
        }
    }

    private void addSweetspot(AnglingServerMinigameSpec.Sweetspot sweetspot) {
        if (sweetspots.size() >= AnglingServerMinigameSpec.MAX_RUNTIME_SWEETSPOTS) return;
        if (sweetspot.behavior() == AnglingSweetspotBehaviorType.TREASURE && !spec.treasureAvailable()) return;
        ActiveSweetspot active = new ActiveSweetspot(sweetspot, nextSweetspotIndex++);
        reposition(active);
        sweetspots.add(active);
    }

    private void maybeAddTreasure() {
        if (spec.treasureAvailable() && treasureProgress == 0
                && sweetspots.stream().noneMatch(s -> s.spec.behavior() == AnglingSweetspotBehaviorType.TREASURE)
                && random.nextFloat() > 0.98F) {
            addSweetspot(TREASURE_SPOT);
        }
    }

    private void tickSpawningModifiers() {
        for (AnglingNativeModifier modifier : activeModifiers) {
            if (modifier instanceof AnglingNativeModifier.AddLeaves leaves
                    && random.nextFloat() < leaves.chancePerTick()
                    && sweetspots.stream().noneMatch(spot -> spot.spec.behavior() == AnglingSweetspotBehaviorType.LEAF)) {
                addSweetspot(LEAF_SPOT);
            } else if (modifier instanceof AnglingNativeModifier.SpawnSweetspots spawning
                    && (spawning.length() < 0 || elapsedTicks <= spawning.length())
                    && elapsedTicks % spawning.cooldown() == 0
                    && random.nextFloat() < spawning.chance()
                    && sweetspots.size() < AnglingServerMinigameSpec.MAX_RUNTIME_SWEETSPOTS) {
                addSweetspot(spawning.sweetspot());
            }
        }
    }

    private boolean has(Class<? extends AnglingNativeModifier> type) {
        return activeModifiers.stream().anyMatch(type::isInstance);
    }

    private void reposition(ActiveSweetspot spot) {
        int initial = random.nextInt(360);
        for (int offset = 0; offset < 180; offset++) {
            for (int direction = 1; direction >= -1; direction -= 2) {
                int candidate = (int) normalize(initial + offset * direction);
                boolean occupied = sweetspots.stream().anyMatch(other -> other != spot && !other.removed
                        && overlaps(candidate, other.position,
                        (other.spec.hitboxSizePixels() + spot.spec.hitboxSizePixels()) / 2));
                if (!occupied) {
                    spot.position = candidate;
                    return;
                }
            }
        }
        spot.position = random.nextInt(360);
    }

    private void finish(AnglingServerMinigameStatus terminalStatus) {
        if (status.terminal()) return;
        if (!terminalStatus.terminal()) throw new IllegalArgumentException("Terminal status required");
        status = terminalStatus;
        inputGate.close();
        revision++;
    }

    public AnglingServerMinigameSnapshot snapshot() {
        List<AnglingServerMinigameSnapshot.Sweetspot> projected = sweetspots.stream()
                .filter(spot -> !spot.removed)
                .map(spot -> new AnglingServerMinigameSnapshot.Sweetspot(
                        spot.index, spot.spec.behavior(), spot.spec.texturePath(), spot.position, spot.layer,
                        spot.spec.hitboxSizePixels(), spot.alpha, spot.spec.color()))
                .toList();
        return new AnglingServerMinigameSnapshot(
                sessionId, revision, elapsedTicks, status, pointerPosition, pointerSpeed,
                pointerRotation, pointerLayer, maximumLayers,
                progress, smoothedProgress, treasureProgress, perfect,
                consecutiveHits, totalHits, darkness, projected);
    }

    public UUID sessionId() { return sessionId; }
    public UUID actorId() { return actorId; }
    public int bobberEntityId() { return bobberEntityId; }
    public long openedAtTick() { return openedAtTick; }
    public AnglingServerMinigameStatus status() { return status; }
    public boolean treasureCompleted() { return treasureProgress >= 100; }

    static boolean overlaps(float first, float second, int leeway) {
        float distance = Math.abs(normalize(first) - normalize(second));
        return distance < leeway || distance > 360.0F - leeway;
    }

    private static float normalize(float degrees) {
        float result = degrees % 360.0F;
        return result < 0.0F ? result + 360.0F : result;
    }

    private final class ActiveSweetspot {
        private final AnglingServerMinigameSpec.Sweetspot spec;
        private final int index;
        private int layer;
        private int rotation = -1;
        private int ticksActive;
        private float position;
        private float alpha = 1.0F;
        private boolean removed;

        private ActiveSweetspot(AnglingServerMinigameSpec.Sweetspot spec, int index) {
            this.spec = spec;
            this.index = index;
            this.layer = maximumLayers == 0 ? 0 : random.nextInt(maximumLayers + 1);
            if (spec.behavior() == AnglingSweetspotBehaviorType.AQUA) alpha = 1.0F;
        }

        private void tick() {
            ticksActive++;
            position = normalize(position + spec.movingRate() * rotation);
            float vanish = spec.behavior() == AnglingSweetspotBehaviorType.AQUA
                    ? 0.018F : spec.vanishingRate();
            alpha -= vanish;
            if ((spec.behavior() == AnglingSweetspotBehaviorType.AQUA
                    || spec.behavior() == AnglingSweetspotBehaviorType.TNT) && alpha <= 0.0F) removed = true;
            if (spec.behavior() == AnglingSweetspotBehaviorType.TNT && ticksActive > 40) removed = true;
            if (spec.behavior() == AnglingSweetspotBehaviorType.TREASURE && treasureProgress >= 100) removed = true;
        }

        private boolean removed() {
            return removed;
        }
    }

    private enum SpeedEffectType { FREEZE, BURN }

    private record SpeedEffect(SpeedEffectType type, int length, int rampTime, float extraSpeed, int elapsed) {
        private SpeedEffect {
            Objects.requireNonNull(type, "type");
            length = Math.max(0, length);
            rampTime = Math.max(0, rampTime);
        }

        private SpeedEffect next() {
            return new SpeedEffect(type, length, rampTime, extraSpeed, elapsed + 1);
        }
    }
}

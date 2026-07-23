package panetina.elarion.addons.angling.compile;

import panetina.elarion.addons.angling.minigame.AnglingNativeModifier;
import panetina.elarion.addons.angling.minigame.AnglingNativeModifierCompilers;
import panetina.elarion.addons.angling.minigame.AnglingSweetspotBehaviorType;
import panetina.elarion.addons.angling.minigame.AnglingSweetspotBehaviors;
import panetina.elarion.addons.angling.restriction.AnglingRestriction;
import panetina.elarion.addons.angling.restriction.AnglingRestrictionCompilers;

/** Complete typed compiler set for every schema used by the 148 native catches. */
public final class AnglingNativeDefinitionCompilers {
    private AnglingNativeDefinitionCompilers() {
    }

    public static AnglingDefinitionCompilerSet<AnglingRestriction, AnglingNativeModifier,
            AnglingSweetspotBehaviorType> create() {
        return new AnglingDefinitionCompilerSet<>(
                AnglingRestrictionCompilers.create(),
                AnglingNativeModifierCompilers.create(),
                AnglingSweetspotBehaviors.create()
        );
    }
}

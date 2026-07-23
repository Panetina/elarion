package panetina.elarion.addons.angling.compile;

import net.minecraft.util.Identifier;

/** Actionable reload failure for one catch definition and one typed node. */
public final class AnglingDefinitionCompileException extends IllegalArgumentException {
    private final Identifier definitionId;
    private final Identifier nodeType;

    public AnglingDefinitionCompileException(Identifier definitionId, Identifier nodeType, String message) {
        super("Catch " + definitionId + ", node " + nodeType + ": " + message);
        this.definitionId = definitionId;
        this.nodeType = nodeType;
    }

    public Identifier definitionId() {
        return definitionId;
    }

    public Identifier nodeType() {
        return nodeType;
    }
}

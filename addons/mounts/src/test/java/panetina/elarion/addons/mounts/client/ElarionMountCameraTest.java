package panetina.elarion.addons.mounts.client;

import net.minecraft.client.option.Perspective;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElarionMountCameraTest {
    @Test
    void forcedMountedPerspectiveAlwaysUsesRearThirdPerson() {
        assertEquals(Perspective.THIRD_PERSON_BACK,
                ElarionMountCamera.forcedMountedPerspective(Perspective.FIRST_PERSON));
        assertEquals(Perspective.THIRD_PERSON_BACK,
                ElarionMountCamera.forcedMountedPerspective(Perspective.THIRD_PERSON_BACK));
        assertEquals(Perspective.THIRD_PERSON_BACK,
                ElarionMountCamera.forcedMountedPerspective(Perspective.THIRD_PERSON_FRONT));
    }
}

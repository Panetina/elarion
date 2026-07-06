package panetina.elarion.addons.mounts.client;

public final class ElarionMountRiderRenderContext {
    private static boolean renderingMountedRider;

    private ElarionMountRiderRenderContext() {
    }

    public static boolean renderingMountedRider() {
        return renderingMountedRider;
    }

    public static void renderMountedRider(Runnable renderer) {
        boolean previous = renderingMountedRider;
        renderingMountedRider = true;
        try {
            renderer.run();
        } finally {
            renderingMountedRider = previous;
        }
    }
}

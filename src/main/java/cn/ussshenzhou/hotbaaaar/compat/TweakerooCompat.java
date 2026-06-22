package cn.ussshenzhou.hotbaaaar.compat;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

public class TweakerooCompat {

    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("tweakeroo");
    private static Method isZoomActive;
    private static Method isSpyglassZoomActive;
    private static Object tweakZoomEnum;
    private static Method getKeybind;
    private static Method isKeybindHeld;

    static {
        if (LOADED) {
            try {
                Class<?> miscUtils = Class.forName("fi.dy.masa.tweakeroo.util.MiscUtils");
                isZoomActive = miscUtils.getMethod("isZoomActive");
                isSpyglassZoomActive = miscUtils.getMethod("isSpyglassZoomActive");

                @SuppressWarnings("unchecked")
                Class<? extends Enum<?>> featureToggle = (Class<? extends Enum<?>>) Class.forName("fi.dy.masa.tweakeroo.config.FeatureToggle");
                for (Enum<?> e : featureToggle.getEnumConstants()) {
                    if ("TWEAK_ZOOM".equals(e.name())) {
                        tweakZoomEnum = e;
                        break;
                    }
                }
                if (tweakZoomEnum != null) {
                    getKeybind = tweakZoomEnum.getClass().getMethod("getKeybind");
                    Object kb = getKeybind.invoke(tweakZoomEnum);
                    isKeybindHeld = kb.getClass().getMethod("isKeybindHeld");
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static boolean isZoomScrollActive() {
        if (!LOADED) {
            return false;
        }
        try {
            if (tweakZoomEnum != null && getKeybind != null && isKeybindHeld != null) {
                Object kb = getKeybind.invoke(tweakZoomEnum);
                if ((boolean) isKeybindHeld.invoke(kb)) {
                    return true;
                }
            }
            if (isZoomActive != null && (boolean) isZoomActive.invoke(null)) {
                return true;
            }
            if (isSpyglassZoomActive != null && (boolean) isSpyglassZoomActive.invoke(null)) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}

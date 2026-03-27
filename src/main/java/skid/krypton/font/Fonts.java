package skid.krypton.font;

public final class Fonts {
    // Using a modern, clean font - you can change the path to your custom font file
    public static GlyphPageFontRenderer FONT = GlyphPageFontRenderer.init("/assets/uranium/fonts/Inter-Regular.ttf", 18, false, false, false);
    public static GlyphPageFontRenderer TITLE_FONT = GlyphPageFontRenderer.init("/assets/uranium/fonts/Inter-Bold.ttf", 24, false, false, false);
    public static GlyphPageFontRenderer SMALL_FONT = GlyphPageFontRenderer.init("/assets/uranium/fonts/Inter-Regular.ttf", 14, false, false, false);
}

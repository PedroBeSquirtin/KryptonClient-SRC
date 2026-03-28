package skid.krypton.module.setting;

public class BlocksSetting extends Setting {

    private String display;

    public BlocksSetting(String name) {
        super(name);
        this.display = "Blocks: 0 Blocks";
    }

    public void setDisplay(String text) {
        this.display = text;
    }

    public String getDisplay() {
        return display;
    }
}

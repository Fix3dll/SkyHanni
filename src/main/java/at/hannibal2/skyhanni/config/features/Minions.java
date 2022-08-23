package at.hannibal2.skyhanni.config.features;

import at.hannibal2.skyhanni.config.gui.core.config.annotations.*;
import com.google.gson.annotations.Expose;

public class Minions {

    @ConfigOption(name = "Last Clicked", desc = "")
    @ConfigEditorAccordion(id = 0)
    public boolean lastClickedMinion = false;

    @Expose
    @ConfigOption(name = "Last Minion Display", desc = "Marks the location of the last clicked minion (even through walls)")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 0)
    public boolean lastClickedMinionDisplay = false;

    @Expose
    @ConfigOption(
            name = "Last Minion Color",
            desc = "The colour in which the last minion should be displayed"
    )
    @ConfigEditorColour
    @ConfigAccordionId(id = 0)
    public String lastOpenedMinionColor = "0:245:85:255:85";

    @Expose
    @ConfigOption(
            name = "Last Minion Time",
            desc = "Time in seconds how long the last minion should be displayed"
    )
    @ConfigEditorSlider(
            minValue = 3,
            maxValue = 120,
            minStep = 1
    )
    @ConfigAccordionId(id = 0)
    public int lastOpenedMinionTime = 20;

    @ConfigOption(name = "Emptied Time", desc = "")
    @ConfigEditorAccordion(id = 1)
    public boolean emptiedTime = false;

    @Expose
    @ConfigOption(name = "Emptied Time Display", desc = "Show the time when the hopper in the minion was last emptied")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 1)
    public boolean emptiedTimeDisplay = false;

    @Expose
    @ConfigOption(
            name = "Emptied Time Distance",
            desc = "At what distance is this display shown"
    )
    @ConfigEditorSlider(
            minValue = 3,
            maxValue = 30,
            minStep = 1
    )
    @ConfigAccordionId(id = 1)
    public int emptiedTimeDistance = 10;

    @Expose
    @ConfigOption(name = "Hide Mob Nametag", desc = "Hiding the nametag of mobs close to minions")
    @ConfigEditorBoolean
    public boolean hideMobsNametagNearby = false;
}

package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.events.TablistFooterUpdateEvent
import at.hannibal2.skyhanni.mixins.hooks.tabListGuard
import at.hannibal2.skyhanni.mixins.transformers.AccessorGuiPlayerTabOverlay
import at.hannibal2.skyhanni.utils.ConditionalUtils.conditionalTransform
import at.hannibal2.skyhanni.utils.ConditionalUtils.transformIf
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import com.google.common.collect.ComparisonChain
import com.google.common.collect.Ordering
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.world.WorldSettings
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object TabListData {
    private var tablistCache = emptyList<String>()
    private var debugCache: List<String>? = null

    private var header = ""
    private var footer = ""

    var fullyLoaded = false

    // TODO replace with TabListUpdateEvent
    fun getTabList() = debugCache ?: tablistCache
    fun getHeader() = header
    fun getFooter() = footer

    fun toggleDebugCommand() {
        if (debugCache != null) {
            ChatUtils.chat("Disabled tab list debug.")
            debugCache = null
            return
        }
        SkyHanniMod.coroutineScope.launch {
            val clipboard = OSUtils.readFromClipboard() ?: return@launch
            debugCache = clipboard.lines()
            ChatUtils.chat("Enabled tab list debug with your clipboard.")
        }
    }

    fun copyCommand(args: Array<String>) {
        if (debugCache != null) {
            ChatUtils.clickableChat("Tab list debug is enabled!", "shdebugtablist")
            return
        }

        val resultList = mutableListOf<String>()
        val noColor = args.size == 1 && args[0] == "true"
        for (line in getTabList()) {
            val tabListLine = line.transformIf({ noColor }) { removeColor() }
            if (tabListLine != "") resultList.add("'$tabListLine'")
        }

        val tabHeader = header.conditionalTransform(noColor, { this.removeColor() }, { this })
        val tabFooter = footer.conditionalTransform(noColor, { this.removeColor() }, { this })

        val string = "Header:\n\n$tabHeader\n\nBody:\n\n${resultList.joinToString("\n")}\n\nFooter:\n\n$tabFooter"

        OSUtils.copyToClipboard(string)
        ChatUtils.chat("Tab list copied into the clipboard!")
    }

    private val playerOrdering = Ordering.from(PlayerComparator())

    @SideOnly(Side.CLIENT)
    internal class PlayerComparator : Comparator<NetworkPlayerInfo> {

        override fun compare(o1: NetworkPlayerInfo, o2: NetworkPlayerInfo): Int {
            val team1 = o1.playerTeam
            val team2 = o2.playerTeam
            return ComparisonChain.start().compareTrueFirst(
                o1.gameType != WorldSettings.GameType.SPECTATOR,
                o2.gameType != WorldSettings.GameType.SPECTATOR
            )
                .compare(
                    if (team1 != null) team1.registeredName else "",
                    if (team2 != null) team2.registeredName else ""
                )
                .compare(o1.gameProfile.name, o2.gameProfile.name).result()
        }
    }

    private fun readTabList(): List<String>? {
        val thePlayer = Minecraft.getMinecraft()?.thePlayer ?: return null
        val players = playerOrdering.sortedCopy(thePlayer.sendQueue.playerInfoMap)
        val result = mutableListOf<String>()
        tabListGuard = true
        for (info in players) {
            val name = Minecraft.getMinecraft().ingameGUI.tabList.getPlayerName(info)
            result.add(LorenzUtils.stripVanillaMessage(name))
        }
        tabListGuard = false
        return result.dropLast(1)
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!event.isMod(2)) return

        val tabList = readTabList() ?: return
        if (tablistCache != tabList) {
            tablistCache = tabList
            TabListUpdateEvent(getTabList()).postAndCatch()
        }

        val tabListOverlay = Minecraft.getMinecraft().ingameGUI.tabList as AccessorGuiPlayerTabOverlay
        header = tabListOverlay.header_skyhanni?.formattedText ?: ""

        val tabFooter = tabListOverlay.footer_skyhanni?.formattedText ?: ""
        if (tabFooter != footer && tabFooter != "") {
            TablistFooterUpdateEvent(tabFooter).postAndCatch()
        }
        footer = tabFooter
    }
}

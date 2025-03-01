package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.SlayerAPI
import at.hannibal2.skyhanni.events.SlayerProgressChangeEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

class SlayerBossSpawnSoon {

    private val config get() = SkyHanniMod.feature.slayer.slayerBossWarning

    private val progressPattern by RepoPattern.pattern(
        "slayer.bosswarning.progress",
        " \\(?(?<progress>[0-9.,k]+)/(?<total>[0-9.,k]+)\\)?.*"
    )

    private var lastCompletion = 0.0
    private var warned = false

    @SubscribeEvent
    fun onSlayerProgressChange(event: SlayerProgressChangeEvent) {
        if (!isEnabled()) return
        if (!SlayerAPI.isInCorrectArea) return

        val completion = progressPattern.matchMatcher(event.newProgress.removeColor()) {
            group("progress").formatDouble() / group("total").formatDouble()
        } ?: return

        if (completion > config.percent / 100.0) {
            if (!warned || (config.repeat && completion != lastCompletion)) {
                SoundUtils.playBeepSound()
                LorenzUtils.sendTitle("§eSlayer boss soon!", 2.seconds)
                warned = true
            }
        } else {
            warned = false
        }
        lastCompletion = completion
    }

    fun isEnabled() = config.enabled && SlayerAPI.hasActiveSlayerQuest()
}

package com.coomix.app.all.manager

import com.coomix.app.all.AllOnlineApp
import com.coomix.app.all.model.bean.DevPowerMode
import com.coomix.app.all.model.response.Cmd
import com.coomix.app.all.model.response.SendCmd
import com.coomix.app.all.model.response.TypeCmd
import com.coomix.app.all.util.FileUtil
import kotlin.concurrent.thread

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019-12-19.
 */
object CmdManager {
    const val TEMP = "TEMPTIMER"
    const val TIMER = "TIMER"
    const val PARAM = "PARAM"
    const val RELAY = "RELAY"
    val cmdsMap = hashMapOf<String, Cmd>()
    var tempTimerCmd: SendCmd? = null
    var timerCmd: SendCmd? = null
    var paramCmd: SendCmd? = null
    var lockCmd: SendCmd? = null
    var powerModeMap = hashMapOf<String, DevPowerMode>()

    init {
        thread {
            powerModeMap = FileUtil.readObject(
                FileUtil.getModePath(AllOnlineApp.mApp)
            ) as? HashMap<String, DevPowerMode> ?: hashMapOf<String, DevPowerMode>()
        }
    }

    fun saveCmds(typeCmd: TypeCmd) {
        if (typeCmd.cmd.size > 0) {
            for (c in typeCmd.cmd) {
                cmdsMap.put(c.head, c)
            }
        }
    }

    fun saveSingleCmd(cmd: Cmd) {
        cmdsMap.put(cmd.head, cmd)
    }

    fun makeCmds(type: String) {
        if (tempTimerCmd == null && cmdsMap.containsKey(TEMP) && cmdsMap.get(TEMP) != null) {
            val tempTimer = cmdsMap.get(TEMP)
            val cmds = mutableListOf<Cmd>()
            cmds.add(tempTimer!!)
            val typeCmd = TypeCmd(type, cmds)
            val typeCmdList = mutableListOf<TypeCmd>()
            typeCmdList.add(typeCmd)
            tempTimerCmd = SendCmd(typeCmdList)
        }
        if (timerCmd == null && cmdsMap.containsKey(TIMER) && cmdsMap.get(TIMER) != null) {
            val timer = cmdsMap.get(TIMER)
            val cmds = mutableListOf<Cmd>()
            cmds.add(timer!!)
            val typeCmd = TypeCmd(type, cmds)
            val typeCmdList = mutableListOf<TypeCmd>()
            typeCmdList.add(typeCmd)
            timerCmd = SendCmd(typeCmdList)
        }
        if (paramCmd == null && cmdsMap.containsKey(PARAM) && cmdsMap.get(PARAM) != null) {
            val param = cmdsMap.get(PARAM)
            val cmds = mutableListOf<Cmd>()
            cmds.add(param!!)
            val typeCmd = TypeCmd(type, cmds)
            val typeCmdList = mutableListOf<TypeCmd>()
            typeCmdList.add(typeCmd)
            paramCmd = SendCmd(typeCmdList)
        }
        if (lockCmd == null && cmdsMap.containsKey(RELAY) && cmdsMap[RELAY] != null) {
            val relay = cmdsMap.get(RELAY)
            val cmds = mutableListOf<Cmd>()
            cmds.add(relay!!)
            val typeCmd = TypeCmd(type, cmds)
            val typeCmdList = mutableListOf<TypeCmd>()
            typeCmdList.add(typeCmd)
            lockCmd = SendCmd(typeCmdList)
        }
    }

    fun saveMode(
        mode: DevPowerMode,
        disk: Boolean = false
    ) {
        powerModeMap.put(mode.imei, mode)
        if (disk) {
            thread {
                FileUtil.saveOjbect(null, powerModeMap, FileUtil.getModePath(AllOnlineApp.mApp))
            }
        }
    }

    fun getDevMode(imei: String): DevPowerMode? {
        return powerModeMap[imei]
    }
}
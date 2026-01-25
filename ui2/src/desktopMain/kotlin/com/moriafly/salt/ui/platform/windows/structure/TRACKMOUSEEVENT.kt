package com.moriafly.salt.ui.platform.windows.structure

import com.moriafly.salt.ui.UnstableSaltUiApi
import com.sun.jna.Structure
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.DWORD


/**
 * https://learn.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-trackmouseevent
 */
@UnstableSaltUiApi
@Structure.FieldOrder("cbSize", "dwFlags", "hwndTrack", "dwHoverTime")
open class TRACKMOUSEEVENT : Structure() {
    @JvmField var cbSize: DWORD = DWORD(0)
    @JvmField var dwFlags: DWORD = DWORD(0)
    @JvmField var hwndTrack: WinDef.HWND? = null
    @JvmField var dwHoverTime: DWORD = DWORD(0)

    class ByReference : TRACKMOUSEEVENT(), Structure.ByReference

    companion object {
        /** The caller wants to cancel a prior tracking request. */
        const val TME_CANCEL = 0x80000000.toInt()
        /** The caller wants hover notification. */
        const val TME_HOVER = 0x00000001
        /** The caller wants leave notification. */
        const val TME_LEAVE = 0x00000002
        /** The caller wants leave notification for the nonclient areas. */
        const val TME_NONCLIENT = 0x00000010
    }
}
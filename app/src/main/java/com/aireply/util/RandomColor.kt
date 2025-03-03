package com.aireply.util

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

object RandomColor {
    fun randomColor(): Color{
        val colorList = mapOf(1 to Color(0xFFcd6155), 2 to Color(0xFF85c1e9), 3 to Color(0xFF52be80), 4 to Color(0xFFf39c12), 5 to Color(0xFFa569bd))
        val randomNumber = Random.nextInt(0, 5)

        return colorList[randomNumber]?: Color(0xFFb2babb)
    }
}
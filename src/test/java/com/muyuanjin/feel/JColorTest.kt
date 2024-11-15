package com.muyuanjin.feel

import com.diogonunes.jcolor.Ansi
import com.diogonunes.jcolor.AnsiFormat
import com.diogonunes.jcolor.Attribute
import com.muyuanjin.common.util.ColorUtil
import org.junit.jupiter.api.Test

/**
 * @author muyuanjin
 */
class JColorTest {
    @Test
    fun test_2024_11_14_19_13_12() {
        // Use Case 1: use Ansi.colorize() to format inline
        println(Ansi.colorize("This text will be yellow on magenta", Attribute.YELLOW_TEXT(), Attribute.FRAMED()))
        println("\n")

        // Use Case 2: compose Attributes to create your desired format
        val myFormat = arrayOf(Attribute.RED_TEXT(), Attribute.YELLOW_BACK(), Attribute.BOLD())
        println(Ansi.colorize("This text will be red on yellow", *myFormat))
        println("\n")

        // Use Case 3: AnsiFormat is syntactic sugar for an array of Attributes
        val fWarning = AnsiFormat(Attribute.GREEN_TEXT(), Attribute.BLUE_BACK(), Attribute.BOLD())
        println(Ansi.colorize("AnsiFormat is just a pretty way to declare formats", fWarning))
        println(fWarning.format("...and use those formats without calling colorize() directly"))
        println("\n")

        // Use Case 4: you can define your formats and use them throughout your code
        val fInfo = AnsiFormat(Attribute.CYAN_TEXT())
        val fError = AnsiFormat(Attribute.YELLOW_TEXT(), Attribute.RED_BACK())
        println(fInfo.format("This info message will be cyan"))
        println("This normal message will not be formatted")
        println(fError.format("This error message will be yellow on red"))
        println("\n")

        // Use Case 5: we support bright colors
        val fNormal = AnsiFormat(Attribute.MAGENTA_BACK(), Attribute.YELLOW_TEXT())
        val fBright = AnsiFormat(Attribute.BRIGHT_MAGENTA_BACK(), Attribute.BRIGHT_YELLOW_TEXT())
        println(fNormal.format("You can use normal colors ") + fBright.format(" and bright colors too"))

        // Use Case 6: we support 8-bit colors
        println("Any 8-bit color (0-255), as long as your terminal supports it:")
        for (i in 0..255) {
            val txtColor = Attribute.TEXT_COLOR(i)
            Ansi.generateCode()
            print(Ansi.colorize(String.format("%4d", i), txtColor))
        }
        println("\n")

        // Use Case 7: we support true colors (RGB)
        println("Any TrueColor (RGB), as long as your terminal supports it:")
        for (i in 0..300) {
            val bkgColor = Attribute.BACK_COLOR(
                (Math.random() * 255).toInt(),
                (Math.random() * 255).toInt(),
                (Math.random() * 255).toInt()
            )
            print(Ansi.colorize("   ", bkgColor))
        }
        println("\n")

        // Credits
        print("This example used JColor 5.0.0   ")
        print(Ansi.colorize("\tMADE ", Attribute.BOLD(), Attribute.BRIGHT_YELLOW_TEXT(), Attribute.GREEN_BACK()))
        println(Ansi.colorize("IN PORTUGAL\t", Attribute.BOLD(), Attribute.BRIGHT_YELLOW_TEXT(), Attribute.RED_BACK()))
        println("I hope you find it useful ;)")
    }


    @Test
    fun test_2024_11_14_19_00_06() {
        println("\u001b[1;97m" + "Hello World" + ColorUtil.RESET)
        println("\u001b[2;97m" + "Hello World" + ColorUtil.RESET)
        println("\u001b[3;97m" + "Hello World" + ColorUtil.RESET)
        println("\u001b[1;3;4;97m" + "Hello World" + ColorUtil.RESET)
        println("\u001b[1;3;4m" + "Hello World" + ColorUtil.RESET)
        println("\u001b[4;0m" + "Hello World" + ColorUtil.RESET)
        println("\u001b[31;1;4mHello\u001b[0m")
    }
}

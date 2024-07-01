package io.vexel.kobold.test.lexer

import io.vexel.kobold.Token
import io.vexel.kobold.lexer.lexer
import io.vexel.kobold.test.tokens.Number
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class `Lexer should` {
    @Test
    fun `match simple token definitions` () {
        val lexer = lexer {
            val alpha = ('a' to 'z') or ('A' to 'Z')
            val numeric = '0' to '9'

            alpha then (alpha or numeric).zeroOrMore() with { Label(it) }
            numeric.oneOrMore() with { Number(it) }
            ignore(" " or "!" or "," or ",")

        }

        val tokens = lexer.tokenize("H3llo Kobold Lexer! 9999")
        val expected = listOf(
            Label("H3llo"),
            Label("Kobold"),
            Label("Lexer"), Number("9999")
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `properly process line and column numbers` () {
        val lexer = lexer {
            "@".oneOrMore() with { Ats(it) }
            ignore(newline or spaces)
        }

        val text = """
            @@@@    @
            @@    @ @
        """.trimIndent()

        val tokens = lexer.tokenize(text)
        val expected = listOf(
            Ats("@@@@", "@@@@    @", 1, 1, 0, 4),
            Ats("@", "@@@@    @", 1, 9, 8, 9),
            Ats("@@", "@@    @ @", 2, 1, 10, 12),
            Ats("@", "@@    @ @", 2, 7, 16, 17),
            Ats("@", "@@    @ @", 2, 9, 18, 19)
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `match always the longest sequence`() {
        val lexer = lexer {
            "@" with { Ats(it) }
            "@@" with { Ats(it) }
        }

        val tokens = lexer.tokenize("@@")
        val expected = listOf(Ats("@@",  "@@", 1, 1, 0, 2))
        assertEquals(expected, tokens)
    }

    @Test
    fun `match the first rule of two with the same length`() {
        val lexer = lexer {
            "@" with { Ats(it) }
            ("@" or "#") with { Token(it) }
        }

        val tokens = lexer.tokenize("@")
        val expected = listOf(Ats("@",  "@", 1, 1, 0, 1))
        assertEquals(expected, tokens)
    }

    @Test
    fun `match newlines from both conventions`() {
        val text = "@@@\r\n@@@\n@@@".trimIndent()

        val lexer = lexer {
            "@" with { Ats(it) }
            ignore(newline)
        }

        val tokens = lexer.tokenize(text)

        assert(tokens.all { it.text == "@" })
        assertEquals(9, tokens.count())
    }

    @Test
    fun `match spaces and tabs`() {
        val text = "@\t   \t@ @\t@".trimIndent()

        val lexer = lexer {
            "@" with { Ats(it) }
            ignore(spaces)
        }

        val tokens = lexer.tokenize(text)

        assert(tokens.all { it.text == "@" })
        assertEquals(4, tokens.count())
    }

    class Ats(
        text: String,
        lineText: String = "",
        line: Int = 1,
        column: Int = 1,
        start: Int = 0,
        end: Int = 0
    ) : Token(text) {
        init {
            this.addMetadata(lineText, line, column, start, end)
        }

        override fun toString(): String {
            return "Ats(text=$text, line=$line, column=$column, start=$start, end=$end)"
        }

        override fun equals(other: Any?) =
            super.equals(other) && other is Ats &&
                lineText == other.lineText &&
                line == other.line &&
                column == other.column &&
                start == other.start &&
                end == other.end

        override fun hashCode(): Int =
            super.hashCode() + Objects.hash(line, column)
    }
}

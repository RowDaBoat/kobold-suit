package kobold.parser.dsl

import kobold.Accepted
import kobold.matchers.Token
import kobold.parser.dsl.support.*
import kobold.parser.parser
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class NonTerminalShould {
    @Test
    fun acceptSequenceMatchingRightRecursiveGrammar() {
        val sequence = parser {
            val expression = nonTerminal()
            expression from (("a" then expression) or empty)
        }

        assertIs<Accepted>(sequence.parse(tokens("aaaaaaaaaaaaa")))
    }

    @Test
    fun acceptSequenceMatchingLeftRecursiveGrammar() {
        val sequence = parser {
            val expression = nonTerminal()
            expression from ((expression then "a") or empty)
            expression
        }
        assertIs<Accepted>(sequence.parse(tokens("aaaaaaaaaaaaa")))
    }

    @Test
    fun produceASimpleExpressionTreeFromANonTerminal() {
        val grammar = parser {
            val expression = nonTerminal { Expression(it) }
            expression from (("a" then expression) or empty)
        }

        val result = grammar.parse(tokens("aa"))
        assertIs<Accepted>(result)
        assertEquals(Expression(Expression()), (result as Accepted).tree)
    }


    @Test
    fun produceAMoreComplexExpressionTree() {
        val grammar = parser {
            val open = Token("(")
            val close = Token(")")
            val expression = nonTerminal()
            val enclosure = nonTerminal { Enclosure(it) }
            val enclosed = nonTerminal()

            val label = nonTerminal { Label(it) }

            val character = (anyOf(tokenMatchers("abcdefghijklmnopqrstuvwxyz")))

            expression from (enclosure or label)
            enclosure from (open then enclosed then close)
            enclosed from (enclosure.oneOrMore() or label or empty)
            label from (character.oneOrMore())

            expression
        }

        val result = grammar.parse(tokens("((a)(b))"))
        assertIs<Accepted>(result)

        val tree = (result as Accepted).tree
        assert(tree == Enclosure(Enclosure(Label("a")), Enclosure(Label("b"))))
    }
}


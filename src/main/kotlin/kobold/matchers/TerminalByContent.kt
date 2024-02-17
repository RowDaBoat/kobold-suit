package kobold.matchers

import kobold.Accepted
import kobold.Rejected
import kobold.Result
import kobold.lexer.dsl.NothingToken

class TerminalByContent(private val toMatch: Token) : Matcher {
    override fun match(tokens: List<Token>, rest: Tokens, evaluate: Evaluator): Result {
        val position = tokens.count() - rest.count()

        return when (rest.any() && tokens[position] == toMatch) {
            true -> Accepted(rest.take(1), rest.drop(1), tokens[position])
            false -> Rejected(rest.firstOrNothing(), rest)
        }
    }
}

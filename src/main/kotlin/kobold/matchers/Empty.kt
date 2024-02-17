package kobold.matchers

import kobold.Accepted

class Empty : Matcher {
    override fun match(tokens: List<Token>, rest: Tokens, evaluate: Evaluator) =
        Accepted(emptySequence(), rest)
}

@file:JvmName("CoreMatchers")

package com.natpryce.hamkrest

/**
 * A [Matcher] that matches anything, always returning [MatchResult.Match].
 */
@JvmField
val anything = object : Matcher<Any> {
    override fun invoke(actual: Any): MatchResult = MatchResult.Match
    override val description: String get() = "anything"
    override val negatedDescription: String get() = "nothing"
}

/**
 * A [Matcher] that matches nothing, always returning a [MatchResult.Mismatch].
 */
@JvmField
val nothing = !anything


/**
 * Returns a matcher that reports if a value is equal to an [expected] value.
 */
fun <T> equalTo(expected: T): Matcher<T> =
        object : Matcher<T> {
            override fun invoke(actual: T): MatchResult = match(actual == expected) { "was ${describe(actual)}" }
            override val description : String get() = "is equal to ${describe(expected)}"
            override val negatedDescription : String get() = "is not equal to ${describe(expected)}"
        }

/**
 * Returns a matcher that reports if a value is the same instance as [expected] value.
 */
fun <T> sameInstance(expected: T): Matcher<T> =
        object : Matcher<T> {
            override fun invoke(actual: T): MatchResult = match(actual === expected) { "was ${describe(actual)}" }
            override val description: String get() = "is same instance as ${describe(expected)}"
            override val negatedDescription: String get() = "is not same instance as ${describe(expected)}"
        }


/**
 * Returns a matcher that reports if a value is null.
 */
fun <T> absent(): Matcher<T?> = object : Matcher<T?> {
    override fun invoke(actual: T?): MatchResult = match(actual == null) { "was ${describe(actual)}" }
    override val description: String get() = "null"
}

/**
 * Returns a matcher that reports if a value is not null and meets the criteria of the [valueMatcher]
 */
fun <T> present(valueMatcher: Matcher<T>): Matcher<T?> = object : Matcher<T?> {
            override fun invoke(actual: T?): MatchResult {
                return when (actual) {
                    null -> MatchResult.Mismatch("was null")
                    else -> valueMatcher(actual)
                }
            }

            override val description: String get() {
                return "is not null & " + valueMatcher.description
            }
        }

/**
 * Returns a matcher that reports if a value of [Any] type is of a type compatible with [downcastMatcher] and, if so,
 * if the value meets its criteria.
 */
inline fun <reified T : Any> cast(downcastMatcher: Matcher<T>): Matcher<Any> {
    return object : Matcher<Any> {
        override fun invoke(actual: Any): MatchResult {
            return when (actual) {
                is T -> {
                    downcastMatcher(actual)
                }
                else -> {
                    MatchResult.Mismatch("had type ${actual.javaClass.kotlin.qualifiedName}")
                }
            }
        }

        override val description: String get() {
            return "has type " + T::class.qualifiedName + " & " + downcastMatcher.description
        }
    }
}

/**
 * Returns a matcher that reports if a [Comparable] value is greater than [n]
 */
fun <N : Comparable<N>> greaterThan(n: N) = _comparesAs("greater than", n) { it > 0 }

/**
 * Returns a matcher that reports if a [Comparable] value is greater than or equal to [n]
 */
fun <N : Comparable<N>> greaterThanOrEqualTo(n: N) = _comparesAs("greater than or equal to", n) { it >= 0 }

/**
 * Returns a matcher that reports if a [Comparable] value is less than [n]
 */
fun <N : Comparable<N>> lessThan(n: N) = _comparesAs("less than", n) { it < 0 }

/**
 * Returns a matcher that reports if a [Comparable] value is less than or equal to [n]
 */
fun <N : Comparable<N>> lessThanOrEqualTo(n: N) = _comparesAs("less than or equal to", n) { it <= 0 }

private fun <N : Comparable<N>> _comparesAs(description: String, n: N, expectedSignum: (Int) -> Boolean): Matcher<N> {
    return object : Matcher<N> {
        override fun invoke(actual: N): MatchResult =
                match(expectedSignum(actual.compareTo(n))) { "was ${describe(actual)}" }

        override val description: String get() {
            return "is ${description} ${describe(n)}"
        }
    }
}

/**
 * Returns a matcher that reports if a [kotlin.Comparable] value falls within the given [range].
 *
 * @param range The range that contains matching values.
 */
fun <T : Comparable<T>> isWithin(range: ClosedRange<T>): Matcher<T> {
    fun _isWithin(actual: T, range: ClosedRange<T>): Boolean {
        return range.contains(actual)
    }

    return Matcher.Companion(::_isWithin, range)
}


/**
 * Returns a matcher that reports if a block throws an exception of type [T] and, if [exceptionCriteria] is given,
 * the exception matches the [exceptionCriteria].
 */
inline fun <reified T : Throwable> throws(exceptionCriteria: Matcher<T>? = null): Matcher<() -> Unit> {
    val exceptionName = T::class.qualifiedName

    return object : Matcher<() -> Unit> {
        override fun invoke(actual: () -> Unit): MatchResult =
                try {
                    actual()
                    MatchResult.Mismatch("did not throw")
                } catch (e: Throwable) {
                    if (e is T) {
                        exceptionCriteria?.invoke(e) ?: MatchResult.Match
                    }
                    else {
                        MatchResult.Mismatch("threw ${e.javaClass.kotlin.qualifiedName}")
                    }
                }

        override val description: String get() = "throws ${exceptionName}${exceptionCriteria?.let{" that ${describe(it)}"}?:""}"
        override val negatedDescription: String get() = "does not throw ${exceptionName}${exceptionCriteria?.let{" that ${describe(it)}"}?:""}"
    }
}


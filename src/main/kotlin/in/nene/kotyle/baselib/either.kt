/*
Copyright 2016 Dhananjay Nene

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/*
**  Thanks in no small measure due to
**  http://blog.tmorris.net/posts/scalaoption-cheat-sheet/
**  https://github.com/MarioAriasC/funKTionale/blob/master/src/main/kotlin/org/funktionale/option/Option.kt
**  http://blog.originate.com/blog/2014/06/15/idiomatic-scala-your-options-do-not-match/
 */

package `in`.nene.kotyle.baselib.either

import `in`.nene.kotyle.baselib.helpers.EmptyIterator
import `in`.nene.kotyle.baselib.helpers.SingleItemIterator
import `in`.nene.kotyle.baselib.option.None
import `in`.nene.kotyle.baselib.option.Option



sealed class Either<out L, out R> protected constructor () {
    companion object {
        fun <L, R> toRight(value: R) = Right<L, R>(value)
        fun <L, R> toLeft(value: L) = Left<L, R>(value)
    }

    abstract fun left(): LeftProjection<L,R>
    abstract fun right(): RightProjection<L,R>

    abstract val isRight: Boolean
    abstract val isLeft: Boolean

    abstract fun asLeft(): Option<L>
    abstract fun asRight(): Option<R>

    operator fun component1(): Option<L> = asLeft()
    operator fun component2(): Option<R> = asRight()

    abstract fun<T> fold(fnLeft: (L) -> T, fnRight: (R) ->T): T

    abstract fun swap(): Either<R,L>

    class Right<L, R>(val value: R) : Either<L, R>() {
        override val isRight: Boolean = true
        override val isLeft: Boolean = false
        override fun asLeft(): Option<L> = None
        override fun asRight(): Option<R> = Option.Some(value)
        override fun toString(): String = "Right($value)"
        override fun equals(other: Any?): Boolean =
                this === other || (other != null && other is Right<*,*> && this.hashCode() == other.hashCode()
                        && this.value == (other as? Right<*,*>)!!.value)
        override fun hashCode(): Int = value!!.hashCode()
        override fun<T> fold(fnLeft: (L) -> T, fnRight: (R) ->T): T = fnRight(value)
        override fun left(): LeftProjection<L,R> = LeftProjection.LeftProjectionOfRight(value)
        override fun right(): RightProjection<L,R> = RightProjection.RightProjectionOfRight(value)
        override fun swap(): Either<R,L> = Left<R,L>(value)
    }

    class Left<L, R>(val value: L) : Either<L, R>() {
        override val isRight: Boolean = false
        override val isLeft: Boolean = true
        override fun asLeft(): Option<L> = Option.Some(value)
        override fun asRight(): Option<R> = None
        override fun toString(): String = "Left($value)"
        override fun equals(other: Any?): Boolean =
                this === other || (other != null && other is Left<*,*> && this.hashCode() == other.hashCode()
                        && this.value == (other as? Left<*,*>)!!.value)
        override fun hashCode(): Int = value!!.hashCode()
        override fun<T> fold(fnLeft: (L) -> T, fnRight: (R) ->T): T = fnLeft(value)
        override fun left(): LeftProjection<L,R> = LeftProjection.LeftProjectionOfLeft(value)
        override fun right(): RightProjection<L,R> = RightProjection.RightProjectionOfLeft(value)
        override fun swap(): Either<R,L> = Right<R,L>(value)
    }

    interface Projection<out T>: Collection<T> {
        fun exists(pred: (T) -> Boolean): Boolean
    }
    sealed abstract class LeftProjection<out L, out R>: Projection<L> {
        fun<T> map(f: (L) -> T): Either<T,R> = flatMap {it: L -> Left<T,R>(f(it))}
        abstract fun filter(pred: (L) -> Boolean): Option<Either<L,R>>
        abstract fun toOption(): Option<L>

        class LeftProjectionOfLeft<L, R>(val value: L): LeftProjection<L,R>() {
            override val size: Int = 1
            override fun contains(element: L): Boolean = (value == element)
            override fun containsAll(elements: Collection<L>): Boolean = (elements.all { it == value })
            override fun isEmpty(): Boolean = false
            override fun iterator(): Iterator<L> = SingleItemIterator(value)
            override fun exists(pred: (L) -> Boolean): Boolean = pred(value)
            override fun filter(pred: (L) -> Boolean): Option<Either<L,R>> = if (pred(value)) Option.Some(Left<L,R>(value)) else None
            override fun toOption(): Option<L> = Option.Some(value)
        }
        class LeftProjectionOfRight<L, R>(val value: R): LeftProjection<L,R>() {
            override val size: Int = 0
            override fun contains(element: L): Boolean = false
            override fun containsAll(elements: Collection<L>): Boolean = elements.isEmpty()
            override fun isEmpty(): Boolean = true
            override fun iterator(): Iterator<L> = EmptyIterator
            override fun exists(pred: (L) -> Boolean): Boolean = false
            override fun filter(pred: (L) -> Boolean): Option<Either<L,R>> = None
            override fun toOption(): Option<L> = None
        }
    }
    sealed abstract class RightProjection<out L, out R>: Projection<R> {
        fun<T> map(f: (R) -> T): Either<L, T> = flatMap { it: R -> Right<L, T>(f(it)) }
        abstract fun filter(pred: (R) -> Boolean): Option<Either<L,R>>
        abstract fun toOption(): Option<R>


        class RightProjectionOfLeft<L, R>(val value: L) : RightProjection<L, R>() {
            override val size: Int = 0
            override fun contains(element: R): Boolean = false
            override fun containsAll(elements: Collection<R>): Boolean = elements.isEmpty()
            override fun isEmpty(): Boolean = true
            override fun iterator(): Iterator<R> = EmptyIterator
            override fun exists(pred: (R) -> Boolean): Boolean = false
            override fun filter(pred: (R) -> Boolean): Option<Either<L,R>> = None
            override fun toOption(): Option<R> = None
        }

        class RightProjectionOfRight<L, R>(val value: R) : RightProjection<L, R>() {
            override val size: Int = 1
            override fun contains(element: R): Boolean = (value == element)
            override fun containsAll(elements: Collection<R>): Boolean = (elements.all { it == value })
            override fun isEmpty(): Boolean = false
            override fun iterator(): Iterator<R> = SingleItemIterator(value)
            override fun exists(pred: (R) -> Boolean): Boolean = pred(value)
            override fun filter(pred: (R) -> Boolean): Option<Either<L,R>> = if (pred(value)) Option.Some(Right<L,R>(value)) else None
            override fun toOption(): Option<R> = Option.Some(value)
        }
    }
}

fun<T> doTry(fn: () -> T): Either<Exception,T> = try { Either.Right(fn()) } catch (e: Exception) { Either.Left(e) }

fun<L,R,T> Either.LeftProjection<L, R>.flatMap(f: (L) -> Either<T,R>): Either<T,R> {
    return when (this) {
        is Either.LeftProjection.LeftProjectionOfLeft -> f(this.value)
        is Either.LeftProjection.LeftProjectionOfRight -> Either.Right(this.value)
    }
}

fun<L,R,T> Either.RightProjection<L, R>.flatMap(f: (R) -> Either<L,T>): Either<L,T> {
    return when (this) {
        is Either.RightProjection.RightProjectionOfLeft -> Either.Left(this.value)
        is Either.RightProjection.RightProjectionOfRight -> f(this.value)
    }
}

fun<L, R, P, Q> Either.LeftProjection<L,R>.map(p: Either<P,R>, f:(L,P) -> Q): Either<Q,R> =
    flatMap { l -> p.left().map { pp -> f(l, pp)}}

fun<L, R, P, Q> Either.RightProjection<L,R>.map(p: Either<L, P>, f:(P,R) -> Q): Either<L,Q> =
    flatMap { r -> p.right().map { pp -> f(pp, r)}}

fun<T> Either<T,T>.merge(): T =
    when(this) {
        is Either.Left<T, T> -> this.value
        is Either.Right<T, T> -> this.value
    }

fun<L,R> Either.LeftProjection.LeftProjectionOfLeft   <L,R>.getOrElse(default: () -> L): L = value
fun<L,R> Either.LeftProjection.LeftProjectionOfRight  <L,R>.getOrElse(default: () -> L): L = default()
fun<L,R> Either.RightProjection.RightProjectionOfLeft <L,R>.getOrElse(default: () -> R): R = default()
fun<L,R> Either.RightProjection.RightProjectionOfRight<L,R>.getOrElse(default: () -> R): R = value

fun<L,R> Pair<L,R>.toLeft(): Either.Left<L, R> = Either.Left(this.first)
fun<L,R> Pair<L,R>.toRight(): Either.Right<L, R> = Either.Right(this.second)
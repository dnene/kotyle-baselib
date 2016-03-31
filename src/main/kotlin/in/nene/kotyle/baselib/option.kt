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

package `in`.nene.kotyle.baselib.option

import `in`.nene.kotyle.baselib.helpers.EmptyIterator
import `in`.nene.kotyle.baselib.helpers.SingleItemIterator

sealed class Option<out T>: Collection<T> {
    companion object {
        operator fun<T> invoke(t: T): Option<T> = if (t == null) None else Some(t)
        fun <T> doTry(f: () -> T): Option<T> {
            return try {
                Some(f())
            } catch (e: Exception) {
                None
            }
        }
    }
    abstract fun isDefined(): Boolean
    abstract fun orNull(): T?
    override fun isEmpty(): Boolean = ! isDefined()
    abstract fun filter(p: (T) -> Boolean): Option<T>
    abstract fun filterNot(p: (T) -> Boolean): Option<T>
    abstract fun exists(p: (T) -> Boolean): Boolean
    abstract fun <R> map(f: (T) -> R): Option<R>
    abstract fun <R> flatMap(f: (T) -> Option<R>): Option<R>
    abstract fun <R> fold(initial: () -> R, operation: (T) -> R): R
    abstract fun <R> fold(initial: R, operation: (T) -> R): R
    abstract fun<P,R> map(p: Option<P>, f: (T, P) -> R): Option<R>
    abstract fun<P,Q,R> map(p: Option<P>, q: Option<Q>, f: (T, P, Q) -> R): Option<R>

    class _None<T>: Option<T>() {
        override val size = 0

        override fun toString(): String = "None"
        override fun equals(other: Any?) = (other is _None<*>)
        override fun contains(element: T): Boolean = false
        override fun containsAll(elements: Collection<T>): Boolean = elements.isEmpty()

        override fun iterator(): Iterator<Nothing> = EmptyIterator
        override fun isDefined() = false
        override fun orNull(): T? = null
        override fun filter(p: (T) -> Boolean): Option<T> = None
        override fun filterNot(p: (T) -> Boolean): Option<T> = None
        override fun exists(p: (T) -> Boolean): Boolean = false
        override fun <R> map(f: (T) -> R): Option<R> = None
        override fun <R> flatMap(f: (T) -> Option<R>): Option<R> = None
        override fun <R> fold(initial: () -> R, operation: (T) -> R): R = initial()
        override fun <R> fold(initial: R, operation: (T) -> R): R = initial
        override fun<P,R> map(p: Option<P>, f: (T, P) -> R): Option<R> = None
        override fun<P,Q,R> map(p: Option<P>, q: Option<Q>, f: (T, P, Q) -> R): Option<R> = None

    }
    class Some<T>(val t: T) : Option<T>() {
        init {
            if (t == null) throw IllegalArgumentException("null value passed to constructor of Some")
        }

        override fun toString(): String = "Some(${t})"
        override val size = 1
        override fun equals(other: Any?) = (other is Some<*> && t!!.equals(other.t))
        override fun contains(element: T): Boolean = t == element
        override fun containsAll(elements: Collection<T>): Boolean = !elements.any { it != t }
        override fun iterator(): Iterator<T> = SingleItemIterator(t)
        override fun isDefined() = true
        override fun orNull(): T? = t
        override fun filter(p: (T) -> Boolean): Option<T> = if (p(t)) this else None
        override fun filterNot(p: (T) -> Boolean): Option<T> = if (p(t)) None else this
        override fun exists(p: (T) -> Boolean): Boolean = p(t)
        override fun <R> map(f: (T) -> R): Option<R> = Some(f(t))
        override fun <R> flatMap(f: (T) -> Option<R>): Option<R> {
            val result = f(t)
            return when(result) {
                is Some -> Some(result.t)
                is _None -> None
            }
        }
        override fun <R> fold(initial: () -> R, operation: (T) -> R): R = operation(t)
        override fun <R> fold(initial: R, operation: (T) -> R): R = operation(t)
        override fun<P,R> map(p: Option<P>, f: (T, P) -> R): Option<R> = flatMap { t -> p.map { pp -> f(t,pp)} }
        override fun<P,Q,R> map(p: Option<P>, q: Option<Q>, f: (T, P, Q) -> R): Option<R> =
                flatMap { t -> p.flatMap { pp -> q.map { qq -> f(t,pp,qq)}}}
    }
}
val None = Option._None<Nothing>()

fun<T> Option<T>.getOrElse(t: T): T = if (isDefined()) (this as Option.Some<T>).t else t
fun<T> Option<T>.getOrElse(f: () -> T): T = if (isDefined()) (this as Option.Some<T>).t else f()

fun<T> Option<T>.orElse(t: T): Option<T> = if (isDefined()) this else Option.Some(t)
fun<T> Option<T>.orElse(f: () -> T): Option<T> = if (isDefined()) this else  Option.Some(f())

fun<T> T?.toOption(): Option<T> = if (this != null) Option.Some(this) else None

interface Getter<K, V> {
    val getter: (K) -> V
    operator fun get(key: K): V = getter(key)
}
class GetterImpl<K, V>(override val getter: (K) -> V) : Getter<K, V>


val<K,V> Map<K,V>.optional: Getter<K, Option<V>>
    get () = GetterImpl { k -> this[k].toOption()}

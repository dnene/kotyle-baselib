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

package `in`.nene.kotyle.baselib.option

import `in`.nene.kotyle.baselib.option.Option.Some
import org.junit.Assert.*
import org.junit.Test

class OptionsTest {
    @Test
    fun construction() {
        assertEquals(Some(5), Option(5))
        assertEquals(None,    Option(null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun noNullConstructionOfSome() {
        Some(null)
    }

    @Test
    fun equality() {
        assertEquals("Nones should be equal", None, None)
        assertEquals("Two somes should be equal", Some(5), Some(5))
        assertNotEquals("None is not equal to some", None, Some(5))
        assertNotEquals("Some is not equal to none", Some(5), None)
        assertNotEquals("Somes with different values are unequal", Some(5), Some(6))
        assertNotEquals("Somes with same value different types are unequal", Some(5), Some(5.toLong()))

    }

    @Test
    fun sizes() {
        assertEquals("None should always have size 0", 0, None.size)
        assertEquals("Some should always have size 1", 1, Some(5).size)
    }

    @Test
    fun contains() {
        assertFalse("None should always return contains as false", None.contains("foo"))
        assertFalse("Some should check for inequality of elements", Some(5).contains(10))
        assertTrue("Some should check for equality of elements", Some(5).contains(5))
    }

    @Test
    fun containsAll() {
        assertTrue("None should always return empty contains all as true", None.containsAll(listOf<String>()))
        assertFalse("None should always return non empty contains all as false", None.containsAll(listOf<String>("foo")))
        assertTrue("Some should always return empty contains all as true", Some(5).containsAll(listOf<Int>()))
        assertTrue("Some should always check each element of contains all", Some(5).containsAll(listOf<Int>(5,5)))
        assertFalse("Some contains all should check for inequality of elements", Some(5).containsAll(listOf<Int>(5,10)))
    }

    @Test
    fun isEmpty() {
        assertTrue("None is always empty", None.isEmpty())
        assertFalse("Some is never empty", Some(5).isEmpty())
    }

    @Test
    fun isDefined() {
        assertFalse("None is not defined", None.isDefined())
        assertTrue("Some is defined", Some(5).isDefined())
    }

    @Test
    fun map() {
        val bar = Some(5).map { (it * 2).toString() }
        assertEquals("None mapped should return None",
                None, None.map { it: Int -> (it * 2).toString() })
        assertTrue("Some mapped should perform the map and return a Some",
                bar is Some && bar.t == "10")
    }

    @Test
    fun multiParamMap() {
        assertEquals(Some("Hello5"), Some("Hello").map(Some(5)){ i: String, j: Int -> i + j.toString() })
        assertEquals(None,           Some("Hello").map(None)   { i: String, j: Int -> i + j.toString() })
        assertEquals(None,           None         .map(Some(5)){ i: String, j: Int -> i + j.toString() })
        assertEquals(None,           None         .map(None)   { i: String, j: Int -> i + j.toString() })

        assertEquals(Some("Hello5true"), Some("Hello").map(Some(5), Some(true)){ i: String, j: Int, k: Boolean -> i + j.toString() + k.toString() })
        assertEquals(None,               Some("Hello").map(None,    Some(true)){ i: String, j: Int, k: Boolean -> i + j.toString() + k.toString() })
        assertEquals(None,               None         .map(Some(5), Some(true)){ i: String, j: Int, k: Boolean -> i + j.toString() + k.toString() })
        assertEquals(None,               None         .map(None,    Some(true)){ i: String, j: Int, k: Boolean -> i + j.toString() + k.toString() })
        assertEquals(None,               Some("Hello").map(Some(5), None)      { i: String, j: Int, k: Boolean -> i + j.toString() + k.toString() })
        assertEquals(None,               Some("Hello").map(None,    None)      { i: String, j: Int, k: Boolean -> i + j.toString() + k.toString() })
        assertEquals(None,               None         .map(Some(5), None)      { i: String, j: Int, k: Boolean -> i + j.toString() + k.toString() })
        assertEquals(None,               None         .map(None,    None)      { i: String, j: Int, k: Boolean -> i + j.toString() + k.toString() })

    }
    @Test
    fun getOrElse() {
        assertEquals("None getOrElse should return function result", 5, None.getOrElse { 5 })
        assertEquals("None getOrElse should return default value", 5, None.getOrElse(5))
        assertEquals("Some getOrElse should ignore function result", 5, Some(5).getOrElse { 10 })
        assertEquals("Some getOrElse should ignore default value", 5, Some(5).getOrElse(10))
    }

    @Test
    fun orElse() {
        assertEquals("None orElse should return function result", Some(5), None.orElse { 5 })
        assertEquals("None orElse should return default value", Some(5), None.orElse(5))
        assertEquals("Some orElse should return function result", Some(5), Some(5).orElse { 10 })
        assertEquals("Some orElse should return default value", Some(5), Some(5).orElse(10))
    }

    @Test
    fun orNull() {
        assertTrue("None orNull should return a null", null == None.orNull())
        assertEquals("Some orNull should return its intrinsic value", 5, Some(5).orNull())
    }

    @Test
    fun flatMap() {
        val foo = Some(5).flatMap { Some((it * 2).toString()) }
        assertEquals("None flatMapped with a function returning None should return None",
                None, None.flatMap { it: Int -> None })
        assertEquals("None flatMapped with a function returning Some should return None",
                None, None.flatMap { it: Int -> Some((it * 2).toString()) })
        assertEquals("Some flatMapped with a None result should return a None",
                None, Some(5).flatMap { None })
        assertTrue("Some flatMapped with a Some result should return a Some",
                foo is Some && foo.t == "10")
    }

    @Test
    fun fold() {
        assertEquals("A fold over None always returns initial value", 5, None.fold(5){ it })
        assertEquals("A fold over None always returns initial value", 5, None.fold({5},{it}))
        assertEquals("A fold over Some always returns the computation", 15, Some(5).fold(10){it * 3})
        assertEquals("A fold over Some always returns the computation", 15, Some(5).fold({10},{it * 3}))
    }

    @Test
    fun filter() {
        val isEven: (Int) -> Boolean = {i: Int -> i%2 == 0}
        assertEquals("A filter    over a None always returns a None",        None,     None.filter(isEven))
        assertEquals("A filterNot over a None always returns a None",        None,     None.filterNot(isEven))
        assertEquals("A filter    over a Some returns a None on a No Match", None,     Some(9).filter(isEven))
        assertEquals("A filterNot over a Some returns a None on a Match",    None,     Some(10).filterNot(isEven))
        assertEquals("A filter    over a Some returns self on a Match",      Some(10), Some(10).filter(isEven))
        assertEquals("A filterNot over a Some returns self on a No Match",   Some(9),  Some(9).filterNot(isEven))
    }

    @Test
    fun toOption() {
        assertEquals("Null should result in a None",     None,    null.toOption())
        assertEquals("Non null should result in a Some", Some(5), 5.toOption())
    }

    @Test
    fun tryAsAnOption() {
        assertEquals(Some(2), Option.doTry { 4 / 2 })
        assertEquals(None, Option.doTry { 4 / 0 })
    }

    @Test
    fun getFromMap() {
        val map = mapOf(1 to "one", 2 to "two", 3 to "three")
        assertEquals(Some("one"), map.optional[1])
        assertEquals(None, map.optional[99])
    }
}

class OptionAsACollectionExamples {
    /*
    ** These methods are not directly methods on Option per se. But become automatically available by virtue of
    ** Option inheriting from Collection/Iterator. They do not thus need to be individually tested since the
    ** methods implementing the Collection/Iterator implementations have been. Hence this is but a small subset of
    ** the available functions being tested.
     */
    @Test
    fun forEach() {
        var forEachCalled = 0
        var forEachValue: Int? = null
        Some(5).forEach {
            forEachCalled += 1
            forEachValue = it
        }
        assertTrue("Some forEach should get called exactly once", forEachCalled == 1 && forEachValue == 5)

        val foo = None
        forEachCalled = 0
        forEachValue = null
        None.forEach {
            forEachCalled += 1
            forEachValue = it
        }
        assertTrue("None forEach should never get called", forEachCalled == 0 && forEachValue == null)
    }
    @Test
    fun fold() {
        assertEquals("A fold over none should return initial value", 5, None.fold(5,  {i, r -> r}))
        assertEquals("A fold over some should return computed value", 10, Some(10).fold(5,  {i, r -> r}))
    }
}

class ExampleUsages {
    /*
    ** These are just some indicative usages of Option documented as tests
     */
    @Test
    fun mapAFunctionOnASome() {
        assertEquals(Some("10"), Some(5).flatMap { it -> Some((it * 2).toString()) })
        assertEquals(None, None.flatMap { it: Int -> Some((it * 2).toString()) })
    }

    @Test
    fun forLoops() {
        // Note: for loops only express themselves through side effects
        //       Hence the mutable accumulator and the accumulate function
        var accumulator: Int = 0
        fun accumulate(n: Int) { accumulator += n}

        assertEquals(0, accumulator)
        for(i: Int in Some(5)) for(j: Int in Some(10)) { accumulate(i+j) }
        assertEquals(15, accumulator)
        for(i: Int in Some(5)) for(j: Int in None)     { accumulate(i+j) }
        assertEquals(15, accumulator)
        for(i: Int in None)    for(j: Int in Some(10)) { accumulate(i+j) }
        assertEquals(15, accumulator)
        for(i: Int in None)    for(j: Int in None)     { accumulate(i+j) }
        assertEquals(15, accumulator)
    }

    @Test
    fun forComprehensionsViaMap() {
        assertEquals(Some(15), Some(5).flatMap { i: Int -> Some(10).map {j: Int -> i + j }})
        assertEquals(None,     Some(5).flatMap { i: Int -> None    .map {j: Int -> i + j }})
        assertEquals(None,     None   .flatMap { i: Int -> Some(10).map {j: Int -> i + j }})
        assertEquals(None,     None   .flatMap { i: Int -> None    .map {j: Int -> i + j }})
    }

    @Test
    fun collectionFlattening() {
        assertEquals("Hello World!", listOf(Some("Hello"), None, Some("World!")).flatten().joinToString(" "))
        assertEquals(Some("HELLO WORLD"),Some("Hello World").map { it.toString() }.filter { it.length > 0}.map { it.toUpperCase()})
        assertEquals(None,Some("Hello World").map { it.toString() }.filter { it.length > 20}.map { it.toUpperCase()})
    }
}
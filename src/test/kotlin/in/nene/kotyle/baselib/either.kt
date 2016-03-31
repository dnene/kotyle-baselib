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

package `in`.nene.kotyle.baselib.either

import `in`.nene.kotyle.baselib.option.None
import `in`.nene.kotyle.baselib.option.Option
import org.junit.Assert.*
import org.junit.Test


data class Good(val n: Int) {}
data class GoodStr(val str: String) {}
data class Bad(val msg: String) {}

class EitherTest {
    @Test
    fun construction() {
        val good = Either.toRight<Bad, Good>(Good(5))
        assertTrue(good is Either.Right)
        assertTrue(good.isRight)
        assertFalse(good.isLeft)
        assertEquals(Option.Some(Good(5)), good.asRight())
        assertEquals(None, good.asLeft())

        val bad = Either.toLeft<Bad,Good>(Bad("boo"))
        assertTrue(bad is Either.Left)
        assertTrue(bad.isLeft)
        assertFalse(bad.isRight)
        assertEquals(Option.Some(Bad("boo")), bad.asLeft())
        assertEquals(None, bad.asRight())
    }

    @Test
    fun equality() {
        val one = Either.Right<Bad, Good>(Good(5))
        val two = Either.Right<Bad, Good>(Good(5))
        val three = Either.Right<Bad, Good>(Good(10))
        val failOne = Either.Left<Bad, Good>(Bad("boo"))
        val failTwo = Either.Left<Bad, Good>(Bad("boo"))
        val failThree = Either.Left<Bad, Good>(Bad("zoo"))

        assertNotEquals(one, null)
        assertEquals(one, one)
        assertNotEquals(one, failOne)
        assertEquals(one, two)
        assertNotEquals(one, three)

        assertNotEquals(failOne, null)
        assertEquals(failOne, failOne)
        assertNotEquals(failOne, one)
        assertEquals(failOne, failTwo)
        assertNotEquals(failOne, failThree)
    }

    @Test
    fun fold() {
        assertEquals(23, Either.Right<Bad, Good>(Good(5)).fold({ bad -> bad.msg.length * 3 }, { good -> good.n * 4 + 3 }))
        assertEquals(9, Either.Left<Bad, Good>(Bad("boo")).fold({ bad -> bad.msg.length * 3 }, { good -> good.n * 4 + 3 }))
    }

    @Test
    fun decomposition() {
        val (b1, g1) = Either.Right<Bad, Good>(Good(5))
        val (b2, g2) = Either.Left<Bad, Good>(Bad("boo"))
        assertEquals(None, b1)
        assertEquals(Option.Some(Good(5)), g1)
        assertEquals(Option.Some(Bad("boo")), b2)
        assertEquals(None, g2)
    }

    @Test
    fun forEach() {
        var leftLeft = 0
        var leftRight = 0
        var rightLeft = 0
        var rightRight = 0

        Either.Left<Bad, Good>(Bad("boo")).left().forEach { leftLeft++ }
        Either.Left<Bad, Good>(Bad("boo")).right().forEach { leftRight++ }
        Either.Right<Bad, Good>(Good(5)).left().forEach { rightLeft++ }
        Either.Right<Bad, Good>(Good(5)).right().forEach { rightRight++ }

        assertEquals(1, leftLeft)
        assertEquals(0, leftRight)
        assertEquals(0, rightLeft)
        assertEquals(1, rightRight)
    }
}
package com.robinhood.ticker;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TickerCharacterListTest {

    @Test
    public void test_initialization() {
        final TickerCharacterList list = new TickerCharacterList("012");
        final char[] expected = new char[] { TickerUtils.EMPTY_CHAR, '0', '1', '2', '0', '1', '2' };
        assertArrayEquals(expected, list.getCharacterList());
    }

    @Test
    public void test_getCharacterIndices() {
        final TickerCharacterList list = new TickerCharacterList("012");
        final TickerCharacterList.CharacterIndices indices = list.getCharacterIndices('0', '1', TickerView.ScrollingDirection.ANY);
        assertEquals(1, indices.startIndex);
        assertEquals(2, indices.endIndex);
    }

    @Test
    public void test_getCharacterIndicesWraparound() {
        final TickerCharacterList list = new TickerCharacterList("012");
        final TickerCharacterList.CharacterIndices indices = list.getCharacterIndices('2', '0', TickerView.ScrollingDirection.ANY);
        assertEquals(3, indices.startIndex);
        assertEquals(4, indices.endIndex);
    }

    @Test
    public void test_getCharacterIndicesWraparound2() {
        final TickerCharacterList list = new TickerCharacterList("012");
        final TickerCharacterList.CharacterIndices indices = list.getCharacterIndices('0', '2', TickerView.ScrollingDirection.ANY);
        assertEquals(4, indices.startIndex);
        assertEquals(3, indices.endIndex);
    }

    @Test
    public void test_getCharacterIndicesForcedDown() {
        final TickerCharacterList list = new TickerCharacterList("012");
        final TickerCharacterList.CharacterIndices indices = list.getCharacterIndices('2', '0', TickerView.ScrollingDirection.DOWN);
        assertEquals(3, indices.startIndex);
        assertEquals(4, indices.endIndex);
    }

    @Test
    public void test_getCharacterIndicesForcedDown2() {
        final TickerCharacterList list = new TickerCharacterList("012");
        final TickerCharacterList.CharacterIndices indices = list.getCharacterIndices('0', '2', TickerView.ScrollingDirection.DOWN);
        assertEquals(1, indices.startIndex);
        assertEquals(3, indices.endIndex);
    }

    @Test
    public void test_getCharacterIndicesForcedUp() {
        final TickerCharacterList list = new TickerCharacterList("012");
        final TickerCharacterList.CharacterIndices indices = list.getCharacterIndices('2', '0', TickerView.ScrollingDirection.UP);
        assertEquals(3, indices.startIndex);
        assertEquals(1, indices.endIndex);
    }

    @Test
    public void test_getCharacterIndicesForcedUp2() {
        final TickerCharacterList list = new TickerCharacterList("012");
        final TickerCharacterList.CharacterIndices indices = list.getCharacterIndices('0', '2', TickerView.ScrollingDirection.UP);
        assertEquals(4, indices.startIndex);
        assertEquals(3, indices.endIndex);
    }

    @Test
    public void test_getCharacterIndicesEmptyNoWraparound() {
        final TickerCharacterList list = new TickerCharacterList("012");
        final TickerCharacterList.CharacterIndices indices =
                list.getCharacterIndices('2', TickerUtils.EMPTY_CHAR, TickerView.ScrollingDirection.ANY);
        assertEquals(3, indices.startIndex);
        assertEquals(0, indices.endIndex);
    }

    @Test
    public void test_getCharacterIndicesEmptyForcedUp() {
        final TickerCharacterList list = new TickerCharacterList("012");
        final TickerCharacterList.CharacterIndices indices =
                list.getCharacterIndices('2', TickerUtils.EMPTY_CHAR, TickerView.ScrollingDirection.UP);
        assertEquals(3, indices.startIndex);
        assertEquals(0, indices.endIndex);
    }

    @Test
    public void test_getCharacterIndicesEmptyForcedDown() {
        final TickerCharacterList list = new TickerCharacterList("012");
        final TickerCharacterList.CharacterIndices indices =
                list.getCharacterIndices('2', TickerUtils.EMPTY_CHAR, TickerView.ScrollingDirection.DOWN);
        assertEquals(3, indices.startIndex);
        assertEquals(7, indices.endIndex);
    }
}

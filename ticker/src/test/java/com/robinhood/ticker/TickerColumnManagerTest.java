package com.robinhood.ticker;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TickerColumnManagerTest {
    private TickerColumnManager tickerColumnManager;

    @Before
    public void setup() {
        final TickerDrawMetrics metrics = mock(TickerDrawMetrics.class);
        tickerColumnManager = new TickerColumnManager(metrics);
        tickerColumnManager.setCharacterList((TickerUtils.EMPTY_CHAR + "1234567890").toCharArray());
    }

    @Test
    public void test_insertColumnsUpTo() {
        assertEquals(0, tickerColumnManager.size());
        tickerColumnManager.insertColumnsUpTo(2);
        assertEquals(2, tickerColumnManager.size());
        tickerColumnManager.insertColumnsUpTo(5);
        assertEquals(5, tickerColumnManager.size());
        tickerColumnManager.insertColumnsUpTo(1);
        assertEquals(5, tickerColumnManager.size());
    }

    @Test
    public void test_ensureColumnSize() {
        assertEquals(0, tickerColumnManager.size());
        tickerColumnManager.ensureColumnSize(2);
        assertEquals(2, tickerColumnManager.size());
        tickerColumnManager.ensureColumnSize(5);
        assertEquals(5, tickerColumnManager.size());
        tickerColumnManager.ensureColumnSize(1);
        assertEquals(1, tickerColumnManager.size());
    }

    @Test
    public void test_setText_animate() {
        assertEquals(0, tickerColumnManager.size());

        tickerColumnManager.setText("1234".toCharArray(), true);
        assertEquals(4, tickerColumnManager.size());
        assertEquals('1', tickerColumnManager.get(0).getTargetChar());
        assertEquals('2', tickerColumnManager.get(1).getTargetChar());
        assertEquals('3', tickerColumnManager.get(2).getTargetChar());
        assertEquals('4', tickerColumnManager.get(3).getTargetChar());

        tickerColumnManager.setText("999".toCharArray(), true);
        assertEquals(4, tickerColumnManager.size());
        assertEquals('9', tickerColumnManager.get(0).getTargetChar());
        assertEquals('9', tickerColumnManager.get(1).getTargetChar());
        assertEquals('9', tickerColumnManager.get(2).getTargetChar());
        assertEquals(TickerUtils.EMPTY_CHAR, tickerColumnManager.get(3).getTargetChar());
    }

    @Test
    public void test_setText_noAnimate() {
        assertEquals(0, tickerColumnManager.size());

        tickerColumnManager.setText("1234".toCharArray(), false);
        assertEquals(4, tickerColumnManager.size());
        assertEquals('1', tickerColumnManager.get(0).getTargetChar());
        assertEquals('2', tickerColumnManager.get(1).getTargetChar());
        assertEquals('3', tickerColumnManager.get(2).getTargetChar());
        assertEquals('4', tickerColumnManager.get(3).getTargetChar());

        tickerColumnManager.setText("999".toCharArray(), false);
        assertEquals(3, tickerColumnManager.size());
        assertEquals('9', tickerColumnManager.get(0).getTargetChar());
        assertEquals('9', tickerColumnManager.get(1).getTargetChar());
        assertEquals('9', tickerColumnManager.get(2).getTargetChar());
    }

    @Test
    public void test_shouldDebounce() {
        tickerColumnManager.setText("1234".toCharArray(), false);
        assertTrue(tickerColumnManager.shouldDebounceText("1234".toCharArray()));
        assertFalse(tickerColumnManager.shouldDebounceText("12345".toCharArray()));
    }
}

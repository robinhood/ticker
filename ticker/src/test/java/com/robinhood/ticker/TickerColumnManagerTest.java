package com.robinhood.ticker;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TickerColumnManagerTest {
    @Mock TickerDrawMetrics metrics;

    private TickerColumnManager tickerColumnManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        tickerColumnManager = new TickerColumnManager(metrics);
        tickerColumnManager.setCharacterList((TickerUtils.EMPTY_CHAR + "1234567890").toCharArray());
    }

    @Test
    public void test_insertColumnsUpTo() {
        assertEquals(0, numberOfTickerColumns());
        tickerColumnManager.insertColumnsUpTo(2);
        assertEquals(2, numberOfTickerColumns());
        tickerColumnManager.insertColumnsUpTo(5);
        assertEquals(5, numberOfTickerColumns());
        tickerColumnManager.insertColumnsUpTo(1);
        assertEquals(5, numberOfTickerColumns());
    }

    @Test
    public void test_ensureColumnSize() {
        assertEquals(0, numberOfTickerColumns());
        tickerColumnManager.ensureColumnSize(2);
        assertEquals(2, numberOfTickerColumns());
        tickerColumnManager.ensureColumnSize(5);
        assertEquals(5, numberOfTickerColumns());
        tickerColumnManager.ensureColumnSize(1);
        assertEquals(1, numberOfTickerColumns());
    }

    @Test
    public void test_setText_animate() {
        assertEquals(0, numberOfTickerColumns());

        tickerColumnManager.setText("1234".toCharArray(), true);
        assertEquals(4, numberOfTickerColumns());
        assertEquals('1', tickerColumnAtIndex(0).getTargetChar());
        assertEquals('2', tickerColumnAtIndex(1).getTargetChar());
        assertEquals('3', tickerColumnAtIndex(2).getTargetChar());
        assertEquals('4', tickerColumnAtIndex(3).getTargetChar());

        tickerColumnManager.setText("999".toCharArray(), true);
        assertEquals(4, numberOfTickerColumns());
        assertEquals('9', tickerColumnAtIndex(0).getTargetChar());
        assertEquals('9', tickerColumnAtIndex(1).getTargetChar());
        assertEquals('9', tickerColumnAtIndex(2).getTargetChar());
        assertEquals(TickerUtils.EMPTY_CHAR, tickerColumnAtIndex(3).getTargetChar());
    }

    @Test
    public void test_setText_noAnimate() {
        assertEquals(0, numberOfTickerColumns());

        tickerColumnManager.setText("1234".toCharArray(), false);
        assertEquals(4, numberOfTickerColumns());
        assertEquals('1', tickerColumnAtIndex(0).getTargetChar());
        assertEquals('2', tickerColumnAtIndex(1).getTargetChar());
        assertEquals('3', tickerColumnAtIndex(2).getTargetChar());
        assertEquals('4', tickerColumnAtIndex(3).getTargetChar());

        tickerColumnManager.setText("999".toCharArray(), false);
        assertEquals(3, numberOfTickerColumns());
        assertEquals('9', tickerColumnAtIndex(0).getTargetChar());
        assertEquals('9', tickerColumnAtIndex(1).getTargetChar());
        assertEquals('9', tickerColumnAtIndex(2).getTargetChar());
    }

    @Test
    public void test_shouldDebounce() {
        tickerColumnManager.setText("1234".toCharArray(), false);
        assertTrue(tickerColumnManager.shouldDebounceText("1234".toCharArray()));
        assertFalse(tickerColumnManager.shouldDebounceText("12345".toCharArray()));
    }

    private TickerColumn tickerColumnAtIndex(int index) {
        return tickerColumnManager.tickerColumns.get(index);
    }

    private int numberOfTickerColumns() {
        return tickerColumnManager.tickerColumns.size();
    }
}

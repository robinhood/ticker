package com.robinhood.ticker;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyChar;
import static org.mockito.Mockito.when;

public class TickerColumnManagerTest {
    @Mock TickerDrawMetrics metrics;

    private TickerColumnManager tickerColumnManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(metrics.getCharWidth(anyChar())).thenReturn(5f);
        when(metrics.getCharWidth(TickerUtils.EMPTY_CHAR)).thenReturn(0f);

        tickerColumnManager = new TickerColumnManager(metrics);
        tickerColumnManager.setCharacterList((TickerUtils.EMPTY_CHAR + "1234567890").toCharArray());
    }

    @Test
    public void test_setText_animate() {
        assertEquals(0, numberOfTickerColumns());

        tickerColumnManager.setText("1234".toCharArray());
        tickerColumnManager.setAnimationProgress(1f);
        assertEquals(4, numberOfTickerColumns());
        assertEquals('1', tickerColumnAtIndex(0).getTargetChar());
        assertEquals('2', tickerColumnAtIndex(1).getTargetChar());
        assertEquals('3', tickerColumnAtIndex(2).getTargetChar());
        assertEquals('4', tickerColumnAtIndex(3).getTargetChar());

        tickerColumnManager.setText("999".toCharArray());
        assertEquals(4, numberOfTickerColumns());
        assertEquals(TickerUtils.EMPTY_CHAR, tickerColumnAtIndex(0).getTargetChar());
        assertEquals('9', tickerColumnAtIndex(1).getTargetChar());
        assertEquals('9', tickerColumnAtIndex(2).getTargetChar());
        assertEquals('9', tickerColumnAtIndex(3).getTargetChar());

        tickerColumnManager.setAnimationProgress(1f);
        tickerColumnManager.setText("899".toCharArray());
        assertEquals(3, numberOfTickerColumns());
        assertEquals('8', tickerColumnAtIndex(0).getTargetChar());
        assertEquals('9', tickerColumnAtIndex(1).getTargetChar());
        assertEquals('9', tickerColumnAtIndex(2).getTargetChar());
    }

    @Test
    public void test_setText_noAnimate() {
        assertEquals(0, numberOfTickerColumns());

        tickerColumnManager.setText("1234".toCharArray());
        assertEquals(4, numberOfTickerColumns());
        assertEquals('1', tickerColumnAtIndex(0).getTargetChar());
        assertEquals('2', tickerColumnAtIndex(1).getTargetChar());
        assertEquals('3', tickerColumnAtIndex(2).getTargetChar());
        assertEquals('4', tickerColumnAtIndex(3).getTargetChar());

        tickerColumnManager.setText("999".toCharArray());
        assertEquals(3, numberOfTickerColumns());
        assertEquals('9', tickerColumnAtIndex(0).getTargetChar());
        assertEquals('9', tickerColumnAtIndex(1).getTargetChar());
        assertEquals('9', tickerColumnAtIndex(2).getTargetChar());
    }

    private TickerColumn tickerColumnAtIndex(int index) {
        return tickerColumnManager.tickerColumns.get(index);
    }

    private int numberOfTickerColumns() {
        return tickerColumnManager.tickerColumns.size();
    }
}

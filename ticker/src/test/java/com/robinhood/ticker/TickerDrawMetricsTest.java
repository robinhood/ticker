package com.robinhood.ticker;

import android.graphics.Paint;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TickerDrawMetricsTest {
    @Mock  Paint paint;
    private TickerDrawMetrics metrics;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        final Paint.FontMetrics fontMetrics = new Paint.FontMetrics();
        fontMetrics.top = -50f;
        fontMetrics.bottom = 20f;
        when(paint.getFontMetrics()).thenReturn(fontMetrics);

        metrics = new TickerDrawMetrics(paint);
    }

    @Test
    public void test_fontMetrics() {
        assertEquals(70f, metrics.getCharHeight(), 0f);
        assertEquals(50f, metrics.getCharBaseline(), 0f);
    }

    @Test
    public void test_charWidth() {
        when(paint.measureText("1")).thenReturn(1f);
        when(paint.measureText("2")).thenReturn(2f);
        when(paint.measureText("3")).thenReturn(3f);

        assertEquals(1f, metrics.getCharWidth('1'), 0f);
        assertEquals(2f, metrics.getCharWidth('2'), 0f);
        assertEquals(3f, metrics.getCharWidth('3'), 0f);

        // Subsequent calls should be cached
        assertEquals(1f, metrics.getCharWidth('1'), 0f);
        assertEquals(2f, metrics.getCharWidth('2'), 0f);
        assertEquals(3f, metrics.getCharWidth('3'), 0f);

        metrics.invalidate();

        // These calls should re-measure based on the paint
        assertEquals(1f, metrics.getCharWidth('1'), 0f);
        assertEquals(2f, metrics.getCharWidth('2'), 0f);
        assertEquals(3f, metrics.getCharWidth('3'), 0f);

        verify(paint, times(2)).measureText("1");
        verify(paint, times(2)).measureText("2");
        verify(paint, times(2)).measureText("3");
        verify(paint, times(2)).getFontMetrics();
        verifyNoMoreInteractions(paint);
    }
}

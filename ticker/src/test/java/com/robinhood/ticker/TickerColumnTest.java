package com.robinhood.ticker;

import android.graphics.Canvas;
import android.text.TextPaint;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyChar;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TickerColumnTest {
    private static final float CHAR_HEIGHT = 5f;
    private static final float DEFAULT_CHAR_WIDTH = 10f;

    private static TickerCharacterList characterList = new TickerCharacterList("01234");

    @Mock TickerDrawMetrics metrics;
    @Mock Canvas canvas;
    @Mock TextPaint paint;

    private TickerColumn tickerColumn;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(metrics.getCharHeight()).thenReturn(CHAR_HEIGHT);
        when(metrics.getCharWidth(anyChar())).thenReturn(DEFAULT_CHAR_WIDTH);
        when(metrics.getCharWidth(TickerUtils.EMPTY_CHAR)).thenReturn(0f);
        when(metrics.getPreferredScrollingDirection()).thenReturn(TickerView.ScrollingDirection.ANY);

        tickerColumn = new TickerColumn(
                new TickerCharacterList[] { characterList },
                metrics
        );
    }

    @Test
    public void test_draw_differentWidth() {
        // Going from empty to not empty
        tickerColumn.setTargetChar('0');
        assertEquals((int) DEFAULT_CHAR_WIDTH, (int) tickerColumn.getMinimumRequiredWidth());
        assertEquals(0, (int) tickerColumn.getCurrentWidth());

        setProgress(0.4f);
        assertEquals((int) DEFAULT_CHAR_WIDTH, (int) tickerColumn.getMinimumRequiredWidth());
        assertEquals(4, (int) tickerColumn.getCurrentWidth());

        setProgress(1f);
        assertEquals((int) DEFAULT_CHAR_WIDTH, (int) tickerColumn.getMinimumRequiredWidth());
        assertEquals((int) DEFAULT_CHAR_WIDTH, (int) tickerColumn.getCurrentWidth());

        // Going from not empty to not empty
        tickerColumn.setTargetChar('1');
        assertEquals((int) DEFAULT_CHAR_WIDTH, (int) tickerColumn.getMinimumRequiredWidth());
        assertEquals((int) DEFAULT_CHAR_WIDTH, (int) tickerColumn.getCurrentWidth());

        setProgress(0.4f);
        assertEquals((int) DEFAULT_CHAR_WIDTH, (int) tickerColumn.getMinimumRequiredWidth());
        assertEquals((int) DEFAULT_CHAR_WIDTH, (int) tickerColumn.getCurrentWidth());

        setProgress(1f);
        assertEquals((int) DEFAULT_CHAR_WIDTH, (int) tickerColumn.getMinimumRequiredWidth());
        assertEquals((int) DEFAULT_CHAR_WIDTH, (int) tickerColumn.getCurrentWidth());
    }

    @Test
    public void test_draw_noAnimation() {
        tickerColumn.setTargetChar('0');
        setProgress(1f);
        assertEquals('0', tickerColumn.getTargetChar());
        verifyDraw(1, 0f);
        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_noAnimation_edge1() {
        tickerColumn.setTargetChar(TickerUtils.EMPTY_CHAR);
        setProgress(1f);
        verifyDraw(0, 0f);
        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_noAnimation_edge2() {
        tickerColumn.setTargetChar('2');
        setProgress(1f);
        verifyDraw(3, 0f);
        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_startAnimation() {
        tickerColumn.setTargetChar('1');
        setProgress(0f);
        verifyDraw(0, 0f);
        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_duringAnimation1() {
        tickerColumn.setTargetChar('1');
        setProgress(0.5f);
        verifyDraw(1, 0f);
        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_duringAnimation2() {
        tickerColumn.setTargetChar('1');
        setProgress(0.75f);
        // We should be half way between '0' and '1'.
        verifyDraw(1, CHAR_HEIGHT / 2);
        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_interruptedStartAnimation_startAnimation() {
        tickerColumn.setTargetChar('1');
        setProgress(0f);

        tickerColumn.setTargetChar('2');
        setProgress(0f);

        verifyDraw(0, 0f, 2);

        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_interruptedStartAnimation_midAnimation() {
        tickerColumn.setTargetChar('1');
        setProgress(0f);
        verifyDraw(0, 0f);

        tickerColumn.setTargetChar('2');
        setProgress(0.25f);
        // We should be 3 quarters way between EMPTY_CHAR and '0'.
        verifyDraw(0, CHAR_HEIGHT / 4 * 3);

        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_interruptedStartAnimation_endAnimation() {
        tickerColumn.setTargetChar('1');
        setProgress(0f);
        verifyDraw(0, 0f);

        tickerColumn.setTargetChar('0');
        setProgress(1f);
        verifyDraw(1, 0f);

        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_interruptedMidAnimation_startAnimation() {
        tickerColumn.setTargetChar('1');
        setProgress(0.75f);

        tickerColumn.setTargetChar(TickerUtils.EMPTY_CHAR);
        setProgress(0f);

        // We should still be half way between '0' and '1' since the new animation just started.
        verifyDraw(1, CHAR_HEIGHT / 2, 2);

        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_interruptedMidAnimation_midAnimation() {
        tickerColumn.setTargetChar('1');
        setProgress(0.75f);
        // We should be half way between '0' and '1'.
        verifyDraw(1, CHAR_HEIGHT / 2);

        tickerColumn.setTargetChar('0');
        setProgress(0.5f);
        // We are now quarter way between '0' and '1'.
        verifyDraw(1, CHAR_HEIGHT / 4);

        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_interruptedMidAnimation_endAnimation() {
        tickerColumn.setTargetChar('1');
        setProgress(0.75f);
        // We should be half way between '0' and '1'.
        verifyDraw(1, CHAR_HEIGHT / 2);

        tickerColumn.setTargetChar('0');
        setProgress(1f);
        verifyDraw(1, 0f);

        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_mockConsecutiveAnimations() {
        // Simulates how the ticker column would normally get callbacks

        tickerColumn.setTargetChar('0');
        setProgress(0f);
        verifyDraw(0, 0f);

        setProgress(0.25f);
        verifyDraw(0, CHAR_HEIGHT / 4);

        setProgress(0.75f);
        verifyDraw(0, CHAR_HEIGHT / 4 * 3);

        setProgress(1f);
        verifyDraw(1, 0f);

        verifyNoMoreInteractions(canvas);
        reset(canvas);

        tickerColumn.setTargetChar('1');
        setProgress(0f);
        verifyDraw(1, 0f);

        setProgress(0.25f);
        verifyDraw(1, CHAR_HEIGHT / 4);

        setProgress(0.75f);
        verifyDraw(1, CHAR_HEIGHT / 4 * 3);

        setProgress(1f);
        verifyDraw(2, 0f);

        verifyNoMoreInteractions(canvas);
        reset(canvas);

        tickerColumn.setTargetChar(TickerUtils.EMPTY_CHAR);
        setProgress(0f);
        verifyDraw(2, 0f);

        setProgress(0.25f);
        verifyDraw(2, -CHAR_HEIGHT / 2);

        setProgress(0.75f);
        verifyDraw(1, -CHAR_HEIGHT / 2);

        setProgress(1f);
        verifyDraw(0, 0f);

        verifyNoMoreInteractions(canvas);
    }

    @Test
    public void test_draw_wraparound() {
        tickerColumn.setTargetChar('4');
        setProgress(1f);
        verifyDraw(5, 0f);

        tickerColumn.setTargetChar('1');
        setProgress(0.5f);
        verifyDraw(6, 0f);

        setProgress(1f);
        verifyDraw(7, 0f);
    }

    private void setProgress(float progress) {
        tickerColumn.setAnimationProgress(progress);
        tickerColumn.draw(canvas, paint);
    }

    private void verifyDraw(int index, float offset) {
        verifyDraw(index, offset, 1);
    }

    private void verifyDraw(int index, float offset, int times) {
        verifyDraw(characterList.getCharacterList(), index, offset, times);
    }

    private void verifyDraw(char[] charList, int index, float offset, int times) {
        verify(canvas, times(times))
                .drawText(charList, index, 1, 0f, offset, paint);
        if (index < charList.length - 1) {
            verify(canvas, times(times))
                    .drawText(charList, index + 1, 1, 0f, offset - CHAR_HEIGHT, paint);
        }
        if (index >= 1) {
            verify(canvas, times(times))
                    .drawText(charList, index - 1, 1, 0f, offset + CHAR_HEIGHT, paint);
        }
    }
}

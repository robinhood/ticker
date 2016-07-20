package com.robinhood.ticker;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Gravity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TickerViewTest {
    @Mock Canvas canvas;
    @Mock Rect viewBounds;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // 100 x 100 square for available width and height
        viewBounds.right = 100;
        viewBounds.bottom = 100;
        when(viewBounds.width()).thenReturn(viewBounds.right - viewBounds.left);
        when(viewBounds.height()).thenReturn(viewBounds.bottom - viewBounds.top);
    }

    @Test
    public void test_realignAndClipCanvasForGravity_start() {
        testAndCheckGravity(Gravity.START, 30f, 30f, 0f, 0f);
    }

    @Test
    public void test_realignAndClipCanvasForGravity_end() {
        testAndCheckGravity(Gravity.END, 30f, 30f, 70f, 0f);
    }

    @Test
    public void test_realignAndClipCanvasForGravity_centerHorizontal() {
        testAndCheckGravity(Gravity.CENTER_HORIZONTAL, 30f, 30f, 35f, 0f);
    }

    @Test
    public void test_realignAndClipCanvasForGravity_centerVertical() {
        testAndCheckGravity(Gravity.CENTER_VERTICAL, 30f, 30f, 0f, 35f);
    }

    @Test
    public void test_realignAndClipCanvasForGravity_center() {
        testAndCheckGravity(Gravity.CENTER, 30f, 30f, 35f, 35f);
    }

    @Test
    public void test_realignAndClipCanvasForGravity_bottom() {
        testAndCheckGravity(Gravity.BOTTOM, 30f, 30f, 0f, 70f);
    }

    @Test
    public void test_realignAndClipCanvasForGravity_bottomEnd() {
        testAndCheckGravity(Gravity.BOTTOM | Gravity.END, 30f, 30f, 70f, 70f);
    }

    @Test
    public void test_realignAndClipCanvasForGravity_centerVerticalEnd() {
        testAndCheckGravity(Gravity.CENTER_VERTICAL | Gravity.END, 30f, 30f, 70f, 35f);
    }

    @Test
    public void test_realignAndClipCanvasForGravity_bottomCenterHorizontal() {
        testAndCheckGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 30f, 30f, 35f, 70f);
    }

    private void testAndCheckGravity(int gravity, float currWidth, float currHeight,
            float translationX, float translationY) {
        TickerView.realignAndClipCanvasForGravity(canvas, gravity, viewBounds, currWidth,
                currHeight);

        verify(canvas).translate(translationX, translationY);
        verify(canvas).clipRect(0f, 0f, currWidth, currHeight);
        verifyNoMoreInteractions(canvas);
    }

}

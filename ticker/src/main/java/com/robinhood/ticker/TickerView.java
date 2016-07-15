/**
 * Copyright (C) 2016 Robinhood Markets, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.robinhood.ticker;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * The primary view for showing a ticker text view that handles smoothly scrolling from the
 * current text to a given text. The scrolling behavior is defined by
 * {@link #setCharacterList(char[])} which dictates what characters come in between the starting
 * and ending characters.
 *
 * <p>This class primarily handles the drawing customization of the ticker view, for example
 * setting animation duration, interpolator, colors, etc. It ensures that the canvas is properly
 * positioned, and then it delegates the drawing of each column of text to
 * {@link TickerColumnManager}.
 *
 * <p>This class's API should behave similarly to that of a {@link android.widget.TextView}.
 * However, I chose to extend from {@link View} instead of {@link android.widget.TextView}
 * because it allows me full flexibility in customizing the drawing and also support different
 * customization attributes as they are implemented.
 *
 * @author Jin Cao, Robinhood
 */
public class TickerView extends View {
    private static final int DEFAULT_TEXT_SIZE = 12;
    private static final int DEFAULT_TEXT_COLOR = Color.BLACK;
    private static final long DEFAULT_ANIMATION_DURATION = 350;
    private static final Interpolator DEFAULT_ANIMATION_INTERPOLATOR =
            new AccelerateDecelerateInterpolator();

    protected final Paint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private final TickerDrawMetrics metrics = new TickerDrawMetrics(textPaint);
    private final TickerColumnManager columnManager = new TickerColumnManager(metrics);

    // Minor optimizations for re-positioning the canvas for the composer.
    private final Rect canvasFrame = new Rect();

    private ValueAnimator animator;

    // View attributes, defaults are set in init().
    private float textSize;
    private int textColor;
    private long animationDurationInMillis;
    private Interpolator animationInterpolator;

    public TickerView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public TickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public TickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * We currently only support the following set of XML attributes:
     * <ul>
     *     <li>app:textColor
     *     <li>app:textSize
     * </ul>
     */
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final Resources res = context.getResources();

        // Set the view attributes from XML or from default values defined in this class
        final TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.TickerView,
                defStyleAttr, defStyleRes);

        final int textColor = arr.getColor(R.styleable.TickerView_ticker_textColor,
                DEFAULT_TEXT_COLOR);
        setTextColor(textColor);
        final float textSize = arr.getDimension(R.styleable.TickerView_ticker_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE,
                        res.getDisplayMetrics()));
        setTextSize(textSize);

        animationDurationInMillis = DEFAULT_ANIMATION_DURATION;
        animationInterpolator = DEFAULT_ANIMATION_INTERPOLATOR;

        arr.recycle();

        animator = ValueAnimator.ofFloat(1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                columnManager.setAnimationProgress(animation.getAnimatedFraction());
                invalidate();
            }
        });
    }


    /********** BEGIN PUBLIC API **********/


    /**
     * This is the primary API that the view uses to determine how to animate from one character
     * to another. The provided character array dictates what characters will appear between
     * the start and end characters.
     *
     * <p>For example, given the list [a,b,c,d,e], if the view wants to animate from 'd' to 'a',
     * it will know that it has to go from 'd' -> 'c' -> 'b' -> 'a', and these are the characters
     * that show up during the animation scroll.
     *
     * <p>You can find some helpful character list generators in {@link TickerUtils}.
     *
     * <p>Special note: the character list must contain {@link TickerUtils#EMPTY_CHAR} because the
     * ticker needs to know how to animate from empty to another character (e.g. when the length
     * of the string changes).
     *
     * @param characterList the character array that dictates character orderings.
     */
    public void setCharacterList(char[] characterList) {
        boolean foundEmpty = false;
        for (int i = 0; i < characterList.length; i++) {
            if (characterList[i] == TickerUtils.EMPTY_CHAR) {
                foundEmpty = true;
                break;
            }
        }

        if (!foundEmpty) {
            throw new IllegalArgumentException("Missing TickerUtils#EMPTY_CHAR in character list");
        }

        columnManager.setCharacterList(characterList);
    }

    /**
     * Sets the string value to display. If the TickerView is currently empty, then this method
     * will immediately display the provided text. Otherwise, it will run the default animation
     * to reach the provided text.
     *
     * @param text the text to display.
     */
    public void setText(String text) {
        setText(text, columnManager.getCurrentWidth() > 0);
    }

    /**
     * Similar to {@link #setText(String)} but provides the optional argument of whether to
     * animate to the provided text or not.
     *
     * @param text the text to display.
     * @param animate whether to animate to {@param text}.
     */
    public synchronized void setText(String text, boolean animate) {
        final char[] targetText = text == null ? new char[0] : text.toCharArray();

        if (columnManager.shouldDebounceText(targetText)) {
            return;
        }

        columnManager.setText(targetText, animate);
        columnManager.setAnimationProgress(animate ? 0f : 1f);
        setContentDescription(text);
        checkForRelayout();

        if (animate) {
            // Kick off the animator that draws the transition
            if (animator.isRunning()) {
                animator.cancel();
            }

            animator.setDuration(animationDurationInMillis);
            animator.setInterpolator(animationInterpolator);
            animator.start();
        } else {
            invalidate();
        }
    }

    /**
     * @return the current text color that's being used to draw the text.
     */
    public int getTextColor() {
        return textColor;
    }

    /**
     * Sets the text color used by this view. The default text color is defined by
     * {@link #DEFAULT_TEXT_COLOR}.
     *
     * @param color the color to set the text to.
     */
    public void setTextColor(int color) {
        textColor = color;
        textPaint.setColor(textColor);
        invalidate();
    }

    /**
     * @return the current text size that's being used to draw the text.
     */
    public float getTextSize() {
        return textSize;
    }

    /**
     * Sets the text size used by this view. The default text size is defined by
     * {@link #DEFAULT_TEXT_SIZE}.
     *
     * @param textSize the text size to set the text to.
     */
    public void setTextSize(float textSize) {
        this.textSize = textSize;
        textPaint.setTextSize(textSize);
        onTextPaintChanged();
    }

    /**
     * @return the duration in milliseconds that the transition animations run for.
     */
    public long getAnimationDuration() {
        return animationDurationInMillis;
    }

    /**
     * Sets the duration in milliseconds that this TickerView runs its transition animations. The
     * default animation duration is defined by {@link #DEFAULT_ANIMATION_DURATION}.
     *
     * @param animationDurationInMillis the duration in milliseconds.
     */
    public void setAnimationDuration(long animationDurationInMillis) {
        this.animationDurationInMillis = animationDurationInMillis;
    }

    /**
     * @return the interpolator used to interpolate the animated values.
     */
    public Interpolator getAnimationInterpolator() {
        return animationInterpolator;
    }

    /**
     * Sets the interpolator for the transition animation. The default interpolator is defined by
     * {@link #DEFAULT_ANIMATION_INTERPOLATOR}.
     *
     * @param animationInterpolator the interpolator for the animation.
     */
    public void setAnimationInterpolator(Interpolator animationInterpolator) {
        this.animationInterpolator = animationInterpolator;
    }


    /********** END PUBLIC API **********/


    /**
     * Force the view to call {@link #requestLayout()} if the new text doesn't match the old bounds
     * we set for the previous view state.
     */
    private void checkForRelayout() {
        if (getWidth() != computeDesiredWidth() || getHeight() != computeDesiredHeight()) {
            requestLayout();
        }
    }

    private int computeDesiredWidth() {
        return (int) columnManager.getMinimumRequiredWidth() + getPaddingLeft() + getPaddingRight();
    }

    private int computeDesiredHeight() {
        return (int) metrics.getCharHeight() + getPaddingTop() + getPaddingBottom();
    }

    /**
     * Re-initialize all of our variables that are dependent on the TextPaint measurements.
     */
    private void onTextPaintChanged() {
        metrics.invalidate();
        checkForRelayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int desiredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = MeasureSpec.getSize(heightMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.AT_MOST:
                desiredWidth = Math.min(desiredWidth, computeDesiredWidth());
                break;
            case MeasureSpec.UNSPECIFIED:
                desiredWidth = computeDesiredWidth();
                break;
        }

        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.AT_MOST:
                desiredHeight = Math.min(desiredHeight, computeDesiredHeight());
                break;
            case MeasureSpec.UNSPECIFIED:
                desiredHeight = computeDesiredHeight();
                break;
        }

        setMeasuredDimension(desiredWidth, desiredHeight);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        canvasFrame.set(getPaddingLeft(), getPaddingTop(), width - getPaddingRight(),
                height - getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        final int availableWidth = canvasFrame.width();
        final int availableHeight = canvasFrame.height();
        final float currentWidth = columnManager.getCurrentWidth();
        final float currentHeight = metrics.getCharHeight();

        // We translate first by the canvas frame and then re-align so that we are drawing in
        // the center of the canvas (if availableWidth is greater than currentWidth).
        final float translationX = canvasFrame.left + (availableWidth - currentWidth) / 2f;
        final float translationY = canvasFrame.top + (availableHeight - currentHeight) / 2f;
        canvas.translate(translationX ,translationY);
        canvas.clipRect(0f, 0f, currentWidth, currentHeight);

        // canvas.drawText writes the text on the baseline so we need to translate beforehand.
        canvas.translate(0f, metrics.getCharBaseline());

        columnManager.draw(canvas, textPaint);

        canvas.restore();
    }
}

/*
  Copyright (C) 2016 Robinhood Markets, Inc.

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

package com.robinhood.ticker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * The primary view for showing a ticker text view that handles smoothly scrolling from the
 * current text to a given text. The scrolling behavior is defined by
 * {@link #setCharacterLists} which dictates what characters come in between the starting
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

    public enum ScrollingDirection {
        ANY, UP, DOWN
    }

    private static final int DEFAULT_TEXT_SIZE = 12;
    private static final int DEFAULT_TEXT_COLOR = Color.BLACK;
    private static final int DEFAULT_ANIMATION_DURATION = 350;
    private static final Interpolator DEFAULT_ANIMATION_INTERPOLATOR =
            new AccelerateDecelerateInterpolator();
    private static final int DEFAULT_GRAVITY = Gravity.START;

    protected final Paint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private final TickerDrawMetrics metrics = new TickerDrawMetrics(textPaint);
    private final TickerColumnManager columnManager = new TickerColumnManager(metrics);
    private final ValueAnimator animator = ValueAnimator.ofFloat(1f);

    // Minor optimizations for re-positioning the canvas for the composer.
    private final Rect viewBounds = new Rect();

    private String text;

    private int lastMeasuredDesiredWidth, lastMeasuredDesiredHeight;

    // View attributes, defaults are set in init().
    private int gravity;
    private int textColor;
    private float textSize;
    private int textStyle;
    private long animationDelayInMillis;
    private long animationDurationInMillis;
    private Interpolator animationInterpolator;
    private boolean animateMeasurementChange;
    // pending text set from XML because we didn't have a character list initially
    private String pendingTextToSet;

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
     *
     * @param context context from constructor
     * @param attrs attrs from constructor
     * @param defStyleAttr defStyleAttr from constructor
     * @param defStyleRes defStyleRes from constructor
     */
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final Resources res = context.getResources();
        final StyledAttributes styledAttributes = new StyledAttributes(res);

        // Set the view attributes from XML or from default values defined in this class
        final TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.TickerView,
                defStyleAttr, defStyleRes);

        final int textAppearanceResId = arr.getResourceId(
                R.styleable.TickerView_android_textAppearance, -1);

        // Check textAppearance first
        if (textAppearanceResId != -1) {
            final TypedArray textAppearanceArr = context.obtainStyledAttributes(
                    textAppearanceResId, R.styleable.TickerView);
            styledAttributes.applyTypedArray(textAppearanceArr);
            textAppearanceArr.recycle();
        }

        // Custom set attributes on the view should override textAppearance if applicable.
        styledAttributes.applyTypedArray(arr);

        // After we've fetched the correct values for the attributes, set them on the view
        animationInterpolator = DEFAULT_ANIMATION_INTERPOLATOR;
        this.animationDurationInMillis = arr.getInt(
                R.styleable.TickerView_ticker_animationDuration, DEFAULT_ANIMATION_DURATION);
        this.animateMeasurementChange = arr.getBoolean(
                R.styleable.TickerView_ticker_animateMeasurementChange, false);
        this.gravity = styledAttributes.gravity;

        if (styledAttributes.shadowColor != 0) {
            textPaint.setShadowLayer(styledAttributes.shadowRadius, styledAttributes.shadowDx,
                    styledAttributes.shadowDy, styledAttributes.shadowColor);
        }
        if (styledAttributes.textStyle != 0) {
            textStyle = styledAttributes.textStyle;
            setTypeface(textPaint.getTypeface());
        }

        setTextColor(styledAttributes.textColor);
        setTextSize(styledAttributes.textSize);

        final int defaultCharList =
                arr.getInt(R.styleable.TickerView_ticker_defaultCharacterList, 0);
        switch (defaultCharList) {
            case 1:
                setCharacterLists(TickerUtils.provideNumberList());
                break;
            case 2:
                setCharacterLists(TickerUtils.provideAlphabeticalList());
                break;
            default:
                if (isInEditMode()) {
                    setCharacterLists(TickerUtils.provideNumberList());
                }
        }

        final int defaultPreferredScrollingDirection =
                arr.getInt(R.styleable.TickerView_ticker_defaultPreferredScrollingDirection, 0);

        switch (defaultPreferredScrollingDirection) {
            case 0:
                metrics.setPreferredScrollingDirection(ScrollingDirection.ANY);
                break;
            case 1:
                metrics.setPreferredScrollingDirection(ScrollingDirection.UP);
                break;
            case 2:
                metrics.setPreferredScrollingDirection(ScrollingDirection.DOWN);
                break;
            default:
                throw new IllegalArgumentException("Unsupported ticker_defaultPreferredScrollingDirection: " + defaultPreferredScrollingDirection);
        }

        if (isCharacterListsSet()) {
            setText(styledAttributes.text, false);
        } else {
            this.pendingTextToSet = styledAttributes.text;
        }

        arr.recycle();

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                columnManager.setAnimationProgress(
                        animation.getAnimatedFraction());
                checkForRelayout();
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                columnManager.onAnimationEnd();
                checkForRelayout();
                invalidate();
            }
        });
    }

    /**
     * Only attributes that can be applied from `android:textAppearance` should be added here.
     */
    private class StyledAttributes {
        int gravity;
        int shadowColor;
        float shadowDx;
        float shadowDy;
        float shadowRadius;
        String text;
        int textColor;
        float textSize;
        int textStyle;

        StyledAttributes(Resources res) {
            textColor = DEFAULT_TEXT_COLOR;
            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    DEFAULT_TEXT_SIZE, res.getDisplayMetrics());
            gravity = DEFAULT_GRAVITY;
        }

        void applyTypedArray(TypedArray arr) {
            gravity = arr.getInt(R.styleable.TickerView_android_gravity, gravity);
            shadowColor = arr.getColor(R.styleable.TickerView_android_shadowColor,
                    shadowColor);
            shadowDx = arr.getFloat(R.styleable.TickerView_android_shadowDx, shadowDx);
            shadowDy = arr.getFloat(R.styleable.TickerView_android_shadowDy, shadowDy);
            shadowRadius = arr.getFloat(R.styleable.TickerView_android_shadowRadius,
                    shadowRadius);
            text = arr.getString(R.styleable.TickerView_android_text);
            textColor = arr.getColor(R.styleable.TickerView_android_textColor, textColor);
            textSize = arr.getDimension(R.styleable.TickerView_android_textSize, textSize);
            textStyle = arr.getInt(R.styleable.TickerView_android_textStyle, textStyle);
        }
    }


    /********** BEGIN PUBLIC API **********/


    /**
     * This is the primary class that Ticker uses to determine how to animate from one character
     * to another. The provided strings dictates what characters will appear between
     * the start and end characters.
     *
     * <p>For example, given the string "abcde", if the view wants to animate from 'd' to 'b',
     * it will know that it has to go from 'd' to 'c' to 'b', and these are the characters
     * that show up during the animation scroll.
     *
     * <p>We allow for multiple character lists, and the character lists will be prioritized with
     * latter lists given a higher priority than the previous lists. e.g. given "123" and "13",
     * an animation from 1 to 3 will use the sequence [1,3] rather than [1,2,3].
     *
     * <p>You can find some helpful character list in {@link TickerUtils}.
     *
     * @param characterLists the list of character lists that dictates animation.
     */
    public void setCharacterLists(String... characterLists) {
        columnManager.setCharacterLists(characterLists);
        if (pendingTextToSet != null) {
            setText(pendingTextToSet, false);
            pendingTextToSet = null;
        }
    }

    /**
     * @return whether or not the character lists (via {@link #setCharacterLists}) have been set.
     *         Can use this value to determine if you need to call {@link #setCharacterLists}
     *         before calling {@link #setText}.
     */
    public boolean isCharacterListsSet() {
        return columnManager.getCharacterLists() != null;
    }

    /**
     * Sets the string value to display. If the TickerView is currently empty, then this method
     * will immediately display the provided text. Otherwise, it will run the default animation
     * to reach the provided text.
     *
     * @param text the text to display.
     */
    public void setText(String text) {
        setText(text, !TextUtils.isEmpty(this.text));
    }

    /**
     * Similar to {@link #setText(String)} but provides the optional argument of whether to
     * animate to the provided text or not.
     *
     * @param text the text to display.
     * @param animate whether to animate to text.
     */
    public void setText(String text, boolean animate) {
        if (TextUtils.equals(text, this.text)) {
            return;
        }

        this.text = text;
        final char[] targetText = text == null ? new char[0] : text.toCharArray();

        columnManager.setText(targetText);
        setContentDescription(text);

        if (animate) {
            // Kick off the animator that draws the transition
            if (animator.isRunning()) {
                animator.cancel();
            }

            animator.setStartDelay(animationDelayInMillis);
            animator.setDuration(animationDurationInMillis);
            animator.setInterpolator(animationInterpolator);
            animator.start();
        } else {
            columnManager.setAnimationProgress(1f);
            columnManager.onAnimationEnd();
            checkForRelayout();
            invalidate();
        }
    }

    /**
     * Get the last set text on the view. This does not equate to the current shown text on the
     * UI because the animation might not have started or finished yet.
     *
     * @return last set text on this view.
     */
    public String getText() {
        return text;
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
        if (this.textColor != color) {
            textColor = color;
            textPaint.setColor(textColor);
            invalidate();
        }
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
     * @param textSize the text size in pixel units.
     */
    public void setTextSize(float textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            textPaint.setTextSize(textSize);
            onTextPaintMeasurementChanged();
        }
    }

    /**
     * @return the current text typeface.
     */
    public Typeface getTypeface() {
        return textPaint.getTypeface();
    }

    /**
     * Sets the typeface size used by this view.
     *
     * @param typeface the typeface to use on the text.
     */
    public void setTypeface(Typeface typeface) {
        if (textStyle == Typeface.BOLD_ITALIC) {
            typeface = Typeface.create(typeface, Typeface.BOLD_ITALIC);
        } else if (textStyle == Typeface.BOLD) {
            typeface = Typeface.create(typeface, Typeface.BOLD);
        } else if (textStyle == Typeface.ITALIC) {
            typeface = Typeface.create(typeface, Typeface.ITALIC);
        }

        textPaint.setTypeface(typeface);
        onTextPaintMeasurementChanged();
    }

    /**
     * @return the delay in milliseconds before the transition animations runs
     */
    public long getAnimationDelay() {
        return animationDelayInMillis;
    }

    /**
     * Sets the delay in milliseconds before this TickerView runs its transition animations. The
     * default animation delay is 0.
     *
     * @param animationDelayInMillis the delay in milliseconds.
     */
    public void setAnimationDelay(long animationDelayInMillis) {
        this.animationDelayInMillis = animationDelayInMillis;
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

    /**
     * Sets the preferred scrolling direction for ticker animations.
     * Eligible params include {@link ScrollingDirection#ANY}, {@link ScrollingDirection#UP}
     * and {@link ScrollingDirection#DOWN}.
     *
     * The default value is {@link ScrollingDirection#ANY}.
     *
     * @param direction the preferred {@link ScrollingDirection}
     */
    public void setPreferredScrollingDirection(ScrollingDirection direction) {
        this.metrics.setPreferredScrollingDirection(direction);
    }

    /**
     * @return the current text gravity used to align the text. Should be one of the values defined
     *         in {@link android.view.Gravity}.
     */
    public int getGravity() {
        return gravity;
    }

    /**
     * Sets the gravity used to align the text.
     *
     * @param gravity the new gravity, should be one of the values defined in
     *                {@link android.view.Gravity}.
     */
    public void setGravity(int gravity) {
        if (this.gravity != gravity) {
            this.gravity = gravity;
            invalidate();
        }
    }

    /**
     * Enables/disables the flag to animate measurement changes. If this flag is enabled, any
     * animation that changes the content's text width (e.g. 9999 to 10000) will have the view's
     * measured width animated along with the text width. However, a side effect of this is that
     * the entering/exiting character might get truncated by the view's view bounds as the width
     * shrinks or expands.
     *
     * <p>Warning: using this feature may degrade performance as it will force a re-measure and
     * re-layout during each animation frame.
     *
     * <p>This flag is disabled by default.
     *
     * @param animateMeasurementChange whether or not to animate measurement changes.
     */
    public void setAnimateMeasurementChange(boolean animateMeasurementChange) {
        this.animateMeasurementChange = animateMeasurementChange;
    }

    /**
     * @return whether or not we are currently animating measurement changes.
     */
    public boolean getAnimateMeasurementChange() {
        return animateMeasurementChange;
    }

    /**
     * Adds a custom {@link android.animation.Animator.AnimatorListener} to listen to animator
     * update events used by this view.
     *
     * @param animatorListener the custom animator listener.
     */
    public void addAnimatorListener(Animator.AnimatorListener animatorListener) {
        animator.addListener(animatorListener);
    }

    /**
     * Removes the specified custom {@link android.animation.Animator.AnimatorListener} from
     * this view.
     *
     * @param animatorListener the custom animator listener.
     */
    public void removeAnimatorListener(Animator.AnimatorListener animatorListener) {
        animator.removeListener(animatorListener);
    }

    /**
     * Configures the textpaint used for drawing individual ticker characters.
     * See {@link Paint#setFlags(int)} for more information.
     *
     * @param flags the new flag bits for the paint
     */
    public void setPaintFlags(int flags) {
        textPaint.setFlags(flags);
        onTextPaintMeasurementChanged();
    }

    /**
     * Exposing method to add or remove blur mask filter to ticker text.
     *
     * @param style Blur mask filter type
     * @param radius Density of filter
     */
    public void setBlurMaskFilter(BlurMaskFilter.Blur style, float radius) {
        if (style != null && radius > 0f) {
            BlurMaskFilter filter = new BlurMaskFilter(radius, style);
            textPaint.setMaskFilter(filter);
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            textPaint.setMaskFilter(null);
        }
    }


    /********** END PUBLIC API **********/


    /**
     * Force the view to call {@link #requestLayout()} if the new text doesn't match the old bounds
     * we set for the previous view state.
     */
    private void checkForRelayout() {
        final boolean widthChanged = lastMeasuredDesiredWidth != computeDesiredWidth();
        final boolean heightChanged = lastMeasuredDesiredHeight != computeDesiredHeight();

        if (widthChanged || heightChanged) {
            requestLayout();
        }
    }

    private int computeDesiredWidth() {
        final int contentWidth = (int) (animateMeasurementChange ?
                columnManager.getCurrentWidth() : columnManager.getMinimumRequiredWidth());
        return contentWidth + getPaddingLeft() + getPaddingRight();
    }

    private int computeDesiredHeight() {
        return (int) metrics.getCharHeight() + getPaddingTop() + getPaddingBottom();
    }

    /**
     * Re-initialize all of our variables that are dependent on the TextPaint measurements.
     */
    private void onTextPaintMeasurementChanged() {
        metrics.invalidate();
        checkForRelayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        lastMeasuredDesiredWidth = computeDesiredWidth();
        lastMeasuredDesiredHeight = computeDesiredHeight();

        int desiredWidth = resolveSize(lastMeasuredDesiredWidth, widthMeasureSpec);
        int desiredHeight = resolveSize(lastMeasuredDesiredHeight, heightMeasureSpec);

        setMeasuredDimension(desiredWidth, desiredHeight);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        viewBounds.set(getPaddingLeft(), getPaddingTop(), width - getPaddingRight(),
                height - getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        realignAndClipCanvasForGravity(canvas);

        // canvas.drawText writes the text on the baseline so we need to translate beforehand.
        canvas.translate(0f, metrics.getCharBaseline());

        columnManager.draw(canvas, textPaint);

        canvas.restore();
    }

    private void realignAndClipCanvasForGravity(Canvas canvas) {
        final float currentWidth = columnManager.getCurrentWidth();
        final float currentHeight = metrics.getCharHeight();
        realignAndClipCanvasForGravity(canvas, gravity, viewBounds, currentWidth, currentHeight);
    }

    // VisibleForTesting
    static void realignAndClipCanvasForGravity(Canvas canvas, int gravity, Rect viewBounds,
            float currentWidth, float currentHeight) {
        final int availableWidth = viewBounds.width();
        final int availableHeight = viewBounds.height();

        float translationX = 0;
        float translationY = 0;
        if ((gravity & Gravity.CENTER_VERTICAL) == Gravity.CENTER_VERTICAL) {
            translationY = viewBounds.top + (availableHeight - currentHeight) / 2f;
        }
        if ((gravity & Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL) {
            translationX = viewBounds.left + (availableWidth - currentWidth) / 2f;
        }
        if ((gravity & Gravity.TOP) == Gravity.TOP) {
            translationY = 0;
        }
        if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            translationY = viewBounds.top + (availableHeight - currentHeight);
        }
        if ((gravity & Gravity.START) == Gravity.START) {
            translationX = 0;
        }
        if ((gravity & Gravity.END) == Gravity.END) {
            translationX = viewBounds.left + (availableWidth - currentWidth);
        }

        canvas.translate(translationX ,translationY);
        canvas.clipRect(0f, 0f, currentWidth, currentHeight);
    }
}

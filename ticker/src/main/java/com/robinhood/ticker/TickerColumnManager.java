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

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * In ticker, each character in the rendered text is represented by a {@link TickerColumn}. The
 * column can be seen as a column of text in which we can animate from one character to the next
 * by scrolling the column vertically. The {@link TickerColumnManager} is then a
 * manager/convenience class for handling a list of {@link TickerColumn} which then combines into
 * the entire string we are rendering.
 *
 * @author Jin Cao, Robinhood
 */
class TickerColumnManager {
    final ArrayList<TickerColumn> tickerColumns = new ArrayList<>();
    private final TickerDrawMetrics metrics;

    // The character list that dictates how to transition from one character to another.
    private char[] characterList;
    // A minor optimization so that we can cache the indices of each character.
    private Map<Character, Integer> characterIndicesMap;

    TickerColumnManager(TickerDrawMetrics metrics) {
        this.metrics = metrics;
    }

    /**
     * @see {@link TickerView#setCharacterList(char[])}.
     */
    void setCharacterList(char[] characterList) {
        this.characterList = characterList;
        this.characterIndicesMap = new HashMap<>(characterList.length);

        for (int i = 0; i < characterList.length; i++) {
            characterIndicesMap.put(characterList[i], i);
        }
    }

    /**
     * @return whether or not {@param text} should be debounced because it's the same as the
     *         current target text of this column manager.
     */
    boolean shouldDebounceText(char[] text) {
        final int newTextSize = text.length;
        if (newTextSize == tickerColumns.size()) {
            for (int i = 0; i < newTextSize; i++) {
                if (text[i] != tickerColumns.get(i).getTargetChar()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Tell the column manager the new target text that it should display.
     */
    boolean setText(char[] text, boolean animate) {
        if (characterList == null) {
            throw new IllegalStateException("Need to call setCharacterList(char[]) first.");
        }

        final int newTextSize = text.length;
        if (animate) {
            // If we are animating, we don't want to remove old columns yet.
            insertColumnsUpTo(newTextSize);
        } else {
            ensureColumnSize(newTextSize);
        }

        final int columnsSize = tickerColumns.size();
        for (int i = 0; i < columnsSize; i++) {
            tickerColumns.get(i).setTargetChar(i < newTextSize ? text[i] : TickerUtils.EMPTY_CHAR);
        }

        return true;
    }

    void onAnimationEnd() {
        for (int i = 0, size = tickerColumns.size(); i < size; i++) {
            final TickerColumn column = tickerColumns.get(i);
            column.onAnimationEnd();
        }
    }

    void setAnimationProgress(float animationProgress) {
        for (int i = 0, size = tickerColumns.size(); i < size; i++) {
            final TickerColumn column = tickerColumns.get(i);
            column.setAnimationProgress(animationProgress);
        }
    }

    float getMinimumRequiredWidth() {
        float width = 0f;
        for (int i = 0, size = tickerColumns.size(); i < size; i++) {
            width += tickerColumns.get(i).getMinimumRequiredWidth();
        }
        return width;
    }

    float getCurrentWidth() {
        float width = 0f;
        for (int i = 0, size = tickerColumns.size(); i < size; i++) {
            width += tickerColumns.get(i).getCurrentWidth();
        }
        return width;
    }

    /**
     * This method will draw onto the canvas the appropriate UI state of each column dictated
     * by {@param animationProgress}. As a side effect, this method will also translate the canvas
     * accordingly for the draw procedures.
     */
    void draw(Canvas canvas, Paint textPaint) {
        for (int i = 0, size = tickerColumns.size(); i < size; i++) {
            final TickerColumn column = tickerColumns.get(i);
            column.draw(canvas, textPaint);
            canvas.translate(column.getCurrentWidth(), 0f);
        }
    }

    /**
     * Ensure that the number of columns matches {@param targetSize}.
     */
    void ensureColumnSize(int targetSize) {
        final int columnSize = tickerColumns.size();
        if (targetSize > columnSize) {
            insertColumnsUpTo(targetSize);
        } else {
            for (int i = 0; i < columnSize - targetSize; i++) {
                tickerColumns.remove(tickerColumns.size() - 1);
            }
        }
    }

    /**
     * Insert (if applicable) columns until the number of columns match {@param targetSize}.
     */
    void insertColumnsUpTo(int targetSize) {
        final int currentSize = tickerColumns.size();
        if (targetSize > currentSize) {
            final int toInsert = targetSize - currentSize;
            for (int i = 0; i < toInsert; i++) {
                tickerColumns.add(new TickerColumn(characterList, characterIndicesMap, metrics));
            }
        }
    }
}

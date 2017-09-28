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
     * Tell the column manager the new target text that it should display.
     */
    void setText(char[] text) {
        if (characterList == null) {
            throw new IllegalStateException("Need to call setCharacterList(char[]) first.");
        }

        // First remove any zero-width columns
        for (int i = 0; i < tickerColumns.size(); ) {
            final TickerColumn tickerColumn = tickerColumns.get(i);
            if (tickerColumn.getCurrentWidth() > 0) {
                i++;
            } else {
                tickerColumns.remove(i);
            }
        }

        // Use Levenshtein distance algorithm to figure out how to manipulate the columns
        final int[] actions = LevenshteinUtils.computeColumnActions(getCurrentText(), text);
        int columnIndex = 0;
        int textIndex = 0;
        for (int i = 0; i < actions.length; i++) {
            switch (actions[i]) {
                case LevenshteinUtils.ACTION_INSERT:
                    tickerColumns.add(columnIndex,
                            new TickerColumn(characterList, characterIndicesMap, metrics));
                case LevenshteinUtils.ACTION_SAME:
                    tickerColumns.get(columnIndex).setTargetChar(text[textIndex]);
                    columnIndex++;
                    textIndex++;
                    break;
                case LevenshteinUtils.ACTION_DELETE:
                    tickerColumns.get(columnIndex).setTargetChar(TickerUtils.EMPTY_CHAR);
                    columnIndex++;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action: " + actions[i]);
            }
        }
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

    char[] getCurrentText() {
        final int size = tickerColumns.size();
        final char[] currentText = new char[size];
        for (int i = 0; i < size; i++) {
            currentText[i] = tickerColumns.get(i).getCurrentChar();
        }
        return currentText;
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
}

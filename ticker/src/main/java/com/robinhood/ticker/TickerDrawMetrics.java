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

import android.graphics.Paint;

import java.util.HashMap;
import java.util.Map;

/**
 * This wrapper class represents some core drawing metrics that {@link TickerView} and
 * {@link TickerColumnManager} require to calculate the positions and offsets for rendering
 * the text onto the canvas.
 *
 * @author Jin Cao
 */
class TickerDrawMetrics {
    private final Paint textPaint;

    // These are attributes on the text paint used for measuring and drawing the text on the
    // canvas. These attributes are reset whenever anything on the text paint changes.
    private final Map<Character, Float> charWidths = new HashMap<>(256);
    private float charHeight, charBaseline;

    TickerDrawMetrics(Paint textPaint) {
        this.textPaint = textPaint;
        invalidate();
    }

    void invalidate() {
        charWidths.clear();
        final Paint.FontMetrics fm = textPaint.getFontMetrics();
        charHeight = fm.bottom - fm.top;
        charBaseline = -fm.top;
    }

    float getCharWidth(char character) {
        if (character == TickerUtils.EMPTY_CHAR) {
            return 0;
        }

        // This method will lazily initialize the char width map.
        final Float value = charWidths.get(character);
        if (value != null) {
            return value;
        } else {
            final float width = textPaint.measureText(Character.toString(character));
            charWidths.put(character, width);
            return width;
        }
    }

    float getCharHeight() {
        return charHeight;
    }

    float getCharBaseline() {
        return charBaseline;
    }
}

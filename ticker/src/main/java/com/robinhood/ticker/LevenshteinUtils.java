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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Helper class to compute the Levenshtein distance between two strings.
 * https://en.wikipedia.org/wiki/Levenshtein_distance
 */
public class LevenshteinUtils {
    static final int ACTION_SAME = 0;
    static final int ACTION_INSERT = 1;
    static final int ACTION_DELETE = 2;

    /**
     * This is a wrapper function around {@link #appendColumnActionsForSegment} that
     * additionally takes in supportedCharacters. It uses supportedCharacters to compute whether
     * the current character should be animated or if it should remain in-place.
     *
     * For specific implementation details, see {@link #appendColumnActionsForSegment}.
     *
     * @param source the source char array to animate from
     * @param target the target char array to animate to
     * @param supportedCharacters all characters that support custom animation.
     * @return an int array of size min(source.length, target.length) where each index
     *         corresponds to one of {@link #ACTION_SAME}, {@link #ACTION_INSERT},
     *         {@link #ACTION_DELETE} to represent if we update, insert, or delete a character
     *         at the particular index.
     */
    public static int[] computeColumnActions(char[] source, char[] target,
            Set<Character> supportedCharacters) {
        int sourceIndex = 0;
        int targetIndex = 0;

        List<Integer> columnActions = new ArrayList<>();
        while (true) {
            // Check for terminating conditions
            final boolean reachedEndOfSource = sourceIndex == source.length;
            final boolean reachedEndOfTarget = targetIndex == target.length;
            if (reachedEndOfSource && reachedEndOfTarget) {
                break;
            } else if (reachedEndOfSource) {
                fillWithActions(columnActions, target.length - targetIndex, ACTION_INSERT);
                break;
            } else if (reachedEndOfTarget) {
                fillWithActions(columnActions, source.length - sourceIndex, ACTION_DELETE);
                break;
            }

            final boolean containsSourceChar = supportedCharacters.contains(source[sourceIndex]);
            final boolean containsTargetChar = supportedCharacters.contains(target[targetIndex]);

            if (containsSourceChar && containsTargetChar) {
                // We reached a segment that we can perform animations on
                final int sourceEndIndex =
                        findNextUnsupportedChar(source, sourceIndex + 1, supportedCharacters);
                final int targetEndIndex =
                        findNextUnsupportedChar(target, targetIndex + 1, supportedCharacters);

                appendColumnActionsForSegment(
                        columnActions,
                        source,
                        target,
                        sourceIndex,
                        sourceEndIndex,
                        targetIndex,
                        targetEndIndex
                );
                sourceIndex = sourceEndIndex;
                targetIndex = targetEndIndex;
            } else if (containsSourceChar) {
                // We are animating in a target character that isn't supported
                columnActions.add(ACTION_INSERT);
                targetIndex++;
            } else if (containsTargetChar) {
                // We are animating out a source character that isn't supported
                columnActions.add(ACTION_DELETE);
                sourceIndex++;
            } else {
                // Both characters are not supported, perform default animation to replace
                columnActions.add(ACTION_SAME);
                sourceIndex++;
                targetIndex++;
            }
        }

        // Concat all of the actions into one array
        final int[] result = new int[columnActions.size()];
        for (int i = 0; i < columnActions.size(); i++) {
            result[i] = columnActions.get(i);
        }
        return result;
    }

    private static int findNextUnsupportedChar(char[] chars, int startIndex,
            Set<Character> supportedCharacters) {
        for (int i = startIndex; i < chars.length; i++) {
            if (!supportedCharacters.contains(chars[i])) {
                return i;
            }
        }
        return chars.length;
    }

    private static void fillWithActions(List<Integer> actions, int num, int action) {
        for (int i = 0; i < num; i++) {
            actions.add(action);
        }
    }

    /**
     * Run a slightly modified version of Levenshtein distance algorithm to compute the minimum
     * edit distance between the current and the target text within the start and end bounds.
     * Unlike the traditional algorithm, we force return all {@link #ACTION_SAME} for inputs that
     * are the same length (so optimize update over insertion/deletion).
     *
     * @param columnActions the target list to append actions into
     * @param source the source character array
     * @param target the target character array
     * @param sourceStart the start index of source to compute column actions (inclusive)
     * @param sourceEnd the end index of source to compute column actions (exclusive)
     * @param targetStart the start index of target to compute column actions (inclusive)
     * @param targetEnd the end index of target to compute column actions (exclusive)
     */
    private static void appendColumnActionsForSegment(
            List<Integer> columnActions,
            char[] source,
            char[] target,
            int sourceStart,
            int sourceEnd,
            int targetStart,
            int targetEnd
    ) {
        final int sourceLength = sourceEnd - sourceStart;
        final int targetLength = targetEnd - targetStart;
        final int resultLength = Math.max(sourceLength, targetLength);

        if (sourceLength == targetLength) {
            // No modifications needed if the length of the strings are the same
            fillWithActions(columnActions, resultLength, ACTION_SAME);
            return;
        }

        final int numRows = sourceLength + 1;
        final int numCols = targetLength + 1;

        // Compute the Levenshtein matrix
        final int[][] matrix = new int[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            matrix[i][0] = i;
        }
        for (int j = 0; j < numCols; j++) {
            matrix[0][j] = j;
        }

        int cost;
        for (int row = 1; row < numRows; row++) {
            for (int col = 1; col < numCols; col++) {
                cost = source[row - 1 + sourceStart] == target[col - 1 + targetStart] ? 0 : 1;

                matrix[row][col] = min(
                        matrix[row-1][col] + 1,
                        matrix[row][col-1] + 1,
                        matrix[row-1][col-1] + cost);
            }
        }

        // Reverse trace the matrix to compute the necessary actions
        final List<Integer> resultList = new ArrayList<>(resultLength * 2);
        int row = numRows - 1;
        int col = numCols - 1;
        while (row > 0 || col > 0) {
            if (row == 0) {
                // At the top row, can only move left, meaning insert column
                resultList.add(ACTION_INSERT);
                col--;
            } else if (col == 0) {
                // At the left column, can only move up, meaning delete column
                resultList.add(ACTION_DELETE);
                row--;
            } else {
                final int insert = matrix[row][col-1];
                final int delete = matrix[row-1][col];
                final int replace = matrix[row-1][col-1];

                if (insert < delete && insert < replace) {
                    resultList.add(ACTION_INSERT);
                    col--;
                } else if (delete < replace) {
                    resultList.add(ACTION_DELETE);
                    row--;
                } else {
                    resultList.add(ACTION_SAME);
                    row--;
                    col--;
                }
            }
        }

        // Reverse the actions to get the correct ordering
        final int resultSize = resultList.size();
        for (int i = resultSize - 1; i >= 0; i--) {
            columnActions.add(resultList.get(i));
        }
    }

    private static int min(int first, int second, int third) {
        return Math.min(first, Math.min(second, third));
    }
}

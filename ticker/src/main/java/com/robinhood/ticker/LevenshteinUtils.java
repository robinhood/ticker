package com.robinhood.ticker;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to compute the Levenshtein distance between two strings.
 * https://en.wikipedia.org/wiki/Levenshtein_distance
 */
public class LevenshteinUtils {
    static final int ACTION_SAME = 0;
    static final int ACTION_INSERT = 1;
    static final int ACTION_DELETE = 2;

    /**
     * Run a slightly modified version of Levenshtein distance algorithm to compute the minimum
     * edit distance between the current and the target text. Unlike the traditional algorithm,
     * we force return all {@link #ACTION_SAME} for inputs that are the same length (so optimize
     * update over insertion/deletion).
     *
     * @param source the source character array
     * @param target the target character array
     * @return an int array of size min(source.length, target.length) where each index
     *         corresponds to one of {@link #ACTION_SAME}, {@link #ACTION_INSERT},
     *         {@link #ACTION_DELETE} to represent if we update, insert, or delete a character
     *         at the particular index.
     */
    public static int[] computeColumnActions(char[] source, char[] target) {
        final int sourceLength = source.length;
        final int targetLength = target.length;
        final int resultLength = Math.max(sourceLength, targetLength);

        if (sourceLength == targetLength) {
            // No modifications needed if the length of the strings are the same
            return new int[resultLength];
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
        for (int j = 1; j < numCols; j++) {
            for (int i = 1; i < numRows; i++) {
                cost = source[i-1] == target[j-1] ? 0 : 1;

                matrix[i][j] = min(
                        matrix[i-1][j] + 1,
                        matrix[i][j-1] + 1,
                        matrix[i-1][j-1] + cost);
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

        final int resultSize = resultList.size();
        final int[] result = new int[resultSize];
        for (int i = 0; i < resultSize; i++) {
            result[resultSize - 1 - i] = resultList.get(i);
        }
        return result;
    }

    private static int min(int first, int second, int third) {
        return Math.min(first, Math.min(second, third));
    }
}

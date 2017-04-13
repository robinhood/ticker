package com.robinhood.ticker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LevenshteinUtilsTest {
    @Test
    public void test_insert1() {
        runTest("1111", "11211", "00100");
    }

    @Test
    public void test_insert2() {
        runTest("123", "0213", "0010");
    }

    @Test
    public void test_delete() {
        runTest("11211", "1111", "00200");
    }

    @Test
    public void test_equal() {
        runTest("1234", "1234", "0000");
    }

    @Test
    public void test_completelyDifferent() {
        runTest("1234", "5678", "0000");
    }

    @Test
    public void test_shift() {
        // The reason why this isn't 20001 which is a shift of "234" over is because that
        // would require 5 changes rather than 4.
        runTest("1234", "2345", "0000");
    }

    @Test
    public void test_mix1() {
        // "15" should shift to the right 1 place, then delete two columns after "15"
        runTest("15233", "9151", "100220");
    }

    @Test
    public void test_mix2() {
        runTest("12345", "230", "20020");
    }

    @Test
    public void test_currency1() {
        runTest("$123.99", "$1223.98", "00010000");
    }

    private void runTest(String source, String target, String actions) {
        final int[] result = LevenshteinUtils.computeColumnActions(
                source.toCharArray(), target.toCharArray());

        final StringBuilder resultActions = new StringBuilder(result.length);
        for (int resultChar : result) {
            resultActions.append(Integer.toString(resultChar));
        }

        assertEquals(actions, resultActions.toString());
    }
}

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
        runTest("1234", "2345", "0000");
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

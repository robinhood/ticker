package com.robinhood.ticker.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.robinhood.ticker.TickerView;

import java.util.Random;

public class MainActivity extends BaseActivity {
    private final String alphabetlist = "abcdefghijklmnopqrstuvwxyz";

    private TickerView ticker1, ticker2, ticker3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ticker1 = findViewById(R.id.ticker1);
        ticker2 = findViewById(R.id.ticker2);
        ticker3 = findViewById(R.id.ticker3);

        ticker1.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);
        ticker2.setPreferredScrollingDirection(TickerView.ScrollingDirection.UP);
        ticker3.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);

        findViewById(R.id.perfBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PerfActivity.class));
            }
        });
    }

    @Override
    protected void onUpdate() {
        final int digits = RANDOM.nextInt(2) + 6;

        ticker1.setText(getRandomNumber(digits));
        final String currencyFloat = Float.toString(RANDOM.nextFloat() * 100);
        ticker2.setText("$" + currencyFloat.substring(0, Math.min(digits, currencyFloat.length())));
        ticker3.setText(generateChars(RANDOM, alphabetlist, digits));
    }

    private String generateChars(Random random, String list, int numDigits) {
        final char[] result = new char[numDigits];
        for (int i = 0; i < numDigits; i++) {
            result[i] = list.charAt(random.nextInt(list.length()));
        }
        return new String(result);
    }
}

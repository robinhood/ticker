package com.robinhood.ticker.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import java.util.ArrayList;
import java.util.List;

public class PerfActivity extends BaseActivity {
    private static final char[] CHAR_LIST = TickerUtils.getDefaultNumberList();

    private final List<TickerViewHolder> boundViewHolders = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perf);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new TestAdapter());
    }

    @Override
    protected void onUpdate() {
        for (TickerViewHolder viewHolder : boundViewHolders) {
            viewHolder.update(true);
        }
    }

    private class TestAdapter extends RecyclerView.Adapter<TickerViewHolder> {
        private LayoutInflater inflater;

        @Override
        public TickerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent.getContext());
            }

            return new TickerViewHolder(inflater.inflate(R.layout.row_perf, parent, false));
        }

        @Override
        public void onBindViewHolder(TickerViewHolder holder, int position) {
            boundViewHolders.add(holder);
            holder.update(false);
        }

        @Override
        public int getItemCount() {
            return 1000;
        }
    }

    private class TickerViewHolder extends RecyclerView.ViewHolder {
        private TickerView ticker1, ticker2, ticker3, ticker4;

        TickerViewHolder(View itemView) {
            super(itemView);
            ticker1 = itemView.findViewById(R.id.ticker1);
            ticker2 = itemView.findViewById(R.id.ticker2);
            ticker3 = itemView.findViewById(R.id.ticker3);
            ticker4 = itemView.findViewById(R.id.ticker4);

            ticker1.setCharacterList(CHAR_LIST);
            ticker2.setCharacterList(CHAR_LIST);
            ticker3.setCharacterList(CHAR_LIST);
            ticker4.setCharacterList(CHAR_LIST);
        }

        void update(boolean animate) {
            ticker1.setText(getRandomNumber(8), animate);
            ticker2.setText(getRandomNumber(8), animate);
            ticker3.setText(getRandomNumber(8), animate);
            ticker4.setText(getRandomNumber(8), animate);
        }
    }
}

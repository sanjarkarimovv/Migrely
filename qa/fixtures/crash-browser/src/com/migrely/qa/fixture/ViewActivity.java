package com.migrely.qa.fixture;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * QA fixture (T83, #280): a browser-like handler for https and market:// links and a text/plain share target. In the "crash" variant it throws in
 * onCreate, so the crash lands while the starting app is still PAUSED. In the "ok" variant it shows the link.
 */
public class ViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String data = getIntent().getAction() + " " + getIntent().getData()
                + (getIntent().hasExtra("android.intent.extra.TEXT") ? " text=yes" : "");
        Log.i("QAFIX", "onCreate variant=" + Variant.NAME + " task=" + getTaskId() + " data=" + data
                + " flags=0x" + Integer.toHexString(getIntent().getFlags()));
        if (Variant.CRASH) {
            throw new IllegalStateException("QAFIX crash at launch (intended)");
        }
        TextView text = new TextView(this);
        text.setText("QA fixture page\n" + data);
        text.setTextSize(22f);
        text.setPadding(48, 200, 48, 48);
        setContentView(text);
    }
}

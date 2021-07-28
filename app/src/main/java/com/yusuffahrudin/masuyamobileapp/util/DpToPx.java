package com.yusuffahrudin.masuyamobileapp.util;

import android.content.Context;

public class DpToPx {
    private Context context;

    public DpToPx(Context context) {
        this.context = context;
    }

    public int dpToPx(int dp) {
        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
}

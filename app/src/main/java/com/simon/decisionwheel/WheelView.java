package com.simon.decisionwheel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class WheelView extends View {
    private Paint slicePaint;
    private Paint textPaint;
    private RectF rectF;
    private String[] options = new String[]{"Pizza", "Burgers", "Sushi", "Tacos"};
    private int[] colors = {Color.parseColor("#FF5252"), Color.parseColor("#448AFF"),
            Color.parseColor("#69F0AE"), Color.parseColor("#FFAB40"),
            Color.parseColor("#E040FB"), Color.parseColor("#FF4081"),
            Color.parseColor("#00BCD4"), Color.parseColor("#FFC107"),
            Color.parseColor("#8BC34A"), Color.parseColor("#9C27B0")}; // 10 Colors
    private float currentRotation = 0f;

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slicePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        rectF = new RectF();
    }

    public void setOptions(String[] newOptions) {
        this.options = newOptions;
        // Auto-adjust text size based on number of options
        if (options.length > 6) {
            textPaint.setTextSize(35f);
        } else {
            textPaint.setTextSize(50f);
        }
        invalidate();
    }

    public void setRotationAngle(float angle) {
        this.currentRotation = angle;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (options == null || options.length == 0) return;

        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2f - 20f;

        canvas.save();
        canvas.translate(width / 2f, height / 2f);
        canvas.rotate(currentRotation);

        rectF.set(-radius, -radius, radius, radius);
        float sweepAngle = 360f / options.length;

        for (int i = 0; i < options.length; i++) {
            slicePaint.setColor(colors[i % colors.length]);
            canvas.drawArc(rectF, i * sweepAngle, sweepAngle, true, slicePaint);

            canvas.save();
            canvas.rotate((i * sweepAngle) + (sweepAngle / 2f));
            canvas.drawText(options[i], 0, -radius / 2f, textPaint);
            canvas.restore();
        }
        canvas.restore();
    }
}
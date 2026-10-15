package com.example.homnayangi.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.example.homnayangi.repository.FoodRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RouletteWheelView extends View {

    private List<FoodRepository.RouletteItem> items = new ArrayList<>();
    private Paint paintPie, paintText, paintBorder, paintCenter;
    private RectF rectF;
    private int[] colors = {
            0xFFFF6B35, 0xFF4CAF50, 0xFF03A9F4, 0xFFE91E63,
            0xFF9C27B0, 0xFFFFC107, 0xFF00BCD4, 0xFF8BC34A,
            0xFFFF9800, 0xFF607D8B, 0xFFCDDC39, 0xFF795548
    };
    private float currentRotation = 0f;
    private boolean soundEnabled = true;
    private ToneGenerator toneGenerator;

    public interface OnSpinEndListener {
        void onSpinEnd(FoodRepository.RouletteItem selectedItem);
    }

    public RouletteWheelView(Context context) {
        super(context);
        init();
    }

    public RouletteWheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintPie = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPie.setStyle(Paint.Style.FILL);

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setColor(Color.WHITE);
        paintBorder.setStrokeWidth(3f);

        paintCenter = new Paint(Paint.ANTI_ALIAS_FLAG);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.WHITE);
        paintText.setFakeBoldText(true);
        paintText.setShadowLayer(5f, 1.5f, 1.5f, Color.argb(180, 0, 0, 0));

        rectF = new RectF();
    }

    public void setItems(List<FoodRepository.RouletteItem> newItems) {
        this.items = newItems;
        invalidate(); // Vẽ lại
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int minSize = Math.min(w, h);
        int padding = 16;
        rectF.set(padding, padding, minSize - padding, minSize - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (items.isEmpty()) {
            // Vẽ vòng tròn trống
            paintPie.setColor(0xFFE0E0E0);
            canvas.drawArc(rectF, 0, 360, true, paintPie);

            // Vẽ viền trang trí
            paintBorder.setColor(0xFFBDBDBD);
            paintBorder.setStrokeWidth(4f);
            canvas.drawArc(rectF, 0, 360, false, paintBorder);

            float x = rectF.centerX();
            float y = rectF.centerY();
            float density = getResources().getDisplayMetrics().scaledDensity;
            paintText.setTextAlign(Paint.Align.CENTER);
            paintText.setColor(0xFF757575);
            paintText.setTextSize(16f * density);
            canvas.drawText("Chưa có món", x, y - (6f * density), paintText);
            paintText.setTextSize(13f * density);
            canvas.drawText("Thêm món ở tab Món ăn", x, y + (18f * density), paintText);
            paintText.setColor(Color.WHITE);
            return;
        }

        int count = items.size();
        float sweepAngle = 360f / count;
        float startAngle = -90f; // Bắt đầu từ đỉnh

        // Cỡ chữ theo sp tính dựa trên số lượng món để đảm bảo luôn to rõ
        float baseTextSizeSp;
        if (count <= 2) {
            baseTextSizeSp = 18f;
        } else if (count <= 4) {
            baseTextSizeSp = 16f;
        } else if (count <= 8) {
            baseTextSizeSp = 14f;
        } else {
            baseTextSizeSp = 12f;
        }
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        float baseTextSizePx = baseTextSizeSp * scaledDensity;

        float centerX = rectF.centerX();
        float centerY = rectF.centerY();
        float radius = rectF.width() / 2f;
        // Đặt vị trí chữ ở khoảng 58% bán kính (nằm ngay giữa dải quạt)
        float textRadius = radius * 0.58f;

        for (int i = 0; i < count; i++) {
            // Vẽ mảnh bánh
            paintPie.setColor(colors[i % colors.length]);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paintPie);

            // Vẽ viền trắng giữa các mảnh
            paintBorder.setColor(Color.WHITE);
            paintBorder.setStrokeWidth(2f);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paintBorder);

            // Xử lý text
            String text = items.get(i).name;
            if (text == null) text = "";
            text = text.trim();
            // Nếu quá dài mới cắt bớt
            if (text.length() > 16) {
                text = text.substring(0, 14) + "...";
            }

            // Tự điều chỉnh cỡ chữ nếu tên dài hơn 10 ký tự
            float itemTextSize = baseTextSizePx;
            if (text.length() > 11) {
                itemTextSize = baseTextSizePx * 0.85f;
            }
            paintText.setTextSize(itemTextSize);

            // Góc chính giữa của mảnh quạt hiện tại
            float midAngle = startAngle + sweepAngle / 2f;
            float normalizedAngle = (midAngle % 360f + 360f) % 360f;

            canvas.save();
            canvas.translate(centerX, centerY);

            // Nếu góc nằm ở nửa bên trái (90 -> 270 độ), lật 180 độ để chữ không bị lộn ngược đầu
            boolean flip = (normalizedAngle > 90f && normalizedAngle < 270f);
            if (flip) {
                canvas.rotate(midAngle + 180f);
                paintText.setTextAlign(Paint.Align.CENTER);
                float textY = (paintText.descent() + paintText.ascent()) / 2f;
                canvas.drawText(text, -textRadius, -textY, paintText);
            } else {
                canvas.rotate(midAngle);
                paintText.setTextAlign(Paint.Align.CENTER);
                float textY = (paintText.descent() + paintText.ascent()) / 2f;
                canvas.drawText(text, textRadius, -textY, paintText);
            }

            canvas.restore();
            startAngle += sweepAngle;
        }

        // Vẽ viền ngoài
        paintBorder.setColor(0xFFFF6B35);
        paintBorder.setStrokeWidth(6f);
        canvas.drawArc(rectF, 0, 360, false, paintBorder);

        // Vẽ tâm
        paintCenter.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, 20, paintCenter);
        paintCenter.setColor(0xFFFF6B35);
        canvas.drawCircle(centerX, centerY, 14, paintCenter);
    }

    public void spin(OnSpinEndListener listener) {
        if (items.isEmpty()) return;

        Random random = new Random();
        int selectedIndex = random.nextInt(items.size());

        int count = items.size();
        float sweepAngle = 360f / count;

        // Tâm của mảnh được chọn
        float targetAngleInView = (selectedIndex * sweepAngle) + (sweepAngle / 2);

        // Quay để target nằm ở đỉnh
        float finalRotationOffset = 360f - targetAngleInView;

        // Thêm nhiều vòng quay hơn (7 vòng) cho kịch tính
        float spins = 360f * 7;
        float finalRotation = currentRotation + spins + finalRotationOffset - (currentRotation % 360);

        // Phát âm thanh tick-tick khi quay
        if (soundEnabled) {
            playSpinSound(4000);
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, View.ROTATION, currentRotation, finalRotation);
        animator.setDuration(4500); // Lâu hơn một chút
        animator.setInterpolator(new DecelerateInterpolator(1.5f));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentRotation = finalRotation % 360;
                setRotation(currentRotation);

                // Phát âm thanh kết quả
                if (soundEnabled) {
                    playResultSound();
                }

                if (listener != null) {
                    listener.onSpinEnd(items.get(selectedIndex));
                }
            }
        });
        animator.start();
    }

    // 📌 [GHI CHÚ]: Tái sử dụng một instance ToneGenerator duy nhất cho toàn bộ lượt quay
    // Tránh việc 'new ToneGenerator' liên tục trong mỗi nhịp tick làm cạn kiệt tài nguyên AudioTrack native của hệ thống
    private ToneGenerator getToneGenerator() {
        if (toneGenerator == null) {
            try {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60);
            } catch (Exception ignored) {}
        }
        return toneGenerator;
    }

    // 📌 [GHI CHÚ]: Giải phóng tài nguyên ToneGenerator khi View bị detach khỏi màn hình (tránh memory leak)
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (toneGenerator != null) {
            try {
                toneGenerator.release();
            } catch (Exception ignored) {}
            toneGenerator = null;
        }
    }

    /** Phát âm thanh tick-tick trong quá trình quay */
    private void playSpinSound(int durationMs) {
        Handler handler = new Handler(Looper.getMainLooper());
        final int[] tickCount = {0};
        final int maxTicks = 30;

        Runnable tickRunnable = new Runnable() {
            @Override
            public void run() {
                if (tickCount[0] >= maxTicks) return;
                try {
                    ToneGenerator tg = getToneGenerator();
                    if (tg != null) {
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP, 30);
                    }
                } catch (Exception ignored) {}

                tickCount[0]++;
                // Tăng dần khoảng cách giữa các tick (chậm dần)
                int delay = 60 + (tickCount[0] * tickCount[0]);
                if (delay > 600) delay = 600;
                handler.postDelayed(this, delay);
            }
        };
        handler.post(tickRunnable);
    }

    /** Phát âm thanh khi có kết quả */
    private void playResultSound() {
        try {
            ToneGenerator tg = getToneGenerator();
            if (tg != null) {
                tg.startTone(ToneGenerator.TONE_PROP_ACK, 200);
            }
        } catch (Exception ignored) {}
    }
}

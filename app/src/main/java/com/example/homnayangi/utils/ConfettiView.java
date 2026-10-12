package com.example.homnayangi.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * View hiệu ứng confetti (hoa giấy) hiển thị khi có kết quả vòng quay.
 * Tự động phát và tự tắt sau khi animation kết thúc.
 */
public class ConfettiView extends View {

    private static final int CONFETTI_COUNT = 80;
    private final List<Confetti> confettiList = new ArrayList<>();
    private final Random random = new Random();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator animator;

    private final int[] COLORS = {
            0xFFFF6B35, 0xFF4CAF50, 0xFF03A9F4, 0xFFE91E63,
            0xFF9C27B0, 0xFFFFC107, 0xFFFF5722, 0xFF00BCD4,
            0xFFFFEB3B, 0xFF8BC34A
    };

    public ConfettiView(Context context) {
        super(context);
        setVisibility(GONE);
    }

    public ConfettiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setVisibility(GONE);
    }

    /** Khởi chạy animation confetti */
    public void fire() {
        if (getWidth() == 0 || getHeight() == 0) return;

        confettiList.clear();
        for (int i = 0; i < CONFETTI_COUNT; i++) {
            confettiList.add(new Confetti(getWidth(), getHeight()));
        }

        setVisibility(VISIBLE);

        if (animator != null && animator.isRunning()) animator.cancel();

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(2500);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(anim -> {
            float progress = (float) anim.getAnimatedValue();
            for (Confetti c : confettiList) {
                c.update(progress);
            }
            invalidate();
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                setVisibility(GONE);
                confettiList.clear();
            }
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Confetti c : confettiList) {
            paint.setColor(c.color);
            paint.setAlpha(c.alpha);
            canvas.save();
            canvas.rotate(c.rotation, c.x, c.y);
            canvas.drawRect(c.x - c.width / 2f, c.y - c.height / 2f,
                    c.x + c.width / 2f, c.y + c.height / 2f, paint);
            canvas.restore();
        }
    }

    private class Confetti {
        float x, y, startX, startY;
        float speedX, speedY;
        float width, height;
        float rotation, rotationSpeed;
        int color;
        int alpha = 255;

        Confetti(int viewWidth, int viewHeight) {
            startX = random.nextFloat() * viewWidth;
            startY = -random.nextFloat() * viewHeight * 0.3f; // Bắt đầu từ trên
            x = startX;
            y = startY;
            speedX = (random.nextFloat() - 0.5f) * viewWidth * 0.5f;
            speedY = random.nextFloat() * viewHeight * 1.2f + viewHeight * 0.3f;
            width = 6 + random.nextFloat() * 10;
            height = 4 + random.nextFloat() * 8;
            rotation = random.nextFloat() * 360;
            rotationSpeed = (random.nextFloat() - 0.5f) * 720;
            color = COLORS[random.nextInt(COLORS.length)];
        }

        void update(float progress) {
            x = startX + speedX * progress;
            y = startY + speedY * progress;
            rotation += rotationSpeed * 0.02f;
            // Fade out ở cuối
            if (progress > 0.7f) {
                alpha = (int) (255 * (1f - (progress - 0.7f) / 0.3f));
                if (alpha < 0) alpha = 0;
            }
        }
    }
}

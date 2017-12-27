package fyp.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;


public class SatelliteSignalView extends View {

    private Paint mLinePaint;
    private Paint mBarPaintUsed;
    private Paint mBarPaintUnused;
    private Paint mBarPaintNoFix;
    private Paint mBarOutlinePaint;
    private Paint mTextPaint;
    private Paint mBackground;


    private SatelliteDataProvider mProvider = null;
    private int mSatellites = 0;
    private int[] mPnrs = new int[SatelliteDataProvider.maxSatellites];
    private float[] mSnrs = new float[SatelliteDataProvider.maxSatellites];
    private int[] mUsedInFixMask = new int[1];

    public SatelliteSignalView(Context context) {
        this(context, null);
    }

    public SatelliteSignalView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SatelliteSignalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mLinePaint = new Paint();
        mLinePaint.setColor(0xFFDDDDDD);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Style.STROKE);
        mLinePaint.setStrokeWidth(1.0f);


        mBarPaintUsed = new Paint();
        mBarPaintUsed.setColor(0xFF00BB00);
        mBarPaintUsed.setAntiAlias(true);
        mBarPaintUsed.setStyle(Style.FILL);
        mBarPaintUsed.setStrokeWidth(1.0f);

        mBarPaintUnused = new Paint(mBarPaintUsed);
        mBarPaintUnused.setColor(0xFFFFCC33);

        mBarPaintNoFix = new Paint(mBarPaintUsed);
        mBarPaintNoFix.setStyle(Style.STROKE);

        mBarOutlinePaint = new Paint();
        mBarOutlinePaint.setColor(0xFFFFFFFF);
        mBarOutlinePaint.setAntiAlias(true);
        mBarOutlinePaint.setStyle(Style.STROKE);
        mBarOutlinePaint.setStrokeWidth(1.0f);

        mTextPaint = new Paint();
        mTextPaint.setColor(0xFFFFFFFF);
        mTextPaint.setTextSize(15.0f);
        mTextPaint.setTextAlign(Align.CENTER);
        mBackground = new Paint();
        mBackground.setColor(0xFF222222);

    }

    void setDataProvider(SatelliteDataProvider provider) {
        mProvider = provider;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int fill = 6;
        final int baseline = getHeight() - 30;
        final int maxHeight = getHeight() - 40;
        final float scale = maxHeight / 100.0F;

        float slotWidth = (float) Math.floor(getWidth() / SatelliteDataProvider.maxSatellites);
        float barWidth = slotWidth - fill;
        float margin = (getWidth() - (slotWidth * SatelliteDataProvider.maxSatellites)) / 2;

        canvas.drawPaint(mBackground);

        if (mProvider != null) {
            mSatellites = mProvider.getSatelliteStatus(mPnrs, mSnrs, null, null, 0, 0, mUsedInFixMask);
            for (int i = 0; i < mSatellites; ++i) {
                if (mSnrs[i] < 0) {
                    mSnrs[i] = 0;
                }
            }
        }

        canvas.drawLine(0, baseline, getWidth(), baseline, mLinePaint);
        int drawn = 0;

        for (int i = 0; i < mSatellites; ++i) {
            if (mPnrs[i] <= 0) {
                continue;
            }
            float left = margin + (drawn * slotWidth) + fill / 2;
            if (0 == mUsedInFixMask[0]) {
                canvas.drawRect(left, baseline - (mSnrs[i] * scale), left + barWidth, baseline, mBarPaintNoFix);
            } else if (0 != (mUsedInFixMask[0] & (1 << (32 - mPnrs[i])))) {
                canvas.drawRect(left, baseline - (mSnrs[i] * scale), left + barWidth, baseline, mBarPaintUsed);
            } else {
                canvas.drawRect(left, baseline - (mSnrs[i] * scale), left + barWidth, baseline, mBarPaintUnused);
            }
            canvas.drawRect(left, baseline - (mSnrs[i] * scale), left + barWidth, baseline, mBarOutlinePaint);
            canvas.drawText(new Integer(mPnrs[i]).toString(), left + barWidth / 2, baseline + 15, mTextPaint);
            drawn += 1;
        }
    }
}
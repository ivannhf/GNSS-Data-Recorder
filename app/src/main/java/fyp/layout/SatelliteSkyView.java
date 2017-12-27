package fyp.layout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;


public class SatelliteSkyView extends View {
    private Paint mGridPaint;
    private Paint mTextPaint;
    private Paint mBackground;
    private Bitmap mSatelliteBitmapUsed;
    private Bitmap mSatelliteBitmapUnused;
    private Bitmap mSatelliteBitmapNoFix;

    private float mBitmapAdjustment;

    private SatelliteDataProvider mProvider = null;
    private int mSatellites;
    private int[] mPnrs = new int[SatelliteDataProvider.maxSatellites];
    private float[] mElevation = new float[SatelliteDataProvider.maxSatellites];
    private float[] mAzimuth = new float[SatelliteDataProvider.maxSatellites];
    private float[] mSnrs = new float[SatelliteDataProvider.maxSatellites];

    private float[] mX = new float[SatelliteDataProvider.maxSatellites];
    private float[] mY = new float[SatelliteDataProvider.maxSatellites];
    private int[] mUsedInFixMask = new int[1];

    private void computeXY() {
        for (int i = 0; i < mSatellites; ++i) {
            double theta = -(mAzimuth[i] - 90);
            double rad = theta * Math.PI / 180.0;
            mX[i] = (float) Math.cos(rad);
            mY[i] = -(float) Math.sin(rad);

            mElevation[i] = 90 - mElevation[i];
        }
    }

    public SatelliteSkyView(Context context) {
        this(context, null);
    }

    public SatelliteSkyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SatelliteSkyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mGridPaint = new Paint();
        mGridPaint.setColor(0xFFDDDDDD);
        mGridPaint.setAntiAlias(true);
        mGridPaint.setStyle(Style.STROKE);
        mGridPaint.setStrokeWidth(1.0f);
        mBackground = new Paint();
        mBackground.setColor(0xFF4444DD);

        mTextPaint = new Paint();
        mTextPaint.setColor(0xFFFFFFFF);
        mTextPaint.setTextSize(15.0f);
        mTextPaint.setTextAlign(Align.CENTER);


        mSatelliteBitmapUsed = ((BitmapDrawable) getResources().getDrawable(R.drawable.satgreen)).getBitmap();
        mSatelliteBitmapUnused = ((BitmapDrawable) getResources().getDrawable(R.drawable.satyellow)).getBitmap();
        mSatelliteBitmapNoFix = ((BitmapDrawable) getResources().getDrawable(R.drawable.satred)).getBitmap();
        mBitmapAdjustment = mSatelliteBitmapUsed.getHeight() / 2;
    }

    void setDataProvider(SatelliteDataProvider provider) {
        mProvider = provider;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerY = getHeight() / 2;
        float centerX = getWidth() / 2;
        int radius = (int) (getHeight() / 2) - 8;

        final Paint gridPaint = mGridPaint;
        final Paint textPaint = mTextPaint;
        canvas.drawPaint(mBackground);
        canvas.drawCircle(centerX, centerY, radius, gridPaint);
        canvas.drawCircle(centerX, centerY, radius * 3 / 4, gridPaint);
        canvas.drawCircle(centerX, centerY, radius >> 1, gridPaint);
        canvas.drawCircle(centerX, centerY, radius >> 2, gridPaint);
        canvas.drawLine(centerX, centerY - (radius >> 2), centerX, centerY - radius, gridPaint);
        canvas.drawLine(centerX, centerY + (radius >> 2), centerX, centerY + radius, gridPaint);
        canvas.drawLine(centerX - (radius >> 2), centerY, centerX - radius, centerY, gridPaint);
        canvas.drawLine(centerX + (radius >> 2), centerY, centerX + radius, centerY, gridPaint);

        double scale = radius / 90.0;

        if (mProvider != null) {
            mSatellites = mProvider.getSatelliteStatus(mPnrs, mSnrs, mElevation, mAzimuth, 0, 0, mUsedInFixMask);
            computeXY();
        }

        for (int i = 0; i < mSatellites; ++i) {
            if (mElevation[i] >= 90 || mAzimuth[i] <= 0 || mPnrs[i] <= 0) {
                continue;
            }
            double a = mElevation[i] * scale;

            int x = (int) Math.round(centerX + (mX[i] * a) - mBitmapAdjustment);
            int y = (int) Math.round(centerY + (mY[i] * a) - mBitmapAdjustment);
            if (0 == (mUsedInFixMask[0]) || mSnrs[i] <= 0) {
                canvas.drawBitmap(mSatelliteBitmapNoFix, x, y, gridPaint);
            } else if (0 != (mUsedInFixMask[0] & (1 << (32 - mPnrs[i])))) {
                canvas.drawBitmap(mSatelliteBitmapUsed, x, y, gridPaint);
            } else {
                canvas.drawBitmap(mSatelliteBitmapUnused, x, y, gridPaint);
            }
            canvas.drawText(new Integer(mPnrs[i]).toString(), x, y, textPaint);
        }

    }
}

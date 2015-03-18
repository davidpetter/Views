package uiview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import view.dp.se.dpviews.R;

/**
 * Image view that scales height or width proportionally to the
 * dimensions of the drawable and provided aspect ratio
 */
public class ScaledImageView extends ImageView {

    private static final String TAG = ScaledImageView.class.getName();

    /**
     * Scaling mode
     */
    //Do not scale
    public static final int SCALE_NONE = 0;
    //Scale the width of the image with respect to the height
    public static final int SCALE_WIDTH = 1;
    //Scale the height of the image with respect to the width
    public static final int SCALE_HEIGHT = 2;

    /**
     * Proportions constants
     */
    // Default proportion
    public static final int PROPORTION_DEFAULT = 1;


    private float mProportionHeight = PROPORTION_DEFAULT;
    private float mProportionWidth = PROPORTION_DEFAULT;
    private int mScaleDirection = SCALE_NONE;

    public ScaledImageView(Context context) {
        super(context);
    }

    public ScaledImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        valuesFromAttributes(context, attrs);
    }

    public ScaledImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        valuesFromAttributes(context, attrs);
    }

    public void setScaleType(int scaleType) {
        mScaleDirection = scaleType;
    }

    /**
     * Set the image view aspect ratio by providing the width and height proportions
     * @param proportionWidth
     * @param proportionHeight
     */
    public void setProportions(int proportionWidth, int proportionHeight) {
        mProportionHeight = proportionHeight;
        mProportionWidth = proportionWidth;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final Drawable drawable = getDrawable();
        if (drawable != null) {
            drawable.setDither(true);
            int width;
            int height;
            switch (mScaleDirection) {
                case SCALE_WIDTH:
                    Log.i(TAG, "Scale width");
                    height = MeasureSpec.getSize(heightMeasureSpec);
                    float denominator = (float) drawable.getIntrinsicHeight() * mProportionHeight;
                    float numerator = height * (float) drawable.getIntrinsicWidth() * mProportionWidth;
                    width = (int) Math.ceil(numerator / denominator);
                    setMeasuredDimension(width, height);
                    break;
                case SCALE_HEIGHT:
                    Log.i(TAG, "Scale height");
                    width = MeasureSpec.getSize(widthMeasureSpec);
                    denominator = (float) drawable.getIntrinsicWidth() * mProportionWidth;
                    numerator = width * (float) drawable.getIntrinsicHeight() * mProportionHeight;
                    height = (int) Math.ceil(numerator / denominator);
                    setMeasuredDimension(width, height);
                    break;
                default:
                    Log.i(TAG, "Scale value:" + mScaleDirection);
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    break;
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Set scale and proportions from attributes
     * @param context
     * @param attrs
     */
    private void valuesFromAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScaledImageView);
        mScaleDirection = typedArray.getInt(R.styleable.ScaledImageView_scaleDirection, SCALE_NONE);
        mProportionHeight = typedArray.getFloat(R.styleable.ScaledImageView_HeightProportion, PROPORTION_DEFAULT);
        mProportionWidth = typedArray.getFloat(R.styleable.ScaledImageView_WidthProportion, PROPORTION_DEFAULT);
        typedArray.recycle();
    }
}

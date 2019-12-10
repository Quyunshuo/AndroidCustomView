package com.quyunshuo.androidcustomview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 弧形带刻度进度条
 */
public class ArcProgressBar extends View {

    /**
     * 进度条渐变色组
     */
    private int[] mScheduleColors = {0xFF0071EB, 0xFF74DEFF};

    /**
     * 是否开启抗锯齿
     */
    private boolean mIsAntialiasing;

    /**
     * 进度条最大进度值
     */
    private float mMaxValue;
    /**
     * 进度条当前进度值
     */
    private float mCurrentValue;
    /**
     * 两刻度之间的间隔
     */
    private int mCalibrationInterval;

    /**
     * 进度圆弧宽度
     */
    private float mArcWidth;
    /**
     * 进度圆弧的画笔
     */
    private Paint mArcPaint;

    /**
     * 圆弧开始角度
     */
    private float mStartAngle;
    /**
     * 圆弧扫描的角度
     */
    private float mSweepAngle;

    /**
     * 每次改变进度的动画时长
     */
    private long mAnimTime;
    /**
     * 背景圆弧画笔
     */
    private Paint mBgArcPaint;
    /**
     * 背景圆弧颜色
     */
    private int mBgArcColor;
    /**
     * 刻度的画笔
     */
    private Paint mCalibrationPaint;
    /**
     * 刻度的宽度
     */
    private float mCalibrationWidth;
    /**
     * 刻度的颜色
     */
    private int mCalibrationColor;

    /**
     * 默认大小
     */
    private int mDefaultSize;
    /**
     * 路径
     */
    private RectF mRectF;
    /**
     * 圆心坐标
     */
    private Point mCircleCenterPoint;

    /**
     * 圆弧半径
     */
    private float mRadius;

    /**
     * 当前进度，[0.0f,1.0f]
     */
    private float mCurrentProgress;
    /**
     * 绘制圆弧的动画
     */
    private ValueAnimator mAnimator;

    public ArcProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * 初始化操作
     */
    private void init(Context context, AttributeSet attrs) {
        //赋值默认大小
        mDefaultSize = dipToPx(context, 150);
        mRectF = new RectF();
        mCircleCenterPoint = new Point();
        initConfig(context, attrs);
        initPaint();
        setCurrentValue(mCurrentValue);
    }

    // TODO: 2019-12-10
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measuring(widthMeasureSpec, mDefaultSize),
                measuring(heightMeasureSpec, mDefaultSize));
    }

    // TODO: 2019-12-10
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int minSize = Math.min(getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - 2 * (int) mArcWidth,
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - 2 * (int) mArcWidth);
        mRadius = minSize / 2;
        mCircleCenterPoint.x = getMeasuredWidth() / 2;
        mCircleCenterPoint.y = getMeasuredHeight() / 2;
        //绘制圆弧的边界
        mRectF.left = mCircleCenterPoint.x - mRadius - mArcWidth / 2;
        mRectF.top = mCircleCenterPoint.y - mRadius - mArcWidth / 2;
        mRectF.right = mCircleCenterPoint.x + mRadius + mArcWidth / 2;
        mRectF.bottom = mCircleCenterPoint.y + mRadius + mArcWidth / 2;
        updateArcPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制进度
        drawArc(canvas);
        //绘制刻度
        drawCalibration(canvas);
    }

    /**
     * 绘制刻度
     */
    private void drawCalibration(Canvas canvas) {
        //获取分成多少个间隔
        int total = (int) (mSweepAngle / mCalibrationInterval);
        canvas.save();
        canvas.rotate(mStartAngle, mCircleCenterPoint.x, mCircleCenterPoint.y);

        for (int i = 0; i <= total; i++) {
            //这一点可能比较难理解点:drawLine(...)从表面看画的是圆最右边的一条白线(白色小矩形),但是由于在drawArc()中已经将canvas顺时针旋转了135度,一次刻度间隔的白线也就从圆弧起点开始了
            canvas.drawLine(mCircleCenterPoint.x + mRadius,
                    mCircleCenterPoint.y,
                    mCircleCenterPoint.x + mRadius + mArcWidth, mCircleCenterPoint.y,
                    mCalibrationPaint);
            canvas.rotate(mCalibrationInterval, mCircleCenterPoint.x, mCircleCenterPoint.y);
        }
        canvas.restore();
    }

    /**
     * 绘制圆弧
     * TODO:
     */
    private void drawArc(Canvas canvas) {
        // 绘制背景圆弧
        // 从进度圆弧结束的地方开始重新绘制，优化性能
        float currentAngle = mSweepAngle * mCurrentProgress;
        canvas.save();
        canvas.rotate(mStartAngle, mCircleCenterPoint.x, mCircleCenterPoint.y);
        canvas.drawArc(mRectF, currentAngle, mSweepAngle - currentAngle, false, mBgArcPaint);
        // 第一个参数 oval 为 RectF 类型，即圆弧显示区域
        // startAngle 和 sweepAngle  均为 float 类型，分别表示圆弧起始角度和圆弧度数
        // 3点钟方向为0度，顺时针递增
        // 如果 startAngle < 0 或者 > 360,则相当于 startAngle % 360
        // useCenter:如果为True时，在绘制圆弧时将圆心包括在内，通常用来绘制扇形
        canvas.drawArc(mRectF, 0, currentAngle, false, mArcPaint);
        canvas.restore();
    }

    /**
     * 测量
     * TODO: 2019-12-10
     */
    private int measuring(int measureSpec, int defaultSize) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    /**
     * 初始化配置参数
     */
    private void initConfig(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ArcProgressBar);
        //获取是否开启抗锯齿      默认开启
        mIsAntialiasing = typedArray.getBoolean(R.styleable.ArcProgressBar_isAntialiasing, true);
        //获取进度条最大进度值    默认100f
        mMaxValue = typedArray.getFloat(R.styleable.ArcProgressBar_maxValue, 100f);
        //获取进度条当前进度值    默认50f
        mCurrentValue = typedArray.getFloat(R.styleable.ArcProgressBar_currentValue, 50f);
        //获取两刻度之间的间隔    默认15
        mCalibrationInterval = typedArray.getInt(R.styleable.ArcProgressBar_calibrationInterval, 15);
        //获取进度圆弧的宽度      默认40f
        mArcWidth = typedArray.getDimension(R.styleable.ArcProgressBar_arcWidth, 40f);
        //获取圆弧开始的角度      默认135f
        mStartAngle = typedArray.getFloat(R.styleable.ArcProgressBar_startAngle, 135f);
        //获取圆弧的度数          默认270f
        mSweepAngle = typedArray.getFloat(R.styleable.ArcProgressBar_sweepAngle, 270f);
        //获取每次改变进度的动画时长 默认3000ms
        mAnimTime = typedArray.getInt(R.styleable.ArcProgressBar_animTime, 3000);
        //获取背景圆弧的颜色       默认灰色
        mBgArcColor = typedArray.getColor(R.styleable.ArcProgressBar_bgArcColor, Color.GRAY);
        //获取刻度的宽度           默认5
        mCalibrationWidth = typedArray.getDimension(R.styleable.ArcProgressBar_calibrationWidth, 5);
        //获取刻度的颜色          默认白色
        mCalibrationColor = typedArray.getColor(R.styleable.ArcProgressBar_calibrationColor, Color.WHITE);
        typedArray.recycle();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        //进度圆弧画笔
        mArcPaint = new Paint();
        //是否开启抗锯齿
        mArcPaint.setAntiAlias(mIsAntialiasing);
        //样式 仅描边
        mArcPaint.setStyle(Paint.Style.STROKE);
        //宽度
        mArcPaint.setStrokeWidth(mArcWidth);
        //设置绘画的线帽样式
        mArcPaint.setStrokeCap(Paint.Cap.BUTT);

        //背景圆弧画笔
        mBgArcPaint = new Paint();
        mBgArcPaint.setAntiAlias(mIsAntialiasing);
        mBgArcPaint.setStyle(Paint.Style.STROKE);
        mBgArcPaint.setStrokeWidth(mArcWidth);
        mBgArcPaint.setStrokeCap(Paint.Cap.BUTT);
        //设置颜色
        mBgArcPaint.setColor(mBgArcColor);

        //刻度画笔
        mCalibrationPaint = new Paint();
        mCalibrationPaint.setAntiAlias(mIsAntialiasing);
        mCalibrationPaint.setColor(mCalibrationColor);
        mCalibrationPaint.setStrokeWidth(mCalibrationWidth);
    }

    /**
     * 设置当前值
     *
     * @param value
     */
    public void setCurrentValue(float value) {
        //如果值大于最大值就设置为最大值
        if (value > mMaxValue) {
            value = mMaxValue;
        }
        //记录当前值
        mCurrentValue = value;
        //开始值为当前进度值 首次为0.0f 结束值为 value / mMaxValue
        startAnimator(mCurrentProgress, value / mMaxValue, mAnimTime);
    }

    /**
     * 更新圆弧画笔
     * TODO:
     */
    private void updateArcPaint() {
        // 设置渐变
        // 渐变的颜色是360度，如果只显示270，那么则会缺失部分颜色
        SweepGradient sweepGradient = new SweepGradient(mCircleCenterPoint.x, mCircleCenterPoint.y, mScheduleColors, null);
        mArcPaint.setShader(sweepGradient);
    }

    /**
     * 获取进度条当前进度值
     */
    public int getCurrentValue() {
        return (int) mCurrentValue;
    }

    /**
     * 获取当前进度[0.0f,1.0f]
     */
    public float getCurrentProgress() {
        return mCurrentProgress;
    }

    /**
     * 获取最大值
     */
    public float getMaxValue() {
        return mMaxValue;
    }

    /**
     * 设置最大值
     */
    public void setMaxValue(float maxValue) {
        mMaxValue = maxValue;
    }

    /**
     * 绘制圆弧进度的动画
     *
     * @param start    开始值
     * @param end      结束值
     * @param animTime 动画时间
     */
    private void startAnimator(float start, float end, long animTime) {
        mAnimator = null;
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(animTime);
        mAnimator.addUpdateListener(animation -> {
            //每次执行动画记录当前进度
            mCurrentProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        mAnimator.start();
    }

    /**
     * 对动画进行销毁
     */
    public void animatorDestroy() {
        if (mAnimator != null) {
            mAnimator.removeAllUpdateListeners();
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    /**
     * 重置进度
     */
    public void reset() {
        mCurrentValue = 0f;
        startAnimator(mCurrentProgress, 0.0f, mAnimTime);
    }

    /**
     * dip 转换成px
     */
    private int dipToPx(Context context, float dip) {
        return (int) (dip * (context.getResources().getDisplayMetrics().density)
                + 0.5f * (dip >= 0 ? 1 : -1));
    }
}

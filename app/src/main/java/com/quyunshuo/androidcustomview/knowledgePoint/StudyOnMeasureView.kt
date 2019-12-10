package com.quyunshuo.androidcustomview.knowledgePoint

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * 关于自定义view的onMeasure方法
 * 博文链接:https://blog.csdn.net/csdnzouqi/article/details/79579562
 */
class StudyOnMeasureView : View {
    private var paint = Paint()

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()

    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = Color.BLUE
    }

    /**
     * 关于SpecMode的三种模式:
     *
     * UNSPECFIED:  父容器不对View有任何限制，要多大给多大，这种情况一般用于系统内部，表示一种测量状态
     * EXACTLY:     父容器已经检测出View所需要的精确大小，这个时候View的最终大小就是SpecSize所指定的值。
     *              它对应于LayoutParams中的match_parent和具体的数值这两种模式。
     * AT_MOST:     父容器指定了一个可用大小即SpecSize，View的大小不能大于这个值，具体是什么值要看不同View的具体实现。
     *              它对应于LayoutParams中的warp_content。
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 获取view宽的SpecSize和SpecMode
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)

        // 获取view高的SpecSize和SpecMode
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            // 当view的宽和高都设置为wrap_content时，调用setMeasuredDimension(measuredWidth,measureHeight)方法设置view的宽/高为400px
            setMeasuredDimension(400, 400)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            // 当view的宽设置为wrap_content时，设置View的宽为你想要设置的大小（这里我设置400px）,高就采用系统获取的heightSpecSize
            setMeasuredDimension(400, heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            // 当view的高设置为wrap_content时，设置View的高为你想要设置的大小（这里我设置400px）,宽就采用系统获取的widthSpecSize
            setMeasuredDimension(widthSpecSize, 400)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.RED)
    }
}
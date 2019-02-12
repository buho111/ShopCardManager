package com.doctormiyabi.shopcardmanager.ui.View

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * TODO: document your custom view class.
 */
class CameraPreview : View {

    /**
     * In the doctormiyabi view, this drawable is drawn above the text.
     */
    val paint: Paint = Paint()
    private var points: List<Double> = listOf()

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        paint.setColor(Color.argb(255, 255, 0, 0))  // alpha.r.g.b
        paint.setStrokeWidth(6F)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if(points.size > 2) {
            for (i in 0..points.size - 2 step 2) {
                canvas.drawPoint(points[i].toFloat(), points[i + 1].toFloat(), paint)
            }
        }
    }

    public fun drawPreview(d: List<Double>){
        points = d
        invalidate()
    }
}

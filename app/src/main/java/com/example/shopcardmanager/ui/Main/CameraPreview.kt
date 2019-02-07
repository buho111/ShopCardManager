package com.example.shopcardmanager.ui.Main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.ImageView






/**
 * TODO: document your custom view class.
 */
class CameraPreview : ImageView {

    /**
     * In the example view, this drawable is drawn above the text.
     */
    val paint: Paint = Paint()

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
        paint.setStrokeWidth(4F)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

    }

    public fun drawPreview(bmp: Bitmap, d: List<Double>){
        val mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        for(i in 0..d.size-2 step 2 ) {
            canvas.drawPoint(d[i].toFloat(), d[i+1].toFloat(), paint)
        }
        this.setImageBitmap(mutableBitmap)
    }
}

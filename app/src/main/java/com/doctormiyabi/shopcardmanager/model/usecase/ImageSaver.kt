package com.doctormiyabi.shopcardmanager.model.usecase

import android.graphics.Bitmap
import android.media.Image
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class ImageSaver private constructor(file: File): Runnable {
    private lateinit var mbuffer: ByteBuffer
    private lateinit var mbytes: ByteArray
    private val mFile = file
    private var mImage: Image? = null

    init {
        mbuffer = ByteBuffer.allocate(0)
        mbytes = ByteArray(0)
    }

    constructor(image: Image, file: File): this(file) {
        mbuffer = image.planes[0].buffer
        mbytes = ByteArray(mbuffer.remaining())
        mImage = image
    }

    constructor(bmp: Bitmap, file: File): this(file) {
        mbuffer = ByteBuffer.allocate(bmp.getByteCount())
        bmp.copyPixelsFromBuffer(mbuffer)
        mbytes = mbuffer.array()
    }

    override fun run() {
        mbuffer.get(mbytes)
        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(mFile)
            output.write(mbytes)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            mImage?.close()
            if (null != output) {
                try {
                    output.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
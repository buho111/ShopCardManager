package com.doctormiyabi.shopcardmanager.ui.Main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.doctormiyabi.shopcardmanager.R
import com.doctormiyabi.shopcardmanager.model.usecase.ImageSaver
import com.doctormiyabi.shopcardmanager.model.usecase.ImageScanner
import com.doctormiyabi.shopcardmanager.model.usecase.TaskQueue
import com.doctormiyabi.shopcardmanager.model.usecase.Throttle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.opencv.android.OpenCVLoader
import com.doctormiyabi.shopcardmanager.ui.Base.Camera2BasicFragment
import java.io.File


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, Camera2BasicFragment.PreviewListener, Throttle.ThrottleEventListener {
    companion object {
        const val CAMERA_REQUEST_CODE = 1
        const val CAMERA_PERMISSION_REQUEST_CODE = 2
    }
    private val THROTTLE_INTERVAL : Long = 3 * 1 * 1000

    private lateinit var fragmentManager: FragmentManager
    private lateinit var mThrottle: Throttle
    private var imgsc : ImageScanner = ImageScanner()
    private var screenBmp : Bitmap? = null
    private var mScanThread: Thread = Thread()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        fragmentManager = getSupportFragmentManager()

        mThrottle = Throttle(THROTTLE_INTERVAL)

        // OpenCVの初期化処理
        if(!OpenCVLoader.initDebug()){
            Log.i("OpenCV", "Failed")
        }else{
            Log.i("OpenCV", "successfully built !")
        }

        fab.setOnClickListener { view ->
            // カメラを利用する権限をチェック
            if (checkCameraPermission()) {
                val transaction = fragmentManager.beginTransaction()
                transaction.add(R.id.container, Camera2BasicFragment.newInstance())
                transaction.addToBackStack(null)
                transaction.commit()
            } else {
                grantCameraPermission()
            }
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPreviewCreated(bmp : Bitmap) {
        screenBmp = bmp

        //throttle処理を入れて間引く
        mThrottle.setThrottle(this)
    }

    override fun onActuate(){
        val runnable = object : Runnable {
            override fun run(){
                var img = imgsc.onImageScan(screenBmp)
                if(img != null) {
 //                   TaskQueue.getInstance().addTask(ImageSaver(img, File("test.jpg")))
                }
            }
        }

        // throttleによって間引かれた処理。
        if (screenBmp != null) {
            // Thread Poolで実行される
            TaskQueue.getInstance().addTask(runnable)
            // MAIN Threadで実行される
            cameraImage.drawPreview(imgsc.getPoints())
        }
    }

    private fun checkCameraPermission() = PackageManager.PERMISSION_GRANTED ==
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)


    private fun grantCameraPermission() =
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE)


}

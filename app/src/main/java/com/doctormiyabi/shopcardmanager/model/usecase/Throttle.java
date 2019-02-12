package com.doctormiyabi.shopcardmanager.model.usecase;

import android.os.Handler;

/**
 * Javaでthrottle処理を行う
 */
public class Throttle {

    /**
     * throttle処理用のハンドラ
     */
    private Handler throttleHandler;

    /**
     * throttle処理を実行した最後の時間
     */
    private long throttleLastTimeMillis;

    /**
     * 処理を間引く間隔（ミリ秒）
     */
    long throtteleInterval;

    /**
     * リスナ
     */
    private ThrottleEventListener throttleEventListener;

    //--------------------------------------------------------------
    // コンストラクタ
    //--------------------------------------------------------------
    public Throttle(long throtteleInterval) {
        //初期化
        throttleHandler = new Handler();
        throttleLastTimeMillis = 0;

        //間引く間隔を定義する
        this.throtteleInterval = throtteleInterval;
    }

    //--------------------------------------------------------------
    // メソッド
    //--------------------------------------------------------------
    public void setThrottle(ThrottleEventListener throttleEventListener) {
        //イベントを通知するリスナーセット
        this.throttleEventListener = throttleEventListener;

        long currentTimeMillis = System.currentTimeMillis();

        // 最後に実行した時間から間引きたい時間経過していたら実行
        if ((throttleLastTimeMillis + throtteleInterval) <= currentTimeMillis) {
            throttleLastTimeMillis = currentTimeMillis;

            //処理実行
            throttleHandler.post(throttleTask);
        } else {
            //重複して呼び出さないようにキャンセルするのを忘れずに
            throttleHandler.removeCallbacks(throttleTask);
            //間引いた場合は、間隔明けに実行する
            throttleHandler.postDelayed(throttleTask, (throttleLastTimeMillis + throtteleInterval) - currentTimeMillis);
        }
    }

    //--------------------------------------------------------------
    // タスク
    //--------------------------------------------------------------
    private final Runnable throttleTask = new Runnable() {
        @Override
        public void run() {
            throttleHandler.removeCallbacks(throttleTask);

            throttleLastTimeMillis = System.currentTimeMillis();

            //処理実行
            throttleEventListener.onActuate();
        }
    };

    //--------------------------------------------------------------
    // イベントリスナ
    //--------------------------------------------------------------
    public interface ThrottleEventListener {
        /**
         * 通知
         */
        void onActuate();
    }
}
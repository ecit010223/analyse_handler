package com.huier.analyse_handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 主线程是一个拥有handler和Looper的消息循环,主线程上创建的Handler会自动与它的Looper相关联。
 * 我们可以将主线程上创建的Handler传递给另一线程,传递出去的Handler与创建它的线程Looper始终保持着联系。
 * 因此，任何已传出Handler负责处理的消息都将在主线程的消息队列中处理。
 * 我们也可从后台线程使用与主线程关联的Handler，安排要在主线程上完成的任务。
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvResult;
    private Button btnMainToSelf;
    private Button btnOtherToMain;
    private Button btnMainToOther;
    private Button btnOtherToSelf;

    /** 主线程的Handler **/
    private Handler mMainHandler;
    /** 其它线程的Handler **/
    private Handler mOtherHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //启动子线程
        new Thread(new MyRunnable()).start();
    }

    private void initView(){
        tvResult = (TextView)findViewById(R.id.tv_result);
        btnMainToSelf = (Button)findViewById(R.id.btn_main_to_self);
        btnMainToSelf.setOnClickListener(this);
        btnOtherToMain = (Button)findViewById(R.id.btn_other_to_main);
        btnOtherToMain.setOnClickListener(this);
        btnMainToOther = (Button)findViewById(R.id.btn_main_to_other);
        btnMainToOther.setOnClickListener(this);
        btnOtherToSelf = (Button)findViewById(R.id.btn_other_to_self);
        btnOtherToSelf.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_main_to_self:
                mainToSelf();
                break;
            case R.id.btn_other_to_main:
                otherToMain();
                break;
            case R.id.btn_main_to_other:
                mainToOther();
                break;
            case R.id.btn_other_to_self:
                otherToSelf();
                break;
        }
    }

    /** 其它线程给自己发送消息 **/
    private void otherToSelf(){
        new Thread(new MyRunnable2()).start();
    }

    /** 主线程给其他线程发送Message **/
    private void mainToOther(){
        //主线程中的消息，这里的Handler实例化在子线程中
        Message message = mOtherHandler.obtainMessage(1,1,1,Thread.currentThread().getName()+"线程发送消息");
        mOtherHandler.sendMessage(message);
    }

    /** 其他线程给主线程发送Message **/
    private void otherToMain(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //主线程的Looper对象
                Looper looper = Looper.getMainLooper();
                //这里以主线程的Looper对象创建了handler,所以,这个handler发送的Message会被传递给主线程的MessageQueue。
                mMainHandler = new MyHandler(looper);
                mMainHandler.removeMessages(0);
                /**
                 * 构建Message对象
                 * 第一个参数：是自己指定的message代号，方便在handler 选择性地接收
                 * 第二三个参数没有什么意义
                 * 第四个参数需要封装的对象
                 */
                Message message = mMainHandler.obtainMessage(1,1,1,Thread.currentThread().getName()+"线程发消息");
                mMainHandler.sendMessage(message);
            }
        }).start();
    }


    /** 主线程给自己发送Message **/
    private void mainToSelf(){
        //主线程的Looper对象
        Looper looper = Looper.getMainLooper();
        //这里以主线程的Looper对象创建了handler,所以,这个handler发送的Message会被传递给主线程的MessageQueue。
        mMainHandler = new MyHandler(looper);
        mMainHandler.removeMessages(0);
        /**
         * 构建Message对象
         * 第一个参数：是自己指定的message代号，方便在handler 选择性地接收
         * 第二三个参数没有什么意义
         * 第四个参数需要封装的对象
         */
        Message message = mMainHandler.obtainMessage(1,1,1,Thread.currentThread().getName()+"线程发消息");
        mMainHandler.sendMessage(message);
    }

    class MyRunnable2 implements Runnable{

        @Override
        public void run() {
            Looper.prepare();
            OtherHandler2 otherHandler2 = new OtherHandler2(Looper.myLooper());
            Message message = otherHandler2.obtainMessage(1,1,1,Thread.currentThread().getName()+"线程发消息");
            otherHandler2.sendMessage(message);
            Looper.loop();
        }

        class OtherHandler2 extends Handler{

            public OtherHandler2(Looper looper){
                super(looper);
            }

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.d(Constants.TAG,Thread.currentThread().getName()+"线程的Handler,收到了"+(String)msg.obj);
            }
        }
    }

    class MyRunnable implements Runnable{

        @Override
        public void run() {
            //创建该线程的Looper对象，用于接收消息
            Looper.prepare();
            mOtherHandler = new OtherHandler(Looper.myLooper());
            Looper.loop();
        }
    }

    /** 主线程的Handler类 **/
    class MyHandler extends Handler{
        public MyHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvResult.setText(Thread.currentThread().getName()+"线程的Handler,收到了"+(String)msg.obj);
        }
    }

    /** 子线程的Handler类 **/
    class OtherHandler extends Handler{
        public OtherHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(Constants.TAG,Thread.currentThread().getName()+"线程的Handler,收到了"+(String)msg.obj);
        }
    }
}

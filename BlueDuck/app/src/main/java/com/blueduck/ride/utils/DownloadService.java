package com.blueduck.ride.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;

import com.blueduck.ride.BuildConfig;
import com.blueduck.ride.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Download the update app service
 * 下载更新App服务
 */
public class DownloadService extends Service{

    private static final String TAG = "DownLoadService";

    private String apkUrl;//下载链接 Download link
    private File file = null;//下载apk存放地址 Download apk storage address
    private int failCount = 5;//允许最大失败次数 Maximum number of failures allowed
    private DownloadTask downloadTask = null;//异步下载线程 Asynchronous download thread


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i(TAG,"onCreate: 启动了下载service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(TAG,"onStartCommand: 调用了onStartCommand");
        apkUrl = intent.getStringExtra("apkUrl");
        if (downloadTask == null) {
            downloadTask = new DownloadTask();
            downloadTask.execute(apkUrl);
            startForeground(CommonSharedValues.DOWNLOAD_SERVICE_NOTIFICATION, getNotification(getString(R.string.download), 0));
            LogUtils.i(TAG,"onStartCommand: 开始下载");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i(TAG,"onCreate: 销毁了下载service");
    }

    private class DownloadTask extends AsyncTask<String, Integer, Integer>{

        private static final int TYPE_SUCCESS = 0;//下载成功 download successful
        private static final int TYPE_FAILED = 1;//下载失败 download failed
        private int lastProgress;//下载的进度值 Download progress value

        @Override
        protected Integer doInBackground(String... params) {
            InputStream is = null;
            RandomAccessFile savedFile = null;
            try {
                long downloadedLength = 0;//记录已下载的文件长度 Record the length of the downloaded file
                String downloadUrl = params[0];
                String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                file = new File(directory + fileName);
                if (file.exists()){
                    downloadedLength = file.length();
                }
                long contentLength = getContentLength(downloadUrl);
                if (contentLength == 0){
                    LogUtils.i(TAG,"doInBackground: 获取文件总长度为0，下载失败!");
                    return TYPE_FAILED;
                }else if (contentLength == downloadedLength){
                    //The downloaded byte and the total bytes of the file are equal, indicating that the download has been completed.
                    //已下载字节和文件总字节相等，说明已经下载完成了
                    LogUtils.i(TAG,"doInBackground: 下载成功");
                    return TYPE_SUCCESS;
                }
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        //Breakpoint download, specify from which byte to start downloading
                        //断点下载，指定从哪个字节开始下载
                        .addHeader("RANGE","bytes=" + downloadedLength + "-")
                        .url(downloadUrl)
                        .build();
                Response response = client.newCall(request).execute();
                if (response != null){
                    is = response.body().byteStream();
                    savedFile = new RandomAccessFile(file, "rw");
                    savedFile.seek(downloadedLength);//跳过已下载的字节 Skip downloaded bytes
                    byte[] b = new byte[1024];
                    int total = 0;
                    int len;
                    while ((len = is.read(b)) != -1){
                        total += len;
                        savedFile.write(b, 0, len);
                        //计算已下载的百分比 Calculate the percentage downloaded
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                    response.body().close();
                    return TYPE_SUCCESS;
                }
            }catch (Exception e){
                e.printStackTrace();
            } finally {
                try {
                    if (is != null){
                        is.close();
                    }
                    if (savedFile != null){
                        savedFile.close();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            LogUtils.i(TAG,"doInBackground: 读取文件出现异常，下载失败!");
            return TYPE_FAILED;
        }

        private long getContentLength(String downloadUrl) throws IOException{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null && response.isSuccessful()){
                long contentLength = response.body().contentLength();
                response.body().close();
                return contentLength;
            }
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress  = values[0];
            if (progress > lastProgress){
                getNotificationManager().notify(CommonSharedValues.DOWNLOAD_SERVICE_NOTIFICATION,getNotification(getString(R.string.download),progress));
                lastProgress = progress;
            }
        }

        @Override
        protected void onPostExecute(Integer status) {
            switch (status){
                case TYPE_SUCCESS://下载成功 download successful
                    //When the download is successful, the foreground service notification is closed,
                    // and a notification of successful download is created, and then the installation interface is started.
                    //下载成功时将前台服务通知关闭，并创建一个下载成功的通知，然后启动安装界面
                    stopForeground(true);
                    getNotificationManager().notify(CommonSharedValues.DOWNLOAD_SERVICE_NOTIFICATION,getNotification(getString(R.string.download_success),-1));
                    startInstall();
                    downloadTask = null;
                    stopSelf();
                    break;
                case TYPE_FAILED://下载失败 download failed
                    downloadTask = null;
                    if (failCount > 0){
                        if (downloadTask == null) {
                            LogUtils.i(TAG,"onPostExecute: ==========================失败次数为：" + failCount);
                            downloadTask = new DownloadTask();
                            downloadTask.execute(apkUrl);
                            failCount--;
                        }
                    }else{
                        //The foreground service notification is closed when the download fails,
                        // and a notification that the download failed is created
                        //下载失败时将前台服务通知关闭，并创建一个下载失败的通知
                        stopForeground(true);
                        getNotificationManager().notify(CommonSharedValues.DOWNLOAD_SERVICE_NOTIFICATION,getNotification(getString(R.string.download_fail),-1));
                        stopSelf();
                    }
                    break;
            }
        }
    }

    /**
     * Start the installation
     * 启动安装
     */
    private void startInstall(){
        Intent intent= new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",file);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }else{
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title,int progress){
//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"use");
        builder.setSmallIcon(R.mipmap.app_icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.app_icon));
//        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress >= 0){
            //当progress大于或等于0时才需要显示下载进度
            builder.setContentText(progress + "%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }
}

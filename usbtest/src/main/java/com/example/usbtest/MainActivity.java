package com.example.usbtest;

import android.Manifest;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.partition.Partition;
import com.github.mjdev.libaums.fs.FileSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //输入的内容
    private EditText u_disk_edt;
    //写入到U盘
    private Button u_disk_write;
    //从U盘读取
    private Button u_disk_read;
    //显示读取的内容
    private TextView u_disk_show;
    //自定义U盘读写权限
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    //当前处接U盘列表
    private UsbMassStorageDevice[] storageDevices;
    private UsbFile cFolder;
    private UsbFile mFolder;
    private final static String U_DISK_FILE_NAME = "GPR035.lte";

    private String mFolderPath;
    private String mFileName;

    public String getmFileName() {
        return mFileName;
    }

    public void setmFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public String getmFolderPath() {
        return mFolderPath;
    }

    public void setmFolderPath(String mFolderPath) {
        this.mFolderPath = mFolderPath;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    showToastMsg(U_DISK_FILE_NAME + "导出成功");
                    break;
                case 101:
                    String txt = msg.obj.toString();
                    if (!TextUtils.isEmpty(txt))
                        u_disk_show.setText("读取到的数据是：" + txt);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getPermission();
//        start();
//        initViews();
//        getAllExterSdcardPath();
//        registerUDiskReceiver();
//        getAllExterSdcardPath();
          registerBroadcast();

    }


    private void initViews() {
        u_disk_edt = (EditText) findViewById(R.id.u_disk_edt);
        u_disk_write = (Button) findViewById(R.id.u_disk_write);
        u_disk_read = (Button) findViewById(R.id.u_disk_read);
        u_disk_show = (TextView) findViewById(R.id.u_disk_show);
        u_disk_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                final String content = u_disk_edt.getText().toString().trim();
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        System.err.println("U盘路径-1:");
                        saveText2UDisk();
//                        start1();
                    }
                });
            }
        });
        u_disk_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        System.err.println("U盘路径-1:");
                    }
                });
            }
        });
    }


    /**
     * @description 保存数据到U盘，目前是保存到根目录的
     * @author ldm
     * @time 2017/9/1 17:17
     */
    private int index;
    private void saveText2UDisk() {
        if (isAllExport) {
            if(cFolder == null) {
                isNull = false;
                getUsbFolders(currentFs);
            }
            else
                ;
            showToastMsg("U盘路3:"+cFolder);

        } else {
            showToastMsg("U盘路径2:"+cFolder);
            getUsbFiles(currentFs);
            showToastMsg("U盘路径4:"+cFolder);
        }
        showToastMsg("U盘路径: " + mFolder);
        //项目中也把文件保存在了SD卡，其实可以直接把文本读取到U盘指定文件
        File file = FileUtil.getSaveFile("gprmeasurement/data/measured", U_DISK_FILE_NAME);
//        System.err.println("文件夹名称："+getmFolderPath()+" "+"文件名称："+getmFileName()+" "+"文件路径："+file.toString());
        if (null != mFolder) {
            FileUtil.saveSDFile2OTG(file, mFolder);
            mHandler.sendEmptyMessage(100);
        }
    }

    /**
     * @description OTG广播注册
     * @author ldm
     * @time 2017/9/1 17:19
     */
    private void registerUDiskReceiver() {
        //监听otg插入 拔出
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mOtgReceiver, usbDeviceStateFilter);
        //注册监听自定义广播
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mOtgReceiver, filter);
    }

    /**
     * @description OTG广播，监听U盘的插入及拔出
     * @author ldm
     * @time 2017/9/1 17:20
     * @param
     */
    private BroadcastReceiver mOtgReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_USB_PERMISSION://接受到自定义广播
//                    showToastMsg("111");
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    //允许权限申请
                    //U盘插入
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            //用户已授权，可以进行读取操作
                            readDevice(getUsbMass(usbDevice));
                        } else {
                            showToastMsg("没有插入U盘");
                        }
                    } else {
                        showToastMsg("未获取到U盘权限");
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED://接收到U盘设备插入广播
//                    showToastMsg("222");
                    UsbDevice device_add = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device_add != null) {
                        //接收到U盘插入广播，尝试读取U盘设备数据
                        redUDiskDevsList();
                        showToastMsg("已经插入U盘");

                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED://接收到U盘设设备拔出广播
                    showToastMsg("U盘已拔出");
                    mFolder = null;
                    break;
            }
        }
    };

    /**
     * @description U盘设备读取
     * @author ldm
     * @time 2017/9/1 17:20
     */
    private void redUDiskDevsList() {
        //设备管理器
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        //获取U盘存储设备
        storageDevices = UsbMassStorageDevice.getMassStorageDevices(this);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        //一般手机只有1个OTG插口
        for (UsbMassStorageDevice device : storageDevices) {
            //读取设备是否有权限
            if (usbManager.hasPermission(device.getUsbDevice())) {
                readDevice(device);
            } else {
                //没有权限，进行申请
                usbManager.requestPermission(device.getUsbDevice(), pendingIntent);
            }
        }
        if (storageDevices.length == 0) {
            showToastMsg("请插入可用的U盘");
        }
    }

    private UsbMassStorageDevice getUsbMass(UsbDevice usbDevice) {
        for (UsbMassStorageDevice device : storageDevices) {
            if (usbDevice.equals(device.getUsbDevice())) {
                return device;
            }
        }
        return null;
    }

    private boolean isAllExport = false;

    public void setAllExport(boolean export) {
        this.isAllExport = export;
    }

    private  FileSystem currentFs;
    private void readDevice(UsbMassStorageDevice device) {
        try {
            device.init();//初始化
            //设备分区
            Partition partition = device.getPartitions().get(0);//仅使用设备的第一个分区
            //文件系统
            currentFs = partition.getFileSystem();
            currentFs.getVolumeLabel();//可以获取到设备的标识
            //通过FileSystem可以获取当前U盘的一些存储信息，包括剩余空间大小，容量等等
            Log.e("Capacity: ", currentFs.getCapacity() + "");
            Log.e("Occupied Space: ", currentFs.getOccupiedSpace() + "");
            Log.e("Free Space: ", currentFs.getFreeSpace() + "");
            Log.e("Chunk size: ", currentFs.getChunkSize() + "");
//            cFolder = currentFs.getRootDirectory();//设置当前文件对象为根目录
//          mFolder = cFolder.createDirectory("LteFile");
            String deviceName = currentFs.getVolumeLabel();//获取设备标签

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //create file folder


    private void showToastMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //动态获取权限
    public void getPermission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.err.println("enter ");
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    private Thread mThread = null;
    private Thread mFileThread = null;

    private void start() {

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        mThread.start();
    }

    private void start1() {
        mFileThread = new Thread(new Runnable() {
            @Override
            public void run() {
//                getmUSBStatus();
                saveText2UDisk();
            }
        });
        mFileThread.start();
    }

    private boolean isGetStatus;

    public boolean getmUSBStatus() {
//        showToastMsg("cFolder111:"+cFolder.toString());
        //            System.err.println("enter 111 "+cFolder);
        //            System.err.println("cFolder111:"+cFolder.toString());
        return cFolder != null;
    }

    private List<UsbFile> usbFiles = new ArrayList<>();
    private List<String> mFileNames = new ArrayList<>();
    private List<String> mFolderNames = new ArrayList<>();
    private List<String> mProjectNames = new ArrayList<>();
    private List <Integer> mIndex = new ArrayList<>();

    public void getUsbFiles(FileSystem fileSystem) {
        usbFiles.clear();
        mFileNames.clear();
        try {
            for (UsbFile file : fileSystem.getRootDirectory()
                    .listFiles()) {  //将所有文件和文件夹路径添加到usbFiles数组中

                if (file.getName().equals("LteFiles")) {
                    usbFiles.add(file);
                } else
                    ;
                mFileNames.add(file.getName());
            }
            if (!isGetFloderName()) {
                mFolder = fileSystem.getRootDirectory().createDirectory("LteFiles");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isGetFloderName() {

        for (int i = 0; i < mFileNames.size(); i++) {
            if (mFileNames.get(i).equals("LteFiles")) {
                mFolder = usbFiles.get(0);
                return true;
            }
        }
        return false;
    }

    public void getUsbFolders(FileSystem fileSystem) {
        mFolderNames.clear();
        try {
            for (UsbFile file : fileSystem.getRootDirectory()
                    .listFiles()) {  //将所有文件和文件夹路径添加到mFolderNames数组中
                mFolderNames.add(file.getName());
            }
            createProject();
            if (isNull) {
                mFolder = fileSystem.getRootDirectory().createDirectory("工程1");
            }else{
//                showToastMsg("enter 10: " + mFolder);
                judgeExist(fileSystem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createProject() {
        mProjectNames.clear();
        for (int i = 0; i < mFolderNames.size(); i++) {
            if (mFolderNames.get(i).substring(0, 2).equals("工程")) {
//                showToastMsg("enter 5: " + mFolder);
                mProjectNames.add(mFolderNames.get(i));
            }
        }
        isZero();
    }

    private boolean isNull = false;

    private void isZero() {
//        showToastMsg("enter 6: " + mFolder);
        mIndex.clear();
        if (mProjectNames.size() != 0) {
            isNull = false;
            for (int i = 0; i < mProjectNames.size(); i++) {
                mIndex.add(Integer.valueOf(mProjectNames.get(i).substring(2)));//string转 integer
//                showToastMsg("enter 7: " + mIndex.get(0));
            }
//            showToastMsg("enter 8: " + mFolder);
        } else {
            isNull = true;
        }
//        showToastMsg("enter 9: " + mFolder);
    }

    List<Integer> list = new ArrayList<Integer>();

    private void judgeExist(FileSystem fileSystem) {
//        showToastMsg("enter 11: " + mFolder);
        list.clear();
        for (int i = 0; i < mIndex.size(); i++) {
//            showToastMsg("enter 12: " + (mIndex.get(0).intValue()));
            list.add(new Integer( mIndex.get(i).intValue()));
//            showToastMsg("enter 12_1: " + mFolder);
        }
//        showToastMsg("enter 12_2: " + mFolder);
        Collections.sort(list);//排序
        for (int i = 0; i < list.size(); i++) {
            showToastMsg("enter 13: " + list.get(i));
        }

        end(fileSystem);
    }

    private void end(FileSystem fileSystem) {

        try {
            if (!list.get(0).equals(1) && list.size() == 1) {
                showToastMsg("enter 14: " + mFolder);
                mFolder = fileSystem.getRootDirectory().createDirectory("工程1");
            } else if (list.get(0).equals(1) && list.size() == 1){
                showToastMsg("enter 15: " + mFolder);
                mFolder = fileSystem.getRootDirectory().createDirectory("工程2");
            }else if (list.size()>1){
                showToastMsg("enter 16: " + mFolder);
                if (!list.get(0).equals(1)){
                    showToastMsg("enter 17: " + mFolder);
                    mFolder = fileSystem.getRootDirectory().createDirectory("工程1");
                }else{
                    for (int i = 0; i <list.size()-1 ; i++) {
                        if ((list.get(i+1)-list.get(i))==1 ){
                            if(list.get(i+1) == list.size()) {
                                showToastMsg("enter 18: " + list.get(i+1));
                                mFolder = fileSystem.getRootDirectory().createDirectory("工程" + (list.get(i + 1) + 1));
                            }
                        }else{
                            showToastMsg("enter 19: " + mFolder);
                            mFolder=fileSystem.getRootDirectory().createDirectory("工程"+(list.get(i)+1));
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void setcFloderValue(){
        cFolder = null;
    }
    //sunyan 2022.3.29 ddd begin
    private String usbPath = null;
    private Application application;


    /**
     * 注册监听U盘拔插广播
     */
    public void registerBroadcast() {
//        this.application = application;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);//如果SDCard未安装,并通过USB大容量存储共享返回
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);//表明sd对象是存在并具有读/写权限
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);//SDCard已卸掉,如果SDCard是存在但没有被安装
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);  //表明对象正在磁盘检查
        filter.addAction(Intent.ACTION_MEDIA_EJECT);  //物理的拔出 SDCARD
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);  //完全拔出
        filter.addDataScheme("file"); // 必须要有此行，否则无法收到广播
        registerReceiver(receiverU, filter);

        //在本app未启动前已经插着U盘的情况下，获取U盘路径
        List<String> list = getPathListByStorageManager();//根据StorageManager获取U盘路径
//        List<String> list = getPathByMount();//根据mount命令获取U盘路径
        if (list.size() > 0) this.usbPath = list.get(0);
        showToastMsg("USB:"+usbPath);
    }

    /**
     * 注销广播
     */
    public void unregisterBroadcast() {
        this.application.getApplicationContext().unregisterReceiver(receiverU);
    }

    /**
     * 获取U盘根路径
     */
    public String getUsbPath() {
        return this.usbPath;
    }

    /**
     * 检测U盘插入和拔出状态
     */
    private BroadcastReceiver receiverU = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {//可以读到/mnt/usb_storage/USB_DISK2/udisk0这个目录
                //U盘插入
                String path = intent.getData().getPath();
                path = getCorrectPath(path);//获取正确的，完整的路径
                usbPath = path;
//                Util.logE("------>U盘路径："+UsbFlashUtil.this.usbPath);
                if (diskListenerList.size() > 0) {
                    for (int i = 0; i < diskListenerList.size(); i++) {
                        if (null != diskListenerList.get(i)) diskListenerList.get(i).onConnect();
                    }
                }
            } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action) || Intent.ACTION_MEDIA_EJECT.equals(action)) {
                //U盘拔出
                usbPath = "";
                if (diskListenerList.size() > 0) {
                    for (int i = 0; i < diskListenerList.size(); i++) {
                        if (null != diskListenerList.get(i)) diskListenerList.get(i).onDisconnect();
                    }
                }
            }
        }
    };

    /**
     * 获取正确的U盘路径，才可以读写文件
     * 有些板子直接拿到的U盘路径是/mnt/usb_storage/udisk0（正确），/mnt/usb_storage/USB_DISK2（不完整）
     * 部分板子会出现"/mnt/usb_storage/USB_DISK2"的不完整路径，需要再追加子目录文件名，
     * 如追加上udisk0："/mnt/usb_storage/USB_DISK2/udisk0"，有的U盘命名过名字，追加的不一定是udisk0
     */
    private String getCorrectPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            int lastSeparator = path.lastIndexOf(File.separator);
            String endStr = path.substring(lastSeparator + 1, path.length());
            if (!TextUtils.isEmpty(endStr) && (endStr.contains("USB_DISK") || endStr.contains("usb_disk"))) {//不区分大小写
                File file = new File(path);
                if (file.exists() && file.listFiles().length == 1 && file.listFiles()[0].isDirectory()) {
                    path = file.listFiles()[0].getAbsolutePath();
                }
            }
        }
        return path;
    }


    /**
     * 根据StorageManager获取Usb插入的U盘路径
     * 可以获取内部存储、sd卡以及所有usb路径
     * 获取到的路径可能是不完整的，需要判断追加
     */
    private List<String> getPathListByStorageManager() {
        List<String> pathList = new ArrayList<>();
        try {
            StorageManager storageManager = (StorageManager) this.application.getSystemService(Context.STORAGE_SERVICE);
            Method method_volumeList = StorageManager.class.getMethod("getVolumeList");
            method_volumeList.setAccessible(true);
            Object[] volumeList = (Object[]) method_volumeList.invoke(storageManager);
            if (volumeList != null) {
                for (int i = 0; i < volumeList.length; i++) {
                    try {
                        String path = (String) volumeList[i].getClass().getMethod("getPath").invoke(volumeList[i]);
                        boolean isRemovable = (boolean) volumeList[i].getClass().getMethod("isRemovable").invoke(volumeList[i]);
                        String state = (String) volumeList[i].getClass().getMethod("getState").invoke(volumeList[i]);
//                        Util.logE("isRemovable:"+isRemovable+" / state:"+state+" / path:"+path);
                        if (isRemovable && "mounted".equalsIgnoreCase(state) && path.contains("usb_storage")) {
                            pathList.add(getCorrectPath(path));//将正确的路径添加到集合中
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pathList;
    }

    /**
     * 使用mount命令获取usb插入的U盘路径
     * 可以获取内部存储、外部存储、tf卡、otg、系统分区等路径，获取到的U盘路径是完整的正确的
     * 限制条件是机子必须得解开root
     */
    public static List<String> getPathByMount() {
        List<String> usbMemoryList = new ArrayList<>();
        try {
            Runtime runtime = Runtime.getRuntime();
            // 运行mount命令，获取命令的输出，得到系统中挂载的所有目录
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                // 将常见的linux分区过滤掉
                if (line.contains("proc") || line.contains("tmpfs") || line.contains("media") || line.contains("asec") || line.contains("secure") || line.contains("system") || line.contains("cache")
                        || line.contains("sys") || line.contains("data") || line.contains("shell") || line.contains("root") || line.contains("acct") || line.contains("misc") || line.contains("obb")) {
                    continue;
                }
//                Util.logE("==========>line:"+line);
                if (!TextUtils.isEmpty(line) && line.contains("usb_storage")) {//根据情况过来需要的字段
                    String items[] = line.split(" ");
                    if (null != items && items.length > 1) {
                        String path = items[1];
//                        Util.logE("------->path:"+path);
                        // 添加一些判断，确保是sd卡，如果是otg等挂载方式，可以具体分析并添加判断条件
                        if (path != null && !usbMemoryList.contains(path) && path.contains("sd"))
                            usbMemoryList.add(items[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usbMemoryList;
    }


    /**
     * U盘连接状态回调
     */
    public interface IUDiskListener {
        void onConnect();

        void onDisconnect();
    }

    private List<UsbFlashUtil.IUDiskListener> diskListenerList = new ArrayList<>();

    public void setUDiskListener(UsbFlashUtil.IUDiskListener uDiskListener) {
        diskListenerList.add(uDiskListener);
    }

    public void removeUDiskListener(UsbFlashUtil.IUDiskListener uDiskListener) {
        diskListenerList.remove(uDiskListener);
    }

    //sunyan 2022.3.29 add end


}

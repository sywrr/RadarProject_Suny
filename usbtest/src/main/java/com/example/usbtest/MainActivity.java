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
    //���������
    private EditText u_disk_edt;
    //д�뵽U��
    private Button u_disk_write;
    //��U�̶�ȡ
    private Button u_disk_read;
    //��ʾ��ȡ������
    private TextView u_disk_show;
    //�Զ���U�̶�дȨ��
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    //��ǰ����U���б�
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
                    showToastMsg(U_DISK_FILE_NAME + "�����ɹ�");
                    break;
                case 101:
                    String txt = msg.obj.toString();
                    if (!TextUtils.isEmpty(txt))
                        u_disk_show.setText("��ȡ���������ǣ�" + txt);
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
                        System.err.println("U��·��-1:");
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
                        System.err.println("U��·��-1:");
                    }
                });
            }
        });
    }


    /**
     * @description �������ݵ�U�̣�Ŀǰ�Ǳ��浽��Ŀ¼��
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
            showToastMsg("U��·3:"+cFolder);

        } else {
            showToastMsg("U��·��2:"+cFolder);
            getUsbFiles(currentFs);
            showToastMsg("U��·��4:"+cFolder);
        }
        showToastMsg("U��·��: " + mFolder);
        //��Ŀ��Ҳ���ļ���������SD������ʵ����ֱ�Ӱ��ı���ȡ��U��ָ���ļ�
        File file = FileUtil.getSaveFile("gprmeasurement/data/measured", U_DISK_FILE_NAME);
//        System.err.println("�ļ������ƣ�"+getmFolderPath()+" "+"�ļ����ƣ�"+getmFileName()+" "+"�ļ�·����"+file.toString());
        if (null != mFolder) {
            FileUtil.saveSDFile2OTG(file, mFolder);
            mHandler.sendEmptyMessage(100);
        }
    }

    /**
     * @description OTG�㲥ע��
     * @author ldm
     * @time 2017/9/1 17:19
     */
    private void registerUDiskReceiver() {
        //����otg���� �γ�
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mOtgReceiver, usbDeviceStateFilter);
        //ע������Զ���㲥
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mOtgReceiver, filter);
    }

    /**
     * @description OTG�㲥������U�̵Ĳ��뼰�γ�
     * @author ldm
     * @time 2017/9/1 17:20
     * @param
     */
    private BroadcastReceiver mOtgReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_USB_PERMISSION://���ܵ��Զ���㲥
//                    showToastMsg("111");
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    //����Ȩ������
                    //U�̲���
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            //�û�����Ȩ�����Խ��ж�ȡ����
                            readDevice(getUsbMass(usbDevice));
                        } else {
                            showToastMsg("û�в���U��");
                        }
                    } else {
                        showToastMsg("δ��ȡ��U��Ȩ��");
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED://���յ�U���豸����㲥
//                    showToastMsg("222");
                    UsbDevice device_add = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device_add != null) {
                        //���յ�U�̲���㲥�����Զ�ȡU���豸����
                        redUDiskDevsList();
                        showToastMsg("�Ѿ�����U��");

                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED://���յ�U�����豸�γ��㲥
                    showToastMsg("U���Ѱγ�");
                    mFolder = null;
                    break;
            }
        }
    };

    /**
     * @description U���豸��ȡ
     * @author ldm
     * @time 2017/9/1 17:20
     */
    private void redUDiskDevsList() {
        //�豸������
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        //��ȡU�̴洢�豸
        storageDevices = UsbMassStorageDevice.getMassStorageDevices(this);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        //һ���ֻ�ֻ��1��OTG���
        for (UsbMassStorageDevice device : storageDevices) {
            //��ȡ�豸�Ƿ���Ȩ��
            if (usbManager.hasPermission(device.getUsbDevice())) {
                readDevice(device);
            } else {
                //û��Ȩ�ޣ���������
                usbManager.requestPermission(device.getUsbDevice(), pendingIntent);
            }
        }
        if (storageDevices.length == 0) {
            showToastMsg("�������õ�U��");
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
            device.init();//��ʼ��
            //�豸����
            Partition partition = device.getPartitions().get(0);//��ʹ���豸�ĵ�һ������
            //�ļ�ϵͳ
            currentFs = partition.getFileSystem();
            currentFs.getVolumeLabel();//���Ի�ȡ���豸�ı�ʶ
            //ͨ��FileSystem���Ի�ȡ��ǰU�̵�һЩ�洢��Ϣ������ʣ��ռ��С�������ȵ�
            Log.e("Capacity: ", currentFs.getCapacity() + "");
            Log.e("Occupied Space: ", currentFs.getOccupiedSpace() + "");
            Log.e("Free Space: ", currentFs.getFreeSpace() + "");
            Log.e("Chunk size: ", currentFs.getChunkSize() + "");
//            cFolder = currentFs.getRootDirectory();//���õ�ǰ�ļ�����Ϊ��Ŀ¼
//          mFolder = cFolder.createDirectory("LteFile");
            String deviceName = currentFs.getVolumeLabel();//��ȡ�豸��ǩ

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //create file folder


    private void showToastMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //��̬��ȡȨ��
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
                    .listFiles()) {  //�������ļ����ļ���·����ӵ�usbFiles������

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
                    .listFiles()) {  //�������ļ����ļ���·����ӵ�mFolderNames������
                mFolderNames.add(file.getName());
            }
            createProject();
            if (isNull) {
                mFolder = fileSystem.getRootDirectory().createDirectory("����1");
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
            if (mFolderNames.get(i).substring(0, 2).equals("����")) {
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
                mIndex.add(Integer.valueOf(mProjectNames.get(i).substring(2)));//stringת integer
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
        Collections.sort(list);//����
        for (int i = 0; i < list.size(); i++) {
            showToastMsg("enter 13: " + list.get(i));
        }

        end(fileSystem);
    }

    private void end(FileSystem fileSystem) {

        try {
            if (!list.get(0).equals(1) && list.size() == 1) {
                showToastMsg("enter 14: " + mFolder);
                mFolder = fileSystem.getRootDirectory().createDirectory("����1");
            } else if (list.get(0).equals(1) && list.size() == 1){
                showToastMsg("enter 15: " + mFolder);
                mFolder = fileSystem.getRootDirectory().createDirectory("����2");
            }else if (list.size()>1){
                showToastMsg("enter 16: " + mFolder);
                if (!list.get(0).equals(1)){
                    showToastMsg("enter 17: " + mFolder);
                    mFolder = fileSystem.getRootDirectory().createDirectory("����1");
                }else{
                    for (int i = 0; i <list.size()-1 ; i++) {
                        if ((list.get(i+1)-list.get(i))==1 ){
                            if(list.get(i+1) == list.size()) {
                                showToastMsg("enter 18: " + list.get(i+1));
                                mFolder = fileSystem.getRootDirectory().createDirectory("����" + (list.get(i + 1) + 1));
                            }
                        }else{
                            showToastMsg("enter 19: " + mFolder);
                            mFolder=fileSystem.getRootDirectory().createDirectory("����"+(list.get(i)+1));
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
     * ע�����U�̰β�㲥
     */
    public void registerBroadcast() {
//        this.application = application;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);//���SDCardδ��װ,��ͨ��USB�������洢������
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);//����sd�����Ǵ��ڲ����ж�/дȨ��
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);//SDCard��ж��,���SDCard�Ǵ��ڵ�û�б���װ
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);  //�����������ڴ��̼��
        filter.addAction(Intent.ACTION_MEDIA_EJECT);  //����İγ� SDCARD
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);  //��ȫ�γ�
        filter.addDataScheme("file"); // ����Ҫ�д��У������޷��յ��㲥
        registerReceiver(receiverU, filter);

        //�ڱ�appδ����ǰ�Ѿ�����U�̵�����£���ȡU��·��
        List<String> list = getPathListByStorageManager();//����StorageManager��ȡU��·��
//        List<String> list = getPathByMount();//����mount�����ȡU��·��
        if (list.size() > 0) this.usbPath = list.get(0);
        showToastMsg("USB:"+usbPath);
    }

    /**
     * ע���㲥
     */
    public void unregisterBroadcast() {
        this.application.getApplicationContext().unregisterReceiver(receiverU);
    }

    /**
     * ��ȡU�̸�·��
     */
    public String getUsbPath() {
        return this.usbPath;
    }

    /**
     * ���U�̲���Ͱγ�״̬
     */
    private BroadcastReceiver receiverU = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {//���Զ���/mnt/usb_storage/USB_DISK2/udisk0���Ŀ¼
                //U�̲���
                String path = intent.getData().getPath();
                path = getCorrectPath(path);//��ȡ��ȷ�ģ�������·��
                usbPath = path;
//                Util.logE("------>U��·����"+UsbFlashUtil.this.usbPath);
                if (diskListenerList.size() > 0) {
                    for (int i = 0; i < diskListenerList.size(); i++) {
                        if (null != diskListenerList.get(i)) diskListenerList.get(i).onConnect();
                    }
                }
            } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action) || Intent.ACTION_MEDIA_EJECT.equals(action)) {
                //U�̰γ�
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
     * ��ȡ��ȷ��U��·�����ſ��Զ�д�ļ�
     * ��Щ����ֱ���õ���U��·����/mnt/usb_storage/udisk0����ȷ����/mnt/usb_storage/USB_DISK2����������
     * ���ְ��ӻ����"/mnt/usb_storage/USB_DISK2"�Ĳ�����·������Ҫ��׷����Ŀ¼�ļ�����
     * ��׷����udisk0��"/mnt/usb_storage/USB_DISK2/udisk0"���е�U�����������֣�׷�ӵĲ�һ����udisk0
     */
    private String getCorrectPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            int lastSeparator = path.lastIndexOf(File.separator);
            String endStr = path.substring(lastSeparator + 1, path.length());
            if (!TextUtils.isEmpty(endStr) && (endStr.contains("USB_DISK") || endStr.contains("usb_disk"))) {//�����ִ�Сд
                File file = new File(path);
                if (file.exists() && file.listFiles().length == 1 && file.listFiles()[0].isDirectory()) {
                    path = file.listFiles()[0].getAbsolutePath();
                }
            }
        }
        return path;
    }


    /**
     * ����StorageManager��ȡUsb�����U��·��
     * ���Ի�ȡ�ڲ��洢��sd���Լ�����usb·��
     * ��ȡ����·�������ǲ������ģ���Ҫ�ж�׷��
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
                            pathList.add(getCorrectPath(path));//����ȷ��·����ӵ�������
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
     * ʹ��mount�����ȡusb�����U��·��
     * ���Ի�ȡ�ڲ��洢���ⲿ�洢��tf����otg��ϵͳ������·������ȡ����U��·������������ȷ��
     * ���������ǻ��ӱ���ý⿪root
     */
    public static List<String> getPathByMount() {
        List<String> usbMemoryList = new ArrayList<>();
        try {
            Runtime runtime = Runtime.getRuntime();
            // ����mount�����ȡ�����������õ�ϵͳ�й��ص�����Ŀ¼
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                // ��������linux�������˵�
                if (line.contains("proc") || line.contains("tmpfs") || line.contains("media") || line.contains("asec") || line.contains("secure") || line.contains("system") || line.contains("cache")
                        || line.contains("sys") || line.contains("data") || line.contains("shell") || line.contains("root") || line.contains("acct") || line.contains("misc") || line.contains("obb")) {
                    continue;
                }
//                Util.logE("==========>line:"+line);
                if (!TextUtils.isEmpty(line) && line.contains("usb_storage")) {//�������������Ҫ���ֶ�
                    String items[] = line.split(" ");
                    if (null != items && items.length > 1) {
                        String path = items[1];
//                        Util.logE("------->path:"+path);
                        // ���һЩ�жϣ�ȷ����sd���������otg�ȹ��ط�ʽ�����Ծ������������ж�����
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
     * U������״̬�ص�
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

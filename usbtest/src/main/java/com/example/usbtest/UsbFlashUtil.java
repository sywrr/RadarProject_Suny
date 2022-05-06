package com.example.usbtest;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Date:2020/06/18
 * Time:15:10
 * Author:QiuShanwen
 * <p>
 * ��ȡU�̵ĸ�·��
 */
public class UsbFlashUtil {
    private static UsbFlashUtil instance;
    private String usbPath = null;
    private Application application;

    public static UsbFlashUtil getInstance() {
        if (null == instance) {
            synchronized (UsbFlashUtil.class) {
                if (null == instance) {
                    instance = new UsbFlashUtil();
                }
            }
        }
        return instance;
    }


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
        this.application.getApplicationContext().registerReceiver(receiverU, filter);

        //�ڱ�appδ����ǰ�Ѿ�����U�̵�����£���ȡU��·��
        List<String> list = getPathListByStorageManager();//����StorageManager��ȡU��·��
//        List<String> list = getPathByMount();//����mount�����ȡU��·��
        if (list.size() > 0) UsbFlashUtil.this.usbPath = list.get(0);
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
        return UsbFlashUtil.this.usbPath;
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
                UsbFlashUtil.this.usbPath = path;
//                Util.logE("------>U��·����"+UsbFlashUtil.this.usbPath);
                if (diskListenerList.size() > 0) {
                    for (int i = 0; i < diskListenerList.size(); i++) {
                        if (null != diskListenerList.get(i)) diskListenerList.get(i).onConnect();
                    }
                }
            } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action) || Intent.ACTION_MEDIA_EJECT.equals(action)) {
                //U�̰γ�
                UsbFlashUtil.this.usbPath = "";
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

    private List<IUDiskListener> diskListenerList = new ArrayList<>();

    public void setUDiskListener(IUDiskListener uDiskListener) {
        diskListenerList.add(uDiskListener);
    }

    public void removeUDiskListener(IUDiskListener uDiskListener) {
        diskListenerList.remove(uDiskListener);
    }


}

package com.ltdpro;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.net.ParseException;

/*
 * ʹ�õ���ģʽ��ֻ����һ���ļ���ÿ��д�븲���ϴε����ݣ�ֻ���浱ǰ�������־hss20140922
 */
public class LogWriter {
    private static LogWriter mLogWriter;

    private static String mPath;

    private static Writer mWriter;

    private static SimpleDateFormat df;

    private static int LOGSIZE = 15;

    private LogWriter(String file_path) {
        LogWriter.mPath = file_path;
        LogWriter.mWriter = null;
    }

    //�ṩ����Դ���ļ��б�
    private static List<File> list = new LinkedList<File>();

    //����������ʵ���ӿ�
    public static LogWriter open() throws IOException {
        String filePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String timeName = getTimeString();
        String fileName = "log_" + timeName + ".txt";
        String folder = filePath + File.separator + "log";
        String fullfilePath = folder + File.separator + fileName;
        manageFolder(folder);//�����ļ��У��ؽ�ɾ��������Ŀ��log��

        if (mLogWriter == null) {
            mLogWriter = new LogWriter(fullfilePath);
        } else
            ;

        mWriter = new BufferedWriter(new FileWriter(mPath), 2048);
        df = new SimpleDateFormat("[yy-MM-dd hh:mm:ss]:");
        return mLogWriter;
    }

    //�����ļ���
    private static void manageFolder(String folder) {
        File dir = new File(folder);
        if (!dir.exists())
            dir.mkdir();
        else {
            //��������������N������ɾ�������N��
            File[] subFiles = dir.listFiles();
            //�����ļ��б�
            if (subFiles != null) {
                list.clear();
                for (File f : subFiles)
                    list.add(f);
            } else
                ;

            //����size�ĸ���������С���ļ��ᱻ����
            if (list.size() > LOGSIZE) {
                SortByDescend sortdescend = new SortByDescend();
                Collections.sort(list, sortdescend);
                for (int i = 0; i < list.size(); i++)
                    DebugUtil.i("LogWriter", "list[" + i + "]=" + list.get(i));
                for (int i = LOGSIZE; i < list.size(); i++) {
                    boolean st = list.get(i).delete();
                    if (!st)
                        DebugUtil.e("LogWriter", list.get(i).getName() + ",delete error!");
                    else
                        ;
                }
            } else
                ;
        }
    }

    //����
    static class SortByDescend implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            String s1 = getFileDate(f1);
            String s2 = getFileDate(f2);
            int ret = 0;
            try {
                ret = compareDate(s1, s2);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (java.text.ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return (ret);
        }
    }

    //�ر�д���ļ�
    public void close() throws IOException {
        mWriter.close();
    }

    public void print(String log) throws IOException {
        try {
            mWriter.write(df.format(new Date()));
            mWriter.write(log);
            mWriter.write("\n");
            mWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void print(String log, String output) throws IOException {
        try {
            DebugUtil.i("DebugUtilWriter", "print log,short output=" + output);
            mWriter.write(df.format(new Date()));
            mWriter.write(output);
            mWriter.write("\n");
            mWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void print(Class cls, String log) throws IOException { //������뿴�����ĸ�����������������
        try {
            mWriter.write(df.format(new Date()));
            mWriter.write(cls.getSimpleName() + " ");
            mWriter.write(log);
            mWriter.write("\n");
            mWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //�õ���ʱ���������ַ���
    public static String getTimeString() {
        //String timeString = null;
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeString = sDateFormat.format(new java.util.Date());

        return timeString;
    }

    public static int compareDate(String time1, String time2)
            throws ParseException, java.text.ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        Date a = sdf.parse(time1);
        Date b = sdf.parse(time2);
        if ((b.getTime() - a.getTime() > 0))
            return 1;
        else
            return -1;
    }

    //�õ��ļ��м������
    public static String getFileDate(File f1) {
        String s1 = f1.getName().toString();
        s1 = s1.substring(s1.lastIndexOf("_") + 1);
        return s1;
    }

    //���ڴ��н���logcat���ļ���
    public boolean beginLogcat() {
        String filePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String timeName = getTimeString();
        String fileName = "log_" + timeName + ".txt";
        String folder = filePath + File.separator + "logcat";
        String fullfilePath = folder + File.separator + fileName;

        File dir = new File(folder);
        if (!dir.exists())
            dir.mkdir();
        else
            ;

        String cmd = "logcat -v time > " + fullfilePath + " &\n";
        do_exec(cmd);
        return true;
    }

    private String dmesgFilePathString = null;

    /*
     * ��ȡdmesg�ں���Ϣ
     */
    public boolean beginDmesg() {
        String filePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String timeName = getTimeString();
        String fileName = "dmesg_" + timeName + ".txt";
        String folder = filePath + File.separator + "dmesg";
        dmesgFilePathString = folder + File.separator + fileName;

        manageFolder(folder);//�����ļ��У��ؽ�ɾ��������Ŀ��log��

        File dir = new File(folder);
        if (!dir.exists())
            dir.mkdir();
        else
            ;

        return true;
    }

    /*
     * ִ��dmesgд���ڴ�
     */
    public boolean writeDmesg() {
        String cmd = "dmesg >>" + dmesgFilePathString + " -c &\n";
        if (do_exec(cmd) != null)
            return true;
        else
            return false;
    }

    //logcat��ӡ
    //ִ��execָ��
    //��root��ǰ���£����Ȼ�ȡsuȨ�ޣ�ÿ��ָ�������Ҫ�س���ִ�У����exit�˳�
    public String do_exec(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(cmd);
            os.writeBytes("exit\n");
            os.flush();   
            
            /*            
             * BufferedReader in = new BufferedReader(  
                                new InputStreamReader(p.getInputStream()));  
            String line = null;  
            StringBuffer stringBuffer = new StringBuffer();
            while ((line = in.readLine()) != null) 
            {  
                stringBuffer.append(line+"-");              
            } 
            System.out.println("cmd="+stringBuffer.toString());*/

        } catch (IOException e) {
            // TODO Auto-generated catch block  
            e.printStackTrace();
        }

        return cmd;
    }

    //����logcat
    public boolean endLogcat() {
        do_exec("killall logcat\n");
        return true;
    }

}
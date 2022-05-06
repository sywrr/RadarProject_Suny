package Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

public class Utils {

    public static native void byteCopy(short[] dest, int index, short[] src, int start, int finish);

    public static native void byteCopy(byte[] dest, int index, byte[] src, int start, int finish);

    public static native void byteCopy(int[] dest, int index, int[] src, int start, int finish);

    public static void intToBytes(int data, byte[] b) {
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (data & 0xff);
            data >>= 8;
        }
    }

    public static void intToBytes(int data, byte[] b, int offset) {
        for (int i = offset; i < b.length; i++) {
            b[i] = (byte) (data & 0xff);
            data >>= 8;
        }
    }

    public static int bytesToInt(byte[] b) {
        int data = 0;
        for (int i = 0; i < 4; i++) {
            data |= (b[i] & 0xff) << (8 * i);
        }
        return data;
    }

    public static int bytesToInt(byte[] b, int offset) {
        int data = 0;
        for (int i = 0; i < 4; i++) {
            data |= (b[i + offset] & 0xff) << (8 * i);
        }
        return data;
    }

    public static void shortToBytes(short data, byte[] b) {
        b[0] = (byte) (data & 0xff);
        data >>= 8;
        b[1] = (byte) (data & 0xff);
    }

    public static void shortToBytes(short data, byte[] b, int offset) {
        b[offset] = (byte) (data & 0xff);
        data >>= 8;
        b[offset + 1] = (byte) (data & 0xff);
    }

    public static short bytesToShort(byte[] b) {
        short data = 0;
        for (int i = 0; i < 2; i++) {
            data |= (b[i] & 0xff) << (8 * i);
        }
        return data;
    }

    public static short bytesToShort(byte[] b, int offset) {
        short data = 0;
        for (int i = 0; i < 2; i++) {
            data |= (b[i + offset] & 0xff) << (8 * i);
        }
        return data;
    }

    private static <T> Object cloneObject(T val) {
        Class<?> cls = val.getClass();
        Method method = null;
        try {
            method = cls.getMethod("clone");
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        Object obj = null;
        try {
            obj = method.invoke(val);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    private static <T> Object serializeObject(T val) {
        try {
            ByteArrayOutputStream baos = null;
            ObjectOutputStream oos = null;
            try {
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(val);
                oos.flush();
            } finally {
                if (oos != null)
                    oos.close();
            }
            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;
            try {
                bais = new ByteArrayInputStream(baos.toByteArray());
                ois = new ObjectInputStream(bais);
                return ois.readObject();
            } finally {
                if (ois != null)
                    ois.close();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isInterface(Class<?> cls, Class<?> Interface) {
        if (cls != null) {
            String name = Interface.getName();
            Class<?>[] clsList = cls.getInterfaces();
            for (Class<?> iCls : clsList) {
                if (iCls.getName().equals(name))
                    return true;
            }
            return isInterface(cls.getSuperclass(), Interface);
        }
        return false;
    }

    public static <T> Object clone(T val) {
        Class<?> cls = val.getClass();
        if (isInterface(cls, Cloneable.class))
            return cloneObject(val);
        if (isInterface(cls, Serializable.class))
            return serializeObject(val);
        return val;
    }

    public static void joinThreadUninterruptibly(Thread t) {
        if (t == null)
            return;
        while (t.isAlive()) {
            try {
                t.join();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

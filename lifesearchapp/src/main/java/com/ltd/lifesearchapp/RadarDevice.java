package com.ltd.lifesearchapp;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

public class RadarDevice {

    String TAG = "RadarDevice";
    private Context mContext = null;
    private short mStatus;         //设备状态
    private short mDetectRange;    //探测范围（米）
    private short mDetingRangeL;   //正在探测范围的起始位置
    private short mDetingRangeH;   //正在探测范围的终止位置
    private short mBefBackShowMode;       //回放前的状态
    public boolean mExistBackShowTarget;   //存在回放目标标志
    public short mBackShowTargetPos;     //回放目标的距离
    public short mNowBegPos;             //当前的起始位置
    private short mDetectMode;            //探测模式
    private short mScanLen = 2048;
    private short mTimeWnd = 20;

    //存放雷达数据的缓冲区
    private byte[][] mDatasBufs;
    private int mBufIndex;
    private int mRBufIndex;
    private int mBufsNumber = 4;
    private int mBufLength = 2048 * 128 * 2;
    private int[] mNowWPos = new int[mBufsNumber];
    private int[] mNowRPos = new int[mBufsNumber];
    public long mHadRcvScans = 0;
    private short[] mOneScanDatas = new short[8192];

    //组合原始数据
    private short mComBufLen = 2048 * 4;
    private short[] mComBuf = new short[mComBufLen];
    private int mComBufWPos = 0;
    private int mBefZeroPos = 0;
    private int mScanCopyPos = 0;
    private short[] mScanComBuffer = new short[2048];

    private short[] mDibBuf = new short[mComBufLen];
    private int mDibBufWPos = 0;

    //保存文件需要的参数
    private int mNowFileindex = 0;          //文件索引
    private boolean mExistSaveFile = false;   //是否存在保存文件
    private FileOutputStream mSaveOS = null;      //
    private FileHeader mFileHeader = new FileHeader();
    public String mSavingFilePath;
    //sd卡路径
    public String mSDCardPath;
    //雷达数据文件夹路径
    public String mLTEFilefolderPath = "/LteFiles/";

    public RadarDevice() {
        // TODO Auto-generated constructor stub
        mStatus = Global.RADAR_STATUS_NOTREADY;
        mDetectRange = Global.DEFAULT_DETECTRANGE;
        mDetingRangeL = 0;
        mDetingRangeH = Global.DEFAULT_DETECTRANGE;
        mNowBegPos = 0;
        //
        mDetectMode = Global.MANY_DETECTMODE;

        //
        mDatasBufs = new byte[mBufsNumber][];
        mBufIndex = 0;
        mRBufIndex = 0;
        for (int i = 0; i < mBufsNumber; i++) {
            mDatasBufs[i] = new byte[mBufLength];
            mNowWPos[i] = 0;
            mNowRPos[i] = 0;
        }
        //
        mSDCardPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.i(TAG, mSDCardPath);
        //
        mFileHeader.rh_nsamp = (short) mScanLen;
        mFileHeader.rh_range = mTimeWnd;

        //生成参数文件夹
        String pathName;

        //生成雷达数据文件夹
        pathName = mSDCardPath + mLTEFilefolderPath;
        File lteDir = new File(pathName);
        if (!lteDir.exists()) {
            lteDir.mkdirs();
        }
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public int getScanLen() {
        return mScanLen;
    }

    public int getTimeWnd() {
        return mTimeWnd;
    }

    public void setStatus(short status) {
        if ((mStatus == Global.RADAR_STATUS_BACKSHOWING) && (status == Global.RADAR_STATUS_READY))
            return;
        mStatus = status;
    }

    public short getStatus() {
        return mStatus;
    }

    public boolean isCanBeginDetect() {
        return mStatus == Global.RADAR_STATUS_READY ? true : false;
    }

    public boolean isCanStopDetect() {
        return mStatus == Global.RADAR_STATUS_DETECTING ? true : false;
    }

    public boolean isCanSaveData() {
        return mStatus == Global.RADAR_STATUS_READY ? true : false;
    }

    public boolean isCanStopSave() {
        return mStatus == Global.RADAR_STATUS_SAVING ? true : false;
    }

    public void setDetectRange(short range) {
        mDetectRange = range;
    }

    public short getDetectRange() {
        return mDetectRange;
    }

    public void setDetingRange(short begPos, short endPos) {
        mDetingRangeL = begPos;
        mDetingRangeH = endPos;
        if (mDetingRangeH > mDetectRange)
            mDetingRangeH = mDetectRange;

        //		Log.e(TAG, "mDetingRangeL = " + String.valueOf(mDetingRangeL) + "mDetingRangeH = "
        //		+ String.valueOf(mDetingRangeH) + "mDetectRange = " + String.valueOf
        //		(mDetectRange));
    }

    public boolean isDeting() {
        return mStatus == Global.RADAR_STATUS_DETECTING;
    }

    public boolean isCanContinueSaveData() {
        return mStatus == Global.RADAR_STATUS_READY ? true : false;
    }

    public boolean isCanStopContinueSave() {
        return mStatus == Global.RADAR_STATUS_CONSAVING ? true : false;
    }

    //判断是否可以读取数据文件
    public boolean isCanReadFiles() {
        return mStatus == Global.RADAR_STATUS_READY ? true : false;
    }

    //检测设备是否处于没有就绪状态
    public boolean isNoReady() {
        return mStatus == Global.RADAR_STATUS_NOTREADY;
    }

    //增加探测范围
    public boolean addDetectRange() {
        mDetectRange += Global.DEFAULT_SCAN_RANGE;
        if (mDetectRange > Global.MAX_DETECTRANGE)
            mDetectRange = Global.MAX_DETECTRANGE;//MIX_DETECTRANGE;
        return true;
    }

    //得到正在探测的范围
    public short[] getDetectingRange() {
        short[] range = new short[2];
        range[0] = mDetingRangeL;
        range[1] = mDetingRangeH;

        return range;

        //		Log.e(TAG, String.valueOf(low) + String.valueOf(high));
    }

    public void delDetectRange() {
        mDetectRange -= Global.DEFAULT_SCAN_RANGE;
        if (mDetectRange < Global.MIX_DETECTRANGE)
            mDetectRange = Global.MIX_DETECTRANGE;//MIX_DETECTRANGE;
    }

    public boolean isBackShowing() {
        return mStatus == Global.RADAR_STATUS_BACKSHOWING;
    }

    public void StopBackShow() {
        mStatus = mBefBackShowMode;
    }

    public boolean isCanEnterBackShow() {
        return mStatus != Global.RADAR_STATUS_DETECTING;
    }

    public void setBackShowTarget(short distance, boolean existTarget) {
        mBackShowTargetPos = distance;
        mExistBackShowTarget = existTarget;
    }

    public void enterBackShow() {
        mBefBackShowMode = mStatus;
        mStatus = Global.RADAR_STATUS_BACKSHOWING;
    }

    public boolean isSingleTargetMode() {
        return mDetectMode == Global.SINGLE_DETECTMODE;
    }

    public boolean isMultiTargetMode() {
        return mDetectMode == Global.MANY_DETECTMODE;
    }

    public void setDetectMode(short mode) {
        mDetectMode = mode;
    }

    public void setBegPos(short begpos) {
        mNowBegPos = begpos;
    }

    public short getBegPos() {
        return mNowBegPos;
    }

    public void recvDatas(byte[] Bufs, int size) {
        if (mComBufWPos + size / 2 <= mComBufLen) {
            for (int i = 0; i < size / 2; i++) {
                mComBuf[mComBufWPos + i] = (short) (Global.getUByte(Bufs[i * 2]) + (Global.getUByte(
                        Bufs[i * 2 + 1]) << 8));
            }
            mComBufWPos += size / 2;

            if (mComBufWPos >= mComBufLen) {
                manageDatas(mComBuf, mComBufWPos * 2);
                mComBufWPos = 0;
            }
        } else {
            manageDatas(mComBuf, mComBufWPos * 2);
            mComBufWPos = 0;
        }
    }

    private int manageDatas(short[] Bufs, int size) {
        int dRet = 0;
        int rcvLen = size / 2;

        short scanL1;
        int i;
        //
        int scanCopyPos = 0;
        int copyLen;

        ////寻找数据头
        int flagNum;
        int[] flagPos = new int[100];
        flagNum = 0;
        scanL1 = mScanLen;
        for (i = 0; i < rcvLen; i++) {
            if (Bufs[i] == 0x7FFF) {
                flagPos[flagNum] = i;
                flagNum++;
                //
            }
        }
        //////对标记做处理
        //1:没有数据头，返回
        int managePos;
        if (flagNum == 0) {
            Log.e(TAG, "No Find  FLAG !");
            return size;
        }
        //2:一个数据头
        if (flagNum == 1) {
            managePos = flagPos[0];
            scanCopyPos = mScanCopyPos;
            if (scanCopyPos + managePos != scanL1)
            //不能组合成一道数据
            {
                Log.e(TAG, "Step1: copyPos:" + scanCopyPos + ", managePos:" + managePos);
                scanCopyPos = 0;
                /////将剩余部分数据拷贝到合适位置
                if (rcvLen - managePos == scanL1) {
                    manageOneScan(Bufs, managePos, scanL1);
                    return rcvLen * 2;
                }
                copyLen = (rcvLen - managePos) * 2;

                for (int j = 0; j < copyLen / 2; j++) {
                    mScanComBuffer[j] = Bufs[managePos + j];
                }
                mScanCopyPos = copyLen / 2;
            } else            //组合成一道数据
            {
                /////组合迁移到数据
                copyLen = managePos * 2;
                for (int j = 0; j < copyLen / 2; j++) {
                    mScanComBuffer[scanCopyPos + j] = Bufs[j];
                }

                manageOneScan(mScanComBuffer, 0, scanL1);
                mScanCopyPos = 0;

                /////将剩余部分数据拷贝到合适位置
                if (rcvLen - managePos > scanL1) {
                    return rcvLen * 2;
                }
                copyLen = (rcvLen - managePos) * 2;
                for (int j = 0; j < copyLen / 2; j++) {
                    mScanComBuffer[j] = Bufs[managePos + j];
                }
                mScanCopyPos = copyLen / 2;
            }
            //
            return rcvLen * 2;
        }
        ////3:多个数据头
        //对第一个标记进行处理
        {
            managePos = flagPos[0];
            scanCopyPos = mScanCopyPos;
            if (scanCopyPos + managePos != scanL1)            //不能组合成一道数据
            {
                scanCopyPos = 0;
            } else        //组合成一道数据
            {
                /////组合迁移到数据
                copyLen = managePos * 2;

                for (int j = 0; j < copyLen / 2; j++) {
                    mScanComBuffer[scanCopyPos + j] = Bufs[j];
                }

                manageOneScan(mScanComBuffer, 0, scanL1);
                mScanCopyPos = 0;
            }
        }
        //对中间的标记进行处理
        int befManagePos;
        for (i = 1; i < flagNum - 1; i++) {
            befManagePos = flagPos[i - 1];
            managePos = flagPos[i];

            //
            if (managePos - befManagePos != scanL1) {
                continue;
            }

            //
            manageOneScan(Bufs, befManagePos, scanL1);
        }
        //对最后一个标记进行处理
        {
            managePos = flagPos[flagNum - 1];
            ////处理前一个
            befManagePos = flagPos[flagNum - 2];
            //
            if (managePos - befManagePos == scanL1) {
                manageOneScan(Bufs, befManagePos, scanL1);
            }
            /////将数据拷贝到组合位置
            //1:剩余数据大于1道 长度
            if (rcvLen - managePos >= scanL1) {
                manageOneScan(Bufs, managePos, scanL1);
                return rcvLen * 2;
            }
            ////拷贝剩余数据
            copyLen = rcvLen - managePos;
            copyLen *= 2;
            for (int k = 0; k < copyLen / 2; k++) {
                mScanComBuffer[mScanCopyPos + k] = Bufs[managePos + k];
            }
            mScanCopyPos = copyLen / 2;
        }
        dRet = rcvLen * 2;
        ;

        return dRet;
    }

    ////从设备读取指定长度的数据
    //    size:要读取的数据长度
    //      返回:读取到的数据长度
    public int manageOneScan(short[] Bufs, int offset, int size) {
        ////对标记进行处理
        int i;
        int j;
        int rLen = size * 2;

        mHadRcvScans++;
        //		Log.e(TAG, "recv scans = " + mHadRcvScans);

        ////
        int bufIndex;
        int wPos;
        int wLength;
        int addNum;
        addNum = 0;
        bufIndex = mBufIndex;
        wPos = mNowWPos[bufIndex];
        wLength = rLen;
        if (wPos + wLength > mBufLength) {
            wLength = mBufLength - wPos;
            //			for(i=0; i<wLength; i++)
            //			{
            //				mDatasBufs[bufIndex][wPos + i] = Bufs[i];
            //			}

            for (i = 0; i < wLength / 2; i++) {
                mDatasBufs[bufIndex][wPos + i * 2] = (byte) Bufs[i + offset];
                mDatasBufs[bufIndex][wPos + i * 2 + 1] = (byte) (Bufs[i + offset] >> 8);
            }

            //			saveDatasToSDCard(bufIndex);

            mNowWPos[bufIndex] = mBufLength;
            addNum = wLength;
            //
            wLength = rLen - wLength;
            bufIndex++;
            bufIndex = bufIndex % mBufsNumber;
            mBufIndex = bufIndex;
            mNowWPos[bufIndex] = 0;
            wPos = 0;
            Log.e(TAG, "Now write bufIndex = " + bufIndex);
        }
        //拷贝剩余的数据
        if (wLength < 0)
            wLength = 0;
        //		for(i=0; i<wLength; i++)
        //		{
        //			mDatasBufs[bufIndex][wPos + i] = Bufs[i + addNum];
        //		}

        for (i = 0; i < wLength / 2; i++) {
            mDatasBufs[bufIndex][wPos + i * 2] = (byte) Bufs[i + offset + addNum / 2];
            mDatasBufs[bufIndex][wPos + i * 2 + 1] = (byte) (Bufs[i + offset + addNum / 2] >> 8);
        }

        mNowWPos[bufIndex] += wLength;

        //		Log.e(TAG, "bufIndex = " + bufIndex + ",mNowWPos = " + mNowWPos[bufIndex]);

        int k = 0;
        for (k = 0; k < mScanLen; k++) {
            mOneScanDatas[k] = mDibBuf[mDibBufWPos + k] = Bufs[k + offset];
        }

        mDibBufWPos += mScanLen;

        if (mDibBufWPos >= mComBufLen) {
            mDibBufWPos = 0;
//            ((MainActivity) mContext).mDibView.changeDatasToDIB(mDibBuf, mComBufLen * 2);
//            ((MainActivity) mContext).mDibView.invalidate();
        }
        return rLen;
    }

    public short[] readRadarDatas(int needLen) {
        int retLen = 0;
        //得到道长
        int scanLen = mScanLen * 2;

        int bufIndex;
        int rPos, wPos;
        int rLength;
        bufIndex = mRBufIndex;
        wPos = mNowWPos[bufIndex];
        rPos = mNowRPos[bufIndex];
        rLength = needLen;

        int temp = mBufIndex;
        if (rPos + rLength > wPos) {
            rLength = wPos - rPos;
        }
        rLength = rLength / scanLen * scanLen;

        if (rLength < 0) {
            Log.e(TAG, "negtive length!");
        }
        short[] buf = new short[rLength / 2];
        if (rLength == 0) {
            return buf;
        }


        //读取数据
        for (int i = 0; i < rLength / 2; i++) {

            buf[i] = (short) (Global.getUByte(mDatasBufs[bufIndex][rPos + i * 2]) +
                              (Global.getUByte(mDatasBufs[bufIndex][rPos + i * 2 + 1]) << 8));
        }

        //改变读取指针位置
        mNowRPos[bufIndex] = rPos + rLength;
        if (buf[0] != 32767) {
            //			Log.e(TAG, "11111111111111111111");
        }
        if (mNowRPos[bufIndex] >= mBufLength) {
            mNowWPos[bufIndex] = 0;
            bufIndex++;
            bufIndex %= mBufsNumber;
            mRBufIndex = bufIndex;
            mNowRPos[bufIndex] = 0;
            Log.e(TAG, "Now read bufIndex = " + bufIndex);
        }

        //
        retLen = rLength;
        if (retLen >= scanLen) {
            for (int k = 0; k < mScanLen; k++) {
                mOneScanDatas[k] = buf[k + (retLen / 2 - mScanLen)];
            }
        }


        //		Log.e(TAG, "///" + mNowRPos[bufIndex] + "---" + mNowWPos[bufIndex] + "----" +
        //		retLen);
        return buf;

    }

    //将一个缓冲区的数据保存到sd卡中
    public void saveDatasToSDCard(int bufIndex) {
        if (mExistSaveFile) {
            try {
                mSaveOS.write(mDatasBufs[bufIndex], 0, mBufLength);
            } catch (Exception e) {
                Toast.makeText(mContext, "保存缓冲区" + bufIndex + "错误", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //进行最后一次数据保存
    public void lastSaveDatas() {
        if (mExistSaveFile) {
            int bufIndex = mBufIndex;
            int pos = mNowWPos[bufIndex];

            try {
                mSaveOS.write(mDatasBufs[bufIndex], 0, pos);
            } catch (Exception e) {
                Toast.makeText(mContext, "保存缓冲区" + bufIndex + "错误", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //得到最近一道数据
    public short[] getRecentScanDatas() {
        //
        return mOneScanDatas;
    }

    //生成新的保存数据文件
    public boolean createNewDatasFile() {
        //生成文件夹
        String folderPath;
        folderPath = mSDCardPath + mLTEFilefolderPath;
        //生成保存文件句柄
        if (!mExistSaveFile) {
            try {
                String fileName = createNewFileName();
                mSaveOS = new FileOutputStream(fileName);
                mSavingFilePath = fileName;
                //
                mExistSaveFile = true;
                refreshFileHeader();
                mFileHeader.save(mSaveOS);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    //生成新的数据文件名
    public String createNewFileName() {
        return createNewFileName_ByIndex();
    }

    //根据索引生成新文件名
    public String createNewFileName_ByIndex() {
        String fileName;
        int index = 1;
        do {
            fileName = "/ltefile" + index + ".lte";
            fileName = android.os.Environment.getExternalStorageDirectory() + mLTEFilefolderPath +
                       fileName;
            File file = new File(fileName);
            if (!file.exists())
                break;
            index++;
        } while (true);
        //
        mNowFileindex = index;
        Log.i(TAG, "createNewFileName:" + fileName);
        return fileName;
    }

    //得到保存数据文件名
    public String getSaveFilename() {
        return mSavingFilePath;
    }

    //根据当前的设备状态，更新文件头内容
    public void refreshFileHeader() {
        mFileHeader.rh_data = 0;
        mFileHeader.rh_nsamp = (short) mScanLen;
        mFileHeader.rh_zero = 0;
        //		mFileHeader.rh_sps = mScanSpeed;
        //		mFileHeader.rh_position = mSignalPos;
        //		mFileHeader.rh_range = mTimeWnd;
        //		mFileHeader.rh_spp = (short)mAntennaFrqVal;
        //		mFileHeader.rh_nrgain = 9;
        //		mFileHeader.rh_epsr = (float)mJDConst;
        //		for(int i=0; i<9; i++)
        //		{
        //			mFileHeader.rh_rgainf[i] = mHardPlus[i];
        //		}
    }

    public void initBuf() {
        //设置内存变量
        mBufIndex = 0;
        mRBufIndex = 0;
        for (int i = 0; i < mBufsNumber; i++) {
            mNowWPos[i] = 0;
            mNowRPos[i] = 0;
        }
        mHadRcvScans = 0;
        mBefZeroPos = 0;
        mScanCopyPos = 0;
        mComBufWPos = 0;
        mDibBufWPos = 0;
    }

    //开始保存数据
    public boolean beginSave() {
        if (getSDFreeSpace() < 40) {
            Toast.makeText(mContext, "SD_CARD Space < 40M!", Toast.LENGTH_SHORT).show();
            return false;
        }
        //生成保存文件
        if (!createNewDatasFile()) {
            Toast.makeText(mContext, "生成保存文件错误!", Toast.LENGTH_SHORT).show();
            return false;
        }
        String txt;
        txt = "开始保存数据文件:" + getSaveFilename() + "!";
        Toast.makeText(mContext, txt, Toast.LENGTH_LONG).show();

        initBuf();

        return true;
    }

    //停止保存数据
    public boolean stopSave() {
        Toast.makeText(mContext, "保存数据结束!", Toast.LENGTH_LONG).show();
        //进行最后一次保存
        lastSaveDatas();

        initBuf();
        //
        if (mExistSaveFile) {
            try {
                mSaveOS.close();
                //
                mExistSaveFile = false;
            } catch (Exception e) {
                Toast.makeText(mContext, e.getMessage(), 1000).show();
            }
        }

        //
        return true;
    }

    //
    public double getSDFreeSpace() {
        String state = Environment.getExternalStorageState();
        double freeSpace = 0;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            StatFs fs = new StatFs(mSDCardPath);
            int blockSize = fs.getBlockSize();
            int blockCount = fs.getBlockCount();
            int availCount = fs.getAvailableBlocks();
            freeSpace = 1.0 * availCount * blockSize / 1024 / 1024;  // 单位Mb
        }

        return freeSpace;
    }
}

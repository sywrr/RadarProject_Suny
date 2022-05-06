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
    private short mStatus;         //�豸״̬
    private short mDetectRange;    //̽�ⷶΧ���ף�
    private short mDetingRangeL;   //����̽�ⷶΧ����ʼλ��
    private short mDetingRangeH;   //����̽�ⷶΧ����ֹλ��
    private short mBefBackShowMode;       //�ط�ǰ��״̬
    public boolean mExistBackShowTarget;   //���ڻط�Ŀ���־
    public short mBackShowTargetPos;     //�ط�Ŀ��ľ���
    public short mNowBegPos;             //��ǰ����ʼλ��
    private short mDetectMode;            //̽��ģʽ
    private short mScanLen = 2048;
    private short mTimeWnd = 20;

    //����״����ݵĻ�����
    private byte[][] mDatasBufs;
    private int mBufIndex;
    private int mRBufIndex;
    private int mBufsNumber = 4;
    private int mBufLength = 2048 * 128 * 2;
    private int[] mNowWPos = new int[mBufsNumber];
    private int[] mNowRPos = new int[mBufsNumber];
    public long mHadRcvScans = 0;
    private short[] mOneScanDatas = new short[8192];

    //���ԭʼ����
    private short mComBufLen = 2048 * 4;
    private short[] mComBuf = new short[mComBufLen];
    private int mComBufWPos = 0;
    private int mBefZeroPos = 0;
    private int mScanCopyPos = 0;
    private short[] mScanComBuffer = new short[2048];

    private short[] mDibBuf = new short[mComBufLen];
    private int mDibBufWPos = 0;

    //�����ļ���Ҫ�Ĳ���
    private int mNowFileindex = 0;          //�ļ�����
    private boolean mExistSaveFile = false;   //�Ƿ���ڱ����ļ�
    private FileOutputStream mSaveOS = null;      //
    private FileHeader mFileHeader = new FileHeader();
    public String mSavingFilePath;
    //sd��·��
    public String mSDCardPath;
    //�״������ļ���·��
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

        //���ɲ����ļ���
        String pathName;

        //�����״������ļ���
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

    //�ж��Ƿ���Զ�ȡ�����ļ�
    public boolean isCanReadFiles() {
        return mStatus == Global.RADAR_STATUS_READY ? true : false;
    }

    //����豸�Ƿ���û�о���״̬
    public boolean isNoReady() {
        return mStatus == Global.RADAR_STATUS_NOTREADY;
    }

    //����̽�ⷶΧ
    public boolean addDetectRange() {
        mDetectRange += Global.DEFAULT_SCAN_RANGE;
        if (mDetectRange > Global.MAX_DETECTRANGE)
            mDetectRange = Global.MAX_DETECTRANGE;//MIX_DETECTRANGE;
        return true;
    }

    //�õ�����̽��ķ�Χ
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

        ////Ѱ������ͷ
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
        //////�Ա��������
        //1:û������ͷ������
        int managePos;
        if (flagNum == 0) {
            Log.e(TAG, "No Find  FLAG !");
            return size;
        }
        //2:һ������ͷ
        if (flagNum == 1) {
            managePos = flagPos[0];
            scanCopyPos = mScanCopyPos;
            if (scanCopyPos + managePos != scanL1)
            //������ϳ�һ������
            {
                Log.e(TAG, "Step1: copyPos:" + scanCopyPos + ", managePos:" + managePos);
                scanCopyPos = 0;
                /////��ʣ�ಿ�����ݿ���������λ��
                if (rcvLen - managePos == scanL1) {
                    manageOneScan(Bufs, managePos, scanL1);
                    return rcvLen * 2;
                }
                copyLen = (rcvLen - managePos) * 2;

                for (int j = 0; j < copyLen / 2; j++) {
                    mScanComBuffer[j] = Bufs[managePos + j];
                }
                mScanCopyPos = copyLen / 2;
            } else            //��ϳ�һ������
            {
                /////���Ǩ�Ƶ�����
                copyLen = managePos * 2;
                for (int j = 0; j < copyLen / 2; j++) {
                    mScanComBuffer[scanCopyPos + j] = Bufs[j];
                }

                manageOneScan(mScanComBuffer, 0, scanL1);
                mScanCopyPos = 0;

                /////��ʣ�ಿ�����ݿ���������λ��
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
        ////3:�������ͷ
        //�Ե�һ����ǽ��д���
        {
            managePos = flagPos[0];
            scanCopyPos = mScanCopyPos;
            if (scanCopyPos + managePos != scanL1)            //������ϳ�һ������
            {
                scanCopyPos = 0;
            } else        //��ϳ�һ������
            {
                /////���Ǩ�Ƶ�����
                copyLen = managePos * 2;

                for (int j = 0; j < copyLen / 2; j++) {
                    mScanComBuffer[scanCopyPos + j] = Bufs[j];
                }

                manageOneScan(mScanComBuffer, 0, scanL1);
                mScanCopyPos = 0;
            }
        }
        //���м�ı�ǽ��д���
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
        //�����һ����ǽ��д���
        {
            managePos = flagPos[flagNum - 1];
            ////����ǰһ��
            befManagePos = flagPos[flagNum - 2];
            //
            if (managePos - befManagePos == scanL1) {
                manageOneScan(Bufs, befManagePos, scanL1);
            }
            /////�����ݿ��������λ��
            //1:ʣ�����ݴ���1�� ����
            if (rcvLen - managePos >= scanL1) {
                manageOneScan(Bufs, managePos, scanL1);
                return rcvLen * 2;
            }
            ////����ʣ������
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

    ////���豸��ȡָ�����ȵ�����
    //    size:Ҫ��ȡ�����ݳ���
    //      ����:��ȡ�������ݳ���
    public int manageOneScan(short[] Bufs, int offset, int size) {
        ////�Ա�ǽ��д���
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
        //����ʣ�������
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
        //�õ�����
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


        //��ȡ����
        for (int i = 0; i < rLength / 2; i++) {

            buf[i] = (short) (Global.getUByte(mDatasBufs[bufIndex][rPos + i * 2]) +
                              (Global.getUByte(mDatasBufs[bufIndex][rPos + i * 2 + 1]) << 8));
        }

        //�ı��ȡָ��λ��
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

    //��һ�������������ݱ��浽sd����
    public void saveDatasToSDCard(int bufIndex) {
        if (mExistSaveFile) {
            try {
                mSaveOS.write(mDatasBufs[bufIndex], 0, mBufLength);
            } catch (Exception e) {
                Toast.makeText(mContext, "���滺����" + bufIndex + "����", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //�������һ�����ݱ���
    public void lastSaveDatas() {
        if (mExistSaveFile) {
            int bufIndex = mBufIndex;
            int pos = mNowWPos[bufIndex];

            try {
                mSaveOS.write(mDatasBufs[bufIndex], 0, pos);
            } catch (Exception e) {
                Toast.makeText(mContext, "���滺����" + bufIndex + "����", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //�õ����һ������
    public short[] getRecentScanDatas() {
        //
        return mOneScanDatas;
    }

    //�����µı��������ļ�
    public boolean createNewDatasFile() {
        //�����ļ���
        String folderPath;
        folderPath = mSDCardPath + mLTEFilefolderPath;
        //���ɱ����ļ����
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

    //�����µ������ļ���
    public String createNewFileName() {
        return createNewFileName_ByIndex();
    }

    //���������������ļ���
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

    //�õ����������ļ���
    public String getSaveFilename() {
        return mSavingFilePath;
    }

    //���ݵ�ǰ���豸״̬�������ļ�ͷ����
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
        //�����ڴ����
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

    //��ʼ��������
    public boolean beginSave() {
        if (getSDFreeSpace() < 40) {
            Toast.makeText(mContext, "SD_CARD Space < 40M!", Toast.LENGTH_SHORT).show();
            return false;
        }
        //���ɱ����ļ�
        if (!createNewDatasFile()) {
            Toast.makeText(mContext, "���ɱ����ļ�����!", Toast.LENGTH_SHORT).show();
            return false;
        }
        String txt;
        txt = "��ʼ���������ļ�:" + getSaveFilename() + "!";
        Toast.makeText(mContext, txt, Toast.LENGTH_LONG).show();

        initBuf();

        return true;
    }

    //ֹͣ��������
    public boolean stopSave() {
        Toast.makeText(mContext, "�������ݽ���!", Toast.LENGTH_LONG).show();
        //�������һ�α���
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
            freeSpace = 1.0 * availCount * blockSize / 1024 / 1024;  // ��λMb
        }

        return freeSpace;
    }
}

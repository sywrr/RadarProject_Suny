package com.example.setsteptime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SetStepTimeActivity extends AppCompatActivity {
    private NumberPicker np_stepTimeFirst, np_stepTimeSecond, np_stepTimeThird, np_stepTimeFourth;
    private View myview;
    private EditText et_step;
    private TextView tv;
    private static final String FileName = "setFactoryParams"; // 文件名称
    private static final String KEY = "stepelapsed";
    private static int MODE = Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE;
    // private static final String PackageName = "com.ltd.idsc2600.activity";
    private Button b_certain, b_cancel;
    private Context c = null;
    final public String INNERSTORAGE = "/mnt/sdcard";
    // 参数文件夹路径
    public String mParamsFilefolderPath = "/radarParams/";
    // private SharedPreferences share = null;
    // private SharedPreferences.Editor edit = null;
    private static float MINVALUE = 3f;
    private static float MAXVALUE = 6f;
    private float m_fstep = 4.40f;
    private static float DEFUALTSTEP = 4.4f;
    private Context mContext;

    private Spinner mBaudrateSpinner = null;
    private TextView mBaudrate = null;
    private ArrayAdapter adapter;
    private Object mObjVlue;
    private int mBdValue;
    private int mPosition;
    private static int DEFAULTBAUDRATE = 115200;// sy20200118增加默认波特率值

    public int getmPosition() {
        return mPosition;
    }

    public void setmPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // share = getSharedPreferences(FileName,MODE);
        // edit = share.edit();


        mBaudrateSpinner = (Spinner) findViewById(R.id.spinner_baudrate);
        mBaudrate = (TextView) findViewById(R.id.st_baudrate);
        // sy2021.01.13 将可选内容与ArrayAdapter连接起来
        adapter = ArrayAdapter.createFromResource(this, R.array.baudrate, android.R.layout.simple_spinner_item);
        // sy2021.01.13设置下拉列表风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBaudrateSpinner.setAdapter(adapter);
        mBaudrateSpinner.setOnItemSelectedListener(new SpinnerXMLSelectedListener());
        et_step = (EditText) findViewById(R.id.edit_number);
        // if(share.contains(KEY))
        // {
        // m_fstep = share.getFloat(KEY, 4.4f);
        // }
        // else
        // {
        // Toast.makeText(this, "没找到KEY！IDSC2600/sharedpref！",
        // Toast.LENGTH_SHORT).show();
        // }
        // sy20201117加载时延值

        loadStepTimeParams();
        et_step.setText(String.valueOf(m_fstep));
        et_step.setInputType(InputType.TYPE_NULL);
        et_step.setOnKeyListener(mStepTimeEditOnKeyListener);

        b_certain = (Button) findViewById(R.id.bt_certain);
        b_cancel = (Button) findViewById(R.id.bt_cancel);

        b_certain.setFocusable(true);
        b_certain.setFocusableInTouchMode(true);
        b_certain.requestFocus();
        b_certain.requestFocusFromTouch();

        b_certain.setOnClickListener(onButtonClickListener);
        b_cancel.setOnClickListener(onButtonClickListener);

        float input1 = Float.parseFloat(et_step.getText().toString());
    }

    public int getmBdValue() {
        return mBdValue;
    }

    public void setmBdValue(int mBdValue) {
        this.mBdValue = mBdValue;
    }

    // sy2021.01.13添加适配器
    class SpinnerXMLSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
            mObjVlue = (adapter.getItem(position));
            setmPosition(position);
            setmBdValue(Integer.parseInt(String.valueOf(mObjVlue)));
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private boolean isEditState = false;

    public EditText.OnKeyListener mStepTimeEditOnKeyListener = new EditText.OnKeyListener() {
        @Override
        public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
            EditText txtView = (EditText) arg0;// 编辑文本

            // 处理 回车键
            if (arg1 == KeyEvent.KEYCODE_ENTER) {
                // 抬起做处理
                if (arg2.getAction() == KeyEvent.ACTION_UP) {
                    if (isEditState) {
                        isEditState = false;
                        txtView.setTextColor(Color.BLACK);
                    } else {
                        isEditState = true;
                        txtView.setTextColor(Color.RED);
                    }
                }
                return true;
            }
            // 处理 上键
            if (arg1 == KeyEvent.KEYCODE_DPAD_UP) {
                if (!isEditState)
                    return false;

                if (arg2.getAction() == KeyEvent.ACTION_UP) {
                    m_fstep += 0.01;
                    if (m_fstep >= MAXVALUE) {
                        m_fstep = MAXVALUE;
                    } else
                        ;

                    BigDecimal b = new BigDecimal(m_fstep);
                    m_fstep = b.setScale(3, BigDecimal.ROUND_HALF_DOWN).floatValue();// 向下取整
                    txtView.setText("" + m_fstep);
                }
                return true;
            }
            // 处理下键
            if (arg1 == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (!isEditState)
                    return false;

                if (arg2.getAction() == KeyEvent.ACTION_UP) {
                    m_fstep -= 0.01;
                    if (m_fstep <= MINVALUE)
                        m_fstep = MINVALUE;
                    else
                        ;

                    BigDecimal b = new BigDecimal(m_fstep);
                    m_fstep = b.setScale(3, BigDecimal.ROUND_HALF_DOWN).floatValue();// 向下取整
                    txtView.setText("" + m_fstep);
                }
                return true;
            }
            if (arg1 == KeyEvent.KEYCODE_DEL || arg1 == KeyEvent.KEYCODE_ESCAPE) {
                return true;
            }
            return true;
        }
    };

    public View.OnClickListener onButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int id = v.getId();
            switch (id) {
                case R.id.bt_certain:
                    // edit.putFloat(KEY,m_fstep);
                    // edit.commit();
                    if (!saveSystemSetFile()) {

                    } else {
                        SetStepTimeActivity.this.finish();
                        System.exit(0);
                    }
                    break;
                case R.id.bt_cancel:
                    SetStepTimeActivity.this.finish();
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }
    };

    // sy20201117弹框
    public void dialogButton() {
        // 弹出提示对话框
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("时延值超出范围3~6，请重新设置").setNegativeButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    // sy20201117弹框
    public void dialog() {
        // 弹出提示对话框
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("时延值设置失败，请重新设置").setNegativeButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    private boolean checkVlaue = false;

    // sy20201117保存时延文件
    public boolean saveSystemSetFile() {
        byte[] buf = new byte[12];
        String pathName;
        pathName = this.INNERSTORAGE + mParamsFilefolderPath + "defsystemset.para";
        try {
            FileOutputStream fileParams = new FileOutputStream(pathName);
            // 保存参数内容

            // 记录存储位置
            // ((IDSC2600MainActivity)mContext).showToastMsg("storageId=" +
            // storageId);
            // sy2021.1.14 存储波特率
            int val = getmBdValue();
            buf[0] = (byte) val;
            buf[1] = (byte) ((int) val >> 8);
            buf[2] = (byte) ((int) val >> 16);
            buf[3] = (byte) ((int) val >> 24);

            if (m_fstep < MINVALUE || m_fstep > MAXVALUE) {
                dialogButton();
                // return false;
            }
            int step = Float.floatToIntBits(m_fstep);

            buf[4] = (byte) step;
            buf[5] = (byte) ((int) step >> 8);
            buf[6] = (byte) ((int) step >> 16);
            buf[7] = (byte) ((int) step >> 24);

            int pos = getmPosition();
            buf[8] = (byte) pos;
            buf[9] = (byte) ((int) pos >> 8);
            buf[10] = (byte) ((int) pos >> 16);
            buf[11] = (byte) ((int) pos >> 24);
            try {
                fileParams.flush();
                fileParams.write(buf, 0, 12);
            } catch (Exception e) {
                e.getStackTrace();
            }
            // 提示保存文件位置
            // Toast.makeText(mContext, "storageId=" + storageId,
            // 1000).show();
            fileParams.close();
            if (!loadStepTimeParams()) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * sy20201110 加载时延参数
     */
    public boolean loadStepTimeParams() {
        // TODO Auto-generated method stub
        byte[] buf = new byte[12];
        String pathName;
        pathName = INNERSTORAGE + mParamsFilefolderPath + "defsystemset.para";
        try {
            File fOS = new File(pathName);
            if (!fOS.exists()) {
                // sy20201116文件未创建，先创建时延文件。
                // fOS.createNewFile();

                return false;
            } else
                ;
            FileInputStream fileOS = new FileInputStream(pathName);
            fileOS.read(buf, 0, 12);
            // sy2021.1.14获取波特率
            mBdValue = (0x000000ff & buf[0]) | (0x0000ff00 & buf[1] << 8) | (0x00ff0000 & buf[2] << 16) | (0xff000000 & buf[3] << 24);
            setmBdValue(mBdValue);
            if (mBdValue == 0) {
                mBdValue = DEFAULTBAUDRATE;
            } else
                ;
            fileOS.close();
            // sy20201105获取参数文件的时延值
            int step = 0;
            step = 0x000000ff & buf[4];
            step |= ((int) buf[5] << 8);
            step &= 0xffff;
            step |= ((int) buf[6] << 16);
            step &= 0xffffff;
            step |= ((int) buf[7] << 24);
            m_fstep = Float.intBitsToFloat(step);
            if (m_fstep > MAXVALUE || m_fstep < MINVALUE) {
                m_fstep = DEFUALTSTEP;
                dialog();
                return false;
            }
            int pos = (0x000000ff & buf[8]) | (0x0000ff00 & buf[9] << 8) | (0x00ff0000 & buf[10] << 16) | (0xff000000 & buf[11] << 24);
            mBaudrateSpinner.setSelection(pos);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

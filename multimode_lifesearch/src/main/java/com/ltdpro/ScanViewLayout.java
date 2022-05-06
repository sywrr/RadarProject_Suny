package com.ltdpro;

import android.content.Context;
import android.util.AttributeSet;

import android.view.View;
import android.view.ViewGroup;

/*
 * 单道波形布局类
 */
public class ScanViewLayout extends ViewGroup
{
	private String TAG="ScanViewLayout";
	private Context mContext;
	
	 /*Constructor*/
	  public ScanViewLayout(Context context)
	  {
	    super(context);
	    mContext = context;
	    /*配置ViewGroup的的宽为WRAP_CONTENT，高为FILL_PARENT*/
	    ScanViewLayout.this.setLayoutParams(new ViewGroup.LayoutParams
	                             (
                                         ViewGroup.LayoutParams.WRAP_CONTENT,
                                         ViewGroup.LayoutParams.FILL_PARENT)
	                             );
	    DebugUtil.i(TAG,"ScanViewLayout()");
	  }

	  /*Constructor for layout*/
	  public ScanViewLayout(Context context, AttributeSet attrs)
	  {
	    super(context, attrs);
	    mContext = context;
	    ScanViewLayout.this.setLayoutParams(new ViewGroup.LayoutParams
	                             (
                                         ViewGroup.LayoutParams.WRAP_CONTENT ,
                                         ViewGroup.LayoutParams.FILL_PARENT)
	                             );
	    //
	    DebugUtil.i(TAG,"ScanViewLayout() with params");
	 }
	  /*继承自ViewGroup必须重写的onLayout()方法*/
	  @Override
	  protected void onLayout(boolean changed, int l, int t, int r, int b)
	  {
//	    DebugUtil.e(TAG,"onLayout");
	    int childLeft = 0;
	    final int count = getChildCount();
	    for (int i = 0; i < count; i++)
	    {
	      final View child = getChildAt(i);
	      if (child.getVisibility() != View.GONE)
	      {
	        final int childWidth = child.getMeasuredWidth();
	        child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
	        childLeft += childWidth;
	      }
	    }
	  }
	  /*覆写onMeasure方法，并判断所在Layout Flag*/
	  @Override
	  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	  {
//	    DebugUtil.e(TAG,"onMeasure");
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    final int width = MeasureSpec.getSize(widthMeasureSpec);
	    final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
	    if (widthMode != MeasureSpec.EXACTLY)
	    {
	      throw new IllegalStateException("error mode.");
	    }
	    final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
	    if (heightMode != MeasureSpec.EXACTLY)
	    {
	      throw new IllegalStateException("error mode.");
	    }
	    final int count = getChildCount();
	    for (int i = 0; i < count; i++)
	    {
	      getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
	    }
	  } 
	  /*
	   * (non-Javadoc)
	   * @see android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
	   * 该函数在onTouchEvent前执行，拦截所有对触摸屏的操作
	   */
	  /*
	  @Override
	  public boolean onInterceptTouchEvent(MotionEvent event)
	  {
	    DebugUtil.i(TAG,"onInterceptTouchEvent");
	    return false;
	  }
	  */
	  /*
	  @Override
	  public boolean onTouchEvent(MotionEvent event)
	  {
	    DebugUtil.i(TAG,"****onTouchEvent****");

	    //
	    return false;
	  }
	  */
}

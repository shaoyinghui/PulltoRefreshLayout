package com.sohu.pulltorefreshlayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;


public class CommonUtils {

    public static int dp2px(Context context, float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }


    public static Bitmap getBitmapFromSrc(Context context,int resId){
        Bitmap bit=null;
        try {
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inJustDecodeBounds=true;
            options.inPurgeable=true;
            options.inInputShareable=true;
            BitmapFactory.decodeResource(context.getResources(),resId,options);
            options.inSampleSize=calculateInSampleSize(options,40,40);
            options.inJustDecodeBounds=false;
            bit= BitmapFactory.decodeResource(context.getResources(),resId,options);
        }catch (Exception e){
        }
        return  bit;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int w, int h) {
        final int height=options.outHeight;
        final int widht=options.outWidth;
        int inSampleSize=1;
        if(widht>w||height>h){
            if(widht>height){
                inSampleSize=Math.round((float)height/(float)h);
            }else{
                inSampleSize=Math.round((float)widht/(float)w);
            }
            final float totalPixels=widht*height;
            final float maxTotalPixels=w*h*2;
            while(totalPixels/(inSampleSize*inSampleSize)>maxTotalPixels){
                inSampleSize++;
            }
        }
        return inSampleSize;
    }



    public static void measureView(View view) {
        ViewGroup.LayoutParams lp=view.getLayoutParams();
        if(lp==null){
            lp=new ViewGroup.LayoutParams(-1,-2);
        }
        int childWidthSpec=ViewGroup.getChildMeasureSpec(0,0,lp.width);
        int lpHeight=lp.height;
        int childHeightSpec;
        if(lpHeight>0){
            childHeightSpec= View.MeasureSpec.makeMeasureSpec(lpHeight, View.MeasureSpec.EXACTLY);
        }else{
            childHeightSpec=View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        }
        view.measure(childWidthSpec,childHeightSpec);
    }

}

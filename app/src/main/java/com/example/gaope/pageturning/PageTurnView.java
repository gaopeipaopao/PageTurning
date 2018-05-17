package com.example.gaope.pageturning;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by gaope on 2018/5/14.
 */

public class PageTurnView extends View{

    private static final String TAG = "PageTurnView";

    /**
     * 画笔
     */
    private Paint paint;

    /**
     * 手指的触摸点
     */
    private PointF a;

    /**
     * 相对于触摸点的边缘点
     */
    private PointF f;

    /**
     * 触摸点与边缘点之间的中点
     */
    private PointF g;

    /**
     * b,c贝塞尔曲线的控制点e
     */
    private PointF e;

    /**
     * j,k贝塞尔曲线的控制点h
     */
    private PointF h;

    /**
     * b,c贝塞尔曲线的起点c
     */
    private PointF c;

    /**
     * j,k贝塞尔曲线的起点j
     */
    private PointF j;

    /**
     * b,c贝塞尔曲线的终点b
     */
    private PointF b;

    /**
     * j,k贝塞尔曲线的终点k
     */
    private PointF k;

    /**
     *b,c贝塞尔曲线上的点d，通过点d来确定背面的C区域
     */
    private PointF d;

    /**
     * j,k贝塞尔曲线上的点i，通过点i来确定背面的C区域
     */
    private PointF i;

    /**
     *用来进行缓存工作的bitmap
     */
    private Bitmap bitmap;

    /**
     * 在缓存的bitmap上面进行绘画的Canvas
     */
    private Canvas bitmapCanvas;

    /**
     * 当前页A的path，
     */
    private Path pathA;

    /**
     * 当前页A的paintA
     */
    private Paint paintA;

    /**
     * 当前页A的背景C的path
     */
    private Path pathC;

    /**
     * 当前页A的背景C的paintC
     */
    private Paint paintC;

    /**
     * 当前页的下一页B的path
     */
    private Path pathB;

    /**
     * 当前页的下一页B的paintB
     */
    private Paint paintB;

    /**
     * 是否触摸，第一次触摸
     */
    private boolean touch;

    /**
     *判断c.x是否大于0，是否达到最大距离,达到翻动的最大距离时，cMax为true;
     */
    private boolean cMax;

    /**
     * 右上,当rightTop为true时，说明翻动为右上
     */
    private boolean bRightTop;

    /**
     * 右下，当rightBottom为true时，说明翻动为右下
     */
    private boolean bRightBottom;

    /**
     *
     * @param context
     * @param attrs
     */



    public PageTurnView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        touch = true;
        cMax = false;
        bRightBottom = false;
        bRightBottom = false;


        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.YELLOW);
        paint.setAntiAlias(true);

        pathA = new Path();
        pathB = new Path();
        pathC = new Path();

        paintA = new Paint();
        paintA.setAntiAlias(true);
        paintA.setStyle(Paint.Style.FILL);
        paintA.setColor(Color.YELLOW);

        paintC = new Paint();
        paintC.setAntiAlias(true);
        paintC.setStyle(Paint.Style.FILL);
        paintC.setColor(Color.RED);
        paintC.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));

        paintB = new Paint();
        paintB.setAntiAlias(true);
        paintB.setStyle(Paint.Style.FILL);
        paintB.setColor(Color.BLUE);
        paintB.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));

        a = new PointF(0,0);
        f = new PointF(0,0);
        g = new PointF(0,0);
        e = new PointF(0,0);
        h = new PointF(0,0);
        c = new PointF(0,0);
        j = new PointF(0,0);
        b = new PointF(0,0);
        k = new PointF(0,0);
        d = new PointF(0,0);
        i = new PointF(0,0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG,"width:"+w);
        Log.d(TAG,"height:"+h);
        a.x = w;
        a.y = h;
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.d(TAG,"cc");

//        if (!cMax){
//            a.x = event.getX();
//            a.y = event.getY();
//        }
        a.x = event.getX();
        a.y = event.getY();


        //只判断一次，在第一次触摸时就会判断是右上翻动还是右下翻动
        if (touch) {
            touch = false;
            if (a.y <= getHeight() / 3){
                bRightTop = true;
            } else if (a.y > getHeight() * 2 / 3 && a.y <= getHeight()) {
                bRightBottom = true;
            }
        }
        if (bRightTop){
            f.x = getWidth();
            f.y = 0;
        }
        if (bRightBottom){
            f.x = getWidth();
            f.y = getHeight();
        }
        Log.d(TAG,"a.y:"+a.y);


        caclData();
        if (c.x < 0){
            cMax = true;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                invalidate();
                Log.d(TAG,"ababbaba");
                return true;
            case MotionEvent.ACTION_UP:
                touch = true;
                bRightBottom = false;
                bRightTop = false;
                invalidate();
                return true;
        }

        invalidate();
        Log.d(TAG,"a.x:"+a.x);

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        bitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        if (touch){
            Log.d(TAG,"aa");
            bitmapCanvas.drawPath(drawA(),paintA);
        }else {
            Log.d(TAG,"bb");
            //画A区域
            if (f.y == 0){
                bitmapCanvas.drawPath(drawARightTop(),paintA);
            }else {
                bitmapCanvas.drawPath(drawARightBottom(),paintA);
            }
            //画C区域
            bitmapCanvas.drawPath(drawC(),paintC);
            //画B区域
            bitmapCanvas.drawPath(drawB(),paintB);
        }

        //在画布上导入已经有了的bitmap图片，null表示没有画笔
        canvas.drawBitmap(bitmap,0,0,null);


    }

    private Path drawA(){
        pathB.reset();
        pathB.moveTo(0,0);
        pathB.lineTo(0,getHeight());
        pathB.lineTo(getWidth(),getHeight());
        pathB.lineTo(getWidth(),0);
        pathB.close();
        return pathB;
    }

    private Path drawARightTop(){
        pathA.reset();
        pathA.moveTo(0,0);
        pathA.lineTo(c.x,c.y);
        pathA.quadTo(e.x,e.y,b.x,b.y);
        pathA.lineTo(a.x,a.y);
        pathA.lineTo(k.x,k.y);
        pathA.quadTo(h.x,h.y,j.x,j.y);
        pathA.lineTo(getWidth(),getHeight());
        pathA.lineTo(0,getHeight());
        pathA.close();
        return pathA;
    }

    private Path drawARightBottom() {
        pathA.reset();
        pathA.moveTo(0,0);
        pathA.lineTo(0,getHeight());
        pathA.lineTo(c.x,c.y);
        pathA.quadTo(e.x,e.y,b.x,b.y);
        pathA.lineTo(a.x,a.y);
        pathA.lineTo(k.x,k.y);
        pathA.quadTo(h.x,h.y,j.x,j.y);
        pathA.lineTo(getWidth(),0);
        pathA.close();
        return pathA;
    }

    private Path drawC() {

        pathC.reset();
        pathC.moveTo(d.x,d.y);
        pathC.lineTo(i.x,i.y);
        pathC.lineTo(a.x,a.y);
        pathC.lineTo(d.x,d.y);
        return pathC;
    }

    private Path drawB() {
        pathB.reset();
        pathB.moveTo(0,0);
        pathB.lineTo(0,getHeight());
        pathB.lineTo(getWidth(),getHeight());
        pathB.lineTo(getWidth(),0);
        pathB.close();
        return pathB;
    }


    /**
     * 计算各个点的坐标
     */
    private void caclData() {

        g.x = (a.x + f.x) / 2 ;
        g.y = (a.y + f.y) / 2;

        e.x = g.x - (f.y - g.y) * (f.y - g.y) / (f.x - g.x);
        e.y = f.y;

        h.x = f.x;
        h.y = g.y - (f.x - g.x) * (f.x - g.x) / (f.y - g.y);

        c.x = e.x - (f.x - e.x) / 2;
        c.y = f.y;

        j.x = f.x;
        j.y = h.y - (f.y - h.y) / 2;

        b = obtainPoint(a.x,a.y,e.x,e.y,c.x,c.y,j.x,j.y);
        k = obtainPoint(a.x,a.y,h.x,h.y,c.x,c.y,j.x,j.y);

        d.x = ((b.x + c.x)/2 + e.x)/2;
        d.y = ((b.y + c.y)/2 + e.y)/2;

        i.x = ((k.x + j.x)/2 + h.x)/2;
        i.y = ((k.y + j.y)/2 + h.y)/2;


        Log.d(TAG,"a.x:"+a.x);
        Log.d(TAG,"f.x:"+f.x);
        Log.d(TAG,"g.x:"+g.x);
        Log.d(TAG,"e.x:"+e.x);
        Log.d(TAG,"h.x:"+h.x);
        Log.d(TAG,"c.x:"+c.x);
        Log.d(TAG,"j.x:"+j.x);
        Log.d(TAG,"d.x:"+d.x);
        Log.d(TAG,"i.x:"+i.x);
        Log.d(TAG,"b.x:"+b.x);
        Log.d(TAG,"k.x:"+k.x);

    }

    PointF obtainPoint(float x1,float y1,float x2,float y2,float x3,float y3,float x4,float y4){
        PointF pointF = new PointF(0,0);
        float x = 0;
        float y = 0;
        float k1 = 0;
        float k2 = 0;
        float b1 = 0;
        float b2 = 0;

        k1 = (y1 - y2)/(x1 - x2);
        k2 = (y3 - y4)/(x3 - x4);

        b1 = (y1 * x2 - y2 * x1)/(x2 - x1);
        b2 = (y3 * x4 - y4 * x3)/(x4 - x3);

        x = (b2 - b1)/(k1 - k2);
        y = k1 * x + b1;

        pointF.x = x;
        pointF.y = y;

        return pointF;
    }

}
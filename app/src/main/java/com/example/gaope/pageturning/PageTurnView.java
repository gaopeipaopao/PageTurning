package com.example.gaope.pageturning;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Scroller;

/**
 * 限制右侧最大的翻页距离
 * 第一种方法:如果c.x大于0则设置a点坐标重新计算各标识点位置，否则a点坐标不变，
 * 这个方法会使出现翻页“瞬移”，造成一种卡顿的感觉，
 * 这是因为c.x小于0时，a的坐标值不变，一直不变，导致绘出来的曲线也一直不变，
 * 当突然手指移动导致，c.x大于0时，从一种效果突然变化到另一种效果，导致卡顿
 * 第二种方法:一直在动态的获取临界的a的坐标，每一次移动的c.x不同，得到的临界的a的坐标不同，
 * 然后一直在重新绘制，没有从一个突然过渡到另一个的状态，不会导致卡顿
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
     * 横向进行翻页,进行横向翻页时，f.x = getWidth,f.y = getHeight;
     */
    private boolean bOrientation;

    /**
     * 弹性的滑动
     */
    private Scroller scroller;

    /**
     * 内容的画笔
     */
    private Paint textPaint;


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
        bOrientation = false;

        scroller = new Scroller(getContext(),new LinearInterpolator());

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);

        pathA = new Path();
        pathB = new Path();
        pathC = new Path();

        paintA = new Paint();
        paintA.setAntiAlias(true);
        paintA.setStyle(Paint.Style.FILL);
        paintA.setColor(Color.YELLOW);
        textPaint = new Paint();


        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        //设置自像素。如果该项为true，将有助于文本在LCD屏幕上的显示效果。
        textPaint.setSubpixelText(true);
        textPaint.setTextSize(30);

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
//        Log.d(TAG,"width:"+w);
//        Log.d(TAG,"height:"+h);
        a.x = w;
        a.y = h;
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {

      //  Log.d(TAG,"cc");

        float x = event.getX();
        float y = event.getY();
//        Log.d(TAG,"x:"+x);
//        Log.d(TAG,"y:"+y);



        //只判断一次，在第一次触摸时就会判断是右上翻动还是右下翻动
        if (touch) {
            touch = false;
            if (y <= getHeight() / 3){
                bRightTop = true;
            } else if (y > getHeight() / 3 && y <= getHeight() * 2 / 3){
                bOrientation = true;
            } else if (y > getHeight() * 2 / 3 && y <= getHeight()) {
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

        a.x = x;
        a.y = y;


//        Log.d(TAG,"a.y:"+a.y);

        caclData();

        if (caclCX(x,y,f) < 0){
            //如果c点x坐标小于0则重新测量临界的a点坐标
            caclCrisisA();
            caclData();
        }

        if (bOrientation){
            Log.d(TAG,"ccc");
            a.y = getHeight() - 3;
            f.x = getWidth();
            f.y = getHeight();
            caclData();
            postInvalidate();

        }



        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                invalidate();
                Log.d(TAG,"ababbaba");
                return true;
            case MotionEvent.ACTION_UP:
                cMax = false;
                bRightBottom = false;
                bRightTop = false;
                bOrientation = false;
                //让a滑动到f点所在位置，留出1像素是为了防止当a和f重叠时出现View闪烁的情况
                scroller.startScroll((int) a.x,(int) a.y,(int) (f.x- a.x - 1),(int) (f.y - a.y - 1),400);
                invalidate();
                return true;
        }
        return true;
    }

    @Override
    public void computeScroll() {

        if (scroller.computeScrollOffset()){
            a.x = scroller.getCurrX();
            a.y = scroller.getCurrY();
            Log.d(TAG,"aax"+a.x);
            Log.d(TAG,"aay"+a.y);
            caclData();
            invalidate();
            Log.d(TAG,"acscga");
            if (scroller.getFinalX() == a.x && scroller.getFinalY() == a.y){
                touch = true;
                invalidate();
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        bitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        if (touch){
          //  Log.d(TAG,"aa");
           // bitmapCanvas.drawPath(drawA(),paintA);
            drawPathAText(bitmapCanvas,drawA(),paintA);
        }else {
          //  Log.d(TAG,"bb");
            //画A区域
            if (f.y == 0){
                //bitmapCanvas.drawPath(drawARightTop(),paintA);
                drawPathAText(bitmapCanvas,drawARightTop(),paintA);
                bitmapCanvas.drawPath(drawC(),paintC);
                //bitmapCanvas.drawPath(drawC(),paintC);
                drawPathCText(bitmapCanvas,drawARightTop(),paint);
                drawPathBText(bitmapCanvas,drawARightTop(),paintB);
            }else {
               // bitmapCanvas.drawPath(drawARightBottom(),paintA);
                drawPathAText(bitmapCanvas,drawARightBottom(),paintA);
                bitmapCanvas.drawPath(drawC(),paintC);
               // bitmapCanvas.drawPath(drawC(),paintC);
                drawPathCText(bitmapCanvas,drawARightBottom(),paint);
                drawPathBText(bitmapCanvas,drawARightBottom(),paintB);
            }
            //画C区域
//            bitmapCanvas.drawPath(drawC(),paintC);
            //画B区域
           // bitmapCanvas.drawPath(drawB(),paintB);
            //drawPathBText(bitmapCanvas,drawB(),paintB);
        }

        //在画布上导入已经有了的bitmap图片，null表示没有画笔
        canvas.drawBitmap(bitmap,0,0,null);


    }

    private void drawPathAText(Canvas canvas,Path path,Paint paint){
        Bitmap bitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.RGB_565);
        Canvas canvasBitmapA = new Canvas(bitmap);
        canvasBitmapA.drawPath(path,paint);
        canvasBitmapA.drawText("脆皮鸭啊啊啊啊啊",getWidth() - 260,getHeight() - 100,textPaint);
        //调用canvas.save()来保存画布当前的状态，当操作之后取出之前保存过的状态，这样就不会对其他的元素进行影响
        canvas.save();
        //对绘制内容进行剪裁，取和A区域的交集
        canvas.clipPath(path,Region.Op.INTERSECT);
        canvas.drawBitmap(bitmap,0,0,null);
        canvas.restore();
    }

    private void drawPathBText(Canvas canvas,Path pathA,Paint paint){
        Bitmap bitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvasBitmapB = new Canvas(bitmap);
        canvasBitmapB.drawPath(drawB(),paint);
        canvasBitmapB.drawText("脆皮鸭啊啊啊啊啊",getWidth() - 260,getHeight() - 100,textPaint);
        canvas.save();
        //裁剪出A区域
        canvas.clipPath(pathA);
        //裁剪出A和C区域的全集
        canvas.clipPath(drawC(),Region.Op.UNION);
        //裁剪出B区域中不同于与AC区域的部分
        canvas.clipPath(pathB,Region.Op.REVERSE_DIFFERENCE);
        canvas.drawBitmap(bitmap,0,0,null);
//        canvas.restore();
//        canvas.save();
        drawBShadow(canvas);
        canvas.restore();
    }

    private void drawBShadow(Canvas canvas) {
        //深色端的颜色
        int deepColor = 0xff111111;
        //浅色端的颜色
        int lightColor = 0x00111111;
        int[] gradientColors = new int[]{deepColor,lightColor};

        //a到f的距离
        float aAndFLength = (float) Math.hypot((a.x - f.x),(a.y - f.y));
        //对角线的长度
        float diagonalLength = (float) Math.hypot(getWidth(),getHeight());

        Log.d(TAG,"aaAndFLength:"+aAndFLength);
        Log.d(TAG,"aaAndFLength4:"+aAndFLength/4);

        //确定阴影矩形的坐标
        int left;
        int right;
        int top = (int) c.y;
        int bottom = (int) (c.y + diagonalLength);

        //生成GradientDrawable对象
        GradientDrawable gradientDrawable;

        //f点在右上角，从左到右渐变
        if (f.y == 0){
            gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,gradientColors);
            //线性渐变
            gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            left = (int) c.x;
            right = (int) (c.x + aAndFLength/4);
        }else {
            //f点在右下角，从右到左渐变
            gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT,gradientColors);
            //线性渐变
            gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            left = (int) (c.x - aAndFLength/4);
            right = (int) c.x ;
        }
        gradientDrawable.setBounds(left,top,right,bottom);
        //旋转角度
        float rotateDegress = (float) Math.toDegrees(Math.atan2(e.x - f.x,h.y - f.y));
        Log.d(TAG,"rotate:"+rotateDegress);
        //以c为中心点旋转
        canvas.rotate(rotateDegress,c.x,c.y);
        gradientDrawable.draw(canvas);
    }


    private void drawPathCText(Canvas canvas,Path pathA,Paint paint){
        Bitmap bitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.RGB_565);
        Canvas canvasBitmapC = new Canvas(bitmap);
        canvasBitmapC.drawPath(drawB(),paint);
        canvasBitmapC.drawText("脆皮鸭啊啊啊啊啊",getWidth() - 260,getHeight() - 100,textPaint);

        canvas.save();
        canvas.clipPath(pathA);
        //裁剪出C区域不同于A区域的部分
        canvas.clipPath(drawC(),Region.Op.REVERSE_DIFFERENCE);
        float ef = (float) Math.hypot(f.x - e.x,h.y - f.y);
        float sina = (f.x - e.x) / ef;
        float cosa = (h.y - f.y) / ef;
        float tana = sina / cosa;
        float a = (float) Math.atan(tana);
        Log.d(TAG,"sina:"+sina);
        Log.d(TAG,"cosa:"+cosa);
        Log.d(TAG,"aaa:" + 2 * Math.toDegrees(a));
        Matrix matrix = new Matrix();
        matrix.reset();
//        //使用post，越靠前越先执行。
//        matrix.postScale(-1,1);
//        matrix.postRotate(- 2 * a);
//        matrix.postTranslate(-e.x,-e.y);
//        matrix.postTranslate(e.x,e.y);

        float[] matrixValue = { 0, 0, 0, 0, 0 ,0, 0, 0, 1.0f };
        matrixValue[0] = -(1 - 2 * sina * sina);
        matrixValue[1] = 2 * sina * cosa;
        matrixValue[3] = 2 * sina * cosa;
        matrixValue[4] = 1 - 2 * sina * sina;
        Log.d(TAG,"pingfang"+(matrixValue[3] * matrixValue[3] + matrixValue[4] * matrixValue[4]));
        Log.d(TAG,"va0:" + matrixValue[0] + "   va1:" + matrixValue[1] + "   va3:" + matrixValue[3] + "   va4:" + matrixValue[4]);
//        Matrix matrix = new Matrix();
//        matrix.reset();
        matrix.setValues(matrixValue);
        Log.d(TAG,"matrix:"+matrix.toString());
        matrix.preTranslate(-e.x,-e.y);
        Log.d(TAG,"matrix1:"+matrix.toString());
        matrix.postTranslate(e.x,e.y);
        Log.d(TAG,"matrix2:"+matrix.toString());
        Log.d(TAG,"matrix,e.x:"+e.x);
        Log.d(TAG,"matrix,e.y:"+e.y);

        canvas.drawBitmap(bitmap,matrix,null);
//        canvas.restore();
//        canvas.save();
        drawCShadow(canvas);
        canvas.restore();
    }

    private void drawCShadow(Canvas canvas) {
        //深色端的颜色
        int deepColor = 0xff111111;
        //浅色端的颜色
        int lightColor = 0x00333333;
        int[] gradientColors = new int[]{lightColor,deepColor};

        //深色端的偏移值
        int deepOffset = 1;
        //浅色端的偏移值
        int lightOffset = -30;

//        //a到f的距离
//        float aAndFLength = (float) Math.hypot((a.x - f.x),(a.y - f.y));
        //对角线的长度
        float diagonalLength = (float) Math.hypot(getWidth(),getHeight());

        int midpoint_ce = (int) (c.x + e.x) / 2;//ce中点
        int midpoint_jh = (int) (j.y + h.y) / 2;//jh中点
        //中点到控制点的最小值
        float minDisToControlPoint = Math.min(Math.abs(midpoint_ce - e.x), Math.abs(midpoint_jh - h.y));

        //确定阴影矩形的坐标
        int left;
        int right;
        int top = (int) c.y;
        //绘制C阴影时，c.y + diagonalLength
        int bottom = (int) (c.y + diagonalLength);

        //生成GradientDrawable对象
        GradientDrawable gradientDrawable;

        //f点在右上角，从左到右渐变
        if (f.y == 0){
            gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,gradientColors);
            //线性渐变
            gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);

            //借助偏移量让矩形右移
            left = (int) c.x - lightOffset;
            right = (int) (c.x + minDisToControlPoint + deepOffset);

            Log.d(TAG,"c.xaa:"+c.x);
            Log.d(TAG,"left:"+left);
            Log.d(TAG,"right:"+right);
        }else {
            //f点在右下角，从右到左渐变
            gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT,gradientColors);
            //线性渐变
            gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);

            //借助偏移量让矩形左移
            left = (int) (c.x - minDisToControlPoint - deepOffset);
            right = (int) (c.x + lightOffset);

            Log.d(TAG,"c.xaa:"+c.x);
            Log.d(TAG,"left:"+left);
            Log.d(TAG,"right:"+right);
        }
        gradientDrawable.setBounds(left,top,right,bottom);
        //旋转角度
        float rotateDegress = (float) Math.toDegrees(Math.atan2(e.x - f.x,h.y - f.y));
        Log.d(TAG,"rotate:"+rotateDegress);
        //以c为中心点旋转
        canvas.rotate(rotateDegress,c.x,c.y);
        gradientDrawable.draw(canvas);
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
     * 计算临界的a点的坐标
     */
    private void caclCrisisA(){
        float w0 = getWidth() - c.x;

        float w1 = Math.abs(f.x - a.x);
        float w2 = getWidth() * w1 / w0;
        a.x = Math.abs(f.x - w2);

        float h1 = Math.abs(f.y - a.y);
        float h2 = w2 * h1 / w1;
        a.y = Math.abs(f.y - h2);
    }

    /**
     *计算c.x的正负
     */
    private float caclCX(float x,float y,PointF f){
        PointF c = new PointF(0,0);
        g.x = (x + f.x) / 2 ;
        g.y = (y + f.y) / 2;

        e.x = g.x - (f.y - g.y) * (f.y - g.y) / (f.x - g.x);
        e.y = f.y;

        c.x = e.x - (f.x - e.x) / 2;
        c.y = f.y;
        return c.x;
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


//        Log.d(TAG,"a.x:"+a.x);
//        Log.d(TAG,"a.y:"+a.y);
//        Log.d(TAG,"f.x:"+f.x);
//        Log.d(TAG,"g.x:"+g.x);
//        Log.d(TAG,"e.x:"+e.x);
//        Log.d(TAG,"e.y:"+e.y);
//        Log.d(TAG,"h.x:"+h.x);
//        Log.d(TAG,"h.y:"+h.y);
//        Log.d(TAG,"c.x:"+c.x);
//        Log.d(TAG,"c.y:"+c.y);
//        Log.d(TAG,"j.x:"+j.x);
//        Log.d(TAG,"j.y:"+j.y);
//        Log.d(TAG,"d.x:"+d.x);
//        Log.d(TAG,"d.y:"+d.y);
//        Log.d(TAG,"i.x:"+i.x);
//        Log.d(TAG,"i.y:"+i.y);
//        Log.d(TAG,"b.x:"+b.x);
//        Log.d(TAG,"b.y:"+b.y);
//        Log.d(TAG,"k.x:"+k.x);
//        Log.d(TAG,"k.y:"+k.y);

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
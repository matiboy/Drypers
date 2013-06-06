package com.suterastudio.drypers.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.suterastudio.ui.multitouch.MaskedTouchImage;
import com.suterastudio.ui.multitouch.MultiTouchController;
import com.suterastudio.ui.multitouch.MultiTouchController.MultiTouchObjectCanvas;
import com.suterastudio.ui.multitouch.MultiTouchController.PointInfo;
import com.suterastudio.ui.multitouch.MultiTouchController.PositionAndScale;

public class BabyHeadView extends View implements MultiTouchObjectCanvas<MaskedTouchImage> {
	private Context mContext;
	
	private MultiTouchController<MaskedTouchImage> multiTouchController = new MultiTouchController<MaskedTouchImage>(this);

    private Bitmap mBabyHeadMask;
    private MaskedTouchImage mBabyHead;        
    
	private static final int UI_MODE_ROTATE = 1, UI_MODE_ANISOTROPIC_SCALE = 2;
	private int mUIMode = UI_MODE_ROTATE;
	
    public BabyHeadView(Context context, AttributeSet attrs) {
        super(context, attrs);        

        mContext = context;     
    }

    public void setBabyHead(Bitmap babyHead, int resourceID) {
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inMutable = true;
        mBabyHeadMask = BitmapFactory.decodeResource(mContext.getResources(), resourceID, options);  
        mBabyHead = new MaskedTouchImage(babyHead, mBabyHeadMask, mContext);//.getResources());
        
        //edit
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
                    
        if(mBabyHead == null) {
        	return;
        }
                
        mBabyHead.draw(canvas);     
    }
    
    @Override
    public void selectObject(MaskedTouchImage img, PointInfo touchPoint) {
    	if (img != null) {
            // Move image to the top of the stack when selected
        } else {
            // Called with img == null when drag stops.
        }
        invalidate();
    }

    @Override    
    public void getPositionAndScale(MaskedTouchImage img, PositionAndScale objPosAndScaleOut) {
    	objPosAndScaleOut.set(img.getCenterX(), img.getCenterY(), (mUIMode & UI_MODE_ANISOTROPIC_SCALE) == 0,
                        (img.getScaleX() + img.getScaleY()) / 2, (mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0, img.getScaleX(), img.getScaleY(),
                        (mUIMode & UI_MODE_ROTATE) != 0, img.getAngle());
    }

    @Override    
    public boolean setPositionAndScale(MaskedTouchImage img, PositionAndScale newImgPosAndScale, PointInfo touchPoint) {
        boolean ok = img.setPos(newImgPosAndScale);
        if (ok) {
                invalidate();
        }
        
        return ok;
    }

    @Override
    public MaskedTouchImage getDraggableObjectAtPoint(PointInfo pt) {
    	return mBabyHead;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return multiTouchController.onTouchEvent(event);
    }
}
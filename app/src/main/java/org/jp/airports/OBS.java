/**
 * Created by John on 3/30/2016.
 */

package org.jp.airports;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.location.Location;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class OBS extends View implements View.OnTouchListener {
    Context context;
    TextPaint numberPaint, typePaint;
    Paint ringPaint, circlePaint, bullseyePaint, linePaint, grayPaint;
    Paint pointerPaint, needlePaint;
    private float h, w, cx, cy, cr, ascent, tick10, tick5,  tick, innerOffset;
    V3 lastTouch = null;
    int radial = 0;
    Place place = null;
    Location location = null;

    public String msg = "";
    
    public OBS(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        circlePaint = new Paint();
        circlePaint.setColor(Color.BLACK);
        circlePaint.setStyle(Paint.Style.FILL);
        ringPaint = new Paint();
        ringPaint.setColor(Color.WHITE);
        ringPaint.setStrokeWidth(10.0f);
        ringPaint.setStyle(Paint.Style.STROKE);
        bullseyePaint = new Paint();
        bullseyePaint.setColor(Color.WHITE);
        bullseyePaint.setStrokeWidth(16.0f);
        bullseyePaint.setStyle(Paint.Style.STROKE);
        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(2.0f);
        needlePaint = new Paint();
        needlePaint.setColor(Color.MAGENTA);
        needlePaint.setStrokeWidth(18.0f);
        grayPaint = new Paint();
        grayPaint.setColor(Color.DKGRAY);
        grayPaint.setStrokeWidth(2.0f);
        numberPaint = new TextPaint();
        numberPaint.setColor(Color.WHITE);
        numberPaint.setTextSize(96);
        typePaint = new TextPaint();
        typePaint.setColor(Color.WHITE);
        typePaint.setTextSize(72);
        pointerPaint = new Paint();
        pointerPaint.setColor(Color.YELLOW);
        pointerPaint.setStyle(Paint.Style.FILL);

        setOnTouchListener(this);
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawFace(canvas);
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public void setRadial(int radial) {
        this.radial = radial;
    }

    public int getRadial() {
        return radial;
    }

    public void setLocation(Location location) {
        this.location = location;
        if ((place != null) && (location != null)) {
            place.setDistanceFrom(location);
        }
    }

    public void incrementRadial(int delta) {
        radial += delta;
        if (radial < 0) radial += 360;
        if (radial > 360) radial = radial % 360;
        invalidate();
    }

    private void drawFace(Canvas canvas) {
        ascent = -numberPaint.ascent();
        h = getMeasuredHeight();
        w = getMeasuredWidth();
        cx = w / 2;
        cy = h / 2;
        cr = Math.min(cx, cy);
        tick10 = 0.1f * cr;
        tick5 = 0.05f * cr;
        innerOffset = ascent + tick10 + 20;

        canvas.drawCircle(cx, cy, cr, circlePaint);
        for (int i=0; i<360; i++) {
            int degrees = (360 + i - radial) % 360;

            if (i % 5 == 0) {
                canvas.save();
                canvas.rotate((float)degrees, cx, cy);
                if ((i % 30) == 0) {
                    String text = Integer.toString(i / 10);
                    float width = numberPaint.measureText(text);
                    canvas.drawText(text, cx - width / 2, cy - cr + 10 + ascent, numberPaint);
                }
                if (i % 10 == 0) {
                    tick = tick10;
                    linePaint.setStrokeWidth(8.0f);
                } else {
                    tick = tick5;
                    linePaint.setStrokeWidth(6.0f);
                }
                float outerR = innerOffset - tick + ((i % 30 == 0) ? 10 : 0);
                canvas.drawLine(cx, innerOffset, cx, outerR, linePaint);
                canvas.restore();
            }
        }
        drawPointer(canvas, 0, -(cy - innerOffset), 60, 85, pointerPaint);
        drawPointer(canvas, 0, (cy - innerOffset), -50, 70, pointerPaint);
        canvas.drawCircle(cx, cy, cx/10, bullseyePaint);
        canvas.drawCircle(cx, cy, cr, ringPaint);

        float dx = cx/10;
        float r = dx/4;
        for (int i=2; i<6; i++) {
            canvas.drawCircle(cx+i*dx, cy, r, linePaint);
            canvas.drawCircle(cx-i*dx, cy, r, linePaint);
            canvas.drawLine(cx+i*dx, cy-dx/2, cx+i*dx, cy+dx/2, linePaint);
            canvas.drawLine(cx-i*dx, cy-dx/2, cx-i*dx, cy+dx/2, linePaint);
            canvas.drawCircle(cx, cy+i*dx, r, linePaint);
            canvas.drawCircle(cx, cy-i*dx, r, linePaint);
            canvas.drawLine(cx-dx/2, cy+i*dx, cx+dx/2, cy+i*dx, linePaint);
            canvas.drawLine(cx-dx/2, cy-i*dx, cx+dx/2, cy-i*dx, linePaint);
        }

        if ((location != null) && (place != null)) {
            V3 placeV3 = new V3(0, place.magBrng).unit();
            V3 radialV3 = new V3(0, radial).unit();
            V3 cross = radialV3.cross(placeV3);
            double ang = placeV3.angle(radialV3);
            ang *= Math.signum(cross.z);
            boolean toFrom = (ang <= 90) && (ang >= -90);
            Paint p = toFrom ? linePaint : grayPaint;
            drawPointer(canvas, (cr - innerOffset) / 2, -cr / 3, 100, 140, p);
            p = toFrom ? grayPaint : linePaint;
            drawPointer(canvas, (cr - innerOffset) / 2, cr / 3, -100, 140, p);

            float tpw = typePaint.measureText(place.id);
            float tpa = typePaint.ascent();
            canvas.drawText(place.id, cx - tpw - dx, cy - dx  + 2*tpa, typePaint);
            tpw = typePaint.measureText(place.type);
            canvas.drawText(place.type, cx - tpw - dx, cy - dx  + tpa, typePaint);
            String dist = String.format("%.1fnm",place.dist);
            tpw = typePaint.measureText(dist);
            canvas.drawText(dist, cx - tpw - dx, cy + 2*dx - tpa, typePaint);

            float nw = needlePaint.getStrokeWidth()/2;
            if (toFrom && (ang >= -10) && (ang <= 10)) {
                float x = cx+((float)ang)*dx/2 - nw;
                canvas.drawLine(x,cy+4*dx, x, cy-4*dx, needlePaint);
            }
            else if (!toFrom && ((ang <= -170) || (ang >= 170))) {
                ang -= 180 * Math.signum(ang);
                ang *= -1;
                float x = cx+((float)ang)*dx/2 - nw;
                canvas.drawLine(x,cy+4*dx, x, cy-4*dx, needlePaint);
            }
        }
    }

    private void drawPointer(Canvas canvas, float x, float y, float h, float w, Paint paint) {
        x += cx;
        y += cy;
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x - w/2, y + h);
        path.lineTo(x + w/2, y + h);
        path.lineTo(x, y);
        canvas.drawPath(path, paint);
    }

    private void drawIncKnob(Canvas canvas, float x, float y, float r) {

    }

    public boolean onTouch(View v, MotionEvent event) {
        double x = (event.getX() - cx);
        double y = (event.getY() - cy);
        double r = Math.sqrt(x*x + y*y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if ((r < cr) && (r > cr - innerOffset)) {
                    lastTouch = new V3(x, y, 0);
                }
                else if ((r > cr) && (y > 0)) {
                    if (x > 0) radial++;
                    else radial--;
                }
                else if ((x > 0) && (location != null) && (place != null)) {
                    int magBrng = (int) Math.round(place.magBrng);
                    if (y < 0) radial = magBrng;
                    else radial = (magBrng + 180) % 360;
                    lastTouch = null;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (lastTouch != null) {
                    V3 thisTouch = new V3(x, y, 0);
                    V3 cross = thisTouch.cross(lastTouch);
                    int angle = (int) thisTouch.angle(lastTouch);
                    if (angle >= 1) {
                        if (cross.z < 0)
                            incrementRadial(-angle);
                        else
                            incrementRadial(angle);
                        lastTouch = thisTouch;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                invalidate();
                lastTouch = null;
                break;
        }
        return true;
    }

}
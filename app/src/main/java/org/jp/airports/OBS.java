/**
 * Created by John on 3/30/2016.
 */

package org.jp.airports;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
    boolean enableGlideSlope = true;

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
        invalidate();
    }

    public void setRadial(int radial) {
        this.radial = radial;
        invalidate();
    }

    public int getRadial() {
        return radial;
    }

    public void setLocation(Location location) {
        this.location = location;
        if ((place != null) && (location != null)) {
            place.setDistanceFrom(location);
            invalidate();
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

        drawInstrumentOutline(canvas);
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
            canvas.drawText(dist, cx - tpw - dx, cy + 2*dx - 0.9f*tpa, typePaint);
            String altString = String.format("%.0fft",location.getAltitude()*39.37/12);
            tpw = typePaint.measureText(altString);
            canvas.drawText(altString, cx - tpw - dx, cy + 2*dx - 2f*tpa, typePaint);

            needlePaint.setColor(Color.MAGENTA);
            float x = 0;
            if (!toFrom) {
                ang -= 180 * Math.signum(ang);
                ang *= -1;
            }
            if ((ang >= -10) && (ang <= 10)) {
                x = cx + ((float)ang)*dx/2;
            }
            else if (ang <= -10) {
                x = cx - 10*dx/2;
                needlePaint.setColor(Color.WHITE);
            }
            else if (ang > 10) {
                x = cx + 10*dx/2;
                needlePaint.setColor(Color.WHITE);
            }
            float nr = cr - innerOffset - 60;
            float nh = (float)Math.sqrt( nr*nr - (x-cx)*(x-cx) );
            canvas.drawLine(x,cy+nh, x, cy-nh, needlePaint);

            if (!place.isFix && enableGlideSlope && toFrom && (place.dist > 0.1) && (place.dist < 25)) {
                needlePaint.setColor(Color.MAGENTA);
                double alt = location.getAltitude() * 39.37/12;
                double alpha = (alt - place.elevation) / (place.dist * 6076);
                alpha *= 180 / Math.PI;
                alpha -= 3;
                if (alpha > 3) {
                    alpha = 3;
                    needlePaint.setColor(Color.WHITE);
                }
                else if (alpha < -3) {
                    alpha = -3;
                    needlePaint.setColor(Color.WHITE);
                }
                alpha *= 5d/3d;
                float y = cy + (float)(alpha*dx);
                nh = (float)Math.sqrt( nr*nr - (y-cy)*(y-cy) );
                canvas.drawLine(cx-nh, y, cx+nh, y, needlePaint);
            }
        }

        float instRadius = 100;
        float dxy = (float)((cr + instRadius*3/4)/Math.sqrt(2));
        drawKnob(canvas, dxy, -dxy, instRadius, "  GS", (enableGlideSlope?0:Color.RED));
        drawKnob(canvas, dxy, dxy, instRadius, "+", 0);
        drawKnob(canvas, -dxy, dxy, instRadius, "-", 0);
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

    private void drawKnob(Canvas canvas, float x, float y, float r, String s, int color) {
        canvas.drawCircle(cx + x, cy + y, r, circlePaint);
        canvas.drawCircle(cx + x, cy + y, r * 0.8f, ringPaint);
        TextPaint p = new TextPaint(numberPaint);
        if (s.length() > 1) p.setTextSize(72);
        float sw = numberPaint.measureText(s);
        float sa = -numberPaint.ascent();
        if (s.length() == 1) sa = sa/3f;
        else sa = sa/4f;
        if (color != 0) p.setColor(color);
        canvas.drawText(s, cx + x - sw/2, cy + y + sa, p);
    }

    private void drawInstrumentOutline(Canvas canvas) {
        float dx = 0.15f * w;
        float dy = 0.15f * h;
        float xm = 0.05f * w;
        Path path = new Path();
        path.moveTo(xm, dy);
        path.lineTo(xm + dx, 0);
        path.lineTo(w - dx - xm, 0);
        path.lineTo(w - xm, dy);
        path.lineTo(w - xm, h - dy);
        path.lineTo(w - dx - xm, h);
        path.lineTo(xm + dx, h);
        path.lineTo(xm, h - dy);
        path.lineTo(xm, dy);
        Paint p = new Paint();
        p.setColor(Color.LTGRAY);
        canvas.drawPath(path, p);
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
                else if ((r > cr) && (y < 0) && (x > 0)) {
                    enableGlideSlope = !enableGlideSlope;
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
package org.jp.airports;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.SensorEvent;
import android.location.Location;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class Wind extends View implements View.OnTouchListener {
    Context context;
    TextPaint numberPaint, typePaint;
    Paint ringPaint, circlePaint, linePaint, spdPaint, titlePaint;
    Paint pointerPaint;
    private float h, w, cx, cy, cr, xm, ym, ascent, tick10, tick5,  tick, innerOffset;
    V3 lastTouch = null;
    Location location = null;
    float speed, heading;
    float compass = 0;
    float prevcompass = 0;
    Canvas canvas = null;
    boolean dragAS = false;
    boolean dragMH = false;
    boolean dgmode = false;
    float lastASY = 0;
    int spdColor = Color.GRAY;
    float spdTop, spdBottom, spdWidth;

    public Wind(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        circlePaint = new Paint();
        circlePaint.setColor(Color.BLACK);
        circlePaint.setStyle(Paint.Style.FILL);
        ringPaint = new Paint();
        ringPaint.setColor(Color.WHITE);
        ringPaint.setStrokeWidth(10.0f);
        ringPaint.setStyle(Paint.Style.STROKE);
        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(2.0f);
        spdPaint = new Paint();
        spdPaint.setColor(Color.GRAY);
        spdPaint.setStrokeWidth(2.0f);
        spdPaint.setStyle(Paint.Style.FILL);
        numberPaint = new TextPaint();
        numberPaint.setColor(Color.WHITE);
        numberPaint.setTextSize(72);
        titlePaint = new TextPaint();
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(96);
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

    public void setHeading(float heading) {
        this.heading = heading;
        invalidate();
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        invalidate();
    }

    public float getSpeed() {
        return speed;
    }

    public float getHeading() {
        return heading;
    }

    public void setLocation(Location location) {
        this.location = location;
        invalidate();
    }

    public void incrementHeading(float delta) {
        heading += delta;
        if (heading <= 0) heading += 360;
        if (heading > 360) heading -= 360;
        invalidate();
    }

    public void setCompass(float comp) {
        float delta = (comp - prevcompass)%360f;
        prevcompass = compass;
        compass = comp;
        if (dgmode) incrementHeading(delta/2);
        invalidate();
    }

    private void drawFace(Canvas canvas) {
        this.canvas = canvas;
        String title = "Wind Calculator";
        float titleWidth = titlePaint.measureText(title);
        float titleAscent = -titlePaint.ascent();
        ascent = -numberPaint.ascent();
        h = getMeasuredHeight();
        w = getMeasuredWidth();
        xm = 80;
        ym = 80 + titleAscent;
        cr = Math.min(h, w)/2 * 3/4;
        cx = cr + xm;
        cy = cr + ym;
        tick10 = 0.1f * cr;
        tick5 = 0.05f * cr;
        innerOffset = ascent + tick10 + 20;

        canvas.drawText(title, (w - titleWidth)/2, titleAscent, titlePaint);

        canvas.drawCircle(cx, cy, cr, circlePaint);
        for (int i=0; i<360; i++) {
            int degrees = (360 + i - (int)heading) % 360;

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
                canvas.drawLine(cx, ym + innerOffset, cx, ym + outerR, linePaint);
                canvas.restore();
            }
        }
        drawPointer(0, -(cr - innerOffset), 60, 85, pointerPaint);
        drawPointer(0, (cr - innerOffset), -50, 70, pointerPaint);
        canvas.drawCircle(cx, cy, cr, ringPaint);

        spdWidth = (w - 2*xm - 2*cr)/2;
        float spdLeft = w - xm - spdWidth;
        float spdRight = spdLeft + spdWidth;
        spdTop = ym;
        spdBottom = ym + 2 * cr;
        spdColor = dragAS ? Color.YELLOW : Color.GRAY;
        spdPaint.setColor(spdColor);
        canvas.drawRect(spdLeft, spdTop+spdWidth, spdRight, spdBottom-spdWidth, spdPaint);
        canvas.drawRect(spdLeft, spdTop+spdWidth, spdRight, spdBottom-spdWidth, ringPaint);

        spdPaint.setColor(Color.YELLOW);
        float d = ringPaint.getStrokeWidth();
        drawSpeedPointer( (spdLeft+spdRight)/2, spdTop-d/2, spdWidth, spdWidth+d, spdPaint);
        drawSpeedPointer( (spdLeft+spdRight)/2, spdBottom+d/2, -spdWidth, spdWidth+d, spdPaint);

        int mhInt = Math.round(heading);
        if (mhInt == 0) mhInt = 360;
        String mh = String.format("%03d", mhInt);
        String as = Math.round(speed) + " kts";
        drawText(3*ascent,"AS:", as);
        drawText(2*ascent,"MH:", mh);

        drawCompassText();

        if (location != null) {
            if (location.hasSpeed() && location.hasBearing()) {
                float gs = location.getSpeed() * (float)(39.37/12 * 3600 / 6076.115); //kts
                float brng = location.getBearing();
                int tkInt = Math.round(brng);
                String tk = String.format("%03d", tkInt);
                drawText(0.5f * ascent, "GS:", Math.round(gs)+" kts");
                drawText(-0.5f * ascent, "TK:", tk);

                double dpr = 180/Math.PI;
                V3 hdgvec = new V3(Math.sin((90-heading)/dpr), Math.cos((90-heading)/dpr), 0);
                hdgvec = hdgvec.scale(speed);
                V3 trkvec = new V3(Math.sin((90-brng)/dpr), Math.cos((90-brng)/dpr), 0);
                trkvec = trkvec.scale(gs);
                V3 windvec = trkvec.minus(hdgvec);
                V3 north = new V3(1,0,0);
                V3 east = new V3(0, 1, 0);
                int ws = (int)Math.round(windvec.length());
                int ang = (int)Math.round(windvec.angle(north));
                if (windvec.dot(east) < 0) ang = 360 - ang;

                drawText(-2 * ascent, "WS:", ws+" kts");
                drawText(-3 * ascent, "WD:", String.format("%03d", ang));
            }
        }

        float instRadius = 100;
        float dxy = (float)((cr + instRadius*3/4)/Math.sqrt(2));
        drawKnob(dxy, dxy, instRadius, "+", 0);
        drawKnob(-dxy, dxy, instRadius, "-", 0);
        drawKnob(-dxy, -dxy, instRadius, "DG", (dgmode?Color.GREEN:0));
    }

    private void drawText(float y, String label, String value) {
        float tw = numberPaint.measureText(label);
        float x = cx - tw - 20;
        y = cy - y + ascent/2;
        canvas.drawText(label, x, y, numberPaint);
        x = cx + 20;
        canvas.drawText(value, x, y, numberPaint);
    }

    private void drawCompassText() {
        int ch = Math.round(compass);
        String text = String.format("Compass: %03d", ch);
        float x = cx - numberPaint.measureText(text)/2;
        float y = cy + cr - 2 * numberPaint.ascent();
        canvas.drawText(text, x, y, numberPaint);
    }

    private void drawSpeedPointer(float x, float y, float h, float w, Paint paint) {
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x - w/2, y + h);
        path.lineTo(x + w/2, y + h);
        path.lineTo(x, y);
        canvas.drawPath(path, paint);
    }

    private void drawPointer(float x, float y, float h, float w, Paint paint) {
        x += cx;
        y += cy;
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x - w/2, y + h);
        path.lineTo(x + w/2, y + h);
        path.lineTo(x, y);
        canvas.drawPath(path, paint);
    }

    private void drawKnob(float x, float y, float r, String s, int color) {
        canvas.drawCircle(cx + x, cy + y, r, circlePaint);
        canvas.drawCircle(cx + x, cy + y, r * 0.8f, ringPaint);
        TextPaint p = new TextPaint(numberPaint);
        if (s.length() > 1) p.setTextSize(72);
        float sw = numberPaint.measureText(s);
        float sa = -numberPaint.ascent();
        sa = sa/3f;
        if (color != 0) p.setColor(color);
        canvas.drawText(s, cx + x - sw/2, cy + y + sa, p);
    }

    public boolean onTouch(View v, MotionEvent event) {
        float x = (event.getX() - cx);
        float y = (event.getY() - cy);
        float r = (float)Math.sqrt(x*x + y*y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if ((r < cr) && (r > cr - innerOffset)) {
                    lastTouch = new V3(x, y, 0);
                    dragMH = true;
                    dragAS = false;
                }
                else if ((r > cr) && (y > 0) && (y < cr) && (x < cr)) {
                    if (x > 0) incrementHeading(1);
                    else incrementHeading(-1);
                }
                else if ((r > cr) && (y < 0) && (x < 0)) {
                    if (!dgmode) heading = compass;
                    dgmode = !dgmode;
                }
                else if (x > cr) {
                    if ((y > -cr + spdWidth) && (y < cr - spdWidth)) {
                        lastASY = y;
                        dragMH = false;
                        dragAS = true;
                    }
                    else if (y < -cr + spdWidth) speed++;
                    else if (y > cr - spdWidth) speed--;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if ((lastTouch != null) && dragMH) {
                    V3 thisTouch = new V3(x, y, 0);
                    V3 cross = thisTouch.cross(lastTouch);
                    int angle = (int) thisTouch.angle(lastTouch);
                    if (angle >= 1) {
                        if (cross.z < 0)
                            incrementHeading(-angle);
                        else
                            incrementHeading(angle);
                        lastTouch = thisTouch;
                    }
                }
                else if (dragAS) {
                    speed += (lastASY - y) * 20f/cr;
                    if (speed < 40) speed = 40;
                    lastASY = y;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                invalidate();
                lastTouch = null;
                dragMH = false;
                dragAS = false;
                break;
        }
        return true;
    }

}
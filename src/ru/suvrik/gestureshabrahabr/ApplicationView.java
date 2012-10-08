package ru.suvrik.gestureshabrahabr;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Vibrator;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Point;

public class ApplicationView extends View {
    Paint paint = new Paint();			// ����� ���������, �����
    static float density;
    Canvas canvas;				// �����
    Bitmap image;				// ���������� ������
    float zoom = 500;			// ���������� �� ������
    Point position = new Point(50, 50);	// ������� ������
    boolean drawingMode = false;
    Point cursor = new Point(0, 0);
    Vibrator vibrator;
    ArrayList<Finger> fingers = new ArrayList<Finger>();		// ��� ������, ����������� �� ������
	long lastTapTime;		// ����� ���������� �������
	Point lastTapPosition;	// ���������� ���������� �������
	
    public ApplicationView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        this.setBackgroundColor(Color.GRAY);	// ������������� ����� ���� ����
        paint.setStrokeWidth(5*density);	// ������������� ������ ����� � 5dp
        image = Bitmap.createBitmap(500, 500, Config.ARGB_4444);	// ������ ���������� ������
        canvas = new Canvas(image);					// ������ �����
        canvas.drawColor(Color.WHITE);					// ����������� ��� ����� ������
        TimerTask task = new TimerTask() {
            public void run() {
            	if(fingers.size() > 0 && fingers.get(0).enabledLongTouch &&
            			System.currentTimeMillis() - fingers.get(0).wasDown > 1000){
            		fingers.get(0).enabledLongTouch = false;	// ������������ ������� �������
            		drawingMode = !drawingMode;			// ������ �����
            		vibrator.vibrate(80);
            	}
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, 300);
        vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	canvas.translate(position.x, position.y);	// ���������� �����
    	canvas.scale(zoom / 500, zoom / 500);		// �������� ���������� �� ������
    	canvas.drawBitmap(image, 0, 0, paint);		// ������ �����
    	if(drawingMode){
    	    int old = paint.getColor();			// ��������� ������ ����
    	    paint.setColor(Color.GRAY);			// ������ ����� ������ �����
    	    canvas.drawCircle((cursor.x-position.x)*500/zoom, (cursor.y-position.y)*500/zoom,
    	        paint.getStrokeWidth() / 2, paint);	// ������ ������ � ���� �����
    	    paint.setColor(old);			// ���������� ������ ����
    	}
    	/*canvas.restore();				// ���������� ���������� ��������� � �����������
    	for(int i = 0; i < fingers.size(); i++){	// ���������� ��� ������� � ���� ������
    	    canvas.drawCircle(fingers.get(i).Now.x, fingers.get(i).Now.y, 40 * density, paint);
    	}*/
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	int id = event.getPointerId(event.getActionIndex());
    	int action = event.getActionMasked();
    	if(action  == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
    	    fingers.add(event.getActionIndex(), new Finger(id, (int)event.getX(), (int)event.getY()));
    	else if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
    	    Finger finger = fingers.get(event.getActionIndex());  // �������� ������ �����
    	    // ���� ������� ������� ����� 100��, ����� ����������� ������� ������ �� ����� 200��,
    	    // ���� ������� ���� ����� ����������� � ��������� ����� ����� ��������� �� ����� 25dp
    	    if(System.currentTimeMillis() - finger.wasDown < 100 && finger.wasDown - lastTapTime < 200 &&
    	       finger.wasDown - lastTapTime > 0 && checkDistance(finger.Now, lastTapPosition) < density * 25){
    	    	Builder builder = new AlertDialog.Builder(getContext());
    	    	String[] items = {"�������", "������", "�����", "�������", "׸����", "�����", "Ƹ���", "�������"};
    	    	final AlertDialog dialog = builder.setTitle("�������� ���� �����").setItems(items, new DialogInterface.OnClickListener() {
    	    	    public void onClick(DialogInterface dialog, int which) {
    	    	        switch (which) {
    	    	            case 0:				// �������
    	    	              paint.setColor(Color.RED);
    	    	              break;
    	    	            case 1:				// ������
    	    	              paint.setColor(Color.GREEN);
    	    	              break;
    	    	            case 2:				// �����
    	    	              paint.setColor(Color.BLUE);
    	    	              break;
    	    	            case 3:				// �������
    	    	              paint.setColor(0xFF99CCFF);
    	    	              break;
    	    	            case 4:				// ׸����
    	    	              paint.setColor(Color.BLACK);
    	    	              break;
    	    	            case 5:				// �����
    	    	              paint.setColor(Color.WHITE);
    	    	              break;
    	    	            case 6:				// Ƹ���
    	    	              paint.setColor(Color.YELLOW);
    	    	              break;
    	    	            case 7:				// �������
    	    	              paint.setColor(0xFFFFCC99);
    	    	              break;
    	    	        }
    	    	    }
    	    	}).create();
    	    	dialog.show();
    	    }
    	    lastTapTime = System.currentTimeMillis();		// ��������� ����� ���������� �������
    	    lastTapPosition = finger.Now;			// ��������� ������� ���������� �������
    	    fingers.remove(fingers.get(event.getActionIndex()));
    	}else if(action == MotionEvent.ACTION_MOVE){
    	  for(int n = 0; n < fingers.size(); n++){
    	    fingers.get(n).setNow((int)event.getX(n), (int)event.getY(n));
    	  }
    	  checkGestures();
    	}
    	return true;
    }
    
    public void checkGestures(){
    	Finger finger = fingers.get(0);
    	if(fingers.size() > 1){
    	    float now = checkDistance(finger.Now, fingers.get(1).Now);
    	    float before = checkDistance(finger.Before, fingers.get(1).Before);
    	    if(!drawingMode){
    	        float oldSize = zoom;
    	        zoom = Math.max(now - before + zoom, density * 25);
    	        position.x -= (zoom - oldSize) / 2;
    	        position.y -= (zoom - oldSize) / 2;
    	    }else paint.setStrokeWidth(paint.getStrokeWidth() + (now - before) / 8);
    	}else{
    	    if(!drawingMode){
    	        position.x += finger.Now.x - finger.Before.x;
    	        position.y += finger.Now.y - finger.Before.y;
    	    }else{
    	        float x1 = (finger.Before.x-position.x)*500/zoom;		// ��������� ����������
    	        float x2 = (finger.Now.x-position.x)*500/zoom;			// � ������ �����������
    	        float y1 = (finger.Before.y-position.y)*500/zoom;		// � ����������
    	        float y2 = (finger.Now.y-position.y)*500/zoom;
    	        canvas.drawLine(x1, y1, x2, y2, paint);				// ������ �����
    	        canvas.drawCircle(x1, y1, paint.getStrokeWidth() / 2, paint);	// ���������� �����
    	        canvas.drawCircle(x2, y2, paint.getStrokeWidth() / 2, paint);
    	        cursor = finger.Now;
    	    }
    	}
    }
    
    static float checkDistance(Point p1, Point p2){	// ������� ���������� ���������� ����� ����� �������
        return FloatMath.sqrt((p1.x - p2.x)*(p1.x - p2.x)+(p1.y - p2.y)*(p1.y - p2.y));
    }
}
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
    Paint paint = new Paint();			// Стиль рисования, кисть
    static float density;
    Canvas canvas;				// Холст
    Bitmap image;				// Содержимое холста
    float zoom = 500;			// Расстояние до холста
    Point position = new Point(50, 50);	// Позиция холста
    boolean drawingMode = false;
    Point cursor = new Point(0, 0);
    Vibrator vibrator;
    ArrayList<Finger> fingers = new ArrayList<Finger>();		// Все пальцы, находящиеся на экране
	long lastTapTime;		// Время последнего касания
	Point lastTapPosition;	// Координаты последнего касания
	
    public ApplicationView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        this.setBackgroundColor(Color.GRAY);	// Устанавливаем серый цвет фона
        paint.setStrokeWidth(5*density);	// Устанавливаем ширину кисти в 5dp
        image = Bitmap.createBitmap(500, 500, Config.ARGB_4444);	// Создаём содержимое холста
        canvas = new Canvas(image);					// Создаём холст
        canvas.drawColor(Color.WHITE);					// Закрашиваем его белым цветом
        TimerTask task = new TimerTask() {
            public void run() {
            	if(fingers.size() > 0 && fingers.get(0).enabledLongTouch &&
            			System.currentTimeMillis() - fingers.get(0).wasDown > 1000){
            		fingers.get(0).enabledLongTouch = false;	// Деактивируем двойное касание
            		drawingMode = !drawingMode;			// Меняем режим
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
    	canvas.translate(position.x, position.y);	// Перемещаем холст
    	canvas.scale(zoom / 500, zoom / 500);		// Изменяем расстояние до холста
    	canvas.drawBitmap(image, 0, 0, paint);		// Рисуем холст
    	if(drawingMode){
    	    int old = paint.getColor();			// Сохраняем старый цвет
    	    paint.setColor(Color.GRAY);			// Курсор будет серого цвета
    	    canvas.drawCircle((cursor.x-position.x)*500/zoom, (cursor.y-position.y)*500/zoom,
    	        paint.getStrokeWidth() / 2, paint);	// Рисуем курсор в виде круга
    	    paint.setColor(old);			// Возвращаем старый цвет
    	}
    	/*canvas.restore();				// Сбрасываем показатели отдаления и перемещения
    	for(int i = 0; i < fingers.size(); i++){	// Отображаем все касания в виде кругов
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
    	    Finger finger = fingers.get(event.getActionIndex());  // Получаем нужный палец
    	    // Если касание длилось менее 100мс, после предыдущего касания прошло не более 200мс,
    	    // если касание было после предыдущего и дистанция между двумя касаниями не более 25dp
    	    if(System.currentTimeMillis() - finger.wasDown < 100 && finger.wasDown - lastTapTime < 200 &&
    	       finger.wasDown - lastTapTime > 0 && checkDistance(finger.Now, lastTapPosition) < density * 25){
    	    	Builder builder = new AlertDialog.Builder(getContext());
    	    	String[] items = {"Красный", "Зелёный", "Синий", "Голубой", "Чёрный", "Белый", "Жёлый", "Розовый"};
    	    	final AlertDialog dialog = builder.setTitle("Выберите цвет кисти").setItems(items, new DialogInterface.OnClickListener() {
    	    	    public void onClick(DialogInterface dialog, int which) {
    	    	        switch (which) {
    	    	            case 0:				// Красный
    	    	              paint.setColor(Color.RED);
    	    	              break;
    	    	            case 1:				// Зелёный
    	    	              paint.setColor(Color.GREEN);
    	    	              break;
    	    	            case 2:				// Синий
    	    	              paint.setColor(Color.BLUE);
    	    	              break;
    	    	            case 3:				// Голубой
    	    	              paint.setColor(0xFF99CCFF);
    	    	              break;
    	    	            case 4:				// Чёрный
    	    	              paint.setColor(Color.BLACK);
    	    	              break;
    	    	            case 5:				// Белый
    	    	              paint.setColor(Color.WHITE);
    	    	              break;
    	    	            case 6:				// Жёлый
    	    	              paint.setColor(Color.YELLOW);
    	    	              break;
    	    	            case 7:				// Розовый
    	    	              paint.setColor(0xFFFFCC99);
    	    	              break;
    	    	        }
    	    	    }
    	    	}).create();
    	    	dialog.show();
    	    }
    	    lastTapTime = System.currentTimeMillis();		// Сохраняем время последнего касания
    	    lastTapPosition = finger.Now;			// Сохраняем позицию последнего касания
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
    	        float x1 = (finger.Before.x-position.x)*500/zoom;		// Вычисляем координаты
    	        float x2 = (finger.Now.x-position.x)*500/zoom;			// с учётом перемещения
    	        float y1 = (finger.Before.y-position.y)*500/zoom;		// и расстояния
    	        float y2 = (finger.Now.y-position.y)*500/zoom;
    	        canvas.drawLine(x1, y1, x2, y2, paint);				// Рисуем линию
    	        canvas.drawCircle(x1, y1, paint.getStrokeWidth() / 2, paint);	// Сглаживаем концы
    	        canvas.drawCircle(x2, y2, paint.getStrokeWidth() / 2, paint);
    	        cursor = finger.Now;
    	    }
    	}
    }
    
    static float checkDistance(Point p1, Point p2){	// Функция вычисления расстояния между двумя точками
        return FloatMath.sqrt((p1.x - p2.x)*(p1.x - p2.x)+(p1.y - p2.y)*(p1.y - p2.y));
    }
}
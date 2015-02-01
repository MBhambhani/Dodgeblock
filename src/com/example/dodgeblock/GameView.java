package com.example.dodgeblock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	
	private GameThread thread;
	
	private final Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private Rect[] blockRects = new Rect[7];
	private Rect playerRect = new Rect();
	private Rect groundRect = new Rect();

	private String healthText = "";
	private int textTop;
	private int textBottom;
	
	
	
	public GameView(Context context) {
		super(context);
		getHolder().addCallback(this);
		
		//set up Rect objects, text position, and drawing thread
		for (int i = 0; i < 7; i++) {
			blockRects[i] = new Rect();
		}
		
		thread = new GameThread(getHolder());
		thread.start();
		
		//delay the thread by 450 milliseconds to allow the SurfaceView to run its background initializing processes without being interrupted
		thread.delay();
	}

	/**Draws graphics onto the screen*/
	@Override
	public void onDraw(Canvas c) {
		//draw on screen
		c.drawColor(Color.WHITE);
		c.drawRect(playerRect, paint1);
		c.drawRect(groundRect, paint2);
		for (int i = 0; i < 7; i++) {
			c.drawRect(blockRects[i], paint2);
		}
		c.drawText(healthText, groundRect.left, textBottom, paint3);
	}

	/**Required for the SurfaceView class, but is not used*/
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

	/**Called when the SurfaceView is created*/
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		setWillNotDraw(false);
		
		//set up Paint objects
		paint3.setColor(Color.WHITE);
		paint3.setStyle(Style.FILL);
		paint3.setTextSize(textBottom - textTop);
		paint2.setColor(Color.BLACK);
		paint2.setStyle(Style.FILL);
		paint1.setColor(Color.rgb(182, 28, 28));
		paint1.setStyle(Style.FILL);
	}

	/**Called when the SurfaceView is destroyed*/
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		thread.setPaused(true);
    }
	
	
	
	////////UPDATE PLAYER AND BLOCK RECTS////////
	
	/**Updates the Rects that correspond to the positions of the player and the blocks*/
	public void updateRects() {
		playerRect.set(GameActivity.getPlayerRect());
		
		int len = blockRects.length;
		for (int i = 0; i < len; i++) {
			blockRects[i].set(GameActivity.getBlockRect(i));
		}
	}
	
	
	
	////////GETTERS & SETTERS////////
	
	/**Getter for the thread in this class*/
	public GameThread getThread() {
		return thread;
	}
	
	/**Sets the text that displays the player's health*/
	public void setHealthText(int health) {
		healthText = "Health: " + health;
	}
	
	/**Sets the Rect object that corresponds to the ground, and the position of the text that displays health*/
	public void setGroundRect(Rect ground) {
		groundRect.set(ground);
		textTop = ((3 * ground.top) + ground.bottom) / 4;
		textBottom = (ground.top + (3 * ground.bottom)) / 4;
	}
	
	
	
	/**GameThread class (draws graphics on the screen)*/
	class GameThread extends Thread {
		private SurfaceHolder sh;
		private boolean run = true, paused = true;
		
		public GameThread(SurfaceHolder surfaceHolder) {
			sh = surfaceHolder;
        }

		/**Used to stop/start the thread*/
        public void setRunning(boolean b) {
            run = b;
        }
        
        /**Used to pause/resume the thread*/
        public void setPaused(boolean b) {
            paused = b;
        }
        
        /**Makes the thread sleep for 450 milliseconds*/
        public void delay() {
        	try {
				sleep(450);
			} catch (InterruptedException e) {}
        }


        @Override
        public void run() {
            Canvas c;
            while (run) { //while the thread is set to run
            	if (!paused) { //if the thread isn't paused
            		c = null;

                    try {
                        c = sh.lockCanvas(null);
                        synchronized (sh) {
                        	//update Rect objects that will be drawn on the screen
                        	updateRects();
                        	//draw on screen
                        	postInvalidate();
                        }
                        sleep(10);
                    } catch (InterruptedException e) {
    				} finally {
    					try {
    						if (c != null) {
                            	sh.unlockCanvasAndPost(c);
                            }
    					}
    					catch (IllegalArgumentException x) {}
                    }
            	}
            }
        }
	}
}
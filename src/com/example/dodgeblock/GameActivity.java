package com.example.dodgeblock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;

public class GameActivity extends Activity implements SensorEventListener {
	
	private static Player player = null;
	private static Block[] blocks = new Block[7];
	private GameView gw;
	private MainThread thread = null;
	private SensorManager sm;
	private Sensor acc;
	private MediaPlayer mp;
	private int screenWidth;
	private int speedModifier = 1;
	private int blockHeight, blockWidth;
	private int groundTop;
	
	
	
	/**Called when the app is started*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//get resolution for scaling to different screen resolutions
		screenWidth = getResources().getDisplayMetrics().widthPixels;
		final int xScale = screenWidth/240;
		final int yScale = getResources().getDisplayMetrics().heightPixels/320;
		
		//set up SensorManager
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		//set up MediaPlayer
		mp = MediaPlayer.create(this, R.raw.game_soundtrack);
		
		//initialize ground
		groundTop = 300 * yScale;
		
		blockHeight = 20 * yScale;
		blockWidth = 15 * xScale;
		
		//initialize the GameView, the Player object, and all Block objects
		initSurface();
		initGame(xScale, yScale);
		
		//initialize the main thread
		thread = new MainThread();
		thread.setSpeed(2 * yScale);
		thread.start();
		thread.setStartTime();
	}
	
	/**Called when user resumes the app*/
	protected void onResume() {
		super.onResume();
		
		if (player.getHealth() != 0) { //if the player is still alive when the game is resumed (dialog is not displayed)
			
			if (!gw.getThread().isAlive()) { //if the drawing thread was stopped (this happens when the surfaceDestroyed method in GameView is called)
				//reset surface
				initSurface();
				gw.setHealthText(player.getHealth());
				
				//pause the main thread to allow the surface to be set up before any movement occurs
				thread.waitToResume();
			}
			
			//unpause threads and resume music
			gw.getThread().setPaused(false);
			thread.setPaused(false);
			thread.setStartTime();
			mp.start();
			
			//set listener for the SensorManager
			sm.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME);
		}
	}
	
	/**Called when user temporarily exits the app*/
	protected void onPause() {
		super.onPause();
		
		//pause threads and music
		thread.setPaused(true);
		thread.updateElapsedTime();
		gw.getThread().setPaused(true);
		mp.pause();
		
		//stop listening to the SensorManager
		sm.unregisterListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**Returns to the main activity*/
	public void returnToMenu() {
		sm.unregisterListener(this);
		//switch to MainActivity
		Intent menu = new Intent(this, MainActivity.class);
		menu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(menu);
	}
	
	/**Initializes a GameView, which will be used to display graphics*/
	public void initSurface() {
		Rect ground = new Rect(0, groundTop, screenWidth, getResources().getDisplayMetrics().heightPixels);
		
		//initialize and display view for graphics
		gw = new GameView(GameActivity.this);
		gw.setGroundRect(ground);
		GameActivity.this.setContentView(gw);
	}
	
	////////UPDATING PLAYER AND BLOCKS////////
	
	/**Initializes player and block objects
	 * @param xScale		Value used to scale the x coordinates of objects
	 * @param yScale		Value used to scale the y coordinates of objects
	 */
	private void initGame(int xScale, int yScale) {
		//reset player
		int left = 110 * xScale;
		Rect newRect = new Rect(left, 280 * yScale, left + 20 * xScale, groundTop);
		player = new Player(newRect, 20 * xScale, 2 * xScale, 3, false);
		gw.setHealthText(3); //reset health text
		
		//reset blocks
		for (int i = 0; i < 7; i++) {
			blocks[i] = new Block(new Rect(-100, -groundTop, blockWidth - 100, blockHeight - groundTop), true);
		}
	}

	/**Sets a random number of blocks to be dropped from the top of the screen*/
	private void setBlocks() {
		//randomize number of blocks to be placed
		int numOfBlocks = (int) (Math.random() * 7);
		int x;
		
		//place the blocks at a random place on the screen and update their position
		for (int i = 0; i <= numOfBlocks; i++) {
			x = (int)(Math.random()*(screenWidth - blockWidth)); //randomize block position
			
			blocks[i].setVisible(true);
			blocks[i].setRect(x, 0, x + blockWidth, blockHeight);
		}
	}
	
	////////GETTERS & SETTERS////////
	
	/**Returns the player object's rect*/
	public static Rect getPlayerRect() {
		return player.getRect();
	}

	/**Returns the rect of a Block at a specified index in the 'blocks' array*/
	public static Rect getBlockRect(int i) {
		return blocks[i].getRect();
	}
	
	
	
	////////SENSOR EVENT LISTENER METHODS////////

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	/**Listens to the sensor manager (changes player velocity)*/
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (player != null) { //if the game has started
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];

			double norm = Math.sqrt((x * x) + (y * y) + (z * z));

			//normalize accelerometer vector
			x = (float) (x / norm);
			y = (float) (y / norm);
			z = (float) (z / norm);
			
			int inclination = (int) Math.round(Math.toDegrees(Math.acos(z)));
			
			if (inclination >= 25 && inclination <= 155) { //if the device is not flat
				int rotation = (int) Math.round(Math.toDegrees(Math.atan2(x, y)));
				int absRotation = Math.abs(rotation);
				
				if (absRotation >= 10) { //rotation angle greater than or equal to 10 degrees
					
					int speed = Math.abs(player.getSpeed());
					speedModifier = absRotation / 10;
					player.setMoving(true);
					
					if (rotation < 0) { //clockwise rotation
						player.setSpeed(speed);
					}
					else { //counter-clockwise rotation
						player.setSpeed(speed * -1);
					}
					
				}
				else { //stop player movement if the rotation angle is less than 10 degrees
					player.setMoving(false);
				}
			}
		}
	}



	/**MainThread class (updates player and block position, and checks for collision)*/
	class MainThread extends Thread {
		private long startTime;
		private long elapsedTime = 0;
		private boolean run = true, paused = true;
		private float blockSpeedModifier = 1;
		private int speed, hitWaitCount;
		
		
		
		////////GETTERS & SETTERS////////
		
		/**Used to start/stop the thread*/
		public void setRunning(boolean b) {
			run = b;
		}
		
		/**Makes the thread sleep for 700 milliseconds*/
		public void waitToResume() {
			try {
				sleep(700);
			} catch (InterruptedException e) {}
		}

		/**Set speed for the falling blocks*/
		public void setSpeed(int speed) {
			this.speed = speed;
		}
		
		/**Used to pause/resume the thread*/
		public void setPaused(boolean b) {
			paused = b;
		}
		
		/**Sets the time that the game was started, in milliseconds*/
		public void setStartTime() {
			startTime = System.currentTimeMillis();
		}
		
		/**Updates elapsed time*/
		public void updateElapsedTime() {
			elapsedTime += System.currentTimeMillis() - startTime;
		}
				
		
		
		@Override
		public void run() {
			while (run) {
				if (!paused) {
					try {
						//PLAYER MOVEMENT
						int top = player.getRect().top;
						int left = player.getRect().left;
						
						if (player.isMoving()) { //if the player is moving
							
							int posIncrement = player.getSpeed() * speedModifier;
							int newLeft = left + posIncrement;
							
							if (newLeft > -2 && newLeft + player.getWidth() < screenWidth + 2) { //if the player is not colliding with either edge of the screen
								left = newLeft;
							}
							else if (newLeft <= -2) { //if the player is colliding with the left edge of the screen
								left = 0;
							}
							else { //if the player is colliding with the right edge of the screen
								left = screenWidth - player.getWidth();
							}
							
							player.setRect(left, top, left + player.getWidth(), groundTop);
						}
						
						
						//BLOCK COLLISION AND MOVEMENT
						int y;
						int x;
						int right = player.getRect().right;
						int blockDeathCount = 0;
						
						//check for collision and move every block
						for (Block block: blocks) {
							
							if (block.isVisible()) { //if the block has not hit the ground (is visible)
								
								y = block.getRect().top;
								x = block.getRect().left;
								
								//collision with player
								if (y + blockHeight >= top && hitWaitCount == 0) {
									if (x + blockWidth >= left && x <= right) {
										
										//update health and vibrate
										player.setHealth(player.getHealth() - 1);
										gw.setHealthText(player.getHealth());
										
										Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
										v.vibrate(500);
										
										
										hitWaitCount = 20; //the player cannot be hit again as long as this counter is above zero (this avoid double counting collisions with the same block)
										
										//PLAYER DEATH
										if (player.getHealth() == 0) { //if the player's health gets to 0
											
											//calculate score
											updateElapsedTime();
											final int score = (int)(elapsedTime / 1000);
											
											//stop threads and music
											gw.getThread().setRunning(false);
											gw.getThread().join();
											setRunning(false);
											mp.pause();
											
											//display score in a dialog
											runOnUiThread(new Runnable() {
												public void run() {
													
													//set up dialog
							                    	final AlertDialog alertDialog = new AlertDialog.Builder(GameActivity.this).create();
							                    	alertDialog.setMessage("You survived for " + score + " seconds.");
							                    	alertDialog.setButton(-3, "OK", new DialogInterface.OnClickListener() {
							                    	
							                    		public void onClick(DialogInterface dialog, int whichButton) {
							                    			//close dialog and return to main menu once the 'OK' button is clicked
							                    			alertDialog.dismiss();
							                    			returnToMenu();
							                    		}
							                    		
							                    	});
							                    	
							                    	alertDialog.show(); //display dialog
							                     }
							                });
											
											join(); //stop thread
										}
									}
								}
								
								//collision with ground
								if (y + blockHeight >= groundTop) {
									block.setVisible(false);
									x = -100;
								}
								
								//move block down
								y += speed * blockSpeedModifier;
								block.setRect(x, y, x + blockWidth, y + blockHeight);
								
							}
							else { //if the block has hit the ground (is not visible)
								blockDeathCount++;
							}
							
						}
						
						if (blockDeathCount == 7) { //if all the blocks are off the screen
							setBlocks(); //drop blocks again
							blockSpeedModifier += 0.02; //increases block velocity
						}
						
						if (hitWaitCount > 0) { //if the player was hit recently
							hitWaitCount -= 1; //decrease wait counter
						}
						
						sleep(10); //sleep for 10 milliseconds
					} catch (InterruptedException e) {
					}
				}
				
			}
		}
	}
}

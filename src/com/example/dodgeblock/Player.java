package com.example.dodgeblock;

import android.graphics.Rect;

public class Player {
	private Rect rect;
	private int speed, width, health;
	private boolean moving;
	
	public Player(Rect rect, int width, int speed, int health, boolean moving) {
		this.rect = rect;
		this.width = width;
		this.speed = speed;
		this.health = health;
		this.moving = moving;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isMoving() {
		return moving;
	}

	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public Rect getRect() {
		return rect;
	}

	public void setRect(int left, int top, int right, int bottom) {
		this.rect.set(left, top, right, bottom);
	}
}
package com.example.dodgeblock;

import android.graphics.Rect;

public class Block {
	private Rect rect;
	private boolean visible;
	
	public Block(Rect rect, boolean visible) {
		this.rect = rect;
		this.visible = visible;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public Rect getRect() {
		return rect;
	}

	public void setRect(int left, int top, int right, int bottom) {
		this.rect.set(left, top, right, bottom);
	}
}
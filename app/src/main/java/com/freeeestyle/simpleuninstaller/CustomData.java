/**
 * @date 2014/03/17
 * @author Shinji
 */
package com.freeeestyle.simpleuninstaller;

import android.graphics.drawable.Drawable;

/**
 * クラスの内容
 */
public class CustomData {
	private Drawable imageData;

	private String textData;

	public void setImagaData(Drawable image) {
		imageData = image;
	}

	public Drawable getImageData() {
		return imageData;
	}

	public void setTextData(String text) {
		textData = text;
	}

	public String getTextData() {
		return textData;
	}
}

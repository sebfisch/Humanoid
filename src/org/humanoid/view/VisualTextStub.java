package org.humanoid.view;

import android.content.Context;
import android.view.View;
import android.view.ViewStub;

public class VisualTextStub extends VisualText {
	public VisualTextStub(final Context context, final String text, final int style) {
		super(context, text, style);
	}
	
	public VisualTextStub(final String text) {
		super(text);
	}
	
	@Override
	public void visualize(final View stub) {
		if (stub != null && !"".equals(this.text)) {
			super.visualize(((ViewStub) stub).inflate());
		}
	}
}

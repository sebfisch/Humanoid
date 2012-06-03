package org.humanoid.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class VisualText implements Visual {

	private transient final Context context;

	protected transient final String text;
	protected transient final int style;

	public VisualText(final Context context, final String text, final int style) {
		this.context = context;
		this.text = text;
		this.style = style;
	}

	public VisualText(final String text) {
		this(null, text, 0);
	}

	@Override
	public void visualize(final View view) {
		final TextView textView = (TextView) view;
		textView.setText(text);

		if (this.context != null && this.style > 0) {
			textView.setTextAppearance(this.context, this.style);
		}
	}
}

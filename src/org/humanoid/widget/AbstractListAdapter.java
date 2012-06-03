package org.humanoid.widget;


import java.util.List;

import org.humanoid.R;
import org.humanoid.view.Visual;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;


public abstract class AbstractListAdapter<Item> extends BaseAdapter implements
		ListAdapter {

	private final transient Context context;
	private final transient LayoutInflater inflater;

	public abstract int getItemLayoutId();

	public abstract List<Item> getItems();

	public abstract Visual getVisualItem(Item item, int position);

	public AbstractListAdapter(final Context context) {
		super();

		this.context = context;
		this.inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return this.getItems().size();
	}

	@Override
	public Object getItem(final int position) {
		return this.getItems().get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		
		// try to release memory used by old view
		if (convertView != null) {
			convertView.setVisibility(View.GONE);
			convertView.destroyDrawingCache();
		}

		final View itemView = this.inflater.inflate(this.getItemLayoutId(),
				null);
		itemView.setLayoutParams(new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, 1));

		final ImageView rightArrow = new ImageView(this.context);
		rightArrow.setImageResource(R.drawable.submenu_arrow_nofocus);

		final LinearLayout.LayoutParams center = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		center.gravity = Gravity.CENTER;
		center.setMargins(0, 0, 10, 0);

		rightArrow.setLayoutParams(center);

		final LinearLayout wrappedView = new LinearLayout(this.context);
		wrappedView.setLayoutParams(new ListView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		wrappedView.addView(itemView);
		wrappedView.addView(rightArrow);

		this.getVisualItem(this.getItems().get(position), position).visualize(
				itemView);
		rightArrow.setVisibility(this.isEnabled(position) ? View.VISIBLE
				: View.INVISIBLE);

		return wrappedView;
	}
}

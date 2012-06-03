package org.humanoid.app;

import org.humanoid.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public abstract class RefreshableListActivity<Model, Item> extends
		SherlockListActivity implements OnItemClickListener {

	private transient boolean isInProgress;
	private transient MenuItem refreshItem, progressItem;
	
	public abstract int getEmptyTextId();

	public abstract ListAdapter createListAdapter(Model model);

	public abstract void onItemClick(Item item);

	public void onPreRefresh() {}

	public abstract Model loadModel();

	public abstract void displayModel(Model model);

	public void onPostRefresh(Model model) {}

	@Override
	public void onCreate(final Bundle savedState) {
		super.onCreate(savedState);

		this.isInProgress = false;

		final ListView listView = this.getListView();
		listView.setOnItemClickListener(this);

		final View emptyView = this.getEmptyView();
		((ViewGroup) listView.getParent()).addView(emptyView);
		listView.setEmptyView(emptyView);
		emptyView.setVisibility(View.GONE);
	}

	public View getEmptyView() {
		final TextView emptyView = new TextView(this);
		emptyView.setText(this.getEmptyTextId());
		return emptyView;
	}

	public int getMenuId() {
		return R.menu.refresh_menu;
	}

	public int getRefreshItemId() {
		return R.id.refresh_item;
	}

	public int getProgressItemId() {
		return R.id.progress_item;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		boolean result;
		if (item.getItemId() == this.getRefreshItemId()) {
			this.onPreRefresh();
			this.refresh();
			result = true;
		} else {
			result = super.onOptionsItemSelected(item);
		}
		return result;
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long rowId) {
		this.onItemClick((Item) this.getListAdapter().getItem(position));
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getSupportMenuInflater().inflate(this.getMenuId(), menu);

		this.refreshItem = menu.findItem(this.getRefreshItemId());
		this.progressItem = menu.findItem(this.getProgressItemId());

		this.setInProgress(this.isInProgress);

		return true;
	}

	protected final void refresh() {
		this.setInProgress(true);
		final RefreshableListActivity<Model, Item> context = this;
		new AsyncTask<Object, Void, Model>() {
			@Override
			protected Model doInBackground(final Object... params) {
				Model result = null;
				if (context != null) {
					result = context.loadModel();
				}
				return result;
			}

			@Override
			protected void onPostExecute(final Model model) {
				if (context != null) {
					context.displayModel(model);
					if (model != null) {
						context.setListAdapter(context.createListAdapter(model));
					}
					context.onPostRefresh(model);
				}
				setInProgress(false);
			}
		}.execute();
	}

	private void setInProgress(final boolean isInProgress) {
		this.isInProgress = isInProgress;
		if (this.refreshItem != null && this.progressItem != null) {
			this.refreshItem.setVisible(!isInProgress);
			this.progressItem.setVisible(isInProgress);
		}
	}
}

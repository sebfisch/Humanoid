package org.humanoid.maps;

import org.mapsforge.android.maps.MapActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.ActionBarSherlock.OnActionModeFinishedListener;
import com.actionbarsherlock.ActionBarSherlock.OnActionModeStartedListener;
import com.actionbarsherlock.ActionBarSherlock.OnCreatePanelMenuListener;
import com.actionbarsherlock.ActionBarSherlock.OnMenuItemSelectedListener;
import com.actionbarsherlock.ActionBarSherlock.OnPreparePanelListener;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class SherlockMapActivity extends MapActivity implements
		OnCreatePanelMenuListener, OnPreparePanelListener,
		OnMenuItemSelectedListener, OnActionModeStartedListener,
		OnActionModeFinishedListener {
	private transient ActionBarSherlock mSherlock;

	protected final ActionBarSherlock getSherlock() {
		if (mSherlock == null) {
			mSherlock = ActionBarSherlock.wrap(this,
					ActionBarSherlock.FLAG_DELEGATE);
		}
		return mSherlock;
	}

	// /////////////////////////////////////////////////////////////////////////
	// Action bar and mode
	// /////////////////////////////////////////////////////////////////////////

	public ActionBar getSupportActionBar() {
		return getSherlock().getActionBar();
	}

	public ActionMode startActionMode(final ActionMode.Callback callback) {
		return getSherlock().startActionMode(callback);
	}

	@Override
	public void onActionModeStarted(final ActionMode mode) {
	}

	@Override
	public void onActionModeFinished(final ActionMode mode) {
	}

	// /////////////////////////////////////////////////////////////////////////
	// General lifecycle/callback dispatching
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getSherlock().dispatchConfigurationChanged(newConfig);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		getSherlock().dispatchPostResume();
	}

	@Override
	protected void onPause() {
		getSherlock().dispatchPause();
		super.onPause();
	}

	@Override
	protected void onStop() {
		getSherlock().dispatchStop();
		super.onStop();
	}

	@Override
	protected void onPostCreate(final Bundle savedState) {
		getSherlock().dispatchPostCreate(savedState);
		super.onPostCreate(savedState);
	}

	@Override
	protected void onTitleChanged(final CharSequence title, final int color) {
		getSherlock().dispatchTitleChanged(title, color);
		super.onTitleChanged(title, color);
	}

	@Override
	public final boolean onMenuOpened(final int featureId,
			final android.view.Menu menu) {
		if (getSherlock().dispatchMenuOpened(featureId, menu)) {
			return true;
		}
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public void onPanelClosed(final int featureId, final android.view.Menu menu) {
		getSherlock().dispatchPanelClosed(featureId, menu);
		super.onPanelClosed(featureId, menu);
	}

	@Override
	public boolean dispatchKeyEvent(final KeyEvent event) {
		if (getSherlock().dispatchKeyEvent(event)) {
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	// /////////////////////////////////////////////////////////////////////////
	// Native menu handling
	// /////////////////////////////////////////////////////////////////////////

	public MenuInflater getSupportMenuInflater() {
		return getSherlock().getMenuInflater();
	}

	public void invalidateOptionsMenu() {
		getSherlock().dispatchInvalidateOptionsMenu();
	}

	public void supportInvalidateOptionsMenu() {
		invalidateOptionsMenu();
	}

	@Override
	public final boolean onCreateOptionsMenu(final android.view.Menu menu) {
		return getSherlock().dispatchCreateOptionsMenu(menu);
	}

	@Override
	public final boolean onPrepareOptionsMenu(final android.view.Menu menu) {
		return getSherlock().dispatchPrepareOptionsMenu(menu);
	}

	@Override
	public final boolean onOptionsItemSelected(final android.view.MenuItem item) {
		return getSherlock().dispatchOptionsItemSelected(item);
	}

	@Override
	public void openOptionsMenu() {
		if (!getSherlock().dispatchOpenOptionsMenu()) {
			super.openOptionsMenu();
		}
	}

	@Override
	public void closeOptionsMenu() {
		if (!getSherlock().dispatchCloseOptionsMenu()) {
			super.closeOptionsMenu();
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// Sherlock menu handling
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public boolean onCreatePanelMenu(final int featureId, final Menu menu) {
		if (featureId == Window.FEATURE_OPTIONS_PANEL) {
			return onCreateOptionsMenu(menu);
		}
		return false;
	}

	public boolean onCreateOptionsMenu(final Menu menu) {
		return true;
	}

	@Override
	public boolean onPreparePanel(final int featureId, final View view,
			final Menu menu) {
		if (featureId == Window.FEATURE_OPTIONS_PANEL) {
			return onPrepareOptionsMenu(menu);
		}
		return false;
	}

	public boolean onPrepareOptionsMenu(final Menu menu) {
		return true;
	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		if (featureId == Window.FEATURE_OPTIONS_PANEL) {
			return onOptionsItemSelected(item);
		}
		return false;
	}

	public boolean onOptionsItemSelected(final MenuItem item) {
		return false;
	}

	// /////////////////////////////////////////////////////////////////////////
	// Content
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public void addContentView(final View view, final LayoutParams params) {
		getSherlock().addContentView(view, params);
	}

	@Override
	public void setContentView(final int layoutResId) {
		getSherlock().setContentView(layoutResId);
	}

	@Override
	public void setContentView(final View view, final LayoutParams params) {
		getSherlock().setContentView(view, params);
	}

	@Override
	public void setContentView(final View view) {
		getSherlock().setContentView(view);
	}

	public void requestWindowFeature(final long featureId) {
		getSherlock().requestFeature((int) featureId);
	}

	// /////////////////////////////////////////////////////////////////////////
	// Progress Indication
	// /////////////////////////////////////////////////////////////////////////

	public void setSupportProgress(final int progress) {
		getSherlock().setProgress(progress);
	}

	public void setSupportProgressBarIndeterminate(final boolean indeterminate) {
		getSherlock().setProgressBarIndeterminate(indeterminate);
	}

	public void setSupportProgressBarIndeterminateVisibility(final boolean visible) {
		getSherlock().setProgressBarIndeterminateVisibility(visible);
	}

	public void setSupportProgressBarVisibility(final boolean visible) {
		getSherlock().setProgressBarVisibility(visible);
	}

	public void setSupportSecondaryProgress(final int secondaryProgress) {
		getSherlock().setSecondaryProgress(secondaryProgress);
	}
}

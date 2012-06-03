package org.humanoid.maps;


import org.mapsforge.core.GeoPoint;

import android.location.Location;


public abstract class Tracker {

	public interface OnTrackerStatusChangeListener {
		void onTrackerStatusChange();
	}

	private transient final MoveListener moveListener;
	private transient final ZoomListener zoomListener;

	private transient MapView mapView;
	private transient OnTrackerStatusChangeListener listener;
	private transient boolean isTracking;

	private class MoveListener extends MapView.OnMoveListener {

		private transient boolean isActive = true;

		@Override
		public void onMove(final GeoPoint location) {
			if (isTracking && this.isActive) {
				stopTracking();
			}
		}
	}

	private class ZoomListener extends MapView.OnZoomListener {

		@Override
		public void onZoom(final byte zoomLevel) {
			if (isTracking) {
				updateLocation(getTrackedLocation());
			}
		}
	}

	public void setStatusChangeListener(
			final OnTrackerStatusChangeListener listener) {
		this.listener = listener;
	}

	private void notifyStatusChangeListener() {
		if (this.listener != null) {
			this.listener.onTrackerStatusChange();
		}
	}

	public abstract void startListening();

	public void stopListening() {
		this.notifyStatusChangeListener();
	}

	public void startTracking() {
		this.isTracking = true;
		this.propagateTrackedLocation();
	}

	public void stopTracking() {
		this.isTracking = false;
		this.notifyStatusChangeListener();
	}

	public boolean isTracking() {
		return this.isTracking;
	}

	public abstract Location getTrackedLocation();

	public Tracker() {
		this.moveListener = new MoveListener();
		this.zoomListener = new ZoomListener();
		this.isTracking = false;
	}

	public void setMapView(final MapView mapView) {
		this.mapView = mapView;
		this.mapView.getOnRedrawListeners().add(this.moveListener);
		this.mapView.getOnRedrawListeners().add(this.zoomListener);
		this.propagateTrackedLocation();
	}

	protected void updateLocation(final Location location) {
		if (isTracking && mapView != null) {
			mapView.post(new Runnable() {

				@Override
				public void run() {
					moveListener.isActive = false;
					mapView.setCenter(new GeoPoint(location.getLatitude(),
							location.getLongitude()));
					moveListener.isActive = true;
				}
			});
		}

		this.notifyStatusChangeListener();
	}

	protected void propagateTrackedLocation() {
		if (this.getTrackedLocation() != null) {
			this.updateLocation(this.getTrackedLocation());
		}
	}
}

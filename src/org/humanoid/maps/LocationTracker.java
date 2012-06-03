package org.humanoid.maps;

import org.humanoid.location.BoundedLocationTracker;
import org.humanoid.location.BoundedLocationTracker.BoundedLocationListener;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;


public class LocationTracker extends Tracker {

	private transient final BoundedLocationTracker tracker;
	private transient final Listener listener;

	private transient LocationOverlay overlay;

	private class Listener implements BoundedLocationListener {

		@Override
		public void onLocationChanged(final Location location) {
			updateLocation(location);
		}

		@Override
		public void onLocationOutOfBounds() {
			stopTracking();
			stopListening();
		}

		@Override
		public void onProviderDisabled(final String provider) {
			this.onLocationOutOfBounds();
		}

		@Override
		public void onProviderEnabled(final String provider) {
		}

		@Override
		public void onStatusChanged(final String provider, final int status,
				final Bundle extras) {
		}
	}

	public LocationTracker(final Activity context, final double minLat,
			final double minLon, final double maxLat, final double maxLon) {
		super();
		this.tracker = new BoundedLocationTracker(
				(LocationManager) context
						.getSystemService(Context.LOCATION_SERVICE),
				minLat, minLon, maxLat, maxLon);
		this.listener = new Listener();
	}

	public void setOverlay(final LocationOverlay overlay) {
		this.overlay = overlay;
		this.propagateTrackedLocation();
	}

	@Override
	public void startListening() {
		this.tracker.startListening(LocationManager.GPS_PROVIDER, 0, 0);
		this.tracker.startListening(LocationManager.NETWORK_PROVIDER, 0, 0);
		this.tracker.setLocationListener(this.listener);
	}

	@Override
	public void stopListening() {
		this.tracker.stopListening();
		if (overlay != null) {
			overlay.removeLocationCircle();
		}
		super.stopListening();
	}

	@Override
	protected void updateLocation(final Location location) {
		super.updateLocation(location);
		if (overlay != null) {
			overlay.updateLocationCircle(location);
		}
	}

	@Override
	public Location getTrackedLocation() {
		return this.tracker.getLastKnownLocation();
	}

	public boolean isInsideBounds() {
		return this.getTrackedLocation() != null
				&& this.tracker.isInsideBounds(this.getTrackedLocation());
	}
}

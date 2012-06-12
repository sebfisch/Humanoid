package org.humanoid.maps;

import org.humanoid.location.BoundedLocationTracker;
import org.humanoid.location.BoundedLocationTracker.BoundedLocationListener;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationTracker extends Tracker {

	private transient final BoundedLocationTracker tracker;
	private transient final Listener listener;

	private transient final String coarseLocationProvider;
	private transient final String fineLocationProvider;

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

		final LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		this.tracker = new BoundedLocationTracker(locationManager, minLat,
				minLon, maxLat, maxLon);
		this.listener = new Listener();

		final Criteria coarseCriteria = new Criteria();
		coarseCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
		this.coarseLocationProvider = locationManager.getBestProvider(
				coarseCriteria, true);

		final Criteria fineCriteria = new Criteria();
		fineCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		this.fineLocationProvider = locationManager.getBestProvider(
				fineCriteria, true);
	}

	public void setOverlay(final LocationOverlay overlay) {
		this.overlay = overlay;
		this.propagateTrackedLocation();
	}

	@Override
	public void startListening() {
		if (this.coarseLocationProvider != null) {
			Log.w("LocationTracker", "start tracking "
					+ this.coarseLocationProvider);
			this.tracker.startListening(this.coarseLocationProvider, 0, 0);
		} else {
			Log.w("LocationTracker", "coarse location not availabe");
		}

		if (this.fineLocationProvider != null) {
			Log.w("LocationTracker", "start tracking "
					+ this.fineLocationProvider);
			this.tracker.startListening(this.fineLocationProvider, 0, 0);
		} else {
			Log.w("LocationTracker", "fine location not availabe");
		}

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

package org.humanoid.location;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class BoundedLocationTracker implements LocationListener {

	public interface BoundedLocationListener extends LocationListener {
		void onLocationOutOfBounds();
	}

	private final transient LocationManager manager;
	private transient BoundedLocationListener listener;
	private transient Location lastKnownLocation;

	private final transient double minLat, minLon, maxLat, maxLon;

	public BoundedLocationTracker(final LocationManager locationManager,
			final double minLat, final double minLon, final double maxLat,
			final double maxLon) {

		this.manager = locationManager;

		this.minLat = minLat;
		this.minLon = minLon;
		this.maxLat = maxLat;
		this.maxLon = maxLon;
	}

	public void startListening(final String provider, final long minTime,
			final float minDistance) {
		this.lastKnownLocation = null;
		this.manager.requestLocationUpdates(provider, minTime, minDistance,
				this);
	}

	public void stopListening() {
		this.manager.removeUpdates(this);
		this.listener = null;
	}

	public void setLocationListener(
			final BoundedLocationListener locationListener) {
		this.listener = locationListener;
		if (this.lastKnownLocation != null) {
			this.onLocationChanged(this.lastKnownLocation);
		}
	}

	public Location getLastKnownLocation() {
		return this.lastKnownLocation;
	}

	public boolean isInsideBounds(final Location location) {
		final double lat = location.getLatitude();
		final double lon = location.getLongitude();

		return minLat <= lat && lat <= maxLat && minLon <= lon && lon <= maxLon;
	}

	@Override
	public void onLocationChanged(final Location location) {
		this.lastKnownLocation = location;

		if (this.isInsideBounds(location)) {
			if (this.listener != null) {
				this.listener.onLocationChanged(location);
			}
		} else {
			if (this.listener != null) {
				this.listener.onLocationOutOfBounds();
			}
		}
	}

	@Override
	public void onProviderDisabled(final String provider) {
		if (this.listener != null) {
			this.listener.onProviderDisabled(provider);
		}
	}

	@Override
	public void onProviderEnabled(final String provider) {
		if (this.listener != null) {
			this.listener.onProviderEnabled(provider);
		}
	}

	@Override
	public void onStatusChanged(final String provider, final int status,
			final Bundle extras) {
		if (this.listener != null) {
			this.listener.onStatusChanged(provider, status, extras);
		}
	}
}

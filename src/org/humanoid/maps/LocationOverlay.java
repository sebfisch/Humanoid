package org.humanoid.maps;

import org.humanoid.R;
import org.mapsforge.android.maps.overlay.ArrayCircleOverlay;
import org.mapsforge.android.maps.overlay.OverlayCircle;
import org.mapsforge.core.GeoPoint;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.location.Location;

public class LocationOverlay extends ArrayCircleOverlay {
	// approximate meters per pixel ratio for zoom level 0
	// see: http://wiki.openstreetmap.org/wiki/Zoom_levels
	private static final float METERS_PER_PIXEL = 156412;
	private static final float MARKER_SIZE = 5;
	private static final float STROKE_WIDTH = 2;

	private transient final OverlayCircle locationMarker, locationCircle;
	private transient MapView mapView;
	private transient GeoPoint center;

	public LocationOverlay(final Resources res) {
		super(null, null);

		final Paint fill = new Paint();
		fill.setColor(res.getColor(R.color.holo_blue_transparent));
		final Paint outline = new Paint();
		outline.setColor(res.getColor(R.color.holo_blue_light));
		outline.setAntiAlias(true);
		outline.setStrokeCap(Paint.Cap.ROUND);
		outline.setStyle(Style.STROKE);
		outline.setStrokeWidth(LocationOverlay.STROKE_WIDTH);
		this.locationMarker = new OverlayCircle(fill, outline);
		this.locationCircle = new OverlayCircle(fill, outline);
	}

	public void register(final MapView mapView) {
		this.mapView = mapView;
		this.mapView.getOverlays().add(this);
		this.mapView.getOnRedrawListeners().add(new MapView.OnZoomListener() {

			@Override
			public void onZoom(final byte zoomLevel) {
				if (center != null) {
					locationMarker.setCircleData(center,
							getMarkerSize(zoomLevel));
				}
			}
		});
		this.requestRedraw();
	}

	public void updateLocationCircle(final Location location) {
		this.clear();

		this.center = new GeoPoint(location.getLatitude(),
				location.getLongitude());
		final double radius = this.getMarkerSize(this.mapView.getMapPosition()
				.getZoomLevel());
		this.locationMarker.setCircleData(this.center, (float) radius);
		this.addCircle(this.locationMarker);

		if (location.hasAccuracy()) {
			this.locationCircle.setCircleData(this.center,
					location.getAccuracy());
			this.addCircle(this.locationCircle);
		}

		this.populate();
		this.requestRedraw();
	}

	private float getMarkerSize(final byte zoomLevel) {
		return (float) (MARKER_SIZE * METERS_PER_PIXEL / Math.pow(2, zoomLevel));
	}

	public void removeLocationCircle() {
		this.clear();
		this.populate();
		this.requestRedraw();
	}
}

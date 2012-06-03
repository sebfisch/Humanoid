package org.humanoid.maps;

import java.util.ArrayList;
import java.util.List;

import org.mapsforge.core.GeoPoint;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.Scroller;

/**
 * Extension of a map view that allows to observe the map view when tiles are
 * redrawn and to restrict map movement to a bounding box.
 * 
 * @author Sebastian Fischer
 */
public class MapView extends org.mapsforge.android.maps.MapView {

	/**
	 * Allows to observe the map view when tiles are redrawn.
	 * 
	 * @author Sebastian Fischer
	 */
	public interface OnRedrawListener {

		/**
		 * Called when the observed map view is redrawn. The map view is passed
		 * as parameter and can be queried for map position or zoom level.
		 * 
		 * @param mapView
		 *            observed map view that will be redrawn
		 */
		void onRedraw(MapView mapView);
	}

	/**
	 * Allows to react on map view movements and ignore other changes like
	 * changed zoom levels.
	 * 
	 * @author Sebastian Fischer
	 */
	public static abstract class OnMoveListener implements OnRedrawListener {
		private transient GeoPoint oldCenter;

		/**
		 * Called when the observed map view is moved.
		 * 
		 * @param center
		 *            new map center
		 */
		abstract public void onMove(GeoPoint center);

		@Override
		public void onRedraw(final MapView mapView) {
			final GeoPoint newCenter = mapView.getMapPosition().getMapCenter();

			if (oldCenter == null || !this.oldCenter.equals(newCenter)) {
				this.onMove(newCenter);
			}

			this.oldCenter = newCenter;
		}
	}

	/**
	 * Allows to react on changed zoom levels of a map view and ignore other
	 * changes like map movements.
	 * 
	 * @author Sebastian Fischer
	 */
	public static abstract class OnZoomListener implements OnRedrawListener {
		private transient byte oldZoomLevel = 0;

		/**
		 * Called when the observed map view changes its zoom level.
		 * 
		 * @param zoomLevel
		 *            new zoom level
		 */
		abstract public void onZoom(byte zoomLevel);

		@Override
		public void onRedraw(final MapView mapView) {
			final byte newZoomLevel = mapView.getMapPosition().getZoomLevel();

			if (newZoomLevel != oldZoomLevel) {
				this.oldZoomLevel = newZoomLevel;
				this.onZoom(newZoomLevel);
			}
		}
	}

	private class GestureListener extends SimpleOnGestureListener {

		@Override
		public boolean onFling(final MotionEvent down, final MotionEvent move,
				final float velocityX, final float velocityY) {
			fling(velocityX, velocityY);
			return true;
		}

		@Override
		public boolean onDoubleTap(final MotionEvent tap) {
			if (zoomAroundCenter) {
				getController().zoomIn();
				return true;
			} else {
				return false;
			}
		}
	}

	private transient List<OnRedrawListener> listeners;
	private transient final GestureDetector gestureDetector;
	private transient final Scroller scroller;

	private transient boolean zoomAroundCenter;

	public MapView(final Context ctx) {
		super(ctx);

		this.gestureDetector = new GestureDetector(ctx, new GestureListener());
		this.scroller = new Scroller(ctx);
	}

	public void forceZoomAroundCenter(final boolean zoomAroundCenter) {
		this.zoomAroundCenter = zoomAroundCenter;
	}

	@Override
	public boolean dispatchTouchEvent(final MotionEvent event) {
		try {
			return this.gestureDetector.onTouchEvent(event)
					|| super.dispatchTouchEvent(event);
		} catch (Exception e) {
			Log.w("MapView", "Exception in TouchEvent listener", e);
			return false;
		}
	}

	public void fling(final double velocityX, final double velocityY) {
		final int maxDist = 1000000;
		this.scroller.forceFinished(true);
		this.scroller.fling(0, 0, (int) -velocityX / 3, (int) -velocityY / 3,
				-maxDist, maxDist, -maxDist, maxDist);
		this.scroll();
	}

	private Point getCenterPoint() {
		final Point center = new Point();
		this.getProjection().toPixels(this.getMapPosition().getMapCenter(),
				center);
		return center;
	}

	private void scroll() {
		this.scroll(null);
	}

	private void scroll(final Runnable postAction) {
		this.post(new Runnable() {

			int prevX = 0;
			int prevY = 0;

			@Override
			public void run() {
				if (scroller.isFinished()) {
					if (postAction != null) {
						postAction.run();
					}
				} else {
					if (scroller.computeScrollOffset()) {
						final Point center = getCenterPoint();
						final int deltaX = scroller.getCurrX() - prevX;
						final int deltaY = scroller.getCurrY() - prevY;
						prevX = scroller.getCurrX();
						prevY = scroller.getCurrY();
						setCenter(getProjection().fromPixels(center.x + deltaX,
								center.y + deltaY));
						post(this);
					}
				}
			}
		});
	}

	public void cancelScrolling() {
		this.scroller.forceFinished(true);
	}

	/**
	 * Returns the list of redraw listeners which can be used to add or remove
	 * listeners.
	 * 
	 * @return list of redraw listeners
	 */
	public List<OnRedrawListener> getOnRedrawListeners() {
		if (this.listeners == null) {
			this.listeners = new ArrayList<OnRedrawListener>();
		}
		return this.listeners;
	}

	/**
	 * <p>
	 * Called when the observed map view is redrawn. The map view is passed as
	 * parameter and can be queried for map position or zoom level.
	 * </p>
	 * 
	 * <p>
	 * This method can be overridden in subclasses to handle redraw events. The
	 * default implementation calls all redraw listeners in the list returned by
	 * {@link #getOnRedrawListeners()}.
	 * </p>
	 */
	public void onRedraw() {
		for (OnRedrawListener listener : this.getOnRedrawListeners()) {
			try {
				listener.onRedraw(this);
			} catch (Exception e) {
				Log.w("MapView", "Exception in OnRedrawListener", e);
			}
		}
	}

	@Override
	public void redrawTiles() {
		this.onRedraw();
		super.redrawTiles();
	}

	/**
	 * Restricts map movement to the given bounding box given in degrees.
	 * 
	 * @param minLat
	 *            bottom bound
	 * @param minLon
	 *            left bound
	 * @param maxLat
	 *            top bound
	 * @param maxLon
	 *            right bound
	 */
	public void setBoundingBox(final double minLat, final double minLon,
			final double maxLat, final double maxLon) {
		this.getOnRedrawListeners().add(new MapView.OnRedrawListener() {
			private static final double EPS = 0.001;

			@Override
			public void onRedraw(final MapView mapView) {
				final GeoPoint topLeft = mapView.getProjection().fromPixels(
						mapView.getLeft(), mapView.getTop());
				final GeoPoint bottomRight = mapView.getProjection()
						.fromPixels(mapView.getRight(), mapView.getBottom());

				if (this.isOutOfBounds(topLeft, bottomRight)) {
					this.recenter(mapView, topLeft, bottomRight);
				}
			}

			private boolean isOutOfBounds(final GeoPoint topLeft,
					final GeoPoint bottomRight) {

				return topLeft.getLatitude() > maxLat
						|| topLeft.getLongitude() < minLon
						|| bottomRight.getLatitude() < minLat
						|| bottomRight.getLongitude() > maxLon;
			}

			private void recenter(final MapView mapView,
					final GeoPoint topLeft, final GeoPoint bottomRight) {

				final GeoPoint center = mapView.getMapPosition().getMapCenter();
				double lat = center.getLatitude(), lon = center.getLongitude();

				if (topLeft.getLatitude() > maxLat) {
					lat = lat + maxLat - topLeft.getLatitude() - EPS;
				}

				if (topLeft.getLongitude() < minLon) {
					lon = lon + minLon - topLeft.getLongitude() + EPS;
				}

				if (bottomRight.getLatitude() < minLat) {
					lat = lat + minLat - bottomRight.getLatitude() + EPS;
				}

				if (bottomRight.getLongitude() > maxLon) {
					lon = lon + maxLon - bottomRight.getLongitude() - EPS;
				}

				setCenter(new GeoPoint(lat, lon));
			}
		});
	}
}

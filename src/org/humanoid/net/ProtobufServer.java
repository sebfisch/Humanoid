package org.humanoid.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.google.protobuf.GeneratedMessage;

/**
 * Wrapper to query a server using a protocol buffer interface.
 * 
 * @author Sebastian Fischer
 */
public final class ProtobufServer {
	private static final String TAG = "ProtobufServer";
	private static ProtobufServer instance = null;

	public static boolean isInitialized() {
		return instance != null;
	}

	/**
	 * Returns a singleton instance of this class or null if it has not been
	 * initialized yet.
	 * 
	 * @return object to query a server using protocol buffers
	 */
	public static ProtobufServer getInstance() {
		if (ProtobufServer.instance == null) {
			Log.e(TAG, "server not initialized");
		}
		return ProtobufServer.instance;
	}

	/**
	 * Creates a singleton instance of this class.
	 * 
	 * @param res
	 *            resources to use for getting query strings
	 */
	public static void createInstance(final Activity context,
			final Class stringResCls, final String baseUrl) {
		ProtobufServer.instance = new ProtobufServer(context, stringResCls,
				baseUrl);
	}

	private final transient Activity context;
	private final transient Class stringResCls;
	private final transient String baseUrl;
	private final transient Resources res;
	private final transient StringBuilder queryBuilder;

	private ProtobufServer(final Activity context, final Class stringResCls,
			final String baseUrl) {
		this.context = context;
		this.stringResCls = stringResCls;
		this.baseUrl = baseUrl;
		this.res = this.context.getResources();
		this.queryBuilder = new StringBuilder();
	}

	/**
	 * <p>
	 * Queries the server using the protocol buffer interface.
	 * </p>
	 * 
	 * <p>
	 * The queried message is determined using reflection, the query string is
	 * fetched from an XML file that associates class names to query strings.
	 * </p>
	 * 
	 * @param cls
	 *            class of the message that should be queried
	 * @return instance of the provided class queried from the server
	 */
	public <Msg extends GeneratedMessage> Msg query(final Class<Msg> cls,
			final Object... args) {
		return this.queryAndStore(cls, false, args);
	}

	public <Msg extends GeneratedMessage> Msg queryAndStore(
			final Class<Msg> cls, final boolean store, final Object... args) {

		Msg msg = null;

		int retryCount = 3;
		boolean successful = false;
		while (!successful && retryCount > 0) {
			try {
				msg = tryQueryAndStore(cls, store, args);
				successful = true;
			} catch (Exception e) {
				Log.w(TAG, "cannot parse " + cls.getSimpleName(), e);
				retryCount--;
			}
		}

		return msg;
	}

	@SuppressWarnings("unchecked")
	private <Msg extends GeneratedMessage> Msg tryQueryAndStore(
			final Class<Msg> cls, final boolean store, final Object... args)
			throws Exception {
		Msg msg = null;

		final String name = cls.getSimpleName();

		final HttpURLConnection http = (HttpURLConnection) this.getURL(
				this.stringResCls.getDeclaredField(name).getInt(null), args)
				.openConnection();
		http.setReadTimeout(20000);
		InputStream input = http.getInputStream();

		if (store) {
			final OutputStream output = this.context.openFileOutput(name,
					Context.MODE_PRIVATE);
			final byte[] buffer = new byte[1024];
			int count = input.read(buffer);
			while (count != -1) {
				output.write(buffer, 0, count);
				count = input.read(buffer);
			}
			input.close();
			output.close();
			input = this.context.openFileInput(name);
		}

		msg = (Msg) cls.getDeclaredMethod("parseFrom", InputStream.class)
				.invoke(null, input);
		input.close();

		return msg;
	}

	private URL getURL(final int resourceId, final Object[] args) {
		this.queryBuilder.setLength(0);
		this.queryBuilder.append(baseUrl);
		this.queryBuilder
				.append(String.format(res.getString(resourceId), args));

		URL url = null;
		try {
			url = new URL(this.queryBuilder.toString());
		} catch (MalformedURLException e) {
			Log.e(TAG, "cannot construct URL", e);
		}

		return url;
	}
}

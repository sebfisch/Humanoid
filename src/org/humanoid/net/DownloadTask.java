package org.humanoid.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import android.os.AsyncTask;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Window;

public class DownloadTask extends AsyncTask<Object, Long, File> {

	private static DownloadTask task;

	private synchronized static DownloadTask ensureInstance(
			final long totalBytes, final int downloadTitleId,
			final boolean unzipInput) {
		if (task == null) {
			task = new DownloadTask(totalBytes, downloadTitleId, unzipInput);
		}

		return task;
	}

	public static void unregister() {
		if (task != null) {
			task.callback = null;
			task.actionBar = null;
		}
	}

	public static void process(final Callback callback,
			final ActionBar actionBar, final File outFile, final String url,
			final long totalBytes, final int downloadTitleId,
			final boolean unzipInput) {

		ensureInstance(totalBytes, downloadTitleId, unzipInput);
		task.register(callback, actionBar);

		switch (task.getStatus()) {
		case FINISHED:
			task.onPostExecute(task.downloadedFile);
			break;
		case RUNNING:
			task.actionBar.setTitle(task.downloadTitleId);
			task.actionBar.setSubtitle("");
			break;
		default:
			if (outFile.exists()) {
				task.onPostExecute(outFile);
			} else {
				task.actionBar.setTitle(task.downloadTitleId);
				task.actionBar.setSubtitle("");
				task.execute(outFile, url);
			}
		}
	}

	public interface Callback {
		void setDownloadProgress(int progress);

		void processDownload(File file);
	}

	private transient final long totalBytes;
	private transient final int downloadTitleId;
	private transient final boolean unzipInput;

	private transient ActionBar actionBar;
	private transient Callback callback;

	private transient File downloadedFile;
	private transient float loadedMB;

	protected transient int bufferSize = 1024 * 1024; // 1 MB

	public DownloadTask(final long totalBytes, final int downloadTitleId,
			final boolean unzipInput) {
		super();

		this.totalBytes = totalBytes;
		this.downloadTitleId = downloadTitleId;
		this.unzipInput = unzipInput;

		this.loadedMB = 0;
	}

	private void register(final Callback callback, final ActionBar actionBar) {
		this.callback = callback;
		this.actionBar = actionBar;
	}

	protected InputStream transformInput(final InputStream input)
			throws IOException {
		final InputStream stream = new BufferedInputStream(input);

		return this.unzipInput ? new GZIPInputStream(stream) : stream;
	}

	@Override
	protected File doInBackground(final Object... params) {
		final File outFile = (File) params[0];
		InputStream input = null;
		FileOutputStream output = null;
		try {
			input = transformInput(new URL((String) params[1]).openStream());
			output = new FileOutputStream(outFile);
			final byte[] buffer = new byte[this.bufferSize]; // 1 MB
			long current = 0;
			this.publishProgress(current);
			int count = input.read(buffer);
			while (count != -1) {
				output.write(buffer, 0, count);
				current += count;
				this.publishProgress(current);
				count = input.read(buffer);
			}
		} catch (IOException e) {
			Log.e("DownloadTask", "doInBackground", e);
		} finally {
			try {
				if (input != null) {
					input.close();
				}
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
			}
		}

		return outFile;
	}

	@Override
	protected void onProgressUpdate(final Long... progress) {
		if (this.callback != null && this.actionBar != null) {
			final long loadedBytes = progress[0];
			final int maxProgress = Window.PROGRESS_END - Window.PROGRESS_START;
			this.callback
					.setDownloadProgress((int) ((maxProgress * loadedBytes) / this.totalBytes));

			final float totalMB = this.asRoundedMB(this.totalBytes);
			final float loaded = this.asRoundedMB(loadedBytes);
			if (loaded > this.loadedMB) {
				this.loadedMB = loaded;
				this.actionBar.setSubtitle(this.loadedMB + "/" + totalMB
						+ " MB");
			}
		}
	}

	private float asRoundedMB(final long bytes) {
		return (float) Math.round((double) (10 * bytes) / (1024 * 1024)) / 10;
	}

	@Override
	protected void onPostExecute(final File file) {
		Log.w("download", "finished");
		this.downloadedFile = file;

		if (this.callback != null) {
			this.callback.processDownload(file);
		}
	}
}

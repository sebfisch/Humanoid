package org.humanoid.view;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.view.View;

public class Visuals implements Visual {

	protected transient final Map<Integer, Visual> visuals = new HashMap<Integer, Visual>();

	@Override
	public void visualize(final View view) {
		for (Entry<Integer, Visual> entry : this.visuals.entrySet()) {
			entry.getValue().visualize(view.findViewById(entry.getKey()));
		}
	}
}

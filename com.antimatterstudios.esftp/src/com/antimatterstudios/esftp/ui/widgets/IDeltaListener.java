package com.antimatterstudios.esftp.ui.widgets;

import com.antimatterstudios.esftp.ui.widgets.DeltaEvent;

public interface IDeltaListener {
	public void add(DeltaEvent event);
	public void remove(DeltaEvent event);
}

package com.suterastudio.android.helpers;

import java.util.Comparator;

import com.suterastudio.drypers.data.Babble;

public class BabbleComparator implements Comparator<Babble> {

	public int compare(Babble a, Babble b) {
		int valA = Integer.parseInt(a.id);
		int valB = Integer.parseInt(b.id);

		if (valA > valB) {
			return -1;
		}
		if (valA < valB) {
			return 1;
		}
		return 0;
	}
}
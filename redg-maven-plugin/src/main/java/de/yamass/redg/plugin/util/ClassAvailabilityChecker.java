package de.yamass.redg.plugin.util;

import java.util.Objects;

public class ClassAvailabilityChecker {
	protected final String fqcn;
	private boolean available=true;

	public ClassAvailabilityChecker(String fqcn) {
		this.fqcn = Objects.requireNonNull(fqcn);
		try{
			Class.forName(fqcn);
		}catch (Exception e) {
			this.available=false;
		}
	}
	
	public boolean isAvailable() {
		return available;
	}
}

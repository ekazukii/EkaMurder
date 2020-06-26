package fr.ekazuki.ekamurder;

public enum MurderRole {
	MURDER("murder"),
	DETECTIVE("detective"),
	INNOCENT("innocent");
	
	public String displayName;
	
	private MurderRole(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
}

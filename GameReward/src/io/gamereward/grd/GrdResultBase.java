package io.gamereward.grd;

public class GrdResultBase {
	public GrdResultBase() {
		
	}
	public GrdResultBase(int error, String message) {
		this.error=error;
		this.message=message;
	}
	public int error;
	public String message;
}

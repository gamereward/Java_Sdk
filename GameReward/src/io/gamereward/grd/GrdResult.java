package io.gamereward.grd;

public class GrdResult<T> extends GrdResultBase{
	public GrdResult() {
	}
	public GrdResult(int error,String message,T data) {
		super(error,message);
		this.data=data;
	}
	public T data;
}

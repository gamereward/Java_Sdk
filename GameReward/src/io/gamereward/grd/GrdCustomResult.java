package io.gamereward.grd;


public class GrdCustomResult extends GrdResult<Object> {
	public GrdCustomResult(int error,String message,Object json) {
		super(error,message,json);
	}
	public <T> T GetObject(Class<T>tClass) {
		return GrdManager.getObject(data, tClass);
	}
}

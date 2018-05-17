package io.gamereward.grd;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GrdManager {
	public static final int EXTERNAL_TRANSTYPE = 3;
	public static final int INTERNAL_TRANSTYPE = 2;
	public static final int BASE_TRANSTYPE = 1;
	public static final int SUCCESS_TRANSSTATUS = 0;
	public static final int PENDING_TRANSSTATUS = 1;
	public static final int ERROR_TRANSSTATUS = 2;
	private static final String MAIN_NET_URL = "https://gamereward.io/appapi/";
	private static final String TEST_NET_URL = "https://test.gamereward.io/appapi/";
	private static String baseUrl = "";

	public GrdManager() {
	}

	private static String api_Id = "";
	private static String api_Secret = "";

	public static void init(String appId, String secret,GrdNet net) {
		api_Id = appId;
		api_Secret = secret;
		if(net==GrdNet.MainNet) {
			baseUrl=MAIN_NET_URL;
		}
		else {
			baseUrl=TEST_NET_URL;
		}
	}

	public static String md5(String s) {
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		m.update(s.getBytes(), 0, s.length());
		return new BigInteger(1, m.digest()).toString(16);
	}

	private static String requestHttp(String action, Map<String, String> params, boolean isGet) {
		if (isGet) {
			return getData(baseUrl + action, params);
		}
		return postData(baseUrl + action, params);
	}

	private static JSONObject getJsonObject(String json) {
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (jsonObject == null) {
			try {
				jsonObject = new JSONObject("{\"error\":500,\"message\":\"Server error\"}");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return jsonObject;
	}

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				is.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private static String getAppToken() {
		long t = System.currentTimeMillis() / 1000L;
		t /= 15L;
		int k = (int) (t % 20L);
		int len = api_Secret.length() / 20;
		String str = api_Secret.substring(k * len, (k + 1) * len);
		str = md5(str + t);
		return str;
	}

	private static String getSendData(Map<String, String> params) {
		String data = "api_id=" + api_Id + "&api_key=" + getAppToken();
		if (params != null) {
			for (String key : params.keySet()) {
				data = data + "&" + key + "=" + params.get(key);
			}
		}
		return data;
	}

	private static HttpsURLConnection getConnection(String urlString) {
		SSLContext context = null;
		try {
			context = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		try {
			context.init(null, null, null);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		HttpsURLConnection urlConnection = null;
		try {
			urlConnection = (HttpsURLConnection) url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		urlConnection.setSSLSocketFactory(context.getSocketFactory());
		return urlConnection;
	}

	private static String getData(String urlString, Map<String, String> params) {
		String data = getSendData(params);
		try {
			HttpsURLConnection urlConnection = getConnection(urlString + "?" + data);
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
			String result = convertStreamToString(inputStream);
			int iPos = result.indexOf("{");
			if (iPos > 0) {
			}
			return result.substring(iPos);
		} catch (Exception localException) {
		}

		return "";
	}

	private static String postData(String urlString, Map<String, String> params) {
		String data = getSendData(params);
		OutputStream out = null;
		try {
			HttpsURLConnection urlConnection = getConnection(urlString);
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			out = new BufferedOutputStream(urlConnection.getOutputStream());
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
			writer.write(data);
			writer.flush();
			writer.close();
			out.close();
			BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
			String result = convertStreamToString(inputStream);
			int iPos = result.indexOf("{");
			if (iPos > 0) {
			}
			return result.substring(iPos);

		} catch (Exception e) {

			return "{\"error\":100,\"message\":\"" + e.getMessage() + "\"}";
		}
	}

	private static String serializeJson(Object o) {
		String result = "";
		if (o == null) {
			result = "null";
		} else if (o.getClass().isPrimitive() || o.getClass().isAssignableFrom(Byte.class)
				|| o.getClass().isAssignableFrom(Integer.class) || o.getClass().isAssignableFrom(Long.class)
				|| o.getClass().isAssignableFrom(Double.class) || o.getClass().isAssignableFrom(Boolean.class)
				|| o.getClass().isAssignableFrom(BigDecimal.class)) {
			result = o.toString();
		} else if (o.getClass().isAssignableFrom(String.class)) {
			result = "\"" + o.toString().replaceAll("\"", "\\\"") + "\"";
		} else if (o.getClass().isArray()) {
			result = "[";
			int len = Array.getLength(o);
			for (int i = 0; i < len; i++) {
				Object element = Array.get(o, i);
				result += serializeJson(element) + ",";
			}
			if (result.length() > 2) {
				result = result.substring(0, result.length() - 1);
			}
			result += "]";
		} else if (o instanceof List<?> || o instanceof ArrayList<?>) {
			Method size = getMethod("size", o.getClass());
			Method get = getMethod("get", o.getClass());
			int len = 0;
			try {
				len = (int) size.invoke(o);
				result = "[";
				for (int i = 0; i < len; i++) {
					try {
						Object element = get.invoke(o, i);
						result += serializeJson(element) + ",";
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
				if (result.length() > 2) {
					result = result.substring(0, result.length() - 1);
				}
				result += "]";

			} catch (IllegalAccessException e) {
				result = "";
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				result = "";
				e.printStackTrace();
			}

		} else if (o instanceof Map<?, ?>) {
			try {
				Method keySet = o.getClass().getMethod("keySet");
				Method get = o.getClass().getMethod("get");
				try {
					Object setOfKeys = keySet.invoke(o);
					Method toArray = getMethod("toArray", setOfKeys.getClass());
					Object array = toArray.invoke(setOfKeys);
					int len = Array.getLength(array);
					result = "{";
					for (int i = 0; i < len; i++) {
						Object key = Array.get(array, i);
						try {
							Object element = get.invoke(o, key);
							result += "\"" + key.toString() + "\":" + serializeJson(element) + ",";
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}
					if (result.length() > 2) {
						result = result.substring(0, result.length() - 1);
					}
					result += "}";

				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (NoSuchMethodException e) {
				result = "";
			}
		} else {
			Field[] fields = o.getClass().getFields();
			result = "{";
			for (Field f : fields) {
				Object value = null;
				try {
					value = f.get(o);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				result += "\"" + f.getName() + "\":" + serializeJson(value);
			}
			result = "}";
		}
		return result;
	}

	private static BigDecimal getBigDecimal(String st) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setGroupingSeparator(',');
		symbols.setDecimalSeparator('.');
		String pattern = "#,##0.0#";
		DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
		decimalFormat.setParseBigDecimal(true);
		try {
			BigDecimal bigDecimal = (BigDecimal) decimalFormat.parse(st);
			return bigDecimal;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new BigDecimal(0);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getObject(Object jObj, Class<T> tClass) {
		T t = null;
		if (tClass.isPrimitive()) {
			if (tClass.isAssignableFrom(byte.class)) {
				if (jObj.getClass() == tClass) {
					t = (T) jObj;
				} else {
					Object v = Byte.parseByte(jObj.toString());
					t = (T) v;
				}
			} else if (tClass.isAssignableFrom(int.class)) {
				if (jObj.getClass() == tClass) {
					t = (T) jObj;
				} else {
					Object v = Integer.parseInt(jObj.toString());
					t = (T) v;
				}
			} else if (tClass.isAssignableFrom(long.class)) {
				if (jObj.getClass() == tClass) {
					t = (T) jObj;
				} else {
					Object v = Long.parseLong(jObj.toString());
					t = (T) v;
				}
			} else if (tClass.isAssignableFrom(double.class)) {
				if (jObj.getClass() == tClass) {
					t = (T) jObj;
				} else {
					Object v = Double.parseDouble(jObj.toString());
					t = (T) v;
				}
			} else if (tClass.isAssignableFrom(boolean.class)) {
				Boolean value = !((jObj.toString() == "0" || jObj.toString() == "False" || jObj.toString() == "FALSE"
						|| jObj.toString() == "false"));
				t = (T) value;
			}
		} else if (tClass.isAssignableFrom(Byte.class)) {
			if (jObj instanceof Byte) {
				t = (T) jObj;
			} else {
				Object v = Byte.parseByte(jObj.toString());
				t = (T) v;
			}
		} else if (tClass.isAssignableFrom(Integer.class)) {
			if (jObj instanceof Integer) {
				t = (T) jObj;
			} else {
				Object v = Integer.parseInt(jObj.toString());
				t = (T) v;
			}
		} else if (tClass.isAssignableFrom(Long.class)) {
			if (jObj instanceof Long) {
				t = (T) jObj;
			} else {
				Object v = Long.parseLong(jObj.toString());
				t = (T) v;
			}
		} else if (tClass.isAssignableFrom(Double.class)) {
			if (jObj instanceof Double) {
				t = (T) jObj;
			} else {
				Object v = Double.parseDouble(jObj.toString());
				t = (T) v;
			}
		} else if (tClass.isAssignableFrom(String.class)) {
			t = (T) jObj;
		} else if (tClass.isAssignableFrom(Boolean.class)) {
			Boolean value = !((jObj.toString() == "0" || jObj.toString() == "False" || jObj.toString() == "FALSE"
					|| jObj.toString() == "false"));
			t = (T) value;
		} else if (tClass.isAssignableFrom(BigDecimal.class)) {
			t = (T) getBigDecimal(jObj.toString());
		} else if (tClass.isArray()) {
			JSONArray jarray = (JSONArray) jObj;
			Class<?> elementType = tClass.getComponentType();
			Object array = Array.newInstance(elementType, jarray.length());
			for (int i = 0; i < jarray.length(); i++) {
				try {
					Array.set(array, i, getObject(jarray.get(i), elementType));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			t = (T) array;
		} else {
			try {
				t = tClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			if (t instanceof List<?> || t instanceof List<?>) {
				Method add = getMethod("add", tClass);
				if (add != null) {
					JSONArray jarray = (JSONArray) jObj;
					// Type elementType= add.getParameterTypes()[0];
					// Class elementClass=(Class) elementType;
					for (int i = 0; i < jarray.length(); i++) {
						try {
							Object element = jarray.get(i);// getObject(jarray.get(i), elementClass);
							try {
								add.invoke(t, element);
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			} else if (t instanceof Dictionary<?, ?> || t instanceof Map<?, ?>) {
				Method add = getMethod("put", tClass);
				if (add != null) {
					JSONObject jsonObject = (JSONObject) jObj;
					Iterator<?> keys = jsonObject.keys();
					while (keys.hasNext()) {
						String key = (String)keys.next();
						try {
							Object element = jsonObject.get(key);
							try {
								add.invoke(t, key, element);
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				JSONObject jsonObject = (JSONObject) jObj;
				Field[] fields = tClass.getFields();
				for (Field f : fields) {
					if (jsonObject.has(f.getName())) {
						try {
							Object fvalue = getObject(jsonObject.get(f.getName()), f.getType());
							try {
								f.set(t, fvalue);
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return t;
	}

	private static Method getMethod(String name, Class<?> tClass) {
		Method[] methods = tClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName() == name) {
				return methods[i];
			}
		}
		return null;
	}

	public static String getAddressQRCode(String address) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("text", "gamereward:" + address);
		String data = requestHttp("qrcode", params, false);
		return data;
	}

	public static GrdResult<GrdAccountInfo> accountbalance(String username) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		String data = requestHttp("accountbalance", params, false);
		JSONObject obj = getJsonObject(data);
		int error = 100;
		String message = "";
		GrdAccountInfo info = null;
		try {
			error = obj.getInt("error");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (error == 0) {
			info = getObject(obj, GrdAccountInfo.class);
		} else {
			try {
				message = obj.getString("message");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return new GrdResult<GrdAccountInfo>(error, message, info);
	}

	public static GrdResultBase chargeMoney(String username, BigDecimal money) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("value", money.toString());
		String data = requestHttp("chargemoney", params, false);
		JSONObject obj = getJsonObject(data);
		int error = 100;
		String message = "";
		try {
			error = obj.getInt("error");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			message = obj.getString("message");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new GrdResultBase(error, message);
	}

	public static GrdResult<GrdTransaction[]> getTransactions(String username, int start, int count) {
		HashMap<String, String> pars = new HashMap<String, String>();
		pars.put("username", username);
		pars.put("start", start+"");
		pars.put("count", count+"");
		String data = requestHttp("transactions", pars, false);
		JSONObject jsonObject = getJsonObject(data);
		int error = 100;
		String message = "";
		GrdTransaction[] transactions = null;
		try {
			error = jsonObject.getInt("error");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (error != 0) {
			try {
				message = jsonObject.getString("message");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				transactions = getObject(jsonObject.get("transactions"), GrdTransaction[].class);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return new GrdResult<GrdTransaction[]>(error, message, transactions);
	}

	public static GrdResult<Integer> getTransactionCount(String username) {
		HashMap<String, String> pars = new HashMap<String, String>();
		pars.put("username", username);
		String data = requestHttp("counttransactions", pars, false);
		JSONObject jsonObject = getJsonObject(data);
		int error = 100;
		String message = "";
		int count = 0;
		try {
			error = jsonObject.getInt("error");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (error != 0) {
			try {
				message = jsonObject.getString("message");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				count = jsonObject.getInt("count");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return new GrdResult<Integer>(error, message, count);
	}

	public static GrdResult<GrdSessionData[]> getUserSessionData(String username, String store, String key, int start, int count) {
    HashMap<String, String> pars = new HashMap<String, String>();
    pars.put("username", username);
    pars.put("store", store);
    pars.put("keys", key);
    pars.put("start", start+"");
    pars.put("count", count+"");
    String data = requestHttp("getusersessiondata", pars, false);
    JSONObject jsonObject = getJsonObject(data);
    int error = 100;
    String message = "";
    GrdSessionData[] list = null;
    try {
      error = jsonObject.getInt("error");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    if (error != 0) {
      try {
        message = jsonObject.getString("message");
      } catch (JSONException e) {
        e.printStackTrace();
      }
    } else {
      try {
        list =getObject(jsonObject.get("data"), GrdSessionData[].class);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return new GrdResult<GrdSessionData[]>(error, message, list);
  }

	public static GrdResult<GrdSessionData[]> getUserSessionData(String username, String store, String[] keys,
			int start, int count) {
		String sKey = "";
		for (int i = 0; i < keys.length; i++) {
			sKey = sKey + "," + keys[i];
		}
		if (sKey.length() > 0) {
			sKey = sKey.substring(1);
		}
		return getUserSessionData(username, store, sKey, start, count);
	}

	public static GrdResultBase saveUserScore(String username, String scoreType, double score) {
		HashMap<String, String> pars = new HashMap<String, String>();
		pars.put("username", username);
		pars.put("scoretype", scoreType);
		pars.put("score", score+"");
		String data = requestHttp("saveuserscore", pars, false);
		JSONObject result = getJsonObject(data);
		int error = 100;
		try {
			error = result.getInt("error");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String message = "";
		if (error != 0) {
			try {
				message = result.getString("message");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return new GrdResultBase(error, message);
	}

	public static GrdResultBase increaseUserScore(String username, String scoreType, double score) {
		HashMap<String, String> pars = new HashMap<String, String>();
		pars.put("username", username);
		pars.put("scoretype", scoreType);
		pars.put("score", score+"");
		String data = requestHttp("increaseuserscore", pars, false);
		JSONObject result = getJsonObject(data);
		int error = 100;
		try {
			error = result.getInt("error");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String message = "";
		if (error != 0) {
			try {
				message = result.getString("message");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return new GrdResultBase(error, message);
	}

	public static GrdResult<GrdLeaderBoard> getUserScoreRank(String username, String scoreType) {
		HashMap<String, String> pars = new HashMap<String, String>();
		pars.put("username", username);
		pars.put("scoretype", scoreType);
		String data = requestHttp("getuserscore", pars, false);
		JSONObject jsonObject = getJsonObject(data);
		int error = 100;
		String message = "";
		GrdLeaderBoard score = null;
		try {
			error = jsonObject.getInt("error");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (error != 0) {
			try {
				message = jsonObject.getString("message");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			score = getObject(jsonObject, GrdLeaderBoard.class);
		}
		return new GrdResult<GrdLeaderBoard>(error, message, score);
	}

	public static GrdResult<GrdLeaderBoard[]> getLeaderBoard(String username, String scoreType, int start, int count) {
		HashMap<String, String> pars = new HashMap<String, String>();
		pars.put("username", username);
		pars.put("scoretype", scoreType);
		pars.put("start", start + "");
		pars.put("count", count + "");
		String data = requestHttp("getleaderboard", pars, false);
		JSONObject jsonObject = getJsonObject(data);
		int error = 100;
		String message = "";
		GrdLeaderBoard[] list = null;
		try {
			error = jsonObject.getInt("error");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (error != 0) {
			try {
				message = jsonObject.getString("message");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				list = getObject(jsonObject.get("leaderboard"), GrdLeaderBoard[].class);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return new GrdResult<GrdLeaderBoard[]>(error, message, list);
	}

	public static GrdCustomResult callServerScript(String username, String scriptName, String functionName,
			Object[] parameters) {
		HashMap<String, String> pars = new HashMap<String, String>();
		String vars = serializeJson(parameters);
		pars.put("username", username);
		pars.put("fn", functionName);
		pars.put("script", scriptName);
		pars.put("vars", vars);
		String data = requestHttp("callserverscript", pars, false);
		JSONObject obj = getJsonObject(data);
		int error = 100;
		try {
			error = obj.getInt("error");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Object rdata = null;
		String message = "";
		if (error != 0) {
			try {
				message = obj.getString("message");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				rdata = obj.get("result");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return new GrdCustomResult(error, message, rdata);
	}

	public static GrdResultBase transfer(String username, String address, BigDecimal money) {
		HashMap<String, String> pars = new HashMap<String, String>();
		pars.put("username", username);
		pars.put("to", address);
		pars.put("value", money.toPlainString());
		String data = requestHttp("transfer", pars, false);
		JSONObject result = getJsonObject(data);
		int error = 100;
		try {
			error = result.getInt("error");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String message = "";
		if (error != 0) {
			try {
				message = result.getString("message");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return new GrdResultBase(error, message);
	}
}

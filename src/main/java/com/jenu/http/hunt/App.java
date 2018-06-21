package com.jenu.http.hunt;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Hello world!
 *
 */
public class App {

	public static final String HOST = "https://http-hunt.thoughtworks-labs.net";

	public static final String IN_URL = "/challenge/input";

	public static final String OUT_URL = "/challenge/output";

	public static final String USER_ID = "HJfdkYGgQ";

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static final void main(String[] args) {

		try {

			final String input = getInput();
			System.out.println(input);

			final String response = findProductPrice(input);
			System.out.println(response);

			final String result = postOutput(response);
			System.out.println(result);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String findProductPrice(String input) {
		String responseString = "";
		try {
			final List<Product> request = convertToBean(input, new TypeReference<List<Product>>() {
			});
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			final Date todayDate = new Date();

			Map<String, Integer> response = new HashMap<>();
			int totalPrice = 0;

			for (Product product : request) {
				Date historyDate = sdf.parse(product.getStartDate());

				if (product.getEndDate() == null) {
					if (!historyDate.after(todayDate)) {
						totalPrice += product.getPrice();
					}
				} else {
					Date futureDate = sdf.parse(product.getEndDate());
					if (!historyDate.after(todayDate) && !futureDate.before(todayDate)) {
						totalPrice += product.getPrice();
					}
				}

			}

			response.put("totalValue", totalPrice);

			responseString = convertToJson(response);
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return responseString;
	}

	public static String findProductByCategory(String input) {
		String responseString = "";
		try {
			final List<Product> request = convertToBean(input, new TypeReference<List<Product>>() {
			});
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			final Date todayDate = new Date();

			Map<String, Integer> response = new HashMap<>();

			for (Product product : request) {
				Date historyDate = sdf.parse(product.getStartDate());

				if (product.getEndDate() == null) {
					if (!historyDate.after(todayDate)) {
						Integer k = response.get(product.getCategory());
						if (k == null) {
							response.put(product.getCategory(), 1);
						} else {
							response.put(product.getCategory(), ++k);
						}

					}
				} else {
					Date futureDate = sdf.parse(product.getEndDate());
					if (!historyDate.after(todayDate) && !futureDate.before(todayDate)) {
						Integer k = response.get(product.getCategory());
						if (k == null) {
							response.put(product.getCategory(), 1);
						} else {
							response.put(product.getCategory(), ++k);
						}

					}
				}

			}

			responseString = convertToJson(response);
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return responseString;
	}

	public static String findProductCount(String input) {
		String responseString = "";
		try {
			final List<Product> request = convertToBean(input, new TypeReference<List<Product>>() {
			});
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			final Date todayDate = new Date();
			int active = 0;

			Map<String, Integer> response = new HashMap<>();

			for (Product product : request) {
				Date historyDate = sdf.parse(product.getStartDate());

				if (product.getEndDate() == null) {
					if (!historyDate.after(todayDate)) {
						active++;
					}
				} else {
					Date futureDate = sdf.parse(product.getEndDate());
					if (!historyDate.after(todayDate) && !futureDate.before(todayDate)) {
						active++;
					}
				}

			}

			response.put("count", active);

			responseString = convertToJson(response);
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return responseString;
	}

	public static String processInput(String input) {

		String responseString = "";
		try {
			List<Object> request = convertToBean(input, new TypeReference<List<Object>>() {
			});

			Map<String, Object> response = new HashMap<>();
			response.put("count", request.size());

			responseString = convertToJson(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return responseString;
	}

	public static String getInput() {
		StringBuilder sb = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpGet httpGetRequest = new HttpGet(HOST + IN_URL);
			httpGetRequest.setHeader("UserID", USER_ID);

			HttpResponse httpResponse = httpClient.execute(httpGetRequest);

			System.out.println("----------------------------------------");
			System.out.println(httpResponse.getStatusLine());
			System.out.println("----------------------------------------");

			HttpEntity entity = httpResponse.getEntity();

			byte[] buffer = new byte[1024];
			if (entity != null) {
				InputStream inputStream = entity.getContent();
				try {
					int bytesRead = 0;
					BufferedInputStream bis = new BufferedInputStream(inputStream);

					while ((bytesRead = bis.read(buffer)) != -1) {
						String chunk = new String(buffer, 0, bytesRead);
						sb.append(chunk);
						System.out.println(chunk);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						inputStream.close();
					} catch (Exception ignore) {
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return sb.toString();
	}

	public static String postOutput(String input) {
		StringBuilder sb = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();

		HttpPost httpPost = new HttpPost(HOST + OUT_URL);
		httpPost.addHeader("UserID", USER_ID);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		try {

			HttpEntity entity = new StringEntity(input);
			httpPost.setEntity(entity);

			HttpResponse response = httpClient.execute(httpPost);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

		return sb.toString();
	}

	public static <T> T convertToBean(String json, TypeReference<T> toValueTypeRef) {
		return OBJECT_MAPPER.convertValue(json, toValueTypeRef);
	}

	public static <T> String convertToJson(T obj) throws IOException {
		return OBJECT_MAPPER.writeValueAsString(obj);
	}
}

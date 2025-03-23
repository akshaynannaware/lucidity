package com.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.controller.OfferRequest;
import com.springboot.controller.SegmentResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CartOfferApplicationTests {

	private static final String BASE_URL = "http://localhost:9001/api/v1";

	@Test
	public void testAddFlatXOfferSingleSegment() throws Exception {
		OfferRequest offerRequest = new OfferRequest(1, "FLATX", 10, Arrays.asList("p1"));
		Assert.assertTrue(addOffer(offerRequest));
	}

	@Test
	public void testAddFlatXPercentOfferSingleSegment() throws Exception {
		OfferRequest offerRequest = new OfferRequest(1, "FLATX%", 10, Arrays.asList("p1"));
		Assert.assertTrue(addOffer(offerRequest));
	}

	@Test
	public void testAddOfferMultipleSegments() throws Exception {
		OfferRequest offerRequest = new OfferRequest(2, "FLATX", 20, Arrays.asList("p1", "p2"));
		Assert.assertTrue(addOffer(offerRequest));
	}

	@Test
	public void testApplyFlatXOffer() throws Exception {
		int finalCartValue = applyOffer(200, 1, 1);
		Assert.assertEquals(190, finalCartValue);
	}

	@Test
	public void testApplyFlatXPercentOffer() throws Exception {
		int finalCartValue = applyOffer(200, 1, 1);
		Assert.assertEquals(180, finalCartValue);
	}

	@Test
	public void testApplyOfferInvalidUser() throws Exception {
		int finalCartValue = applyOffer(200, 999, 1);
		Assert.assertEquals(200, finalCartValue);
	}

	@Test
	public void testApplyOfferInvalidRestaurant() throws Exception {
		int finalCartValue = applyOffer(200, 1, 999);
		Assert.assertEquals(200, finalCartValue);
	}

	@Test
	public void testAddOfferInvalidRestaurantId() throws Exception {
		OfferRequest offerRequest = new OfferRequest(-1, "FLATX", 10, Arrays.asList("p1"));
		Assert.assertFalse(addOffer(offerRequest));
	}

	@Test
	public void testAddOfferInvalidOfferType() throws Exception {
		OfferRequest offerRequest = new OfferRequest(1, "INVALID", 10, Arrays.asList("p1"));
		Assert.assertFalse(addOffer(offerRequest));
	}

	@Test
	public void testApplyOfferNoSegment() throws Exception {
		int finalCartValue = applyOffer(200, 2, 1);
		Assert.assertEquals(200, finalCartValue);
	}

	@Test
	public void testAddOfferZeroValue() throws Exception {
		OfferRequest offerRequest = new OfferRequest(1, "FLATX", 0, Arrays.asList("p1"));
		Assert.assertFalse(addOffer(offerRequest));
	}

	@Test
	public void testAddOfferNegativeValue() throws Exception {
		OfferRequest offerRequest = new OfferRequest(1, "FLATX", -10, Arrays.asList("p1"));
		Assert.assertFalse(addOffer(offerRequest));
	}

	@Test
	public void testApplyOfferZeroCartValue() throws Exception {
		int finalCartValue = applyOffer(0, 1, 1);
		Assert.assertEquals(0, finalCartValue);
	}

	@Test
	public void testApplyOfferLargeCartValue() throws Exception {
		int finalCartValue = applyOffer(10000, 1, 1);
		Assert.assertEquals(9990, finalCartValue);
	}

	@Test
	public void testApplyOfferMultipleSegments() throws Exception {
		int finalCartValue = applyOffer(500, 2, 2);
		Assert.assertTrue(finalCartValue < 500);
	}

	public boolean addOffer(OfferRequest offerRequest) throws Exception {
		String urlString = BASE_URL + "/offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();
		String POST_PARAMS = mapper.writeValueAsString(offerRequest);
		try (OutputStream os = con.getOutputStream()) {
			os.write(POST_PARAMS.getBytes());
			os.flush();
		}

		int responseCode = con.getResponseCode();
		return responseCode == HttpURLConnection.HTTP_OK;
	}

	public int applyOffer(int cartValue, int userId, int restaurantId) throws Exception {
		String urlString = BASE_URL + "/cart/apply_offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();
		String POST_PARAMS = "{\"cart_value\":" + cartValue + ",\"user_id\":" + userId + ",\"restaurant_id\":" + restaurantId + "}";
		try (OutputStream os = con.getOutputStream()) {
			os.write(POST_PARAMS.getBytes());
			os.flush();
		}

		int responseCode = con.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return mapper.readTree(response.toString()).get("cart_value").asInt();
		}
		return cartValue;
	}
}

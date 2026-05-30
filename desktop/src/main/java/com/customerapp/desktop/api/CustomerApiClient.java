package com.customerapp.desktop.api;

import com.customerapp.desktop.model.Customer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerApiClient {
    private static final String BASE_URL = "http://localhost:8080/customers";
    private static final String API_KEY = "secret-api-key-12345";
    private final Gson gson;

    public CustomerApiClient() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, 
                    (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class, 
                    (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> 
                        LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .create();
    }

    public List<Customer> getAllCustomers() throws IOException {
        return getAllCustomers(null, 0, 10);
    }

    public List<Customer> getAllCustomers(String search, int page, int size) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        urlBuilder.append("?page=").append(page);
        urlBuilder.append("&size=").append(size);
        
        if (search != null && !search.trim().isEmpty()) {
            urlBuilder.append("&search=").append(URLEncoder.encode(search.trim(), StandardCharsets.UTF_8));
        }
        
        HttpURLConnection connection = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("X-API-Key", API_KEY);

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            Type responseType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> response = gson.fromJson(reader, responseType);
            reader.close();
            
            List<Customer> customers = gson.fromJson(gson.toJson(response.get("content")), 
                    new TypeToken<List<Customer>>(){}.getType());
            return customers;
        } else {
            throw new IOException("Failed to fetch customers. HTTP code: " + connection.getResponseCode());
        }
    }

    public Map<String, Object> getAllCustomersWithPagination(String search, int page, int size) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        urlBuilder.append("?page=").append(page);
        urlBuilder.append("&size=").append(size);
        
        if (search != null && !search.trim().isEmpty()) {
            urlBuilder.append("&search=").append(URLEncoder.encode(search.trim(), StandardCharsets.UTF_8));
        }
        
        HttpURLConnection connection = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("X-API-Key", API_KEY);

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            Type responseType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> response = gson.fromJson(reader, responseType);
            reader.close();
            
            List<Customer> customers = gson.fromJson(gson.toJson(response.get("content")), 
                    new TypeToken<List<Customer>>(){}.getType());
            
            Map<String, Object> result = new HashMap<>();
            result.put("customers", customers);
            
            // Convert numeric values to integers (Gson returns them as doubles)
            Object currentPageObj = response.get("currentPage");
            Object totalItemsObj = response.get("totalItems");
            Object totalPagesObj = response.get("totalPages");
            Object sizeObj = response.get("size");
            
            result.put("currentPage", currentPageObj instanceof Number ? ((Number) currentPageObj).intValue() : currentPageObj);
            result.put("totalItems", totalItemsObj instanceof Number ? ((Number) totalItemsObj).intValue() : totalItemsObj);
            result.put("totalPages", totalPagesObj instanceof Number ? ((Number) totalPagesObj).intValue() : totalPagesObj);
            result.put("size", sizeObj instanceof Number ? ((Number) sizeObj).intValue() : sizeObj);
            
            return result;
        } else {
            throw new IOException("Failed to fetch customers. HTTP code: " + connection.getResponseCode());
        }
    }

    public Customer getCustomerById(Integer id) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/" + id).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("X-API-Key", API_KEY);

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            Customer customer = gson.fromJson(reader, Customer.class);
            reader.close();
            return customer;
        } else {
            throw new IOException("Failed to fetch customer. HTTP code: " + connection.getResponseCode());
        }
    }

    public Customer createCustomer(Customer customer) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("X-API-Key", API_KEY);
        connection.setDoOutput(true);

        String jsonInput = gson.toJson(customer);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            Customer createdCustomer = gson.fromJson(reader, Customer.class);
            reader.close();
            return createdCustomer;
        } else {
            String errorMessage = readError(connection);
            throw new IOException("Failed to create customer: " + errorMessage);
        }
    }

    public Customer updateCustomer(Integer id, Customer customer) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/" + id).openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("X-API-Key", API_KEY);
        connection.setDoOutput(true);

        String jsonInput = gson.toJson(customer);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            Customer updatedCustomer = gson.fromJson(reader, Customer.class);
            reader.close();
            return updatedCustomer;
        } else {
            String errorMessage = readError(connection);
            throw new IOException("Failed to update customer: " + errorMessage);
        }
    }

    public void deleteCustomer(Integer id) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/" + id).openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("X-API-Key", API_KEY);

        if (connection.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
            String errorMessage = readError(connection);
            throw new IOException("Failed to delete customer: " + errorMessage);
        }
    }

    private String readError(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        StringBuilder error = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            error.append(line);
        }
        reader.close();
        return error.toString();
    }
}

package com.tinesh.filter_servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinesh.client_providers.ElasticSearchClientProvider;
import com.tinesh.client_providers.JacksonObjectMapperProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/index-mappings")
public class IndexMappingsServlet extends HttpServlet {
    private RestClient restLowLevelClient;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        restLowLevelClient = ElasticSearchClientProvider.getRestLowLevelClient();
        objectMapper = JacksonObjectMapperProvider.getObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Read the request body to get index name
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        // Parse request body JSON
        String indexName;
        try {
            Map<String, String> requestBody = objectMapper.readValue(sb.toString(), Map.class);
            indexName = requestBody.get("index_name");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> errorMap = new HashMap<>() ;
            errorMap.put("error", "Invalid request data") ;
            objectMapper.writeValue(resp.getWriter(), errorMap);
            return;
        }

        // Fetch the mappings from Elasticsearch
        try {
            String endpoint = "/" + indexName + "/_mapping";
            Request request = new Request("GET", endpoint);
            Response esResponse = restLowLevelClient.performRequest(request);

            // Parse Elasticsearch response
            String esJsonResponse = EntityUtils.toString(esResponse.getEntity());
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("mappings", objectMapper.readValue(esJsonResponse, Object.class));

            // Return the JSON response
            objectMapper.writeValue(resp.getWriter(), responseMap);

        } catch (Exception e) {
            // Send error response if something goes wrong
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch index mappings: " + e.getMessage());
            objectMapper.writeValue(resp.getWriter(), errorResponse);
        }
    }

    @Override
    public void destroy() {
        // Close Elasticsearch client when the servlet is destroyed
        try {
            if (restLowLevelClient != null) {
                restLowLevelClient.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.destroy();
    }
}
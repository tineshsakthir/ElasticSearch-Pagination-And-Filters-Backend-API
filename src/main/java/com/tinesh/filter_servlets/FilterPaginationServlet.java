package com.tinesh.filter_servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinesh.client_providers.ElasticSearchClientProvider;
import com.tinesh.client_providers.JacksonObjectMapperProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@WebServlet("/filterPagination")
public class FilterPaginationServlet extends HttpServlet {
    private RestHighLevelClient restHighLevelClient;
    private ObjectMapper objectMapper ;
    @Override
    public void init() throws ServletException {
        // Initialize Elasticsearch client
        restHighLevelClient = ElasticSearchClientProvider.getRestHighLevelClient();
        objectMapper = JacksonObjectMapperProvider.getObjectMapper() ;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Step 1: Read JSON from request body
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        }
        String jsonPayload = jsonBuilder.toString();
        System.out.println("Received JSON: " + jsonPayload);

        // Step 2: Convert JSON string to Java Map

        Map<String, Object> requestData = objectMapper.readValue(jsonPayload, Map.class);

        // Step 3: Extract index name from the request data
        String indexName = (String) requestData.get("index_name");
        if (indexName == null || indexName.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"index_name is required\"}");
            return;
        }
        System.out.println("Index Name: " + indexName);

        int rowsPerPageFromClient = 0 ;
        try {
            // Step 4: Extract size from the request data, which is already an integer in json
            rowsPerPageFromClient = (Integer) requestData.get("size");
            System.out.println("RowsPerRequest : " + rowsPerPageFromClient);
        }catch(Exception e){
            System.out.println("Error while parsing size : " );
            e.printStackTrace() ;
        }
        // Step 5: Initialize Elasticsearch BoolQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // Step 6: Conditionally add filters if they are present
        if (requestData.containsKey("filters")) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> filters = (List<Map<String, String>>) requestData.get("filters");

            if (filters != null && !filters.isEmpty()) {
                System.out.println("Parsed Filters: " + filters);

                // Apply each filter conditionally
                for (Map<String, String> filter : filters) {
                    String column = filter.get("column");
                    String condition = filter.get("condition");
                    String value = filter.get("value");

                    // Handle conditions (example for 'contains')
                    if ("contains".equalsIgnoreCase(condition)) {
                        boolQuery.must(QueryBuilders.matchQuery(column, value).fuzziness(Fuzziness.AUTO));
                    }
                    // Extend here for additional conditions if needed
                }
                System.out.println("Constructed BoolQuery with filters: " + boolQuery);
            } else {
                System.out.println("No filters provided.");
            }
        } else {
            System.out.println("No filters key in request.");
        }

        // Step 7: Create SearchSourceBuilder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQuery);
        sourceBuilder.size(rowsPerPageFromClient);

        // Step 8: Prepare and execute Elasticsearch search request
        SearchRequest searchRequest = new SearchRequest(indexName); // Use the dynamic index name
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("Search Response: " + searchResponse);

        // Step 9: Convert Elasticsearch response to JSON-compatible format
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "success");
        responseMap.put("data", searchResponse.getHits().getHits()); // Simplify or transform as needed

        // Step 10: Send response as JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resp.getWriter(), responseMap);
    }
    
    @Override
    public void destroy() {
        try {
            restHighLevelClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

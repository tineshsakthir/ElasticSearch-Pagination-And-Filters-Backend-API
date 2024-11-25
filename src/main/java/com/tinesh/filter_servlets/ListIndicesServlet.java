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
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/list-indices")
public class ListIndicesServlet extends HttpServlet {
    RestHighLevelClient restHighLevelClient ;
    RestClient restLowLevelClient ;
    ObjectMapper objectMapper ;

    @Override
    public void init() throws ServletException {
        restHighLevelClient = ElasticSearchClientProvider.getRestHighLevelClient() ;
        restLowLevelClient = ElasticSearchClientProvider.getRestLowLevelClient() ;
        objectMapper = JacksonObjectMapperProvider.getObjectMapper() ;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            // Use low-level client to send request to "_cat/indices" endpoint
            Request request = new Request("GET", "/_cat/indices?format=json");
            Response response = restLowLevelClient.performRequest(request);

            // Parse the response JSON
            String jsonResponse = EntityUtils.toString(response.getEntity());
            Object indices = objectMapper.readValue(jsonResponse, Object.class);

            // Prepare response data
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("indices", indices);

            // Write the JSON response
            objectMapper.writeValue(resp.getWriter(), responseMap);

        } catch (Exception e) {
            // Handle exception and send error response
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch indices: " + e.getMessage());
            objectMapper.writeValue(resp.getWriter(), errorResponse);
        }
    }

    @Override
    public void destroy() {
        // Close Elasticsearch client when the servlet is destroyed
        try {
            if (restHighLevelClient != null) {
                restHighLevelClient.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.destroy();
    }
}

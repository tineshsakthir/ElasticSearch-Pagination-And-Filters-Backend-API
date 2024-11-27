package com.tinesh.filter_servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinesh.client_providers.ElasticSearchClientProvider;
import com.tinesh.client_providers.JacksonObjectMapperProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/getTopOrBottomFieldValueCounts")
public class GetTopOrBottomFieldValueCountsServlet extends HttpServlet {
    private RestHighLevelClient restHighLevelClient;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        restHighLevelClient = ElasticSearchClientProvider.getRestHighLevelClient();
        objectMapper = JacksonObjectMapperProvider.getObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest userReq, HttpServletResponse userResp) throws ServletException, IOException {
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = userReq.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        }
        String jsonPayload = jsonBuilder.toString();
        System.out.println("Received Request : " + jsonPayload);
        // Parse request body JSON

        String indexName;
        String fieldName ;
        int topResultsCount ;
        String sortOrder ;
        Map<String, String> indexToColumnTypeMap ;
        try {
            Map<String, Object> requestBody = objectMapper.readValue(jsonPayload, Map.class);
            indexName = (String)requestBody.get("index_name");
            fieldName = (String)requestBody.get("field_name") ;
            topResultsCount = Integer.parseInt((String)requestBody.get("top_results_count")) ;
            sortOrder = (String)requestBody.get("sort_order") ;
            indexToColumnTypeMap = (Map<String, String>) requestBody.get("indexToColumnTypeMap");
        } catch (Exception e) {
            userResp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Invalid request data");
            errorMap.put("error Date", e.toString()) ;
            objectMapper.writeValue(userResp.getWriter(), errorMap);
            return;
        }

        System.out.println("Index Name: " + indexName);
        System.out.println("Field Name: " + fieldName);
        System.out.println("Top Results Count: " + topResultsCount);
        System.out.println("Sort Order: " + sortOrder);


        String sortOrderInLowerCase = sortOrder.toLowerCase() ;
        if(indexToColumnTypeMap!= null && indexToColumnTypeMap.get(fieldName).equals("text")){
            fieldName = fieldName + ".keyword" ;
        }

        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();

        String jsonString = "{\n" +
                "  \"size\": 0,\n" +
                "  \"aggs\": {\n" +
                "    \"value_counts\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \""+ fieldName +"\",\n" +
                "        \"size\": "+topResultsCount+",\n" +
                "        \"shard_size\": 10000,\n" +
                "        \"order\": {\n" +
                "          \"_count\": \""+sortOrderInLowerCase+"\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";


        Request topValueCountRequest = new Request("POST", "/"+indexName+"/_search");
        topValueCountRequest.setJsonEntity(jsonString);

        Response topValueCountResponse = restClient.performRequest(topValueCountRequest);

        String responseBody = EntityUtils.toString(topValueCountResponse.getEntity()); // I can use the getEntity() function only once, after that it returns empty string....
        System.out.println("i am printing now, Response body for topValueCountResponse :  " + responseBody);
        Map<String, Object> responseBodyMap = objectMapper.readValue(responseBody, Map.class) ;

        // Step 5: Send the response
        userResp.setContentType("application/json");
        userResp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "success");
        responseMap.put("data", responseBodyMap) ;
        objectMapper.writeValue(userResp.getWriter(), responseMap);

        restClient.close();
    }
//
//
//    public static void createIndexTemplate(HttpServletRequest userReq, HttpServletResponse userResp) throws IOException {
//        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
//
//        String newJsonBodyWithSubfields = "{\n" +
//                "  \"index_patterns\": [\"mailtrace*\"],\n" +
//                "  \"settings\": {\n" +
//                "    \"number_of_shards\": 5,\n" +
//                "    \"number_of_replicas\": 0,\n" +
//                "    \"analysis\": {\n" +
//                "      \"normalizer\": {\n" +
//                "        \"lowercase_normalizer\": {\n" +
//                "          \"type\": \"custom\",\n" +
//                "          \"char_filter\": [],\n" +
//                "          \"filter\": [\"lowercase\"]\n" +
//                "        }\n" +
//                "      },\n" +
//                "      \"analyzer\": {\n" +
//                "        \"lowercase_analyzer\": {\n" +
//                "          \"type\": \"custom\",\n" +
//                "          \"tokenizer\": \"standard\",\n" +
//                "          \"filter\": [\"lowercase\"]\n" +
//                "        }\n" +
//                "      }\n" +
//                "    }\n" +
//                "  },\n" +
//                "  \"mappings\": {\n" +
//                "    \"_doc\": {\n" +
//                "      \"properties\": {\n" +
//                "        \"SENDER\": {\n" +
//                "          \"type\": \"keyword\",\n" +
//                "          \"normalizer\": \"lowercase_normalizer\",\n" +
//                "          \"fields\": {\n" +
//                "            \"text\": {\n" +
//                "              \"type\": \"text\",\n" +
//                "              \"analyzer\": \"lowercase_analyzer\"\n" +
//                "            }\n" +
//                "          }\n" +
//                "        },\n" +
//                "        \"RECIPIENT\": {\n" +
//                "          \"type\": \"keyword\",\n" +
//                "          \"normalizer\": \"lowercase_normalizer\",\n" +
//                "          \"fields\": {\n" +
//                "            \"text\": {\n" +
//                "              \"type\": \"text\",\n" +
//                "              \"analyzer\": \"lowercase_analyzer\"\n" +
//                "            }\n" +
//                "          }\n" +
//                "        },\n" +
//                "        \"RECIPIENT_TYPE\": {\n" +
//                "          \"type\": \"keyword\",\n" +
//                "          \"normalizer\": \"lowercase_normalizer\",\n" +
//                "          \"fields\": {\n" +
//                "            \"text\": {\n" +
//                "              \"type\": \"text\",\n" +
//                "              \"analyzer\": \"lowercase_analyzer\"\n" +
//                "            }\n" +
//                "          }\n" +
//                "        },\n" +
//                "        \"MESSAGE_TRACE_ID\": {\n" +
//                "          \"type\": \"keyword\",\n" +
//                "          \"normalizer\": \"lowercase_normalizer\"\n" +
//                "        },\n" +
//                "        \"SUBJECT\": {\n" +
//                "          \"type\": \"text\",\n" +
//                "          \"analyzer\": \"lowercase_analyzer\",\n" +
//                "          \"fields\": {\n" +
//                "            \"keyword\": {\n" +
//                "              \"type\": \"keyword\",\n" +
//                "              \"normalizer\": \"lowercase_normalizer\"\n" +
//                "            },\n" +
//                "            \"text\": {\n" +
//                "              \"type\": \"text\",\n" +
//                "              \"analyzer\": \"lowercase_analyzer\"\n" +
//                "            }\n" +
//                "          }\n" +
//                "        },\n" +
//                "        \"FROM_IP\": {\n" +
//                "          \"type\": \"keyword\",\n" +
//                "          \"normalizer\": \"lowercase_normalizer\"\n" +
//                "        },\n" +
//                "        \"TO_IP\": {\n" +
//                "          \"type\": \"keyword\",\n" +
//                "          \"normalizer\": \"lowercase_normalizer\"\n" +
//                "        },\n" +
//                "        \"SIZE\": {\n" +
//                "          \"type\": \"long\"\n" +
//                "        },\n" +
//                "        \"RECEIVED\": {\n" +
//                "          \"type\": \"date\"\n" +
//                "        },\n" +
//                "        \"STATUS\": {\n" +
//                "          \"type\": \"keyword\",\n" +
//                "          \"normalizer\": \"lowercase_normalizer\",\n" +
//                "          \"fields\": {\n" +
//                "            \"text\": {\n" +
//                "              \"type\": \"text\",\n" +
//                "              \"analyzer\": \"lowercase_analyzer\"\n" +
//                "            }\n" +
//                "          }\n" +
//                "        },\n" +
//                "        \"OBJECT_ID\": {\n" +
//                "          \"type\": \"keyword\",\n" +
//                "          \"normalizer\": \"lowercase_normalizer\"\n" +
//                "        },\n" +
//                "        \"HASH_ID\": {\n" +
//                "          \"type\": \"keyword\",\n" +
//                "          \"normalizer\": \"lowercase_normalizer\"\n" +
//                "        }\n" +
//                "      }\n" +
//                "    }\n" +
//                "  }\n" +
//                "}";
//
//
//
//        Request myIndexCreationRequest = new Request("PUT", "/_template/mailtrace_template");
//        myIndexCreationRequest.setJsonEntity(newJsonBodyWithSubfields);
//
//        Response myIndexCreationResponse = restClient.performRequest(myIndexCreationRequest);
//
//        System.out.println("Response Code: " + myIndexCreationResponse.getStatusLine().getStatusCode());
//        System.out.println("Response Body: " + EntityUtils.toString(myIndexCreationResponse.getEntity()));
//
//
//        // Get the response code and body
//        int responseCode = myIndexCreationResponse.getStatusLine().getStatusCode();
//        String responseBody = EntityUtils.toString(myIndexCreationResponse.getEntity());
//
//        // Set the response type
//        userResp.setContentType("text/html");
//
//        // Get the writer to write to the response output
//        PrintWriter out = userResp.getWriter();
//
//        // Print the response code and body
//        out.println("<html><body>");
//        out.println("<h1>Response Details</h1>");
//        out.println("<p><strong>Response Code:</strong> " + responseCode + "</p>");
//        out.println("<p><strong>Response Body:</strong></p>");
//        out.println("<pre>" + responseBody + "</pre>");
//        out.println("</body></html>");
//
//        restClient.close();
//    }


//    @Override
//    protected void doGet(HttpServletRequest userReq, HttpServletResponse userResp) throws ServletException, IOException {
//
//    }
}
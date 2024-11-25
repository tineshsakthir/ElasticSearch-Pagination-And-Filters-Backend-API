package com.tinesh.servlet;

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

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/createMailTraceTemplate")
public class TemplateCreatingServlet extends HttpServlet {

    public static void createIndexTemplate(HttpServletRequest userReq, HttpServletResponse userResp) throws IOException {
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
//        String jsonBody = "{\n" +
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
//                "        \"SENDER\": {\"type\": \"keyword\", \"normalizer\": \"lowercase_normalizer\"},\n" +
//                "        \"RECIPIENT\": {\"type\": \"keyword\", \"normalizer\": \"lowercase_normalizer\"},\n" +
//                "        \"MESSAGE_TRACE_ID\": {\"type\": \"keyword\", \"normalizer\": \"lowercase_normalizer\"},\n" +
//                "        \"SUBJECT\": {\"type\": \"text\", \"analyzer\": \"lowercase_analyzer\"},\n" +
//                "        \"FROM_IP\": {\"type\": \"keyword\", \"normalizer\": \"lowercase_normalizer\"},\n" +
//                "        \"TO_IP\": {\"type\": \"keyword\", \"normalizer\": \"lowercase_normalizer\"},\n" +
//                "        \"SIZE\": {\"type\": \"long\"},\n" +  // Added comma here
//                "        \"RECEIVED\": {\"type\": \"date\"},\n" +
//                "        \"STATUS\": {\"type\": \"keyword\", \"normalizer\": \"lowercase_normalizer\"},\n" +
//                "        \"OBJECT_ID\": {\"type\": \"keyword\", \"normalizer\": \"lowercase_normalizer\"},\n" +
//                "        \"HASH_ID\": {\"type\": \"keyword\", \"normalizer\": \"lowercase_normalizer\"}\n" +  // Removed trailing comma here
//                "      }\n" +
//                "    }\n" +
//                "  }\n" +
//                "}";


        String newJsonBodyWithSubfields = "{\n" +
                "  \"index_patterns\": [\"mailtrace*\"],\n" +
                "  \"settings\": {\n" +
                "    \"number_of_shards\": 5,\n" +
                "    \"number_of_replicas\": 0,\n" +
                "    \"analysis\": {\n" +
                "      \"normalizer\": {\n" +
                "        \"lowercase_normalizer\": {\n" +
                "          \"type\": \"custom\",\n" +
                "          \"char_filter\": [],\n" +
                "          \"filter\": [\"lowercase\"]\n" +
                "        }\n" +
                "      },\n" +
                "      \"analyzer\": {\n" +
                "        \"lowercase_analyzer\": {\n" +
                "          \"type\": \"custom\",\n" +
                "          \"tokenizer\": \"standard\",\n" +
                "          \"filter\": [\"lowercase\"]\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"mappings\": {\n" +
                "    \"_doc\": {\n" +
                "      \"properties\": {\n" +
                "        \"SENDER\": {\n" +
                "          \"type\": \"keyword\",\n" +
                "          \"normalizer\": \"lowercase_normalizer\",\n" +
                "          \"fields\": {\n" +
                "            \"text\": {\n" +
                "              \"type\": \"text\",\n" +
                "              \"analyzer\": \"lowercase_analyzer\"\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"RECIPIENT\": {\n" +
                "          \"type\": \"keyword\",\n" +
                "          \"normalizer\": \"lowercase_normalizer\",\n" +
                "          \"fields\": {\n" +
                "            \"text\": {\n" +
                "              \"type\": \"text\",\n" +
                "              \"analyzer\": \"lowercase_analyzer\"\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"MESSAGE_TRACE_ID\": {\n" +
                "          \"type\": \"keyword\",\n" +
                "          \"normalizer\": \"lowercase_normalizer\"\n" +
                "        },\n" +
                "        \"SUBJECT\": {\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"lowercase_analyzer\",\n" +
                "          \"fields\": {\n" +
                "            \"keyword\": {\n" +
                "              \"type\": \"keyword\",\n" +
                "              \"normalizer\": \"lowercase_normalizer\"\n" +
                "            },\n" +
                "            \"text\": {\n" +
                "              \"type\": \"text\",\n" +
                "              \"analyzer\": \"lowercase_analyzer\"\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"FROM_IP\": {\n" +
                "          \"type\": \"keyword\",\n" +
                "          \"normalizer\": \"lowercase_normalizer\"\n" +
                "        },\n" +
                "        \"TO_IP\": {\n" +
                "          \"type\": \"keyword\",\n" +
                "          \"normalizer\": \"lowercase_normalizer\"\n" +
                "        },\n" +
                "        \"SIZE\": {\n" +
                "          \"type\": \"long\"\n" +
                "        },\n" +
                "        \"RECEIVED\": {\n" +
                "          \"type\": \"date\"\n" +
                "        },\n" +
                "        \"STATUS\": {\n" +
                "          \"type\": \"keyword\",\n" +
                "          \"normalizer\": \"lowercase_normalizer\",\n" +
                "          \"fields\": {\n" +
                "            \"text\": {\n" +
                "              \"type\": \"text\",\n" +
                "              \"analyzer\": \"lowercase_analyzer\"\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"OBJECT_ID\": {\n" +
                "          \"type\": \"keyword\",\n" +
                "          \"normalizer\": \"lowercase_normalizer\"\n" +
                "        },\n" +
                "        \"HASH_ID\": {\n" +
                "          \"type\": \"keyword\",\n" +
                "          \"normalizer\": \"lowercase_normalizer\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";



        Request myIndexCreationRequest = new Request("PUT", "/_template/mailtrace_template");
        myIndexCreationRequest.setJsonEntity(newJsonBodyWithSubfields);

        Response myIndexCreationResponse = restClient.performRequest(myIndexCreationRequest);

        System.out.println("Response Code: " + myIndexCreationResponse.getStatusLine().getStatusCode());
        System.out.println("Response Body: " + EntityUtils.toString(myIndexCreationResponse.getEntity()));


        // Get the response code and body
        int responseCode = myIndexCreationResponse.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(myIndexCreationResponse.getEntity());

        // Set the response type
        userResp.setContentType("text/html");

        // Get the writer to write to the response output
        PrintWriter out = userResp.getWriter();

        // Print the response code and body
        out.println("<html><body>");
        out.println("<h1>Response Details</h1>");
        out.println("<p><strong>Response Code:</strong> " + responseCode + "</p>");
        out.println("<p><strong>Response Body:</strong></p>");
        out.println("<pre>" + responseBody + "</pre>");
        out.println("</body></html>");

        restClient.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        createIndexTemplate(req, resp);
    }
}

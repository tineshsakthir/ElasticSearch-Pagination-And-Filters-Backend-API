package com.tinesh.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinesh.entity.MailTrace;
import jakarta.servlet.RequestDispatcher;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@WebServlet("/pagination")
public class PaginationServlet extends HttpServlet {

    private RestHighLevelClient client;

    @Override
    public void init() throws ServletException {
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

    }


    // This index template is created, so don't need to call it again now......
    public static void createIndexTemplate() throws IOException {
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();

        String jsonBody = "{\n" +
                "  \"index_patterns\": [\"mailtrace*\"],\n" +
                "  \"settings\": {\n" +
                "    \"number_of_shards\": 5,\n" +
                "    \"number_of_replicas\": 0\n" +
                "  },\n" +
                "  \"mappings\": {\n" +
                "    \"_doc\": {\n" +
                "      \"properties\": {\n" +
                "        \"HASH_ID\": {\"type\": \"keyword\"},\n" +
                "        \"OBJECT_ID\": {\"type\": \"keyword\"},\n" +
                "        \"MESSAGE_TRACE_ID\": {\"type\": \"keyword\"},\n" +
                "        \"RECEIVED\": {\"type\": \"date\"},\n" +
                "        \"SENDER\": {\"type\": \"keyword\"},\n" +
                "        \"RECIPIENT\": {\"type\": \"keyword\"},\n" +
                "        \"SUBJECT\": {\"type\": \"text\"},\n" +
                "        \"FROM_IP\": {\"type\": \"keyword\"},\n" +
                "        \"TO_IP\": {\"type\": \"keyword\"},\n" +
                "        \"STATUS\": {\"type\": \"keyword\"},\n" +
                "        \"SIZE\": {\"type\": \"long\"}\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Request request = new Request("PUT", "/_template/mailtrace_template");
        request.setJsonEntity(jsonBody);

        Response response = restClient.performRequest(request);

        System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
        System.out.println("Response Body: " + EntityUtils.toString(response.getEntity()));

        restClient.close();
    }

    private final ArrayList<Long> RECEIVED_DATETIME_FOR_PAGINATION = new ArrayList<>() ;


    // To handle the initial request in the browser, which is a get request, others are post request from jsp
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response) ;
    }

    private static final String INDEX_NAME = "mailtrace_index" ;
    private static final String PAGE_NUMBER = "pageNumber" ;
    private static final String RECEIVED ="RECEIVED" ;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Get the array of values for each field from the request
        String[] senders = request.getParameterValues("SENDER[]");
        String[] recipients = request.getParameterValues("RECIPIENT[]");
        String[] messageTraceId = request.getParameterValues("MESSAGE_TRACE_ID[]");
        String[] subjects = request.getParameterValues("SUBJECT[]");
        String[] fromIps = request.getParameterValues("FROM_IP[]") ;
        String[] toIps = request.getParameterValues("TO_IP[]") ;
        String[] sizes = request.getParameterValues("SIZE[]") ;
        String[] sizes_from = request.getParameterValues("SIZE_FROM[]") ;
        String[] sizes_to = request.getParameterValues("SIZE_TO[]") ;
        String[] receiveds = request.getParameterValues("RECEIVED[]") ;
        String[] receiveds_from = request.getParameterValues("RECEIVED_FROM[]") ;
        String[] receiveds_to = request.getParameterValues("RECEIVED_TO[]") ;

        // Process the received arrays (example logging them)
        System.out.println("Senders: " + Arrays.toString(senders));
        System.out.println("Recipients: " + Arrays.toString(recipients));
        System.out.println("Message Trace Id: " + Arrays.toString(messageTraceId));
        System.out.println("Subjects: " + Arrays.toString(subjects));
        System.out.println("From IP : " + Arrays.toString(fromIps));
        System.out.println("To IP : " + Arrays.toString(toIps));
        System.out.println("Sizes: " + Arrays.toString(sizes));
        System.out.println("Sizes From: " + Arrays.toString(sizes_from));
        System.out.println("Sizes To: " + Arrays.toString(sizes_to));
        System.out.println("Received: " + Arrays.toString(receiveds));
        System.out.println("Received From: " + Arrays.toString(receiveds_from));
        System.out.println("Received To: " + Arrays.toString(receiveds_to));



        int pageNumber = Integer.parseInt(request.getParameter(PAGE_NUMBER) != null && Integer.parseInt(request.getParameter("pageNumber"))>0 ? request.getParameter("pageNumber") : "1");

        int pageSize = 100;
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);



        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();


        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder() ;
        boolQueryBuilder.must();


        sourceBuilder.query(QueryBuilders.matchAllQuery());

        sourceBuilder.sort(new FieldSortBuilder(RECEIVED).order(SortOrder.ASC));

        sourceBuilder.size(pageSize);

        if(pageNumber > 1){
            Object[] searchAfterValues = {RECEIVED_DATETIME_FOR_PAGINATION.get(pageNumber-2)};
            sourceBuilder.searchAfter(searchAfterValues);
        }
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        List<String> results = new ArrayList<>();
        SearchHits searchHits = searchResponse.getHits() ;
        for (SearchHit hit : searchHits) {
            results.add(hit.getSourceAsString());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<MailTrace> mailTraces = new ArrayList<>() ;
        for(String jsonString : results){
            MailTrace mailTrace = objectMapper.readValue(jsonString, MailTrace.class);
            mailTraces.add(mailTrace) ;
//            System.out.println(mailTrace.toString());
        }

        if(!mailTraces.isEmpty() && RECEIVED_DATETIME_FOR_PAGINATION.size() < pageNumber ){
            MailTrace lastMessage = mailTraces.get(mailTraces.size() - 1) ;
            String datetimeStr = lastMessage.getRECEIVED();

            Instant instant = Instant.parse(datetimeStr);

            // Convert to ZonedDateTime in UTC timezone
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));

            // Convert to milliseconds since epoch
            long timestampMillis = zonedDateTime.toInstant().toEpochMilli();

            RECEIVED_DATETIME_FOR_PAGINATION.add(timestampMillis) ;
        }

        request.setAttribute("mailTraces",mailTraces);
        request.setAttribute("currentPage", pageNumber);
        request.setAttribute("hasNextPage", searchResponse.getHits().getHits().length == pageSize);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/displayResults.jsp") ;
        dispatcher.forward(request,response);
    }


    @Override
    public void destroy() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

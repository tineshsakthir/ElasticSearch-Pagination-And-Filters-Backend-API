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
import org.elasticsearch.index.query.RangeQueryBuilder;
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

        // Adjusting the dateTime format to feed into the elastic search API
        String[] adjustedReceivedsFrom = adjustDateTimeFormat(receiveds_from);
        String[] adjustedReceivedsTo = adjustDateTimeFormat(receiveds_to);
        String[] adjustedReceiveds = adjustDateTimeFormat(receiveds);

        // Getting the Page Number
        int pageNumber = Integer.parseInt(request.getParameter(PAGE_NUMBER) != null && Integer.parseInt(request.getParameter("pageNumber"))>0 ? request.getParameter("pageNumber") : "1");
        int pageSize = 2000;

        // We need to clear the RECEIVED_DATETIME_FOR_PAGINATION, when the action = filter, because starting from page number 1
        String action = request.getParameter("action") ;
        if(action!= null && action.equals("filter")){
            pageNumber = 1 ;
            RECEIVED_DATETIME_FOR_PAGINATION.clear();
        }

        // Start Building the request query

        // Building  the SearchRequest for elastic search
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = buildElasticsearchQuery(senders, recipients, messageTraceId, subjects, fromIps, toIps, sizes, sizes_from, sizes_to, adjustedReceiveds, adjustedReceivedsFrom, adjustedReceivedsTo) ;
        sourceBuilder.query(boolQueryBuilder) ;

        sourceBuilder.sort(new FieldSortBuilder(RECEIVED).order(SortOrder.ASC));
        sourceBuilder.size(pageSize);

        // Applying search after in the query
        if(pageNumber > 1){
            Object[] searchAfterValues = {RECEIVED_DATETIME_FOR_PAGINATION.get(pageNumber-2)};
            sourceBuilder.searchAfter(searchAfterValues);
        }
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // End Building the request query and got the response

/*The SearchResponse searchResponse syntax will be like below
         *  {
  "took" : 32,
  "timed_out" : false,
  "_shards" : {
    "total" : 5,
    "successful" : 5,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : 1800000,
    "max_score" : null,
    "hits" : [
      {
        "_index" : "mailtrace_index",
        "_type" : "_doc",
        "_id" : "0Y9FPpMBQK-XOA5TRY3L",
        "_score" : null,
        "_source" : {
          "SENDER" : "linda.green@example.org",
          "RECIPIENT" : "bcc:noah.wright@example.net",
          "MESSAGE_TRACE_ID" : "ntQjwHTp55954062",
          "SUBJECT" : "Order Status Update",
          "FROM_IP" : "172.16.2.5",
          "TO_IP" : "10.0.5.3",
          "SIZE" : 3383,
          "RECEIVED" : "2024-10-12T00:10:00Z"
        }, -> hit.getSourceAsString() **** In the below code
        "sort" : [
          1728691800000,
          "0Y9FPpMBQK-XOA5TRY3L"
        ]
      } -> hit (each hit) *** In below code
    ] -> searchHits(Array of hits) *** In below code
  } -> searchResponse.getHits() **** In below code
} -> searchResponse **** In below code
* **/
        List<String> results = new ArrayList<>();
        SearchHits searchHits = searchResponse.getHits() ;
        for (SearchHit hit : searchHits) {
            results.add(hit.getSourceAsString());
        }

        // As per in the syntax, each source string is mapped with the MailTrace class with the help of Jackson Json converter.
        ObjectMapper objectMapper = new ObjectMapper();
        List<MailTrace> mailTraces = new ArrayList<>() ;
        for(String jsonString : results){
            MailTrace mailTrace = objectMapper.readValue(jsonString, MailTrace.class);
            mailTraces.add(mailTrace) ;
        }

        // To go to the previous page, we need to save the last element of the result
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

        // Setting the attributes for transferring to the jsp
        request.setAttribute("mailTraces",mailTraces);
        request.setAttribute("currentPage", pageNumber);
        request.setAttribute("hasNextPage", searchResponse.getHits().getHits().length == pageSize);

        // Dispatch the request, here request and response objects are also forwarded, which helps in persisting filters even after n number of request, response cycle
        RequestDispatcher dispatcher = request.getRequestDispatcher("/displayResults.jsp") ;
        dispatcher.forward(request,response);
    }


    public BoolQueryBuilder buildElasticsearchQuery(
            String[] senders,
            String[] recipients,
            String[] messageTraceId,
            String[] subjects,
            String[] fromIps,
            String[] toIps,
            String[] sizes,
            String[] sizes_from,
            String[] sizes_to,
            String[] adjustedReceiveds,
            String[] adjustedReceivedsFrom,
            String[] adjustedReceivedsTo) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // Must query for SENDER
        if (senders != null && senders.length > 0) {
            BoolQueryBuilder senderQuery = QueryBuilders.boolQuery();
            for (String sender : senders) {
                senderQuery.should(QueryBuilders.matchQuery("SENDER", sender));
            }
            boolQueryBuilder.must(senderQuery);
        }

        // Must query for RECIPIENT
        if (recipients != null && recipients.length > 0) {
            BoolQueryBuilder recipientQuery = QueryBuilders.boolQuery();
            for (String recipient : recipients) {
                recipientQuery.should(QueryBuilders.matchQuery("RECIPIENT", recipient));
            }
            boolQueryBuilder.must(recipientQuery);
        }

        // Must query for MESSAGE_TRACE_ID
        if (messageTraceId != null && messageTraceId.length > 0) {
            BoolQueryBuilder traceIdQuery = QueryBuilders.boolQuery();
            for (String traceId : messageTraceId) {
                traceIdQuery.should(QueryBuilders.matchQuery("MESSAGE_TRACE_ID", traceId));
            }
            boolQueryBuilder.must(traceIdQuery);
        }

        // Must query for SUBJECT
        if (subjects != null && subjects.length > 0) {
            BoolQueryBuilder subjectQuery = QueryBuilders.boolQuery();
            for (String subject : subjects) {
                subjectQuery.should(QueryBuilders.matchQuery("SUBJECT", subject));
            }
            boolQueryBuilder.must(subjectQuery);
        }

        // Must query for FROM_IP
        if (fromIps != null && fromIps.length > 0) {
            BoolQueryBuilder fromIpQuery = QueryBuilders.boolQuery();
            for (String fromIp : fromIps) {
                fromIpQuery.should(QueryBuilders.matchQuery("FROM_IP", fromIp));
            }
            boolQueryBuilder.must(fromIpQuery);
        }

        // Must query for TO_IP
        if (toIps != null && toIps.length > 0) {
            BoolQueryBuilder toIpQuery = QueryBuilders.boolQuery();
            for (String toIp : toIps) {
                toIpQuery.should(QueryBuilders.matchQuery("TO_IP", toIp));
            }
            boolQueryBuilder.must(toIpQuery);
        }

        // Must query for exact SIZE values
        if (sizes != null && sizes.length > 0) {
            BoolQueryBuilder sizeQuery = QueryBuilders.boolQuery();
            for (String size : sizes) {
                sizeQuery.should(QueryBuilders.matchQuery("SIZE", size));
            }
            boolQueryBuilder.must(sizeQuery);
        }

        // Range must query for SIZE (from-to)
        if (sizes_from != null && sizes_to != null && sizes_from.length == sizes_to.length) {
            for (int i = 0; i < sizes_from.length; i++) {
                RangeQueryBuilder sizeRangeQuery = QueryBuilders.rangeQuery("SIZE")
                        .from(sizes_from[i])
                        .to(sizes_to[i]);
                boolQueryBuilder.must(sizeRangeQuery);
            }
        }

        // Range must query for RECEIVED dates (adjustedReceivedsFrom to adjustedReceivedsTo)
        if (adjustedReceivedsFrom != null && adjustedReceivedsTo != null && adjustedReceivedsFrom.length == adjustedReceivedsTo.length) {
            for (int i = 0; i < adjustedReceivedsFrom.length; i++) {
                RangeQueryBuilder receivedRangeQuery = QueryBuilders.rangeQuery("RECEIVED")
                        .from(adjustedReceivedsFrom[i])
                        .to(adjustedReceivedsTo[i]);
                boolQueryBuilder.must(receivedRangeQuery);
            }
        }

        // Must query for specific RECEIVED dates
        if (adjustedReceiveds != null && adjustedReceiveds.length > 0) {
            BoolQueryBuilder receivedDateQuery = QueryBuilders.boolQuery();
            for (String receivedDate : adjustedReceiveds) {
                receivedDateQuery.should(QueryBuilders.matchQuery("RECEIVED", receivedDate));
            }
            boolQueryBuilder.must(receivedDateQuery);
        }

        return boolQueryBuilder;
    }



    public String[] adjustDateTimeFormat(String[] dateTimes) {
        if (dateTimes == null) return null;
        return Arrays.stream(dateTimes)
                .map(dateTime -> dateTime + ":00Z")
                .toArray(String[]::new);
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

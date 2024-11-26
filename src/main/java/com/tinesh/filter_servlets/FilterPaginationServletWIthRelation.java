package com.tinesh.filter_servlets;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinesh.client_providers.ElasticSearchClientProvider;
import com.tinesh.client_providers.JacksonObjectMapperProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@WebServlet("/filterPagination")
public class FilterPaginationServletWIthRelation extends HttpServlet {
    private RestHighLevelClient restHighLevelClient;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        restHighLevelClient = ElasticSearchClientProvider.getRestHighLevelClient();
        objectMapper = JacksonObjectMapperProvider.getObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Step 1: Read and parse JSON request body
            StringBuilder jsonBuilder = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
            }

            String jsonPayload = jsonBuilder.toString();
            System.out.println("Received Request : " + jsonPayload);
            Map<String, Object> requestData = objectMapper.readValue(jsonPayload, Map.class);

            // Step 2: Extract required fields and validate
            String indexName = (String) requestData.get("index_name");
            if (indexName == null || indexName.isEmpty()) {
                sendError(resp, "index_name is required");
                return;
            }

            int size = requestData.containsKey("size") ? (Integer) requestData.get("size") : 10; // Default size to 10
            List<Map<String, String>> filters = (List<Map<String, String>>) requestData.get("filters");
            Map<String, String> indexToColumnTypeMap = (Map<String, String>) requestData.get("indexToColumnTypeMap");
            List<String> relations = (List<String>) requestData.get("relations");

            // Collect sorting info from the client
            String sortColumn = (String) requestData.get("sortColumn");
            String sortOrder = (String) requestData.get("sortOrder");

            // Step 3: Construct the Elasticsearch query


            // Step 4: Build and execute the search request
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                    .size(size);

            if(filters != null){
                if(indexToColumnTypeMap != null){
                    if(relations != null){
                        BoolQueryBuilder boolQueryBuilder = getRootQueryBuilder(filters, indexToColumnTypeMap, relations) ;
                        System.out.println("\n\nBuilt BoolQuery : " + boolQueryBuilder+ "\n\n") ;
                        sourceBuilder.query(boolQueryBuilder) ;
                    }else{
                        System.out.println("Relations is null");
                    }
                }else{
                    System.out.println("Index To Column Type Map is null");
                }
            }else{
                System.out.println("Filters is null");
            }





            if (sortOrder.equalsIgnoreCase("ASC")) {
                if(indexToColumnTypeMap!= null && Objects.equals(indexToColumnTypeMap.get(sortColumn), "text")){
                    sourceBuilder.sort(new FieldSortBuilder(sortColumn+".keyword").order(SortOrder.ASC));
                }else{
                    sourceBuilder.sort(new FieldSortBuilder(sortColumn).order(SortOrder.ASC));
                }
                sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));
            } else {
                if(indexToColumnTypeMap!= null && Objects.equals(indexToColumnTypeMap.get(sortColumn), "text")){
                    sourceBuilder.sort(new FieldSortBuilder(sortColumn+".keyword").order(SortOrder.DESC));
                }else{
                    sourceBuilder.sort(new FieldSortBuilder(sortColumn).order(SortOrder.DESC));
                }
                sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
            }
//
//            Object search_after_object = requestData.get("search_after") ;
//            Object search_after_id_object = requestData.get("search_after_id") ;
//            if (search_after_object != null && search_after_id_object != null) {
//                String search_after_value = (String) search_after_object;
//                String search_after_id_value = (String) search_after_id_object ;
//                if(indexToColumnTypeMap.get(sortColumn).equals("date")){
//                    sourceBuilder.searchAfter(new Object[]{getTimeInMillis(search_after_value), search_after_id_value});
//                }else{
//                    sourceBuilder.searchAfter(new Object[]{search_after_value, search_after_id_value});
//                }
//            }

            Object pageNumber = requestData.get("pageNumber") ;
            if(pageNumber != null){
                sourceBuilder.from((((Integer)pageNumber)-1)*size) ;
            }


            System.out.println("\n\nBuilder Source Builder " + sourceBuilder+"\n\n");

            SearchRequest searchRequest = new SearchRequest(indexName).source(sourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//            System.out.println("Search Response: " + searchResponse);

            long totalHits = searchResponse.getHits().getTotalHits();
            int totalPages = (int) Math.ceil((double) totalHits / size);

            // Step 5: Send the response
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", "success");
            responseMap.put("totalPages", totalPages);
            responseMap.put("totalHits", totalHits);
            responseMap.put("data", Arrays.asList(searchResponse.getHits().getHits()));
            objectMapper.writeValue(resp.getWriter(), responseMap);

        } catch (JsonParseException | JsonMappingException e) {
            sendError(resp, "Invalid JSON format");
            e.printStackTrace();
        } catch (ElasticsearchException e) {
            sendError(resp, "Error interacting with Elasticsearch");
            e.printStackTrace(); // Print stack trace for Elasticsearch errors
        } catch (Exception e) {
            sendError(resp, "Internal server error");
            e.printStackTrace(); // Print stack trace for all other exceptions
        }
    }

    private static final String AND_OPERATOR = "AND" ;
    public static BoolQueryBuilder getRootQueryBuilder(List<Map<String, String>> filters , Map<String, String> indexToColumnTypeMap,  List<String> relations) throws IOException {
        if (filters.isEmpty()) {
            // Return an empty bool query instead of matchAllQuery()
            return QueryBuilders.boolQuery(); // No filters means no conditions
        }

        if (filters.size() - 1 != relations.size()) {
            throw new IllegalArgumentException("Number of relations must be one less than the number of filters");
        }

        BoolQueryBuilder rootQuery = QueryBuilders.boolQuery();

        boolean[] processedFilters = new boolean[filters.size()] ;

        int p1 = 0 ;
        while (p1 < relations.size()) {
            if(relations.get(p1).equals(AND_OPERATOR)){
                int p2 = p1 ;
                processedFilters[p2] = true ; // For initial Filter
                while((p2+1)<relations.size() && relations.get(p2+1).equals(AND_OPERATOR)){
                    processedFilters[p2+1] = true ; // For other filters
                    p2++ ;
                }
                processedFilters[p2+1] = true ; // For last Filter
                QueryBuilder tempQuery = getAndQueryBuilders(filters, indexToColumnTypeMap, p1, p2) ;
                rootQuery.should(tempQuery) ;
                //Increment p1 upto p2
                p1 = p2 ;
            }
            p1++ ;
        }

        for(int i=0; i<filters.size() ; i++){
            if(!processedFilters[i]){
                QueryBuilder tempQuery = getQueryBuilder(filters.get(i), indexToColumnTypeMap) ;
                rootQuery.should(tempQuery) ;
            }
        }
        return rootQuery;
    }

    private static QueryBuilder getAndQueryBuilders(List<Map<String, String>> filters , Map<String, String> indexToColumnTypeMap, int p1, int p2) throws IOException {
        QueryBuilder currentQuery = getQueryBuilder(filters.get(p1), indexToColumnTypeMap) ;

        for (int i = p1; i <= p2; i++) {
            QueryBuilder nextQuery = getQueryBuilder(filters.get(i + 1), indexToColumnTypeMap);
            currentQuery = QueryBuilders.boolQuery()
                        .must(currentQuery) // Add current query as must
                        .must(nextQuery);  // Add the next query as must
        }

        return currentQuery ;
    }

    private  void sendError(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        objectMapper.writeValue(resp.getWriter(), errorResponse);
    }

    private long getTimeInMillis(String datetimeStr){
        Instant instant = Instant.parse(datetimeStr);

        // Convert to ZonedDateTime in UTC timezone
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));

        // Convert to milliseconds since epoch
        long timestampMillis = zonedDateTime.toInstant().toEpochMilli();
        return timestampMillis ;
    }

    private static QueryBuilder getQueryBuilder(Map<String, String> filter, Map<String,String> indexToColumnTypeMap) throws IOException {
        String column = filter.get("column");
        String condition = filter.get("condition");
        String value = filter.get("value");
        if(condition.equals("BETWEEN") || condition.equals("NOT BETWEEN")){
            Object from = filter.get("from") ;
            Object to = filter.get("to") ;
            return buildFilter(column, indexToColumnTypeMap.get(column), condition, from, to) ;
        }
        return buildFilter(column,indexToColumnTypeMap.get(column), condition, value, null ) ;

    }

    public static QueryBuilder buildFilter(String columnInEs, String columnTypeInEs, String condition, Object value, Object value2) {
        switch (columnTypeInEs) {
            case "keyword":
                return buildKeywordFilter(columnInEs, condition, value);
            case "text":
                return buildTextFilter(columnInEs, condition, value);
            case "long":
                return buildNumericFilter(columnInEs, condition, value, value2);
            case "date":
                return buildDateFilter(columnInEs, condition, value, value2);
            default:
                throw new IllegalArgumentException("Unsupported field type: " + columnTypeInEs);
        }
    }


    private static final String ASTERISK = "*" ;
    private static QueryBuilder buildKeywordFilter(String columnInEs, String condition, Object value) {
        switch (condition) {
            case "CONTAINS":
//                return QueryBuilders.matchPhraseQuery(columnInEs+".text", value) ;
                return QueryBuilders.wildcardQuery(columnInEs, ASTERISK + value+ ASTERISK);
            case "EQUALS":
                return QueryBuilders.termQuery(columnInEs, value);
            case "NOT CONTAINS":
//                return QueryBuilders.boolQuery().mustNot(QueryBuilders.matchPhraseQuery(columnInEs+".text", value));
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.wildcardQuery(columnInEs, ASTERISK + value+ ASTERISK));
            case "NOT EQUALS":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(columnInEs, value));
            case "IS EMPTY":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(columnInEs));
            case "IS NOT EMPTY":
                return QueryBuilders.existsQuery(columnInEs);
            case "STARTS WITH":
                return QueryBuilders.prefixQuery(columnInEs, value.toString());
            case "ENDS WITH":
                return QueryBuilders.wildcardQuery(columnInEs, ASTERISK + value);
            case "LIKE":
                return QueryBuilders.wildcardQuery(columnInEs, value.toString() );
            case "NOT LIKE":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.wildcardQuery(columnInEs,value.toString()));
            case "IN":
                return QueryBuilders.termsQuery(columnInEs, Arrays.asList(value.toString().split(",")));
            case "NOT IN":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery(columnInEs, Arrays.asList(value.toString().split(","))));
            default:
                throw new IllegalArgumentException("Unsupported condition for keyword: " + condition);
        }
    }

    private static QueryBuilder buildTextFilter(String columnInEs, String condition, Object value) {
        switch (condition) {
            case "CONTAINS":
//                return QueryBuilders.matchPhraseQuery(columnInEs, value) ;
                return QueryBuilders.wildcardQuery(columnInEs + ".keyword", ASTERISK + value+ ASTERISK);
            case "NOT CONTAINS":
//                return QueryBuilders.boolQuery().mustNot(QueryBuilders.matchQuery(columnInEs, value));
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.wildcardQuery(columnInEs + ".keyword", ASTERISK + value+ ASTERISK));
            case "EQUALS":
                return QueryBuilders.termQuery(columnInEs + ".keyword", value);

            case "NOT EQUALS":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(columnInEs + ".keyword", value));

            case "IS EMPTY":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(columnInEs));

            case "IS NOT EMPTY":
                return QueryBuilders.existsQuery(columnInEs);

            case "STARTS WITH":
                return QueryBuilders.prefixQuery(columnInEs+".keyword", value.toString());

            case "ENDS WITH":
                return QueryBuilders.wildcardQuery(columnInEs+".keyword", ASTERISK + value);

            case "LIKE":
                // Uses wildcard query to match patterns; supports * and ?
                return QueryBuilders.wildcardQuery(columnInEs+".keyword",  value.toString());

            case "NOT LIKE":
                // Negates the wildcard query result
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.wildcardQuery(columnInEs+".keyword", value.toString()));

            case "IN":
                return QueryBuilders.termsQuery(columnInEs+".keyword", Arrays.asList(value.toString().split(",")));

            case "NOT IN":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery(columnInEs+".keyword", Arrays.asList(value.toString().split(","))));

            default:
                throw new IllegalArgumentException("Unsupported condition for text: " + condition);
        }
    }

    private static QueryBuilder buildNumericFilter(String columnInEs, String condition, Object value, Object value2) {
        switch (condition) {
            case "EQUALS":
                return QueryBuilders.termQuery(columnInEs, value);
            case "NOT EQUALS":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(columnInEs, value));
            case "IS EMPTY":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(columnInEs));
            case "IS NOT EMPTY":
                return QueryBuilders.existsQuery(columnInEs);
            case "GREATER THAN":
                return QueryBuilders.rangeQuery(columnInEs).gt(value);
            case "GREATER THAN OR EQUALS":
                return QueryBuilders.rangeQuery(columnInEs).gte(value);
            case "LESS THAN":
                return QueryBuilders.rangeQuery(columnInEs).lt(value);
            case "LESS THAN OR EQUALS":
                return QueryBuilders.rangeQuery(columnInEs).lte(value);
            case "BETWEEN":
                return QueryBuilders.rangeQuery(columnInEs).gte(value).lte(value2);
            case "NOT BETWEEN":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.rangeQuery(columnInEs).gte(value).lte(value2));
            default:
                throw new IllegalArgumentException("Unsupported condition for numeric: " + condition);
        }
    }

    private static QueryBuilder buildDateFilter(String columnInEs, String condition, Object value, Object value2) {
        switch (condition) {
            case "IS":
                return QueryBuilders.termQuery(columnInEs, value);
            case "IS NOT":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(columnInEs, value));
            case "BETWEEN":
                return QueryBuilders.rangeQuery(columnInEs).gte(value).lte(value2);
            case "NOT BETWEEN":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.rangeQuery(columnInEs).gte(value).lte(value2));
            case "AFTER":
                return QueryBuilders.rangeQuery(columnInEs).gt(value);
            case "BEFORE":
                return QueryBuilders.rangeQuery(columnInEs).lt(value);
            default:
                throw new IllegalArgumentException("Unsupported condition for date: " + condition);
        }
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
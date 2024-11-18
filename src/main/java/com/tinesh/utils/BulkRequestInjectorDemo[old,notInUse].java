//package com.tinesh.utils;
//
//import org.apache.http.HttpHost;
//import org.apache.http.util.EntityUtils;
//import org.elasticsearch.client.Request;
//import org.elasticsearch.client.Response;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
//
//import java.io.*;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//
//public class BulkRequestInjectorDemo {
//    private static final String CSV_FILE_PATH =  "F:/ZOHO_EVALUATION_PHASE2/LogFolderGenerateRandomCsvFiles/20241113_121853/messagetrace20241012-0015.csv" ;
//    private static final String INDEX_NAME = "mailtrace_index";
//    private static final String ELASTICSEARCH_URL = "http://localhost:9200"; // Elasticsearch endpoint
//
//    public static void Nulaivayul(RestHighLevelClient client) throws IOException {
//        // Read CSV and prepare bulk request
//        List<String> bulkRequestData = parseCSVFile(CSV_FILE_PATH);
//
//        // Send the bulk request to Elasticsearch
//        bulkIndexData(bulkRequestData, client);
//
//    }
//
//    private static List<String> parseCSVFile(String csvFilePath) throws IOException {
//        List<String> bulkRequestData = new ArrayList<>();
//        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
//            String line;
//            boolean firstLine = true;
//
//            int printerCount = 0 ;
//            // Reading each line of the CSV file
//            while ((line = br.readLine()) != null) {
//                if (firstLine) {
//                    firstLine = false; // Skip header line
//                    continue;
//                }
//                // Split CSV line into fields
//                String[] fields = line.split(",");
//                if (fields.length >= 9) {
//                    String jsonData = String.format("{\"SENDER\": \"%s\", \"RECIPIENT\": \"%s\", \"MESSAGE_TRACE_ID\": \"%s\", \"SUBJECT\": \"%s\", \"FROM_IP\": \"%s\", \"TO_IP\": \"%s\", \"SIZE\": %s, \"STATUS\": \"%s\", \"RECEIVED\": \"%s\"}",
//                            fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7], fields[8]);
//
//                    // Prepare bulk index format (index action with type included)
//                    // Set a generic type, such as "doc", if you don't have a specific type
//                    bulkRequestData.add("{ \"index\": { \"_index\": \"" + INDEX_NAME + "\", \"_type\": \"_doc\" } }");
//                    bulkRequestData.add(jsonData);
//
////                    if(printerCount == 500){
////                        printerCount = 0 ;
////                        System.out.println(jsonData);
////                    }
////                    printerCount++ ;
//                }
//            }
//        }
////        System.out.println(bulkRequestData);
//        return bulkRequestData;
//    }
//
//
//    private static boolean printedOneTime = false ;
//    private static void bulkIndexData(List<String> bulkRequestData, RestHighLevelClient client) throws IOException {
//        StringBuilder bulkRequest = new StringBuilder();
//        int count = 0 ;
//        for (String line : bulkRequestData) {
//            bulkRequest.append(line).append("\n");
//            count++ ;
//
//            if(count == 10 && !printedOneTime){
//                System.out.println(bulkRequest);
//                printedOneTime = true;
//            }
//
//            if(count>=500){
//                count = 0 ;
//                pushBulk(bulkRequest, client);
//                bulkRequest.setLength(0);
//            }
//        }
//    }
//
//
//    private static void pushBulk(StringBuilder bulkRequest, RestHighLevelClient client){
//        // Prepare the bulk API request
//        Request request = new Request("POST", "/_bulk");
//        request.setJsonEntity(bulkRequest.toString());
//
//        try {
//            // Send the bulk request
//            Response response = client.getLowLevelClient().performRequest(request);
//
//            // Check the response status and body
////            System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
//            String responseBody = EntityUtils.toString(response.getEntity());
////            System.out.println("Response Body: " + responseBody);
//
//            // Check if the bulk request failed and display error details
//            if (response.getStatusLine().getStatusCode() != 200) {
//                System.err.println("Error in bulk indexing: " + responseBody);
//            }
//        } catch (IOException e) {
//            System.err.println("Error while sending the bulk request: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//}

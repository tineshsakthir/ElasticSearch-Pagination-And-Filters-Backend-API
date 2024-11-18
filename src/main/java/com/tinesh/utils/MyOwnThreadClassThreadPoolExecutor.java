package com.tinesh.utils;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyOwnThreadClassThreadPoolExecutor implements Runnable{
    StringBuffer sb ;
    boolean isWorkerThreadCreated = false ;
    String CSV_FILE_PATH ;
    private static final String INDEX_NAME = "mailtrace_index";
    RestHighLevelClient client ;
    public MyOwnThreadClassThreadPoolExecutor(String CSV_FILE_PATH, RestHighLevelClient client){

        this.CSV_FILE_PATH = CSV_FILE_PATH ;
        sb = new StringBuffer() ;
        this.client = client ;
    }


    @Override
    public void run() {

        System.out.println(new DateTime() + " " + CSV_FILE_PATH );
        try {
            List<String> bulkRequestData = parseCSVFile(CSV_FILE_PATH);
            bulkIndexData(bulkRequestData, client);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(new DateTime() + " " +Thread.currentThread().getName() + " Completed");
    }

    private static List<String> parseCSVFile(String csvFilePath) throws IOException {
        List<String> bulkRequestData = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstLine = true;

            int printerCount = 0 ;
            // Reading each line of the CSV file
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header line
                    continue;
                }
                // Split CSV line into fields
                String[] fields = line.split(",");
                if (fields.length >= 9) {
                    String jsonData = String.format("{\"SENDER\": \"%s\", \"RECIPIENT\": \"%s\", \"MESSAGE_TRACE_ID\": \"%s\", \"SUBJECT\": \"%s\", \"FROM_IP\": \"%s\", \"TO_IP\": \"%s\", \"SIZE\": %s, \"RECEIVED\": \"%s\"}",
                            fields[0], getFormattedRecipientAddress(fields[1], fields[7]), fields[2], fields[3], fields[4], fields[5], fields[6], fields[8]);

                    // Prepare bulk index format (index action with type included)
                    // Set a generic type, such as "doc", if you don't have a specific type
                    bulkRequestData.add("{ \"index\": { \"_index\": \"" + INDEX_NAME + "\", \"_type\": \"_doc\" } }");
                    bulkRequestData.add(jsonData);
                }
            }
        }
//        System.out.println(bulkRequestData);
        return bulkRequestData;
    }


    /*
    * The result will be like bc:example@gmai.com;to:aakash@zoho.com
    * */
    private static String getFormattedRecipientAddress(String recipients, String recipientTypes){
        StringBuilder formattedString = new StringBuilder() ;
        String[] recipientsArray = recipients.split(";") ;
        String[] recipientTypesArray = recipientTypes.split(";") ;
        for(int i=0 ; i<recipientTypesArray.length ; i++){
            formattedString.append(recipientTypesArray[i]) ;
            formattedString.append(":") ;
            formattedString.append(recipientsArray[i]) ;
            if(i != recipientTypesArray.length-1){
                formattedString.append(';') ;
            }
        }
        return formattedString.toString() ;
    }
    private static void bulkIndexData(List<String> bulkRequestData, RestHighLevelClient client) throws IOException {
        StringBuilder bulkRequest = new StringBuilder();
        int count = 0 ;
        for (String line : bulkRequestData) {
            bulkRequest.append(line).append("\n");
            count++ ;

            // Currently my single bulk request contains 500 documents..

            if(count>=50000){
                count = 0 ;
                pushBulk(bulkRequest, client);
                bulkRequest.setLength(0);
            }
        }
//      Push the remaining data that is in the bulkRequest....
        if(bulkRequest.length() != 0) pushBulk(bulkRequest, client);
    }
    private static void pushBulk(StringBuilder bulkRequest, RestHighLevelClient client){
        Request request = new Request("POST", "/_bulk");
        request.setJsonEntity(bulkRequest.toString());
        try {
            Response response = client.getLowLevelClient().performRequest(request);
//            System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
            String responseBody = EntityUtils.toString(response.getEntity());
//            System.out.println("Response Body: " + responseBody);
            if (response.getStatusLine().getStatusCode() != 200) {
                System.err.println("Error in bulk indexing: " + responseBody);
            }
        } catch (IOException e) {
            System.err.println("Error while sending the bulk request: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

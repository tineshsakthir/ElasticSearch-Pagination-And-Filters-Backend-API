package com.tinesh.utils;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.joda.time.DateTime;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyOwnThreadClassThreadPoolExecutor implements Runnable {
    private static final Object LOCK = new Object();
    private static final String LOG_FILE_PATH = "D:\\LogFolderForCSV\\logForMyThreads.txt";

    StringBuffer sb;
    boolean isWorkerThreadCreated = false;
    String CSV_FILE_PATH;
    private static final String INDEX_NAME = "mailtrace_index";
    RestHighLevelClient client;

    public MyOwnThreadClassThreadPoolExecutor(String CSV_FILE_PATH, RestHighLevelClient client) {
        this.CSV_FILE_PATH = CSV_FILE_PATH;
        sb = new StringBuffer();
        this.client = client;
    }

    @Override
    public void run() {
        logMessage("Starting processing of " + CSV_FILE_PATH);
        try {
            List<String> bulkRequestData = parseCSVFile(CSV_FILE_PATH);
            bulkIndexData(bulkRequestData, client);
        } catch (IOException e) {
            logMessage("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        File csvFile = new File(CSV_FILE_PATH);
        if (!csvFile.delete()) {
            logMessage("File not deleted: " + CSV_FILE_PATH);
        } else {
            logMessage("File processed and deleted successfully: " + CSV_FILE_PATH);
        }

        logMessage("Completed processing by " + Thread.currentThread().getName());
    }

    private static void logMessage(String message) {
        synchronized (LOCK) {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(LOG_FILE_PATH, true)))) {
                out.println(new DateTime() + " " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<String> parseCSVFile(String csvFilePath) throws IOException {
        List<String> bulkRequestData = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header line
                    continue;
                }
                String[] fields = line.split(",");
                if (fields.length >= 9) {
                    String[] splittedRecipientAddresses = getSplittedRecipientAddress(fields[1], fields[7]);

                    for (String splittedRecipientAddress : splittedRecipientAddresses) {
                        String jsonData = String.format(
                                "{\"SENDER\": \"%s\", \"RECIPIENT\": \"%s\", \"MESSAGE_TRACE_ID\": \"%s\", \"SUBJECT\": \"%s\", \"FROM_IP\": \"%s\", \"TO_IP\": \"%s\", \"SIZE\": %s, \"RECEIVED\": \"%s\"}",
                                fields[0], splittedRecipientAddress, fields[2], fields[3], fields[4], fields[5],
                                fields[6], fields[8]);
                        bulkRequestData.add("{ \"index\": { \"_index\": \"" + INDEX_NAME + "\", \"_type\": \"_doc\" } }");
                        bulkRequestData.add(jsonData);
                    }
                }
            }
        }
        return bulkRequestData;
    }

    private static String[] getSplittedRecipientAddress(String recipients, String recipientTypes) {
        StringBuilder temp = new StringBuilder();
        String[] recipientsArray = recipients.split(";");
        String[] recipientTypesArray = recipientTypes.split(";");
        String[] splittedRecipientAddresses = new String[recipientsArray.length];
        for (int i = 0; i < recipientTypesArray.length; i++) {
            temp.append(recipientTypesArray[i]);
            temp.append(":");
            temp.append(recipientsArray[i]);
            splittedRecipientAddresses[i] = temp.toString();
            temp.setLength(0);
        }
        return splittedRecipientAddresses;
    }

    private static void bulkIndexData(List<String> bulkRequestData, RestHighLevelClient client) throws IOException {
        StringBuilder bulkRequest = new StringBuilder();
        int count = 0;
        for (String line : bulkRequestData) {
            bulkRequest.append(line).append("\n");
            count++;

            if (count >= 20000) {
                count = 0;
                pushBulk(bulkRequest, client);
                bulkRequest.setLength(0);
            }
        }
        if (bulkRequest.length() != 0) pushBulk(bulkRequest, client);
    }

    private static void pushBulk(StringBuilder bulkRequest, RestHighLevelClient client) {
        Request request = new Request("POST", "/_bulk");
        request.setJsonEntity(bulkRequest.toString());
        try {
            Response response = client.getLowLevelClient().performRequest(request);
            String responseBody = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() != 200) {
                logMessage("Error in bulk indexing: " + responseBody);
            }
        } catch (IOException e) {
            logMessage("Error while sending the bulk request: " + e.getMessage());
        }
    }
}
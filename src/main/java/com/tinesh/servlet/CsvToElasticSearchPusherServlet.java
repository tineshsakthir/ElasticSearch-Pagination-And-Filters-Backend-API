package com.tinesh.servlet;

import com.tinesh.utils.MyOwnThreadClassThreadPoolExecutor;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

@WebServlet("/pushToEs")
public class CsvToElasticSearchPusherServlet extends HttpServlet {

    private ExecutorService executorService;
    private RestHighLevelClient client;

    @Override
    public void init() throws ServletException {
        executorService = new ThreadPoolExecutor(10, 20, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100000));
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );
    }


    private static final Set<String> processedFiles = Collections.synchronizedSet(new HashSet<>());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String folderPath = "D:/LogFolderForCSV/ServerListeningFolder";
        File folder = new File(folderPath);


        if (folder.exists() && folder.isDirectory()) {
            int notFilesFoundCount = 0;

            while (notFilesFoundCount != 40) {
                File[] files = folder.listFiles();

                if (files != null && files.length != 0) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".csv") && !processedFiles.contains(file.getName())) {
                            notFilesFoundCount = 0;
                            processedFiles.add(file.getName());
                            String fileAbsolutePath = folderPath + "/" + file.getName();
                            MyOwnThreadClassThreadPoolExecutor myOwnThreadClass = new MyOwnThreadClassThreadPoolExecutor(fileAbsolutePath, client);
                            executorService.execute(myOwnThreadClass);
                        }
                    }
                } else {
                    notFilesFoundCount++;
                    System.out.println("No files found in the folder.");
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                }
            }

            System.out.println("No new Files Found, so shutting the service off......");

        } else {
            System.out.println("The folder path is invalid or not a directory.");
        }
    }

    @Override
    public void destroy() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(20, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
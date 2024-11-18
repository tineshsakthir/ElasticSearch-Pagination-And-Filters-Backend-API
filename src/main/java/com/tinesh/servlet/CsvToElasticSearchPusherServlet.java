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
import java.util.concurrent.*;

@WebServlet("/pushToEs")
public class CsvToElasticSearchPusherServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String folderPath = "F:/ZOHO_EVALUATION_PHASE2/LogFolderGenerateRandomCsvFiles/20241116_144417";

        File folder = new File(folderPath);

        StringBuilder csvFileNames = new StringBuilder();

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );

        ExecutorService executorService = new ThreadPoolExecutor(10, 20, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<>(150)) ;


        if (folder.exists() && folder.isDirectory()) {

            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".csv")) {
                        csvFileNames.append(file.getName()).append("\n");
                        String fileAbsolutePath = folderPath + "/" + file.getName() ;
                        MyOwnThreadClassThreadPoolExecutor myOwnThreadClass = new MyOwnThreadClassThreadPoolExecutor(fileAbsolutePath,client ) ;
                        executorService.execute(myOwnThreadClass);
                    }
                }
                System.out.println(csvFileNames);
            } else {
                System.out.println("No files found in the folder.");
            }
        } else {
            System.out.println("The folder path is invalid or not a directory.");
        }

    }
}

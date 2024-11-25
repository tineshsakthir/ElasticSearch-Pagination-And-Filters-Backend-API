package com.tinesh.client_providers;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

public class ElasticSearchClientProvider {
    private static RestHighLevelClient restHighLevelClient;
    public static RestHighLevelClient getRestHighLevelClient() {
        if (restHighLevelClient == null) {
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(new HttpHost("localhost", 9200, "http"))
            );
        }
        return restHighLevelClient;
    }


    public static RestClient getRestLowLevelClient(){
        if (restHighLevelClient == null) {
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(new HttpHost("localhost", 9200, "http"))
            );
        }
        return restHighLevelClient.getLowLevelClient() ;
    }
}

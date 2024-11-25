package com.tinesh.client_providers;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonObjectMapperProvider {
    private static ObjectMapper objectMapper ;

    public static ObjectMapper getObjectMapper(){
        if(objectMapper == null){
            objectMapper = new ObjectMapper() ;
        }
        return objectMapper ;
    }
}

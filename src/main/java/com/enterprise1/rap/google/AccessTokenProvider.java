package com.enterprise1.rap.google;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.oauth2.AccessToken;
import com.google.common.collect.ImmutableList;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class AccessTokenProvider {

    public static String getAccessToken(String credentialsFilePath) throws IOException {
        FileInputStream serviceAccountStream = new FileInputStream(credentialsFilePath);

        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);

        //List<String> scopes = ImmutableList.of("https://www.googleapis.com/auth/cloud-platform");
        List<String> scopes = ImmutableList.of("https://www.googleapis.com/auth/pubsub, https://www.googleapis.com/auth/spanner.admin, https://www.googleapis.com/auth/spanner.data, https://www.googleapis.com/auth/datastore, https://www.googleapis.com/auth/sqlservice.admin, https://www.googleapis.com/auth/devstorage.read_only, https://www.googleapis.com/auth/devstorage.read_write, https://www.googleapis.com/auth/cloudruntimeconfig, https://www.googleapis.com/auth/trace.append, https://www.googleapis.com/auth/cloud-platform, https://www.googleapis.com/auth/cloud-vision, https://www.googleapis.com/auth/bigquery, https://www.googleapis.com/auth/monitoring.write");
        credentials = credentials.createScoped(scopes);

        
        
        
        AccessToken token = credentials.refreshAccessToken();
        
        return token.getTokenValue();
    }
}

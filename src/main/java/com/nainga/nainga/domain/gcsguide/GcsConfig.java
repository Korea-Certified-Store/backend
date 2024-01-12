package com.nainga.nainga.domain.gcsguide;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GcsConfig {

    @Bean
    public Storage storage() {
        String keyFileName = "backend-submodule/kcs-dev-gcs.json";
        Storage storage;
        try {
            InputStream keyFile = ResourceUtils.getURL("classpath:" + keyFileName).openStream();
            storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(keyFile))
                    .build()
                    .getService();
        } catch (IOException e) {
            throw new IllegalArgumentException("GCS 에러");
        }

        return storage;
    }
}

package com.nainga.nainga.domain.gcsguide;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.nainga.nainga.global.exception.GlobalException;
import com.nainga.nainga.global.exception.StoreErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Profile({"dev", "prod"})
public class GcsConfig {

    @Value("${GOOGLE_GCS_JSON_LOCATION}")
    private String keyFileName;

    @Bean
    public Storage storage() throws GlobalException {

        Storage storage;
        try {
            InputStream keyFile = ResourceUtils.getURL("classpath:" + keyFileName).openStream();
            storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(keyFile))
                    .build()
                    .getService();
        } catch (IOException e) {
            throw new GlobalException(StoreErrorCode.GCS_SERVER_ERROR);
        }

        return storage;
    }
}

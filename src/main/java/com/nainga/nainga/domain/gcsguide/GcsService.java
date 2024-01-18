package com.nainga.nainga.domain.gcsguide;


import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Profile({"dev", "prod"})
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GcsService {

    private final Storage storage;

    @Value("${GOOGLE_GCS_BUCKET}")
    private String bucketName;

    @Transactional
    public String uploadImage(byte[] bytes) {

        // 이미지 업로드
        String uuid = UUID.randomUUID().toString(); // Google Cloud Storage에 저장될 파일 이름(중복 이름 안되게 저장하도록 주의)
        String ext = ".jpg"; // 파일의 형식 ex) JPG

        // Cloud에 이미지 업로드
        // 이미지 접근 url : https://storage.googleapis.com/버킷이름/UUID값
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uuid)
                .setContentType(ext)
                .build();
        // Cloud 에 업로드
        Blob blob = storage.create(blobInfo, bytes);

        return "https://storage.googleapis.com/" + bucketName + "/" + uuid;
    }
}

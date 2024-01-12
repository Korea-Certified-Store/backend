package com.nainga.nainga.domain.gcsguide;


import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GcsService {

    @Value("${spring.cloud.gcp.storage.bucket}") // application.yml에 써둔 bucket 이름
    private String bucketName;

    private final Storage storage;

    public String uploadImage(GcsResponse response) {

        // 이미지 업로드
        String uuid = UUID.randomUUID().toString(); // Google Cloud Storage에 저장될 파일 이름(중복 이름 안되게 저장하도록 주의)
        String ext = response.getImage().getContentType(); // 파일의 형식 ex) JPG

        // Cloud에 이미지 업로드
        // 이미지 접근 url : https://storage.googleapis.com/버킷이름/UUID값
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uuid)
            정.setContentType(ext)
                    .build();

            Blob blob = storage.create(blobInfo, response.getImage().getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("GCS 에러");
        }

        return "https://storage.googleapis.com/" + bucketName + "/" + uuid;
    }
}

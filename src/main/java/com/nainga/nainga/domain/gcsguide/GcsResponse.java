package com.nainga.nainga.domain.gcsguide;

import lombok.Data;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

@Profile({"dev", "prod"})
@Data
public class GcsResponse {
    private MultipartFile image;
}

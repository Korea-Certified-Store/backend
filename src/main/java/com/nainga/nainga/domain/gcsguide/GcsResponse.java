package com.nainga.nainga.domain.gcsguide;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class GcsResponse {
    private MultipartFile image;
}

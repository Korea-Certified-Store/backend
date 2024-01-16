package com.nainga.nainga.domain.gcsguide;

import com.nainga.nainga.global.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"dev", "prod"})
@RestController
@RequiredArgsConstructor
public class GcsController {


    private final GcsService gcsService;

    @Tag(name = "GCS")
    @Operation(summary = "GCS 접속", description = "GCS 에 이미지 저장!")
    @PostMapping(value = "api/v1/gcs",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<String> saveImageToGCS(@ModelAttribute GcsResponse response) {
        String url = gcsService.uploadImage(response);
        return new Result<>(Result.CODE_SUCCESS, Result.MESSAGE_OK, url);
    }
}

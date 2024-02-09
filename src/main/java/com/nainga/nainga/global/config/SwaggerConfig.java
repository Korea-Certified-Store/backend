package com.nainga.nainga.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        //Google Cloud Storage API는 별도로 Swagger에 명세
        return new OpenAPI()
                .paths(new Paths().addPathItem("https://storage.googleapis.com/{BUCKET_NAME}/{IMAGE_NAME}",
                        new PathItem().get(new Operation().summary("저장된 가게 이미지 제공")
                                        .description("저장된 가게 이미지를 제공하는 API입니다.<br>" +
                                                "본 API는 Google Cloud Storage에서 제공하는 API로 URL이 위와 같으며 이 정보는 각 가게별 local_photos 필드에 저장되어 있습니다.<br>" +
                                                "Dev 환경에서 BUCKET_NAME은 kcs-dev-bucket1이고 Prod 환경에서 BUCKET_NAME은 kcs-prod-bucket1입니다.<br>" +
                                                "가게 이름은 UUID를 활용한 난수로 제공됩니다.<br>" +
                                                "참고로 Swagger 상에서는 Base URL이 달라 테스트가 불가능합니다.<br>" +
                                                "만약 테스트를 원하신다면 브라우저 상에서 직접 URL을 입력해주시면 됩니다.<br>" +
                                                "예) https://storage.googleapis.com/kcs-dev-bucket1/ad06294c-d4ed-42bd-9839-82af8714bd1e")
                                .tags(List.of("[New] 가게 상세 정보"))
                                .responses(new ApiResponses().addApiResponse("200",
                                        new ApiResponse().description("OK")
                                                .content(new Content().addMediaType("image/jpeg", new MediaType()
                                                        .schema(new Schema<>().type("string")
                                                                .format("binary")))))))));
    }
}


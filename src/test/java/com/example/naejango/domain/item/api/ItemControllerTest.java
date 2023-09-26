package com.example.naejango.domain.item.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.item.application.CategoryService;
import com.example.naejango.domain.item.application.ItemService;
import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.CategoryDto;
import com.example.naejango.domain.item.dto.SearchItemInfoDto;
import com.example.naejango.domain.item.dto.request.CreateItemCommandDto;
import com.example.naejango.domain.item.dto.request.CreateItemRequestDto;
import com.example.naejango.domain.item.dto.request.ModifyItemCommandDto;
import com.example.naejango.domain.item.dto.request.ModifyItemRequestDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.dto.response.FindItemResponseDto;
import com.example.naejango.domain.item.dto.response.MatchResponseDto;
import com.example.naejango.domain.item.dto.response.ModifyItemResponseDto;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.global.common.util.AuthenticationHandler;
import com.example.naejango.global.common.util.GeomUtil;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.Point;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@WebMvcTest(ItemController.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class ItemControllerTest extends RestDocsSupportTest {

    @MockBean
    ItemService itemService;
    @MockBean
    CategoryService categoryService;
    @MockBean
    AuthenticationHandler authenticationHandler;
    @MockBean
    ChannelRepository channelRepositoryMock;
    @MockBean
    GeomUtil geomUtilMock;
    GeomUtil geomUtil = new GeomUtil();


    @Nested
    @Order(1)
    @DisplayName("Controller 아이템 생성")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class createItem {
        Long userId;

        CreateItemRequestDto createItemRequestDto =
                CreateItemRequestDto.builder()
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .itemType(ItemType.INDIVIDUAL_SELL)
                        .category("카테고리")
                        .hashTag(Arrays.asList("태그1", "태그2"))
                        .storageId(1L)
                        .build();

        CreateItemResponseDto createItemResponseDto =
                CreateItemResponseDto.builder()
                        .id(1L)
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .itemType(ItemType.INDIVIDUAL_SELL)
                        .hashTag(Arrays.asList("태그1", "태그2"))
                        .category("카테고리")
                        .build();

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("아이템_생성_성공")
        void 아이템_생성_성공() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(createItemRequestDto);

            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);
            BDDMockito.given(itemService.createItem(any(), any(CreateItemCommandDto.class)))
                    .willReturn(createItemResponseDto);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .post("/api/item")
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isCreated());
            resultActions.andExpect(MockMvcResultMatchers.jsonPath("result.id").isNumber());
            resultActions.andExpect(MockMvcResultMatchers.header().exists("Location"));

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("아이템")
                                    .description("아이템 생성")
                                    .responseHeaders(
                                            headerWithName("Location").description("생성된 아이템 URI")
                                    )
                                    .requestFields(
                                            fieldWithPath("name").description("아이템 이름"),
                                            fieldWithPath("description").description("아이템 설명"),
                                            fieldWithPath("imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("hashTag").description("해쉬 태그"),
                                            fieldWithPath("itemType").description("아이템 타입 (INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY)"),
                                            fieldWithPath("category").description("카테고리"),
                                            fieldWithPath("storageId").description("창고 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("result.id").description("아이템 ID"),
                                            fieldWithPath("result.name").description("아이템 이름"),
                                            fieldWithPath("result.description").description("아이템 설명"),
                                            fieldWithPath("result.imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("result.hashTag").description("해쉬 태그"),
                                            fieldWithPath("result.itemType").description("아이템 타입 (INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY)"),
                                            fieldWithPath("result.category").description("카테고리"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .requestSchema(Schema.schema("아이템 생성 Request"))
                                    .responseSchema(Schema.schema("아이템 생성 Response"))
                                    .build()
                    )));
        }


    }

    @Nested
    @Order(2)
    @DisplayName("Controller 아이템 정보 조회")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class findItem {
        Long itemId=1L;
        FindItemResponseDto findItemResponseDto =
                FindItemResponseDto.builder()
                        .itemId(1L)
                        .storageId(2L)
                        .categoryId(3)
                        .categoryName("카테고리 이름")
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .itemType(ItemType.INDIVIDUAL_SELL)
                        .hashTag(Arrays.asList("태그1", "태그2"))
                        .viewCount(100)
                        .build();

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("아이템_정보_조회_성공")
        void 아이템_정보_조회_성공() throws Exception {
            // given
            BDDMockito.given(itemService.findItem(any()))
                    .willReturn(findItemResponseDto);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/item/{itemId}", itemId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("아이템")
                                    .description("아이템 정보 조회")
                                    .pathParameters(
                                            parameterWithName("itemId").description("아이템 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("result.itemId").description("아이템 id"),
                                            fieldWithPath("result.storageId").description("창고 id"),
                                            fieldWithPath("result.categoryId").description("카테고리 id"),
                                            fieldWithPath("result.categoryName").description("카테고리 이름"),
                                            fieldWithPath("result.name").description("아이템 이름"),
                                            fieldWithPath("result.description").description("아이템 소개"),
                                            fieldWithPath("result.imgUrl").description("이미지 링크"),
                                            fieldWithPath("result.itemType").description("아이템 타입 (INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY)"),
                                            fieldWithPath("result.hashTag").description("해쉬 태그"),
                                            fieldWithPath("result.viewCount").description("조회 수"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("아이템 정보 조회 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(3)
    @DisplayName("모든 카테고리 조회")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class getCategory {
        Category cat1 = new Category(1, "의류");
        Category cat2 = new Category(2, "생필품");
        Category cat3 = new Category(3, "디지털기기");
        List<Category> result = List.of(cat1, cat2, cat3);
        List<CategoryDto> resultDto = result.stream().map(CategoryDto::new).collect(Collectors.toList());

        @Test
        @Tag("api")
        @DisplayName("성공")
        void test1() throws Exception {
            // given
            BDDMockito.given(categoryService.findAllCategory()).willReturn(resultDto);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/item/category")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then

            // restDoc
            resultActions.andDo(restDocs.document(resource(ResourceSnippetParameters.builder()
                    .tag("카테고리")
                    .summary("카테고리 조회")
                    .description("저장되어 있는 카테고리를 전부 조회합니다.")
                    .responseFields(
                            fieldWithPath("message").description("결과 메세지"),
                            fieldWithPath("result[].categoryId").description("카테고리 아이디"),
                            fieldWithPath("result[].categoryName").description("카테고리 이름"))
                    .requestSchema(
                            Schema.schema("카테고리 조회 Response"))
                    .build()))
            );
        }
    }

    @Nested
    @Order(4)
    @DisplayName("아이템 검색")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SearchStorageByConditions {
        Point center = geomUtil.createPoint(127.02, 37.49);
        List<SearchItemInfoDto> searchItemInfoDtoList =
                new ArrayList<>(List.of(
                        new SearchItemInfoDto(1L, "창고1 이름", new Coord(127.03, 37.49), 500, 1L, "아이템1 이름", "아이템1 설명", "이미지 URL", ItemType.INDIVIDUAL_BUY, "카테고리 이름"),
                        new SearchItemInfoDto(2L, "창고2 이름", new Coord(127.01, 37.49), 300, 1L, "아이템2 이름", "아이템2 설명", "이미지 URL", ItemType.INDIVIDUAL_SELL, "카테고리 이름")
                ));

        @Test
        @Tag("api")
        @DisplayName("모든 조건으로 아이템과 창고 검색")
        void 모든_조건으로_아이템과_창고_검색() throws Exception {
            // given
            BDDMockito.given(geomUtilMock.createPoint(127.02, 37.49)).willReturn(center);
            BDDMockito.given(itemService.searchItem(any()))
                    .willReturn(searchItemInfoDtoList);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/item/search")
                    .queryParam("lon", "127.02")
                    .queryParam("lat", "37.49")
                    .queryParam("rad","1000")
                    .queryParam("page", "0")
                    .queryParam("size", "10")
                    .queryParam("category", "의류")
                    .queryParam("keyword", "유니클로 청바지")
                    .queryParam("itemType", "INDIVIDUAL_BUY")
                    .queryParam("status", "true")
                    .characterEncoding(StandardCharsets.UTF_8)
                    .header("Authorization", "엑세스 토큰")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(ResourceSnippetParameters.builder()
                            .tag("아이템")
                            .summary("아이템 검색")
                            .description("조건에 맞는 아이템과 창고 정보를 검색합니다.\n\n" +
                                    "좌표, 반경, 카테고리, 키워드, 타입, 상태를 조건으로 받습니다.\n\n" +
                                    "아이템 정보와 창고 정보를 응답합니다.")
                            .requestParameters(
                                    parameterWithName("lon").description("중심 경도 좌표"),
                                    parameterWithName("lat").description("중심 위도 좌표"),
                                    parameterWithName("rad").description("반경 (1,000~5,000m)"),
                                    parameterWithName("page").description("페이지"),
                                    parameterWithName("size").description("사이즈"),
                                    parameterWithName("category").description("카테고리 이름"),
                                    parameterWithName("keyword").description("검색 키워드(2~10자)"),
                                    parameterWithName("itemType").description("타입 (INDIVIDUAL_BUY/ INDIVIDUAL_SELL/ GROUP_BUY)"),
                                    parameterWithName("status").description("상태 (true 거래중/false 거래완료)"),
                                    parameterWithName("_csrf").ignored()
                            ).responseFields(
                                    fieldWithPath("message").description("조회 결과 메세지"),
                                    fieldWithPath("result[].storageId").description("창고 ID"),
                                    fieldWithPath("result[].storageName").description("창고 이름"),
                                    fieldWithPath("result[].coord").description("창고 좌표"),
                                    fieldWithPath("result[].coord.longitude").description("경도"),
                                    fieldWithPath("result[].coord.latitude").description("위도"),
                                    fieldWithPath("result[].distance").description("거리"),
                                    fieldWithPath("result[].id").description("아이템 ID"),
                                    fieldWithPath("result[].name").description("아이템 이름"),
                                    fieldWithPath("result[].description").description("아이템 설명"),
                                    fieldWithPath("result[].imgUrl").description("아이템 이미지 URL"),
                                    fieldWithPath("result[].itemType").description("아이템 타입 (INDIVIDUAL_BUY/ INDIVIDUAL_SELL/ GROUP_BUY)"),
                                    fieldWithPath("result[].categoryName").description("카테고리 이름")

                            ).requestSchema(
                                    Schema.schema("아이템 검색 Request")
                            ).responseSchema(
                                    Schema.schema("아이템 검색 Response")
                            )
                            .build())
            ));
        }
    }

    @Nested
    @Order(5)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("아이템 매치")
    class MatchItems {
        MatchResponseDto dto1 = MatchResponseDto.builder()
                .itemId(1L)
                .name("매치 결과 아이템1")
                .itemType(ItemType.INDIVIDUAL_BUY)
                .imgUrl("이미지 url")
                .category("카테고리")
                .tag(Arrays.asList("태그1", "태그2"))
                .distance(100)
                .ownerId(1L)
                .build();

        MatchResponseDto dto2 = MatchResponseDto.builder()
                .itemId(2L)
                .name("매치 결과 아이템2")
                .itemType(ItemType.GROUP_BUY)
                .imgUrl("이미지 url")
                .category("카테고리")
                .tag(Arrays.asList("태그1", "태그2"))
                .distance(150)
                .ownerId(2L)
                .build();

        @Test
        @Tag("api")
        @DisplayName("매치 성공")
        void test1() throws Exception {
            // given
            BDDMockito.given(itemService.matchItem(anyInt(), anyInt(), anyLong()))
                    .willReturn(Arrays.asList(dto1, dto2));

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/item/match")
                    .queryParam("rad","1000")
                    .queryParam("size", "10")
                    .queryParam("itemId", "32452")
                    .characterEncoding(StandardCharsets.UTF_8)
                    .header("Authorization", "엑세스 토큰")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(ResourceSnippetParameters.builder()
                            .tag("아이템")
                            .summary("아이템 매치")
                            .description("아이템과 수요가 맞는 근처 아이템을 매칭해줍니다.\n\n" +
                                    "size, radius 및 itemId 를 요청하고\n\n" +
                                    "아이템 정보를 응답합니다.")
                            .requestParameters(
                                    parameterWithName("rad").description("반경 (1,000~5,000m)"),
                                    parameterWithName("size").description("매치 결과물 수 (1~10)"),
                                    parameterWithName("itemId").description("아이템 ID"),
                                    parameterWithName("_csrf").ignored()
                            ).responseFields(
                                    fieldWithPath("message").description("조회 결과 메세지"),
                                    fieldWithPath("result[].itemId").description("아이템 ID"),
                                    fieldWithPath("result[].category").description("카테고리 명"),
                                    fieldWithPath("result[].name").description("아이템 이름"),
                                    fieldWithPath("result[].imgUrl").description("이미지 url"),
                                    fieldWithPath("result[].itemType").description("아이템 타입"),
                                    fieldWithPath("result[].distance").description("아이템 과의 거리"),
                                    fieldWithPath("result[].tag").description("태그 목록"),
                                    fieldWithPath("result[].ownerId").description("아이템 등록한 유저 ID")
                            ).requestSchema(
                                    Schema.schema("아이템 검색 Request")
                            ).responseSchema(
                                    Schema.schema("아이템 검색 Response")
                            )
                            .build())
            ));
        }
    }

    @Nested
    @Order(6)
    @DisplayName("Controller 아이템 정보 수정")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class modifyItem {
        Long userId;
        Long itemId=1L;
        ModifyItemRequestDto modifyItemRequestDto =
                ModifyItemRequestDto.builder()
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .category(1)
                        .build();

        ModifyItemResponseDto modifyItemResponseDto =
                ModifyItemResponseDto.builder()
                        .id(1L)
                        .name("아이템 이름")
                        .description("아이템 설명")
                        .imgUrl("이미지 URL")
                        .itemType(ItemType.INDIVIDUAL_SELL)
                        .category("카테고리")
                        .build();

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("아이템_정보_수정_성공")
        void 아이템_정보_수정_성공() throws Exception {
            // given
            String content = objectMapper.writeValueAsString(modifyItemRequestDto);
            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);
            BDDMockito.given(itemService.modifyItem(any(), any(), any(ModifyItemCommandDto.class)))
                    .willReturn(modifyItemResponseDto);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .patch("/api/item/{itemId}", itemId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("아이템")
                                    .description("아이템 정보 수정")
                                    .pathParameters(
                                            parameterWithName("itemId").description("아이템 ID")
                                    )
                                    .requestFields(
                                            fieldWithPath("name").description("아이템 이름"),
                                            fieldWithPath("description").description("아이템 설명"),
                                            fieldWithPath("imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("category").description("카테고리")
                                    )
                                    .responseFields(
                                            fieldWithPath("result.id").description("아이템 id"),
                                            fieldWithPath("result.name").description("아이템 이름"),
                                            fieldWithPath("result.description").description("아이템 설명"),
                                            fieldWithPath("result.imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("result.itemType").description("아이템 타입 (INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY)"),
                                            fieldWithPath("result.category").description("카테고리"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .requestSchema(Schema.schema("아이템 정보 수정 Request"))
                                    .responseSchema(Schema.schema("아이템 정보 수정 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Order(7)
    @DisplayName("Controller 아이템 삭제")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteItem {
        Long userId;
        Long itemId=1L;

        @Test
        @Order(1)
        @Tag("api")
        @DisplayName("아이템_삭제_성공")
        void 아이템_삭제_성공() throws Exception {
            // given
            BDDMockito.given(authenticationHandler.getUserId(any()))
                    .willReturn(userId);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .delete("/api/item/{itemId}", itemId)
                    .header("Authorization", "JWT")
                    .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            resultActions.andDo(restDocs.document(
                    resource(
                            ResourceSnippetParameters.builder()
                                    .tag("아이템")
                                    .summary("아이템 삭제")
                                    .description("아이템을 등록한 유저만 삭제 가능\n\n" +
                                            "아이템을 삭제 하면 실행되는 로직\n\n" +
                                            "- 해당 아이템에 연관된 Wish 삭제\n\n" +
                                            "- 연관된 Transaction 과의 관계 끊어짐\n\n" +
                                            "- 그룹 채널이 생성 되어 있다면 종료\n\n" +
                                            "- 이후 아이템 삭제")
                                    .pathParameters(
                                            parameterWithName("itemId").description("아이템 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("result").description("null"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("아이템 삭제 Response"))
                                    .build()
                    )));
        }
    }
}
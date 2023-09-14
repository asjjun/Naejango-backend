package com.example.naejango.domain.item.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.chat.domain.ChannelType;
import com.example.naejango.domain.chat.domain.GroupChannel;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.item.application.ItemService;
import com.example.naejango.domain.item.domain.Item;
import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.item.dto.SearchItemInfoDto;
import com.example.naejango.domain.item.dto.request.CreateItemCommandDto;
import com.example.naejango.domain.item.dto.request.CreateItemRequestDto;
import com.example.naejango.domain.item.dto.request.ModifyItemCommandDto;
import com.example.naejango.domain.item.dto.request.ModifyItemRequestDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.dto.response.FindItemResponseDto;
import com.example.naejango.domain.item.dto.response.ModifyItemResponseDto;
import com.example.naejango.domain.storage.domain.Storage;
import com.example.naejango.domain.storage.dto.Coord;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
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
import java.util.List;
import java.util.Optional;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class ItemControllerTest extends RestDocsSupportTest {

    @MockBean
    ItemService itemService;
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
                        .storageId(1L)
                        .build();

        CreateItemResponseDto createItemResponseDto =
                CreateItemResponseDto.builder()
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
                                            fieldWithPath("itemType").description("아이템 타입 (INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY)"),
                                            fieldWithPath("category").description("카테고리"),
                                            fieldWithPath("storageId").description("창고 ID")
                                    )
                                    .responseFields(
                                            fieldWithPath("result.id").description("아이템 ID"),
                                            fieldWithPath("result.name").description("아이템 이름"),
                                            fieldWithPath("result.description").description("아이템 설명"),
                                            fieldWithPath("result.imgUrl").description("아이템 이미지 Url"),
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
                                            fieldWithPath("result.id").description("아이템 id"),
                                            fieldWithPath("result.id").description("아이템 id"),
                                            fieldWithPath("result.name").description("아이템 이름"),
                                            fieldWithPath("result.description").description("아이템 설명"),
                                            fieldWithPath("result.imgUrl").description("아이템 이미지 Url"),
                                            fieldWithPath("result.itemType").description("아이템 타입 (INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY)"),
                                            fieldWithPath("result.category").description("카테고리"),
                                            fieldWithPath("message").description("결과 메시지")
                                    )
                                    .responseSchema(Schema.schema("아이템 정보 조회 Response"))
                                    .build()
                    )));
        }
    }

    @Nested
    @Tag("api")
    @DisplayName("아이템&창고 검색")
    class SearchStorageByConditions {
        Point center = geomUtil.createPoint(127.02, 37.49);
        List<SearchItemInfoDto> searchItemInfoDtoList =
                new ArrayList<>(List.of(
                        new SearchItemInfoDto(1L, "창고1 이름", new Coord(127.03, 37.49), 500, 1L, "아이템1 이름", "아이템1 설명", "이미지 URL", ItemType.INDIVIDUAL_BUY, "카테고리 이름"),
                        new SearchItemInfoDto(2L, "창고2 이름", new Coord(127.01, 37.49), 300, 1L, "아이템2 이름", "아이템2 설명", "이미지 URL", ItemType.INDIVIDUAL_SELL, "카테고리 이름")
                ));

        @Test
        @DisplayName("모든 조건으로 아이템과 창고 검색")
        void 모든_조건으로_아이템과_창고_검색() throws Exception {
            // given
            BDDMockito.given(geomUtilMock.createPoint(127.02, 37.49)).willReturn(center);
            BDDMockito.given(itemService.searchItem(any(), any(Integer.class), any(Integer.class), any(Integer.class), any()))
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
    @Order(3)
    @DisplayName("Controller 공동 구매 아이템의 그룹 채널 조회")
    @WithMockUser()
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class findGroupChannel {
        User user = User.builder().id(1L).role(Role.USER).userKey("test_1").password("").build();

        Storage storage = Storage.builder()
                .id(2L)
                .name("테스트 창고1")
                .location(geomUtil.createPoint(127.0371, 37.4951))
                .address("서울시 강남구")
                .build();

        Item item = Item.builder()
                .id(3L)
                .itemType(ItemType.INDIVIDUAL_BUY)
                .name("테스트 아이템2")
                .description("")
                .status(true)
                .imgUrl("")
                .viewCount(0)
                .storage(storage)
                .build();

        GroupChannel channel = GroupChannel.builder()
                .id(4L)
                .channelType(ChannelType.GROUP)
                .item(item)
                .participantsCount(3)
                .channelLimit(5)
                .owner(user)
                .defaultTitle("그룹채널 1")
                .build();

        @Test
        @Tag("api")
        @DisplayName("공동 구매 아이템의 그룹 채널 조회")
        void test1() throws Exception {
            // given
            BDDMockito.given(channelRepositoryMock.findGroupChannelByItemId(item.getId()))
                    .willReturn(Optional.of(channel));

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/item/{itemId}/channel", item.getId())
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(status().isOk());
            resultActions.andExpect(jsonPath("message").value("해당 창고의 그룹 채널 정보가 조회되었습니다."));

            // restDocs
            resultActions.andDo(restDocs.document(
                    resource(ResourceSnippetParameters.builder()
                            .tag("창고")
                            .summary("창고 그룹 채널 조회")
                            .pathParameters(
                                    parameterWithName("itemId").description("공동 구매 아이템 id")
                            ).responseFields(
                                    fieldWithPath("message").description("조회 결과 메세지"),
                                    fieldWithPath("channelInfo").description("조회된 채널"),
                                    fieldWithPath("channelInfo.channelId").description("채널 id"),
                                    fieldWithPath("channelInfo.ownerId").description("채널장 id"),
                                    fieldWithPath("channelInfo.itemId").description("공동구매 아이템 id"),
                                    fieldWithPath("channelInfo.participantsCount").description("채널 참가자 수"),
                                    fieldWithPath("channelInfo.defaultTitle").description("기본 설정된 방제목"),
                                    fieldWithPath("channelInfo.channelLimit").description("채널 최대 참여자 수")
                            ).responseSchema(
                                    Schema.schema("창고 채널 조회 Response")
                            )
                            .build())
            ));
        }
    }


    @Nested
    @Order(4)
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
                        .itemType(ItemType.INDIVIDUAL_SELL)
                        .category("카테고리")
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
                                            fieldWithPath("itemType").description("아이템 타입 (INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY)"),
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

}
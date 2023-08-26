package com.example.naejango.domain.chat.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SendMessageResponseDto {

    private Long senderId;
    private Long channelId;
    private String content;

}

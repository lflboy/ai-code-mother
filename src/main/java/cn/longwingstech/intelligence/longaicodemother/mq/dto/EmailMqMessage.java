package cn.longwingstech.intelligence.longaicodemother.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailMqMessage {
    private String email;
    private String title;
    private String text;
}

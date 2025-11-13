package crudjava.crudjava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationDto {

    private String recipientEmail;
    private String subject;
    private String message;
    private String eventType;
}

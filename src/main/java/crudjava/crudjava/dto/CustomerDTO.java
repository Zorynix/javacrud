package crudjava.crudjava.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String customerType;
    private List<AddressDTO> addresses;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

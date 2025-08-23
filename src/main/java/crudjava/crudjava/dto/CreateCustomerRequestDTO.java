package crudjava.crudjava.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String customerType;
}
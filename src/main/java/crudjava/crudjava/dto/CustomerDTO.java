package crudjava.crudjava.dto;

import java.time.LocalDateTime;
import java.util.List;

import crudjava.crudjava.model.Customer;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

    public CustomerDTO(Customer customer) {
        this.id = customer.getId();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.email = customer.getEmail();
        this.phone = customer.getPhone();
        this.customerType = customer.getCustomerType();
        this.version = customer.getVersion();
        this.createdAt = customer.getCreatedAt();
        this.updatedAt = customer.getUpdatedAt();
        
        if (customer.getAddresses() != null) {
            this.addresses = customer.getAddresses().stream()
                .map(AddressDTO::new)
                .toList();
        }
    }
}

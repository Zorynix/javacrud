package crudjava.crudjava.dto;

import java.time.LocalDateTime;
import java.util.List;

import crudjava.crudjava.model.Customer;

public record CustomerDTO(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phone,
    String customerType,
    List<AddressDTO> addresses,
    Long version,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public CustomerDTO(Customer customer) {
        this(
            customer.getId(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getCustomerType(),
            customer.getAddresses() != null 
                ? customer.getAddresses().stream().map(AddressDTO::new).toList()
                : null,
            customer.getVersion(),
            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }
}

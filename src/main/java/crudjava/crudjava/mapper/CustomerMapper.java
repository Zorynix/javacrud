package crudjava.crudjava.mapper;

import crudjava.crudjava.dto.CustomerDTO;
import crudjava.crudjava.model.Customer;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CustomerMapper {

    public static CustomerDTO toDTO(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerDTO.builder()
            .id(customer.getId())
            .firstName(customer.getFirstName())
            .lastName(customer.getLastName())
            .email(customer.getEmail())
            .phone(customer.getPhone())
            .customerType(customer.getCustomerType())
            .addresses(
                customer.getAddresses() != null
                    ? customer
                          .getAddresses()
                          .stream()
                          .map(AddressMapper::toDTO)
                          .collect(Collectors.toList())
                    : null
            )
            .version(customer.getVersion())
            .createdAt(customer.getCreatedAt())
            .updatedAt(customer.getUpdatedAt())
            .build();
    }
}

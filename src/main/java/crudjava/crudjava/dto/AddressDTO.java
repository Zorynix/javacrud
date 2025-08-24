package crudjava.crudjava.dto;

import crudjava.crudjava.model.Address;

public record AddressDTO(
    Long id,
    String street,
    String city,
    String country,
    String postalCode,
    String addressType
) {
    public AddressDTO(Address address) {
        this(
            address.getId(),
            address.getStreet(),
            address.getCity(),
            address.getCountry(),
            address.getPostalCode(),
            address.getAddressType()
        );
    }
}

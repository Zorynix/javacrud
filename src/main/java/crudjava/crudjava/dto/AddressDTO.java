package crudjava.crudjava.dto;

import crudjava.crudjava.model.Address;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddressDTO {
    private Long id;
    private String street;
    private String city;
    private String country;
    private String postalCode;
    private String addressType;

    public AddressDTO(Address address) {
        this.id = address.getId();
        this.street = address.getStreet();
        this.city = address.getCity();
        this.country = address.getCountry();
        this.postalCode = address.getPostalCode();
        this.addressType = address.getAddressType();
    }
}

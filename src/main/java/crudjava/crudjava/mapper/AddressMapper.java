package crudjava.crudjava.mapper;

import crudjava.crudjava.dto.AddressDTO;
import crudjava.crudjava.model.Address;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AddressMapper {

    public static AddressDTO toDTO(Address address) {
        if (address == null) {
            return null;
        }

        return AddressDTO.builder()
            .id(address.getId())
            .street(address.getStreet())
            .city(address.getCity())
            .country(address.getCountry())
            .postalCode(address.getPostalCode())
            .addressType(address.getAddressType())
            .build();
    }
}

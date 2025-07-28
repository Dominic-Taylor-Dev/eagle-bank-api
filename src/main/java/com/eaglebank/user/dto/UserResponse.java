package com.eaglebank.user.dto;



import com.eaglebank.user.User;

import java.time.Instant;

public record UserResponse(
        String id,
        String name,
        AddressDto address,
        String phoneNumber,
        String email,
        Instant createdTimestamp,
        Instant updatedTimestamp
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                new AddressDto(
                        user.getAddressLine1(),
                        user.getAddressLine2(),
                        user.getAddressLine3(),
                        user.getTown(),
                        user.getCounty(),
                        user.getPostcode()
                ),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getCreatedTimestamp(),
                user.getUpdatedTimestamp()
        );
    }
}

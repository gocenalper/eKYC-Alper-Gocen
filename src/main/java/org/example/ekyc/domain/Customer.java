package org.example.ekyc.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

public record Customer(
        @JsonProperty("customer_id") String customerId,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("date_of_birth") LocalDate dateOfBirth,
        String email,
        String phone,
        String address,
        @JsonIgnore String nationality
) {
    public Customer(
            String customerId,
            String fullName,
            LocalDate dateOfBirth,
            String email,
            String phone,
            String address
    ) {
        this(customerId, fullName, dateOfBirth, email, phone, address, "US");
    }
}

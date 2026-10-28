package fr.eni.bookhub.dto;

public record LoginResponse(String token, UserResponse user) {
}

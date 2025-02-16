package com.avito.merchshop.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
public class AuthRequest {
    private String username;
    private String password;
}

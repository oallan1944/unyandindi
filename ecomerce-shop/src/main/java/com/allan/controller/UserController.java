package com.allan.controller;

import com.allan.model.User;
import com.allan.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> userProfileHandler(
            @RequestHeader(value = "Authorization", required = false) String jwt) {

        // ✅ guard: missing header
        if (jwt == null || jwt.isBlank()) {
            log.debug("Profile request received with no Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // ✅ guard: malformed header
        if (!jwt.startsWith("Bearer ") || jwt.length() <= 7) {
            log.warn("Malformed Authorization header: does not start with 'Bearer ' or token is empty");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User user = userService.findUserByJwtToken(jwt);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to fetch user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}



// package com.allan.controller;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestHeader;
// import org.springframework.web.bind.annotation.RestController;

// import com.allan.model.User;
// import com.allan.service.UserService;

// import lombok.RequiredArgsConstructor;

// @RestController
// @RequiredArgsConstructor
// public class UserController {

//     private final UserService userService;

//     @GetMapping("/users/profile")
//     public ResponseEntity<User> userProfileHandler
//     (@RequestHeader("Authorization") String jwt)
//             throws Exception {
//         // if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//         //     throw new Exception("Invalid Authorization header");
//         // }
//         // String jwt = authorizationHeader.substring(7); // remove "Bearer "
//         User user = userService.findUserByJwtToken(jwt);
//         return ResponseEntity.ok(user);
//     }

// }

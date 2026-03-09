package com.byteme.app;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock private UserAccountRepository userRepo;
    @Mock private SellerRepository sellerRepo;
    @Mock private OrganisationRepository orgRepo;
    @Mock private PasswordEncoder passwordEncoder;
    
    private JwtUtil jwtUtil;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        String testSecret = "my_super_secret_key_32_characters_long_minimum";
        jwtUtil = new JwtUtil(testSecret, 3600000L);
        
        authController = new AuthController(userRepo, sellerRepo, orgRepo, passwordEncoder, jwtUtil);
        
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testRegister_SellerSuccess() throws Exception {
        AuthController.RegisterRequest req = new AuthController.RegisterRequest();
        req.setEmail("new@test.com");
        req.setPassword("pass");
        req.setRole(UserAccount.Role.SELLER);
        req.setBusinessName("Bakery");

        when(userRepo.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepo.save(any())).thenAnswer(i -> {
            UserAccount u = i.getArgument(0);
            u.setUserId(UUID.randomUUID());
            return u;
        });

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        verify(sellerRepo).save(any(Seller.class));
    }

    @Test
    void testLogin_Success() throws Exception {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setEmail("test@byteme.com");
        req.setPassword("password123");

        UserAccount user = new UserAccount();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@byteme.com");
        user.setPasswordHash("hashed_string");
        user.setRole(UserAccount.Role.SELLER);

        when(userRepo.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("test@byteme.com"));
    }

    @Test
    void testMe_Success() throws Exception {
        UUID mockUserId = UUID.randomUUID();
        UserAccount user = new UserAccount();
        user.setUserId(mockUserId);
        user.setRole(UserAccount.Role.SELLER);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(mockUserId, null, Collections.emptyList())
        );

        when(userRepo.findById(mockUserId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(mockUserId.toString()));
    }

    @Test
    void testLogin_Failure() throws Exception {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setEmail("wrong@test.com");
        req.setPassword("wrong");

        when(userRepo.findByEmail(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
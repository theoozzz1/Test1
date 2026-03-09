package com.byteme.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BundleControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private UUID mockUserId;

    @Mock private BundlePostingRepository bundleRepo;
    @Mock private SellerRepository sellerRepo;
    @Mock private CategoryRepository categoryRepo;

    @InjectMocks
    private BundleController bundleController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(bundleController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        
        mockUserId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(mockUserId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetAvailable_Success() throws Exception {
        PageImpl<BundlePosting> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        
        when(bundleRepo.findAvailable(any(Instant.class), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/bundles")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetById_Success() throws Exception {
        UUID id = UUID.randomUUID();
        BundlePosting b = new BundlePosting();
        b.setTitle("Test Title");
        
        when(bundleRepo.findById(id)).thenReturn(Optional.of(b));

        mockMvc.perform(get("/api/bundles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    void testGetBySeller_Success() throws Exception {
        UUID sellerId = UUID.randomUUID();
        when(bundleRepo.findBySeller_SellerId(sellerId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/bundles/seller/{sellerId}", sellerId))
                .andExpect(status().isOk());
    }

    @Test
    void testCreate_Success() throws Exception {
        Seller mockSeller = new Seller();
        mockSeller.setSellerId(UUID.randomUUID());
        
        when(sellerRepo.findByUserUserId(mockUserId)).thenReturn(Optional.of(mockSeller));
        when(bundleRepo.save(any(BundlePosting.class))).thenAnswer(i -> i.getArgument(0));

        BundleController.CreateBundleRequest req = new BundleController.CreateBundleRequest();
        req.setTitle("New Bundle");
        req.setPriceCents(100);
        req.setQuantityTotal(5);
        req.setPickupStartAt(Instant.now());
        req.setPickupEndAt(Instant.now().plusSeconds(3600));

        mockMvc.perform(post("/api/bundles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdate_Success() throws Exception {
        UUID id = UUID.randomUUID();
        UserAccount user = new UserAccount();
        user.setUserId(mockUserId); 
        Seller seller = new Seller();
        seller.setUser(user);
        BundlePosting bundle = new BundlePosting();
        bundle.setSeller(seller);

        when(bundleRepo.findById(id)).thenReturn(Optional.of(bundle));
        when(bundleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        BundleController.UpdateBundleRequest req = new BundleController.UpdateBundleRequest();
        req.setTitle("Updated Title");

        mockMvc.perform(put("/api/bundles/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void testUpdate_Forbidden() throws Exception {
        UUID id = UUID.randomUUID();
        UserAccount otherUser = new UserAccount();
        otherUser.setUserId(UUID.randomUUID()); 
        Seller seller = new Seller();
        seller.setUser(otherUser);
        BundlePosting bundle = new BundlePosting();
        bundle.setSeller(seller);

        when(bundleRepo.findById(id)).thenReturn(Optional.of(bundle));

        mockMvc.perform(put("/api/bundles/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Forbidden\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testActivate_Success() throws Exception {
        UUID id = UUID.randomUUID();
        UserAccount user = new UserAccount();
        user.setUserId(mockUserId);
        Seller seller = new Seller();
        seller.setUser(user);
        BundlePosting b = new BundlePosting();
        b.setSeller(seller);
        when(bundleRepo.findById(id)).thenReturn(Optional.of(b));
        when(bundleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/api/bundles/{id}/activate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testClose_Success() throws Exception {
        UUID id = UUID.randomUUID();
        UserAccount user = new UserAccount();
        user.setUserId(mockUserId);
        Seller seller = new Seller();
        seller.setUser(user);
        BundlePosting b = new BundlePosting();
        b.setSeller(seller);
        when(bundleRepo.findById(id)).thenReturn(Optional.of(b));
        when(bundleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/api/bundles/{id}/close", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void testNotFoundScenarios() throws Exception {
        UUID id = UUID.randomUUID();
        when(bundleRepo.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bundles/{id}", id)).andExpect(status().isNotFound());
        mockMvc.perform(put("/api/bundles/{id}", id).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/bundles/{id}/activate", id)).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/bundles/{id}/close", id)).andExpect(status().isNotFound());
    }
}
package com.fomo.backend.controller;

import com.fomo.backend.entity.Report;
import com.fomo.backend.service.AdminService;
import com.fomo.backend.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private ReportService reportService;

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getReports_asAdmin_returns200() throws Exception {
        when(reportService.getAllReports()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getReports_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReports_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reports"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void resolveReport_asAdmin_returns200() throws Exception {
        UUID reportId = UUID.randomUUID();
        doNothing().when(reportService).resolveReport(any());

        mockMvc.perform(post("/api/v1/admin/reports/" + reportId + "/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Report resolved"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void verifyUser_asAdmin_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(adminService).verifyUser(any());

        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User verified"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void unverifyUser_asAdmin_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(adminService).unverifyUser(any());

        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/unverify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User unverified"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void banUser_asAdmin_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(adminService).banUser(any());

        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/ban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User banned"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void unbanUser_asAdmin_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(adminService).unbanUser(any());

        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/unban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User unbanned"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void deleteUser_asAdmin_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(adminService).deleteUser(any());

        mockMvc.perform(delete("/api/v1/admin/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void banUser_asUser_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users/" + UUID.randomUUID() + "/ban"))
                .andExpect(status().isForbidden());
    }
}

package com.example.statusmonitor;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StatusController.class)
class StatusControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MonitorService monitor;

    @Test
    void healthIsUpAndDoesNotCheckUrls() throws Exception {
        when(monitor.getUrls()).thenReturn(List.of("https://a.com"));
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.servedBy").isNotEmpty());
        verify(monitor, never()).checkAll();
    }

    @Test
    void statusReturnsOneEntryPerUrl() throws Exception {
        when(monitor.checkAll()).thenReturn(List.of(
                new MonitorService.UrlStatus("https://a.com", true, 200, 12, null),
                new MonitorService.UrlStatus("https://b.invalid", false, 0, 3, "UnknownHostException")));
        mvc.perform(get("/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].up").value(true))
                .andExpect(jsonPath("$[1].up").value(false));
    }
}

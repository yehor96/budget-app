package webmvc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import yehor.budget.common.date.DateManager;
import yehor.budget.service.SettingsService;
import yehor.budget.web.dto.full.SettingsFullDto;
import yehor.budget.web.dto.limited.SettingsLimitedDto;

import javax.persistence.EntityNotFoundException;

import static common.factory.SettingsFactory.defaultSettingsFullDto;
import static common.factory.SettingsFactory.defaultSettingsLimitedDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SettingsControllerTest extends BaseWebMvcTest {

    @MockBean
    private SettingsService settingsService;
    @MockBean
    private DateManager dateManager;

    // Get settings

    @Test
    void testGetSettings() throws Exception {
        SettingsFullDto expectedSettings = defaultSettingsFullDto();

        when(settingsService.getSettings()).thenReturn(expectedSettings);

        String response = mockMvc.perform(get(SETTINGS_URL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        SettingsFullDto actualSettings = objectMapper.readValue(response, SettingsFullDto.class);

        verify(settingsService, times(1)).getSettings();
        assertEquals(expectedSettings, actualSettings);
    }

    @Test
    void testTryGettingSettingsWhenTheyAreNotSetInDb() throws Exception {
        String expectedErrorMessage = "expectedErrorMessage";

        doThrow(new EntityNotFoundException(expectedErrorMessage))
                .when(settingsService).getSettings();

        String response = mockMvc.perform(get(SETTINGS_URL))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse().getContentAsString();

        verifyResponseErrorObject(response, INTERNAL_SERVER_ERROR, expectedErrorMessage);
    }

    // Update settings

    @Test
    void testUpdateSettings() throws Exception {
        SettingsLimitedDto settings = defaultSettingsLimitedDto();

        mockMvc.perform(put(SETTINGS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settings)))
                .andExpect(status().isOk());

        verify(settingsService, times(1)).updateSettings(settings);
    }
}

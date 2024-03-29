package context.webmvc;

import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import yehor.budget.common.date.DateManager;
import yehor.budget.common.exception.ObjectNotFoundException;
import yehor.budget.service.recording.StorageRecordingService;
import yehor.budget.web.dto.full.StorageRecordFullDto;
import yehor.budget.web.dto.limited.StorageRecordLimitedDto;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static common.factory.StorageFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StorageWebMvcTest extends BaseWebMvcTest {

    protected static final String STORAGE_INTERVAL_URL = STORAGE_URL + "/interval";

    @MockBean
    private StorageRecordingService storageRecordingService;
    @MockBean
    private DateManager dateManager;

    // Get latest storage record

    @Test
    void testGetLatestSuccessfully() throws Exception {
        StorageRecordFullDto expectedStorageRecordDto = defaultStorageRecordFullDto();

        when(storageRecordingService.getLatest()).thenReturn(Optional.of(expectedStorageRecordDto));

        String response = mockMvc.perform(get(STORAGE_URL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        StorageRecordFullDto actualStorageRecordDto = objectMapper.readValue(response, StorageRecordFullDto.class);

        assertEquals(expectedStorageRecordDto, actualStorageRecordDto);
    }

    @Test
    void testGetLatestThrowsExceptionWhenNotFound() throws Exception {
        String expectedErrorMessage = "There are no storage records";

        when(storageRecordingService.getLatest()).thenReturn(Optional.empty());

        String response = mockMvc.perform(get(STORAGE_URL))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        verifyResponseErrorObject(response, NOT_FOUND, expectedErrorMessage);
    }

    // Save storage record

    @Test
    void testSaveSuccessfully() throws Exception {
        StorageRecordLimitedDto storageRecordDto = defaultStorageRecordLimitedDto();
        StorageRecordFullDto expectedStorageRecordFullDto = defaultStorageRecordFullDto();

        when(storageRecordingService.save(storageRecordDto)).thenReturn(expectedStorageRecordFullDto);

        String response = mockMvc.perform(post(STORAGE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(storageRecordDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        StorageRecordFullDto actualStorageRecordFullDto = objectMapper.readValue(response, StorageRecordFullDto.class);

        verify(storageRecordingService, times(1)).save(storageRecordDto);
        assertEquals(expectedStorageRecordFullDto, actualStorageRecordFullDto);
    }

    @Test
    void testSaveThrowsExceptionWhenInvalidDate() throws Exception {
        String expectedErrorMessage = "expectedErrorMessage";
        StorageRecordLimitedDto storageRecordDto = defaultStorageRecordLimitedDto();

        doThrow(new IllegalArgumentException(expectedErrorMessage)).when(dateManager).validateDateAfterStart(any());

        String response = mockMvc.perform(post(STORAGE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(storageRecordDto)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        verifyResponseErrorObject(response, BAD_REQUEST, expectedErrorMessage);

        verify(storageRecordingService, never()).save(storageRecordDto);
    }

    @Test
    void testSaveThrowsExceptionWhenMissingStorageItems() throws Exception {
        String expectedErrorMessage = "Storage items are not provided";
        StorageRecordLimitedDto storageRecordDto = defaultStorageRecordLimitedDto();
        storageRecordDto.setStorageItems(Collections.emptyList());

        String response = mockMvc.perform(post(STORAGE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(storageRecordDto)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        verifyResponseErrorObject(response, BAD_REQUEST, expectedErrorMessage);

        verify(storageRecordingService, never()).save(storageRecordDto);
    }

    // Get storage records in interval

    @Test
    void testGetStorageRecordsInInterval() throws Exception {
        List<StorageRecordFullDto> expectedRecordsInterval = List.of(defaultStorageRecordFullDto());

        String from = "2022-06-06";
        String to = "2022-07-07";
        LocalDate dateFrom = LocalDate.of(2022, 6, 6);
        LocalDate dateTo = LocalDate.of(2022, 7, 7);

        when(dateManager.parse(from)).thenReturn(dateFrom);
        when(dateManager.parse(to)).thenReturn(dateTo);
        when(storageRecordingService.findAllInInterval(dateFrom, dateTo)).thenReturn(expectedRecordsInterval);

        String response = mockMvc.perform(get(STORAGE_INTERVAL_URL)
                        .param("dateFrom", from)
                        .param("dateTo", to))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectReader listReader = objectMapper.readerForListOf(StorageRecordFullDto.class);
        List<StorageRecordFullDto> actualRecordsInterval = listReader.readValue(response);

        verify(storageRecordingService, times(1)).findAllInInterval(dateFrom, dateTo);
        assertEquals(expectedRecordsInterval, actualRecordsInterval);
    }

    @Test
    void testTryGettingStorageRecordsInIntervalFailingSequentialOrderCheck() throws Exception {
        String expectedErrorMessage = "expectedErrorMessage";
        String from = "2022-06-06";
        String to = "2022-07-07";
        LocalDate dateFrom = LocalDate.of(2022, 6, 6);
        LocalDate dateTo = LocalDate.of(2022, 7, 7);

        when(dateManager.parse(from)).thenReturn(dateFrom);
        when(dateManager.parse(to)).thenReturn(dateTo);
        doThrow(new IllegalArgumentException(expectedErrorMessage))
                .when(dateManager).validateDatesInSequentialOrder(dateFrom, dateTo);

        String response = mockMvc.perform(get(STORAGE_INTERVAL_URL)
                        .param("dateFrom", from)
                        .param("dateTo", to))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        verifyResponseErrorObject(response, BAD_REQUEST, expectedErrorMessage);

        verify(storageRecordingService, never()).findAllInInterval(dateFrom, dateTo);
    }

    @Test
    void testTryGettingStorageRecordsInIntervalFailingDatesWithinBudgetCheck() throws Exception {
        String expectedErrorMessage = "expectedErrorMessage";
        String from = "2022-06-06";
        String to = "2022-07-07";
        LocalDate dateFrom = LocalDate.of(2022, 6, 6);
        LocalDate dateTo = LocalDate.of(2022, 7, 7);

        when(dateManager.parse(from)).thenReturn(dateFrom);
        when(dateManager.parse(to)).thenReturn(dateTo);
        doThrow(new IllegalArgumentException(expectedErrorMessage))
                .when(dateManager).validateDatesWithinBudget(dateFrom, dateTo);

        String response = mockMvc.perform(get(STORAGE_INTERVAL_URL)
                        .param("dateFrom", from)
                        .param("dateTo", to))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        verifyResponseErrorObject(response, BAD_REQUEST, expectedErrorMessage);

        verify(storageRecordingService, never()).findAllInInterval(dateFrom, dateTo);
    }

    @Test
    void testTryGettingStorageRecordsInIntervalFailingParsingOfDateParameter() throws Exception {
        String expectedErrorMessage = "expectedErrorMessage";
        String from = "2022-06-06";
        String to = "2022-07-07";
        LocalDate dateFrom = LocalDate.of(2022, 6, 6);
        LocalDate dateTo = LocalDate.of(2022, 7, 7);

        doThrow(new IllegalArgumentException(expectedErrorMessage))
                .when(dateManager).parse(from);

        String response = mockMvc.perform(get(STORAGE_INTERVAL_URL)
                        .param("dateFrom", from)
                        .param("dateTo", to))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        verifyResponseErrorObject(response, BAD_REQUEST, expectedErrorMessage);

        verify(storageRecordingService, never()).findAllInInterval(dateFrom, dateTo);
    }

    // Delete storage record

    @Test
    void testDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete(STORAGE_URL)
                        .param("id", String.valueOf(DEFAULT_STORAGE_RECORD_ID)))
                .andExpect(status().isOk());

        verify(storageRecordingService, times(1)).delete(DEFAULT_STORAGE_RECORD_ID);
    }

    @Test
    void testTryDeletingNotExistingStorageRecord() throws Exception {
        String expectedErrorMessage = "expectedErrorMessage";

        doThrow(new ObjectNotFoundException(expectedErrorMessage))
                .when(storageRecordingService).delete(DEFAULT_STORAGE_RECORD_ID);

        String response = mockMvc.perform(delete(STORAGE_URL)
                        .param("id", String.valueOf(DEFAULT_STORAGE_RECORD_ID)))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        verifyResponseErrorObject(response, NOT_FOUND, expectedErrorMessage);
    }
}

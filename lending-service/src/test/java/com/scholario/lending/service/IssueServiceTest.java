package com.scholario.lending.service;

import com.scholario.lending.client.BookServiceClient;
import com.scholario.lending.client.IdentityServiceClient;
import com.scholario.lending.client.UserDto;
import com.scholario.lending.dto.BulkIssueInput;
import com.scholario.lending.dto.IssueInput;
import com.scholario.lending.dto.RenewInput;
import com.scholario.lending.dto.ReturnInput;
import com.scholario.lending.event.LendingEventProducer;
import com.scholario.lending.model.*;
import com.scholario.lending.repository.IssueRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class IssueServiceTest {

    @Mock
    private IssueRecordRepository issueRecordRepository;
    @Mock
    private BookServiceClient bookServiceClient;
    @Mock
    private IdentityServiceClient identityServiceClient;
    @Mock
    private LendingEventProducer lendingEventProducer;

    @InjectMocks
    private IssueService issueService;

    @Test
    void testIssueBook_Success() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        when(identityServiceClient.getUserById(10L)).thenReturn(new UserDto(10L, "username", "email", "Full Name", Collections.emptySet()));
        when(issueRecordRepository.findByUserIdAndStateTypeNot(10L, "RETURNED")).thenReturn(new ArrayList<>());
        when(issueRecordRepository.findByBookId(1L)).thenReturn(new ArrayList<>());
        when(issueRecordRepository.save(any(IssueRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        IssueInput input = new IssueInput(1L, 10L);
        IssueRecord record = issueService.issueBook(input);

        assertNotNull(record);
        assertEquals(1L, record.getBookId());
        assertEquals(10L, record.getUserId());
        assertTrue(record.getState() instanceof Issued);
        verify(lendingEventProducer).publishLendingEvent(eq("BOOK_ISSUED"), any(), eq(1L), eq(10L), eq("ISSUED"));
    }

    @Test
    void testIssueBook_BookNotFound() {
        when(bookServiceClient.existsById(1L)).thenReturn(false);

        IssueInput input = new IssueInput(1L, 10L);
        assertThrows(IllegalArgumentException.class, () -> issueService.issueBook(input));
    }

    @Test
    void testIssueBook_UserNotFound() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        when(identityServiceClient.getUserById(10L)).thenReturn(null);

        IssueInput input = new IssueInput(1L, 10L);
        assertThrows(IllegalArgumentException.class, () -> issueService.issueBook(input));
    }

    @Test
    void testIssueBook_MaxLimitReached() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        when(identityServiceClient.getUserById(10L)).thenReturn(new UserDto(10L, "username", "email", "Full Name", Collections.emptySet()));
        
        List<IssueRecord> active = List.of(new IssueRecord(), new IssueRecord(), new IssueRecord(), new IssueRecord(), new IssueRecord());
        when(issueRecordRepository.findByUserIdAndStateTypeNot(10L, "RETURNED")).thenReturn(active);

        IssueInput input = new IssueInput(1L, 10L);
        assertThrows(IllegalStateException.class, () -> issueService.issueBook(input));
    }

    @Test
    void testIssueBook_AlreadyIssued() {
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        when(identityServiceClient.getUserById(10L)).thenReturn(new UserDto(10L, "username", "email", "Full Name", Collections.emptySet()));
        when(issueRecordRepository.findByUserIdAndStateTypeNot(10L, "RETURNED")).thenReturn(new ArrayList<>());

        IssueRecord activeIssue = new IssueRecord();
        activeIssue.setState(new Issued(LocalDateTime.now(), LocalDateTime.now().plusDays(1)));
        when(issueRecordRepository.findByBookId(1L)).thenReturn(List.of(activeIssue));

        IssueInput input = new IssueInput(1L, 10L);
        assertThrows(IllegalStateException.class, () -> issueService.issueBook(input));
    }

    @Test
    void testReturnBook_Success() {
        IssueRecord record = new IssueRecord();
        record.setId(100L);
        record.setBookId(1L);
        record.setUserId(10L);
        record.setDueDate(LocalDateTime.now().minusDays(2)); // overdue by 2 days
        record.setState(new Issued(LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(2)));

        when(issueRecordRepository.findByIdAndUserId(100L, 10L)).thenReturn(Optional.of(record));
        when(issueRecordRepository.save(any(IssueRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        ReturnInput input = new ReturnInput(100L, 10L);
        IssueRecord returned = issueService.returnBook(input);

        assertNotNull(returned);
        assertTrue(returned.getState() instanceof Returned);
        assertTrue(returned.getPenaltyAmount() > 0.0);
        verify(lendingEventProducer).publishLendingEvent(eq("BOOK_RETURNED"), eq(100L), eq(1L), eq(10L), eq("RETURNED"));
    }

    @Test
    void testReturnBook_AlreadyReturned() {
        IssueRecord record = new IssueRecord();
        record.setId(100L);
        record.setState(new Returned(LocalDateTime.now(), 0.0));

        when(issueRecordRepository.findByIdAndUserId(100L, 10L)).thenReturn(Optional.of(record));

        ReturnInput input = new ReturnInput(100L, 10L);
        assertThrows(IllegalStateException.class, () -> issueService.returnBook(input));
    }

    @Test
    void testRenewBook_Success() {
        IssueRecord record = new IssueRecord();
        record.setId(100L);
        record.setBookId(1L);
        record.setUserId(10L);
        record.setRenewalCount(0);
        record.setState(new Issued(LocalDateTime.now(), LocalDateTime.now().plusDays(10)));

        when(issueRecordRepository.findByIdAndUserId(100L, 10L)).thenReturn(Optional.of(record));
        when(issueRecordRepository.save(any(IssueRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        RenewInput input = new RenewInput(100L, 10L);
        IssueRecord renewed = issueService.renewBook(input);

        assertNotNull(renewed);
        assertEquals(1, renewed.getRenewalCount());
        verify(lendingEventProducer).publishLendingEvent(eq("BOOK_RENEWED"), eq(100L), eq(1L), eq(10L), eq("ISSUED"));
    }

    @Test
    void testRenewBook_MaxRenewalsReached() {
        IssueRecord record = new IssueRecord();
        record.setId(100L);
        record.setRenewalCount(2);
        record.setState(new Issued(LocalDateTime.now(), LocalDateTime.now()));

        when(issueRecordRepository.findByIdAndUserId(100L, 10L)).thenReturn(Optional.of(record));

        RenewInput input = new RenewInput(100L, 10L);
        assertThrows(IllegalStateException.class, () -> issueService.renewBook(input));
    }

    @Test
    void testBulkIssueBooks_Success() {
        when(identityServiceClient.getUserById(10L)).thenReturn(new UserDto(10L, "u", "e", "User Name", Collections.emptySet()));
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        when(bookServiceClient.existsById(2L)).thenReturn(true);
        when(issueRecordRepository.findByUserIdAndStateTypeNot(10L, "RETURNED")).thenReturn(new ArrayList<>());
        when(issueRecordRepository.save(any(IssueRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        BulkIssueInput input = new BulkIssueInput(List.of(1L, 2L), 10L);
        List<IssueRecord> issued = issueService.bulkIssueBooks(input);

        assertEquals(2, issued.size());
        verify(lendingEventProducer, times(2)).publishLendingEvent(eq("BOOK_ISSUED"), any(), any(), eq(10L), eq("ISSUED"));
    }

    @Test
    void testBulkIssueBooks_NotEnoughSlots() {
        when(identityServiceClient.getUserById(10L)).thenReturn(new UserDto(10L, "u", "e", "User Name", Collections.emptySet()));
        when(bookServiceClient.existsById(1L)).thenReturn(true);
        when(bookServiceClient.existsById(2L)).thenReturn(true);
        
        List<IssueRecord> active = List.of(new IssueRecord(), new IssueRecord(), new IssueRecord(), new IssueRecord());
        when(issueRecordRepository.findByUserIdAndStateTypeNot(10L, "RETURNED")).thenReturn(active);

        BulkIssueInput input = new BulkIssueInput(List.of(1L, 2L), 10L);
        assertThrows(IllegalStateException.class, () -> issueService.bulkIssueBooks(input));
    }

    @Test
    void testUpdateOverdueStatus() {
        IssueRecord record = new IssueRecord();
        record.setId(100L);
        record.setBookId(1L);
        record.setUserId(10L);
        record.setDueDate(LocalDateTime.now().minusDays(1));
        record.setState(new Issued(LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(1)));

        when(issueRecordRepository.findByDueDateLessThanAndStateType(any(LocalDateTime.class), eq("ISSUED")))
                .thenReturn(List.of(record));
        when(issueRecordRepository.save(any(IssueRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        issueService.updateOverdueStatus();

        assertTrue(record.getState() instanceof Overdue);
        verify(lendingEventProducer).publishLendingEvent(eq("BOOK_OVERDUE"), eq(100L), eq(1L), eq(10L), eq("OVERDUE"));
    }

    @Test
    void testQueriesAndCounts() {
        IssueRecord rec = new IssueRecord();
        when(issueRecordRepository.findByUserIdAndStateTypeNot(10L, "RETURNED")).thenReturn(List.of(rec));
        assertEquals(1, issueService.getIssuedBooksByUser(10L).size());

        when(issueRecordRepository.findByUserId(10L)).thenReturn(List.of(rec));
        assertEquals(1, issueService.getIssueHistory(10L).size());

        when(issueRecordRepository.findByStateType("ISSUED")).thenReturn(List.of(rec));
        when(issueRecordRepository.findByStateType("OVERDUE")).thenReturn(List.of(rec));
        assertEquals(2, issueService.getDueDates().size());

        when(issueRecordRepository.countByBookId(1L)).thenReturn(5L);
        assertEquals(5L, issueService.countByBookId(1L));

        when(issueRecordRepository.countByUserId(10L)).thenReturn(10L);
        assertEquals(10L, issueService.countByUserId(10L));

        when(issueRecordRepository.countByStateTypeNot("RETURNED")).thenReturn(3L);
        assertEquals(3L, issueService.countActive());

        when(issueRecordRepository.countByStateType("OVERDUE")).thenReturn(1L);
        assertEquals(1L, issueService.countOverdue());

        when(issueRecordRepository.countByReturnDateAfter(any())).thenReturn(2L);
        assertEquals(2L, issueService.countReturnedToday());
    }

    @Test
    void testRenewBook_NotFound() {
        when(issueRecordRepository.findById(100L)).thenReturn(Optional.empty());
        RenewInput input = new RenewInput(100L, 10L);
        assertThrows(IllegalArgumentException.class, () -> issueService.renewBook(input));
    }

    @Test
    void testBulkIssueBooks_UserNotFound() {
        when(identityServiceClient.getUserById(10L)).thenReturn(null);
        BulkIssueInput input = new BulkIssueInput(List.of(1L), 10L);
        assertThrows(IllegalArgumentException.class, () -> issueService.bulkIssueBooks(input));
    }

    @Test
    void testBulkIssueBooks_BookNotFound() {
        when(identityServiceClient.getUserById(10L)).thenReturn(new UserDto(10L, "u", "e", "User Name", Collections.emptySet()));
        when(bookServiceClient.existsById(1L)).thenReturn(false);
        BulkIssueInput input = new BulkIssueInput(List.of(1L), 10L);
        assertThrows(IllegalArgumentException.class, () -> issueService.bulkIssueBooks(input));
    }

    @Test
    void testGetIssuesByBook_Success() {
        IssueRecordRepository.BookIssueCount bic = mock(IssueRecordRepository.BookIssueCount.class);
        when(bic.getBookId()).thenReturn(1L);
        when(bic.getIssueCount()).thenReturn(5L);
        when(issueRecordRepository.countIssuesByBook()).thenReturn(List.of(bic));

        List<IssueService.BookIssueCountDto> result = issueService.getIssuesByBook();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).bookId());
        assertEquals(5L, result.get(0).issueCount());
    }
}

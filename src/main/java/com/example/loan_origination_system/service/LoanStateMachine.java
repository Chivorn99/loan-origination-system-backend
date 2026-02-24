package com.example.loan_origination_system.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.model.enums.CollateralStatus;
import com.example.loan_origination_system.model.enums.LoanEvent;
import com.example.loan_origination_system.model.enums.LoanStatus;
import com.example.loan_origination_system.model.loan.PawnItem;
import com.example.loan_origination_system.model.loan.PawnLoan;
import com.example.loan_origination_system.repository.PawnItemRepository;
import com.example.loan_origination_system.repository.PawnLoanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralized state machine for loan lifecycle management.
 * All loan status changes MUST go through this service.
 * 
 * Business Rules:
 * 1. Loan status MUST NOT be set directly anywhere in the code
 * 2. All status changes must be triggered by events
 * 3. Invalid transitions are rejected with BusinessException
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanStateMachine {
    
    private final PawnLoanRepository pawnLoanRepository;
    private final PawnItemRepository pawnItemRepository;
    
    // Define valid state transitions
    private static final Map<LoanStatus, Map<LoanEvent, LoanStatus>> TRANSITION_MAP = new EnumMap<>(LoanStatus.class);
    
    static {
        // Initialize transition rules
        
        // From CREATED state
        Map<LoanEvent, LoanStatus> fromCreated = new EnumMap<>(LoanEvent.class);
        fromCreated.put(LoanEvent.ISSUE_LOAN, LoanStatus.ACTIVE);
        fromCreated.put(LoanEvent.CANCEL, LoanStatus.CANCELLED);
        fromCreated.put(LoanEvent.MANUAL_CANCEL, LoanStatus.CANCELLED);
        TRANSITION_MAP.put(LoanStatus.CREATED, fromCreated);
        
        // From ACTIVE state
        Map<LoanEvent, LoanStatus> fromActive = new EnumMap<>(LoanEvent.class);
        fromActive.put(LoanEvent.PARTIAL_PAYMENT, LoanStatus.PARTIALLY_PAID);
        fromActive.put(LoanEvent.FULL_PAYMENT, LoanStatus.REDEEMED);
        fromActive.put(LoanEvent.DUE_DATE_PASSED, LoanStatus.OVERDUE);
        fromActive.put(LoanEvent.MANUAL_DEFAULT, LoanStatus.DEFAULTED);
        fromActive.put(LoanEvent.MANUAL_REDEEM, LoanStatus.REDEEMED);
        fromActive.put(LoanEvent.MANUAL_CANCEL, LoanStatus.CANCELLED);
        TRANSITION_MAP.put(LoanStatus.ACTIVE, fromActive);
        
        // From PARTIALLY_PAID state
        Map<LoanEvent, LoanStatus> fromPartiallyPaid = new EnumMap<>(LoanEvent.class);
        fromPartiallyPaid.put(LoanEvent.PARTIAL_PAYMENT, LoanStatus.PARTIALLY_PAID);
        fromPartiallyPaid.put(LoanEvent.FULL_PAYMENT, LoanStatus.REDEEMED);
        fromPartiallyPaid.put(LoanEvent.DUE_DATE_PASSED, LoanStatus.OVERDUE);
        fromPartiallyPaid.put(LoanEvent.MANUAL_DEFAULT, LoanStatus.DEFAULTED);
        fromPartiallyPaid.put(LoanEvent.MANUAL_REDEEM, LoanStatus.REDEEMED);
        TRANSITION_MAP.put(LoanStatus.PARTIALLY_PAID, fromPartiallyPaid);
        
        // From OVERDUE state
        Map<LoanEvent, LoanStatus> fromOverdue = new EnumMap<>(LoanEvent.class);
        fromOverdue.put(LoanEvent.GRACE_PERIOD_EXPIRED, LoanStatus.DEFAULTED);
        fromOverdue.put(LoanEvent.FULL_PAYMENT, LoanStatus.REDEEMED);
        fromOverdue.put(LoanEvent.PARTIAL_PAYMENT, LoanStatus.OVERDUE); // Still overdue even with partial payment
        fromOverdue.put(LoanEvent.MANUAL_REDEEM, LoanStatus.REDEEMED);
        TRANSITION_MAP.put(LoanStatus.OVERDUE, fromOverdue);
        
        // Terminal states (no transitions allowed)
        TRANSITION_MAP.put(LoanStatus.REDEEMED, new EnumMap<>(LoanEvent.class));
        TRANSITION_MAP.put(LoanStatus.DEFAULTED, new EnumMap<>(LoanEvent.class));
        TRANSITION_MAP.put(LoanStatus.CANCELLED, new EnumMap<>(LoanEvent.class));
    }
    
    /**
     * Transition a loan from its current state to a new state based on an event.
     * This is the ONLY method that should update loan status in the entire system.
     * 
     * @param loan The loan to transition
     * @param event The event triggering the transition
     * @return The updated loan with new status
     * @throws BusinessException if transition is invalid
     */
    @Transactional
    public PawnLoan transition(PawnLoan loan, LoanEvent event) {
        LoanStatus currentStatus = loan.getStatus();
        LoanStatus newStatus = getNextStatus(currentStatus, event);
        
        log.info("Transitioning loan {} from {} to {} via event {}", 
            loan.getLoanCode(), currentStatus, newStatus, event);
        
        // Apply state-specific business logic
        applyStateActions(loan, currentStatus, newStatus, event);
        
        // Update loan status
        loan.setStatus(newStatus);
        loan.setUpdatedAt(LocalDateTime.now());
        
        // Save the loan
        PawnLoan updatedLoan = pawnLoanRepository.save(loan);
        
        log.info("Loan {} successfully transitioned to {}", loan.getLoanCode(), newStatus);
        return updatedLoan;
    }
    
    /**
     * Get the next status for a given current status and event.
     * 
     * @param currentStatus Current loan status
     * @param event Triggering event
     * @return Next loan status
     * @throws BusinessException if transition is invalid
     */
    public LoanStatus getNextStatus(LoanStatus currentStatus, LoanEvent event) {
        Map<LoanEvent, LoanStatus> transitions = TRANSITION_MAP.get(currentStatus);
        
        if (transitions == null || !transitions.containsKey(event)) {
            throw new BusinessException("INVALID_TRANSITION",
                String.format("Cannot transition from %s via event %s. Valid events: %s",
                    currentStatus, event, getValidEvents(currentStatus)));
        }
        
        return transitions.get(event);
    }
    
    /**
     * Check if a transition is valid without executing it.
     * 
     * @param currentStatus Current loan status
     * @param event Triggering event
     * @return true if transition is valid
     */
    public boolean isValidTransition(LoanStatus currentStatus, LoanEvent event) {
        Map<LoanEvent, LoanStatus> transitions = TRANSITION_MAP.get(currentStatus);
        return transitions != null && transitions.containsKey(event);
    }
    
    /**
     * Get all valid events for a given loan status.
     * 
     * @param status Loan status
     * @return Set of valid events
     */
    public Set<LoanEvent> getValidEvents(LoanStatus status) {
        Map<LoanEvent, LoanStatus> transitions = TRANSITION_MAP.get(status);
        return transitions != null ? transitions.keySet() : Set.of();
    }
    
    /**
     * Apply business logic specific to state transitions.
     * This includes updating collateral status, setting timestamps, etc.
     */
    private void applyStateActions(PawnLoan loan, LoanStatus fromStatus, LoanStatus toStatus, LoanEvent event) {
        switch (toStatus) {
            case REDEEMED:
                handleRedeemed(loan);
                break;
            case DEFAULTED:
                handleDefaulted(loan);
                break;
            case CANCELLED:
                handleCancelled(loan);
                break;
            case ACTIVE:
                handleActivated(loan);
                break;
            case OVERDUE:
                handleOverdue(loan);
                break;
            default:
                // No special actions for other states
                break;
        }
    }
    
    /**
     * Handle loan redemption (fully paid).
     * Business Rules:
     * 1. Set redeemedAt timestamp
     * 2. Release pawn item (set collateral status to AVAILABLE)
     */
    private void handleRedeemed(PawnLoan loan) {
        loan.setRedeemedAt(LocalDateTime.now());
        
        // Release the pawn item
        PawnItem pawnItem = loan.getPawnItem();
        pawnItem.setStatus(CollateralStatus.AVAILABLE);
        pawnItemRepository.save(pawnItem);
        
        log.info("Loan {} redeemed, collateral {} released", loan.getLoanCode(), pawnItem.getId());
    }
    
    /**
     * Handle loan default.
     * Business Rules:
     * 1. Set defaultedAt timestamp
     * 2. Mark pawn item as FORFEITED
     */
    private void handleDefaulted(PawnLoan loan) {
        loan.setDefaultedAt(LocalDateTime.now());
        
        // Mark pawn item as forfeited
        PawnItem pawnItem = loan.getPawnItem();
        pawnItem.setStatus(CollateralStatus.FORFEITED);
        pawnItemRepository.save(pawnItem);
        
        log.info("Loan {} defaulted, collateral {} forfeited", loan.getLoanCode(), pawnItem.getId());
    }
    
    /**
     * Handle loan cancellation.
     * Business Rules:
     * 1. Loan can only be cancelled from CREATED state
     * 2. Release pawn item if it was reserved
     */
    private void handleCancelled(PawnLoan loan) {
        // If loan was just created and not yet active, release the collateral
        if (loan.getStatus() == LoanStatus.CREATED) {
            PawnItem pawnItem = loan.getPawnItem();
            pawnItem.setStatus(CollateralStatus.AVAILABLE);
            pawnItemRepository.save(pawnItem);
            
            log.info("Loan {} cancelled, collateral {} released", loan.getLoanCode(), pawnItem.getId());
        }
    }
    
    /**
     * Handle loan activation (issuance).
     * Business Rules:
     * 1. Loan becomes active
     * 2. Pawn item status changes to PAWNED
     */
    private void handleActivated(PawnLoan loan) {
        // Mark pawn item as pawned
        PawnItem pawnItem = loan.getPawnItem();
        pawnItem.setStatus(CollateralStatus.PAWNED);
        pawnItemRepository.save(pawnItem);
        
        log.info("Loan {} activated, collateral {} pawned", loan.getLoanCode(), pawnItem.getId());
    }
    
    /**
     * Handle loan becoming overdue.
     * Business Rules:
     * 1. Mark loan as overdue
     * 2. Set overdueAt timestamp
     * 3. Calculate grace period end date (default: 30 days from overdue)
     * 4. Can trigger notifications/alerts
     */
    private void handleOverdue(PawnLoan loan) {
        loan.setOverdueAt(LocalDateTime.now());
        
        // Calculate grace period end date (default 30 days from now)
        LocalDate graceEndDate = LocalDate.now().plusDays(30);
        loan.setGracePeriodEndDate(graceEndDate);
        
        log.info("Loan {} marked as overdue, grace period ends on {}",
            loan.getLoanCode(), graceEndDate);
        
        // Additional business logic for overdue loans can be added here
        // e.g., send notifications, calculate penalties, etc.
    }
    
    /**
     * Helper method to transition a loan by ID.
     * 
     * @param loanId Loan ID
     * @param event Triggering event
     * @return Updated loan
     */
    @Transactional
    public PawnLoan transition(Long loanId, LoanEvent event) {
        PawnLoan loan = pawnLoanRepository.findById(loanId)
            .orElseThrow(() -> new BusinessException("LOAN_NOT_FOUND",
                "Loan with ID " + loanId + " not found"));
        
        return transition(loan, event);
    }
    
    /**
     * Helper method to issue a loan (create → active).
     * 
     * @param loanId Loan ID
     * @return Updated loan
     */
    @Transactional
    public PawnLoan issueLoan(Long loanId) {
        return transition(loanId, LoanEvent.ISSUE_LOAN);
    }
    
    /**
     * Helper method to process partial payment.
     * 
     * @param loanId Loan ID
     * @return Updated loan
     */
    @Transactional
    public PawnLoan processPartialPayment(Long loanId) {
        return transition(loanId, LoanEvent.PARTIAL_PAYMENT);
    }
    
    /**
     * Helper method to process full payment.
     * 
     * @param loanId Loan ID
     * @return Updated loan
     */
    @Transactional
    public PawnLoan processFullPayment(Long loanId) {
        return transition(loanId, LoanEvent.FULL_PAYMENT);
    }
    
    /**
     * Helper method to mark loan as overdue.
     * 
     * @param loanId Loan ID
     * @return Updated loan
     */
    @Transactional
    public PawnLoan markAsOverdue(Long loanId) {
        return transition(loanId, LoanEvent.DUE_DATE_PASSED);
    }
    
    /**
     * Helper method to mark loan as defaulted after grace period.
     * 
     * @param loanId Loan ID
     * @return Updated loan
     */
    @Transactional
    public PawnLoan markAsDefaulted(Long loanId) {
        return transition(loanId, LoanEvent.GRACE_PERIOD_EXPIRED);
    }
    
    /**
     * Helper method to cancel a loan.
     * 
     * @param loanId Loan ID
     * @return Updated loan
     */
    @Transactional
    public PawnLoan cancelLoan(Long loanId) {
        return transition(loanId, LoanEvent.CANCEL);
    }
}
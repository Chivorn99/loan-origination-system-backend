package com.example.loan_origination_system.service;

import org.springframework.stereotype.Service;

import com.example.loan_origination_system.dto.PawnForfeitPatchRequest;
import com.example.loan_origination_system.dto.PawnForfeitRequest;
import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.mapper.LoanMapper;
import com.example.loan_origination_system.model.loan.PawnForfeit;
import com.example.loan_origination_system.model.loan.PawnLoan;
import com.example.loan_origination_system.repository.PawnForfeitRepository;
import com.example.loan_origination_system.repository.PawnLoanRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PawnForfeitService {

    private final PawnForfeitRepository pawnForfeitRepository;
    private final PawnLoanRepository pawnLoanRepository;
    private final LoanMapper loanMapper;

    /**
     * CREATE
     */
    @Transactional
    public PawnForfeit createPawnForfeit(PawnForfeitRequest request) {
        // Validate pawn loan exists
        PawnLoan pawnLoan = pawnLoanRepository.findById(request.getPawnLoanId())
                .orElseThrow(() -> new BusinessException(
                        "PAWN_LOAN_NOT_FOUND",
                        "Pawn loan not found with ID: " + request.getPawnLoanId()
                ));

        PawnForfeit pawnForfeit = loanMapper.toPawnForfeit(request);
        pawnForfeit.setPawnLoan(pawnLoan);

        return pawnForfeitRepository.save(pawnForfeit);
    }

    /**
     * GET by ID
     */
    public PawnForfeit getPawnForfeitById(Long id) {
        return pawnForfeitRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "PAWN_FORFEIT_NOT_FOUND",
                        "Pawn forfeit not found with ID: " + id
                ));
    }

    /**
     * FULL UPDATE (PUT)
     */
    @Transactional
    public PawnForfeit updatePawnForfeit(Long id, PawnForfeitRequest request) {
        PawnForfeit pawnForfeit = getPawnForfeitById(id);

        // Validate pawn loan if provided
        if (request.getPawnLoanId() != null) {
            PawnLoan pawnLoan = pawnLoanRepository.findById(request.getPawnLoanId())
                    .orElseThrow(() -> new BusinessException(
                            "PAWN_LOAN_NOT_FOUND",
                            "Pawn loan not found with ID: " + request.getPawnLoanId()
                    ));
            pawnForfeit.setPawnLoan(pawnLoan);
        }

        // Use mapper to update entity
        loanMapper.updatePawnForfeitFromRequest(request, pawnForfeit);

        return pawnForfeitRepository.save(pawnForfeit);
    }

    /**
     * PARTIAL UPDATE (PATCH)
     */
    @Transactional
    public PawnForfeit patchPawnForfeit(Long id, PawnForfeitPatchRequest patch) {
        PawnForfeit pawnForfeit = getPawnForfeitById(id);

        if (patch.getPawnLoanId() != null) {
            PawnLoan pawnLoan = pawnLoanRepository.findById(patch.getPawnLoanId())
                    .orElseThrow(() -> new BusinessException(
                            "PAWN_LOAN_NOT_FOUND",
                            "Pawn loan not found with ID: " + patch.getPawnLoanId()
                    ));
            pawnForfeit.setPawnLoan(pawnLoan);
        }

        if (patch.getForfeitDate() != null) {
            pawnForfeit.setForfeitDate(patch.getForfeitDate());
        }

        if (patch.getNote() != null) {
            pawnForfeit.setNote(patch.getNote());
        }

        return pawnForfeitRepository.save(pawnForfeit);
    }

    /**
     * DELETE
     */
    @Transactional
    public void deletePawnForfeit(Long id) {
        PawnForfeit pawnForfeit = getPawnForfeitById(id);
        pawnForfeitRepository.delete(pawnForfeit);
    }
}
package com.cineBook.cinebook_platform.service;

import com.cineBook.cinebook_platform.dto.*;
import com.cineBook.cinebook_platform.model.Show;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy Pattern: each DiscountRule is a strategy.
 * The DiscountEngine composes them and applies all applicable rules.
 *
 * Current rules:
 *  1. THIRD_TICKET_DISCOUNT  – 50% off on every 3rd ticket in a booking
 *  2. AFTERNOON_SHOW_DISCOUNT – 20% off on all seats for afternoon shows
 */
public class DiscountEngine {

    // ─────────────────────────────────────────────────────────
    // Strategy Interface
    // ─────────────────────────────────────────────────────────

    public interface DiscountRule {
        boolean isApplicable(DiscountContext context);
        DiscountResult apply(DiscountContext context);
    }

    // ─────────────────────────────────────────────────────────
    // Context passed to each rule
    // ─────────────────────────────────────────────────────────

    public record DiscountContext(
            Show show,
            List<BigDecimal> seatPrices  // ordered list, one price per seat being booked
    ) {}

    public record DiscountResult(
            String offerCode,
            String description,
            BigDecimal discountAmount
    ) {}

    // ─────────────────────────────────────────────────────────
    // Rule 1: 50% off on the 3rd ticket (and every 3rd thereafter)
    // ─────────────────────────────────────────────────────────

    @Component
    public static class ThirdTicketDiscountRule implements DiscountRule {

        @Override
        public boolean isApplicable(DiscountContext context) {
            return context.seatPrices().size() >= 3;
        }

        @Override
        public DiscountResult apply(DiscountContext context) {
            List<BigDecimal> prices = context.seatPrices();
            BigDecimal totalDiscount = BigDecimal.ZERO;

            for (int i = 2; i < prices.size(); i += 3) {
                // 50% off every 3rd seat (0-indexed positions 2, 5, 8, ...)
                BigDecimal seatDiscount = prices.get(i)
                        .multiply(new BigDecimal("0.50"))
                        .setScale(2, RoundingMode.HALF_UP);
                totalDiscount = totalDiscount.add(seatDiscount);
            }

            return new DiscountResult(
                    "THIRD_TICKET_50",
                    "50% off on every 3rd ticket",
                    totalDiscount
            );
        }
    }

    // ─────────────────────────────────────────────────────────
    // Rule 2: 20% off for afternoon shows
    // ─────────────────────────────────────────────────────────

    @Component
    public static class AfternoonShowDiscountRule implements DiscountRule {

        @Override
        public boolean isApplicable(DiscountContext context) {
            return context.show().getSlot() == Show.ShowSlot.AFTERNOON;
        }

        @Override
        public DiscountResult apply(DiscountContext context) {
            BigDecimal totalSeatsPrice = context.seatPrices().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal discount = totalSeatsPrice
                    .multiply(new BigDecimal("0.20"))
                    .setScale(2, RoundingMode.HALF_UP);

            return new DiscountResult(
                    "AFTERNOON_20",
                    "20% off on afternoon shows",
                    discount
            );
        }
    }

    // ─────────────────────────────────────────────────────────
    // Engine: orchestrates all rules
    // ─────────────────────────────────────────────────────────

    @Service
    public static class DiscountService {

        private final List<DiscountRule> rules;

        public DiscountService(List<DiscountRule> rules) {
            this.rules = rules;
        }

        /**
         * Applies all applicable discount rules and returns the breakdown.
         * NOTE: discounts are NOT stacked multiplicatively — each is computed
         * on the original price and the maximum single discount is taken if they conflict.
         * Here both AFTERNOON and THIRD_TICKET can apply simultaneously (they target different things).
         */
        public DiscountBreakdown calculate(DiscountContext context) {
            List<BookingDTO.AppliedOffer> applied = new ArrayList<>();
            BigDecimal totalDiscount = BigDecimal.ZERO;

            for (DiscountRule rule : rules) {
                if (rule.isApplicable(context)) {
                    DiscountResult result = rule.apply(context);
                    applied.add(BookingDTO.AppliedOffer.builder()
                            .offerCode(result.offerCode())
                            .description(result.description())
                            .discountAmount(result.discountAmount())
                            .build());
                    totalDiscount = totalDiscount.add(result.discountAmount());
                }
            }

            BigDecimal grossAmount = context.seatPrices().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Discount cannot exceed gross amount
            totalDiscount = totalDiscount.min(grossAmount);

            return new DiscountBreakdown(grossAmount, totalDiscount, applied);
        }

        public record DiscountBreakdown(
                BigDecimal grossAmount,
                BigDecimal totalDiscount,
                List<BookingDTO.AppliedOffer> appliedOffers
        ) {}
    }
}

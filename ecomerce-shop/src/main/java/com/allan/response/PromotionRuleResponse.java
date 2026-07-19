package com.allan.response;

/**
 * Outbound response for a single promotion eligibility rule.
 * Embedded in {@link PromotionResponse#rules()} and returned standalone
 * from rule management endpoints.
 *
 * <p>{@code ruleType} and {@code operator} are serialized as their enum
 * name strings — decouples the API contract from internal enum naming.
 *
 * <p><strong>Value field semantics by ruleType</strong> (for frontend display):
 * <pre>
 *   MIN_ORDER_VALUE   →  UGX amount e.g. "50000"  → display as "UGX 50,000"
 *   MIN_ITEM_COUNT    →  integer e.g. "3"
 *   PRODUCT_IN_CART   →  comma-separated product IDs e.g. "101,204,305"
 *   CATEGORY          →  category ID e.g. "12"
 *   USER_SEGMENT      →  segment name e.g. "NEW_CUSTOMER"
 *   FIRST_ORDER_ONLY  →  "true" (value ignored by engine)
 *   VENDOR_PRODUCTS   →  seller ID e.g. "7"
 * </pre>
 *
 * <p><strong>editable flag:</strong> when {@code false}, the admin UI must
 * hide Edit and Delete controls for this rule. The promotion has active
 * redemptions — rules are permanently immutable at this point.
 */
public record PromotionRuleResponse(

        Long id,

        /** ID of the parent promotion. */
        Long promotionId,

        /** Enum name: MIN_ORDER_VALUE | MIN_ITEM_COUNT | PRODUCT_IN_CART | etc. */
        String ruleType,

        /** Enum name: GTE | LTE | EQ | IN | NOT_IN */
        String operator,

        /**
         * The threshold or reference value as a plain string.
         * Always UPPER CASE and trimmed — normalized on ingestion.
         */
        String value,

        /** Human-readable label for the admin UI. May be null. */
        String description,

        /**
         * False when the parent promotion has confirmed redemptions —
         * the admin UI must disable Edit/Delete for this rule.
         */
        boolean editable
) {}
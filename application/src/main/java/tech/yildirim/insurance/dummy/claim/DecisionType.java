package tech.yildirim.insurance.dummy.claim;

/** Types of decisions that can be made on a claim. */
public enum DecisionType {
  APPROVED,
  PARTIALLY_APPROVED,
  REJECTED,
  REQUIRES_MORE_INFO,
  UNDER_INVESTIGATION
}

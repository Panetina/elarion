package panetina.elarion.addons.economy.model;

public enum TransactionStatus {
    SUCCESS,
    NOT_BOUND,
    INVALID_ACCOUNT,
    INVALID_TYPE_FLOW,
    INVALID_AMOUNT,
    INSUFFICIENT_FUNDS,
    BALANCE_OVERFLOW,
    PERSISTENCE_FAILED
}

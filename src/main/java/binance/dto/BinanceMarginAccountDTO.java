package binance.dto;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class BinanceMarginAccountDTO {
    private final boolean borrowEnabled;
    private final BigDecimal marginLevel;
    private final BigDecimal totalAssetOfBtc;
    private final BigDecimal totalLiabilityOfBtc;
    private final BigDecimal totalNetAssetOfBtc;
    private final boolean tradeEnabled;
    private final boolean transferEnabled;
    List<BinanceMarginAssetDTO> userAssets;

    public BinanceMarginAccountDTO(
            @JsonProperty("borrowEnabled") boolean borrowEnabled,
            @JsonProperty("marginLevel") BigDecimal marginLevel,
            @JsonProperty("totalAssetOfBtc") BigDecimal totalAssetOfBtc,
            @JsonProperty("totalLiabilityOfBtc") BigDecimal totalLiabilityOfBtc,
            @JsonProperty("totalNetAssetOfBtc") BigDecimal totalNetAssetOfBtc,
            @JsonProperty("tradeEnabled") boolean tradeEnabled,
            @JsonProperty("transferEnabled") boolean transferEnabled,
            @JsonProperty("userAssets") List<BinanceMarginAssetDTO> userAssets) {
        this.borrowEnabled = borrowEnabled;
        this.marginLevel = marginLevel;
        this.totalAssetOfBtc = totalAssetOfBtc;
        this.totalLiabilityOfBtc = totalLiabilityOfBtc;
        this.totalNetAssetOfBtc = totalNetAssetOfBtc;
        this.tradeEnabled = tradeEnabled;
        this.transferEnabled = transferEnabled;
        this.userAssets = userAssets;
    }
    public boolean isBorrowEnabled() {
        return borrowEnabled;
    }
    public BigDecimal getMarginLevel() {
        return marginLevel;
    }
    public BigDecimal getTotalAssetOfBtc() {
        return totalAssetOfBtc;
    }
    public BigDecimal getTotalLiabilityOfBtc() {
        return totalLiabilityOfBtc;
    }
    public BigDecimal getTotalNetAssetOfBtc() {
        return totalNetAssetOfBtc;
    }
    public boolean isTradeEnabled() {
        return tradeEnabled;
    }
    public boolean isTransferEnabled() {
        return transferEnabled;
    }
    public List<BinanceMarginAssetDTO> getUserAssets() {
        return userAssets;
    }
}

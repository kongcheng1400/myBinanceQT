package binance.dto;

import java.math.BigDecimal;
import org.knowm.xchange.currency.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;
public final class BinanceMarginAssetDTO {

    private final Currency asset;
    private final BigDecimal borrowed;
    private final BigDecimal free;
    private final BigDecimal interest;
    private final BigDecimal locked;
    private final BigDecimal netAsset;
    public Currency getAsset() {
        return asset;
    }
    public BigDecimal getBorrowed() {
        return borrowed;
    }
    public BigDecimal getFree() {
        return free;
    }
    public BigDecimal getInterest() {
        return interest;
    }
    public BigDecimal getLocked() {
        return locked;
    }
    public BigDecimal getNetAsset() {
        return netAsset;
    }
    public BinanceMarginAssetDTO(
            @JsonProperty("asset") String asset,
            @JsonProperty("borrowed") BigDecimal borrowed,
            @JsonProperty("free") BigDecimal free,
            @JsonProperty("interest") BigDecimal interest,
            @JsonProperty("locked")BigDecimal locked,
            @JsonProperty("netAsset")BigDecimal netAsset) {
        this.asset = Currency.getInstance(asset);
        this.borrowed = borrowed;
        this.free = free;
        this.interest = interest;
        this.locked = locked;
        this.netAsset = netAsset;
    }
}

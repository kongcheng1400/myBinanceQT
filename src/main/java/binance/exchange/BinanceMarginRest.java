package binance.exchange;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import binance.dto.BinanceMarginAccountDTO;
import org.knowm.xchange.binance.Binance;
import org.knowm.xchange.binance.BinanceAuthenticated;
import org.knowm.xchange.binance.dto.meta.BinanceSystemStatus;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.SynchronizedValueFactory;


@Path("")
@Produces(MediaType.APPLICATION_JSON)
public interface BinanceMarginRest extends BinanceAuthenticated {
    String SIGNATURE = "signature";
    String X_MBX_APIKEY = "X-MBX-APIKEY";

    @GET
    @Path("sapi/v1/margin/account")
    BinanceMarginAccountDTO getMarginAccount(
            @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature);
}
import com.github.zemke.*;
import de.yamass.redg.runtime.AbstractRedG;

public class RedG {

    public AbstractRedG createDataSet() {

        RedG redG = new RedG();

        MyExchangeRef exchangeRef1 = redG.addExchangeRef()
            .name("Lea")
            .id(new java.math.BigDecimal("1"));
        MyExchangeRate exchangeRate1 = redG.addExchangeRate(exchangeRef1)
            .firstName("Flo1")
            .id(new java.math.BigDecimal("11"));
        MyExchangeRate exchangeRate2 = redG.addExchangeRate(exchangeRef1)
            .firstName("Flo2")
            .id(new java.math.BigDecimal("12"))
            .composite(exchangeRate1);

        return redG;
    }
}
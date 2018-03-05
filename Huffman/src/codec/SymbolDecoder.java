package codec;

import java.io.IOException;

import io.BitSource;
import io.InsufficientBitsLeftException;
import models.Symbol;

public interface SymbolDecoder {

	Symbol decode(BitSource bit_source) throws InsufficientBitsLeftException, IOException;

}
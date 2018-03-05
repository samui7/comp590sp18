package codec;

import java.io.IOException;
import io.BitSink;
import models.Symbol;

public interface SymbolEncoder {	
	void encode(Symbol s, BitSink out) throws IOException;
}
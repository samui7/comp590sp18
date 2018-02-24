package io;

import java.io.IOException;

public interface BitSink {

	int write(int bits, int length) throws IOException;
	int write(String bitstring) throws IOException;
	int padToWord() throws IOException;
	
}

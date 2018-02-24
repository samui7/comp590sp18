package io;

import java.io.IOException;

public interface BitSource {

	int next(int count) throws InsufficientBitsLeftException, IOException;
}

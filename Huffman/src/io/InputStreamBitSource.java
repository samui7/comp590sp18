package io;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamBitSource implements BitSource {

	private InputStream _stream;
	private int _buffer;
	private int _available;

	public InputStreamBitSource(InputStream stream) {
		_stream = stream;
		_buffer = 0x0;
		_available = 0;
	}

	@Override
	public int next(int count) throws InsufficientBitsLeftException, IOException {

		if (count > 32) {
			throw new RuntimeException("Can't read more than 32 bits as an int");
		}
		if (count <= 0) {
			return 0;
		}

		int overflow = 0x0;
		int num_bits_in_overflow = 0;

		while (_available < count) {
			int bits_from_stream = _stream.read();
			if (bits_from_stream == -1) {
				throw new InsufficientBitsLeftException(_available);
			}

			if (_available > 24) {
				overflow = ((_buffer >> 24) & 0xff);
				num_bits_in_overflow = _available%24;				
			}

			_buffer = ((_buffer << 8) | bits_from_stream);
			_available += 8;
		}

		// If there are bits in the overflow, then we know they will be part of the result.
		int next_bits = overflow;
		next_bits <<= (count - num_bits_in_overflow);
		next_bits |= ((_buffer >> (_available - (count-num_bits_in_overflow))) &
					  (~(0xffffffff << (count-num_bits_in_overflow))));
		_available -= count;
		
		return next_bits;
	}
}

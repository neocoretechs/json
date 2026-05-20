package org.json.cbor.decoder;

import java.io.InputStream;

import org.json.cbor.CborDecoder;
import org.json.cbor.CborException;
import org.json.cbor.model.SinglePrecisionFloat;

public class SinglePrecisionFloatDecoder extends AbstractDecoder<SinglePrecisionFloat> {

	public SinglePrecisionFloatDecoder(CborDecoder decoder, InputStream inputStream) {
		super(decoder, inputStream);
	}

	@Override
	public SinglePrecisionFloat decode(int initialByte) throws CborException {
		int bits = 0;
		bits |= nextSymbol() & 0xFF;
		bits <<= 8;
		bits |= nextSymbol() & 0xFF;
		bits <<= 8;
		bits |= nextSymbol() & 0xFF;
		bits <<= 8;
		bits |= nextSymbol() & 0xFF;
		return new SinglePrecisionFloat(Float.intBitsToFloat(bits));
	}

}

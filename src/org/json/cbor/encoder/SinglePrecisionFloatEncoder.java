package org.json.cbor.encoder;

import java.io.OutputStream;

import org.json.cbor.CborEncoder;
import org.json.cbor.CborException;
import org.json.cbor.model.SinglePrecisionFloat;

public class SinglePrecisionFloatEncoder extends AbstractEncoder<SinglePrecisionFloat> {

	public SinglePrecisionFloatEncoder(CborEncoder encoder, OutputStream outputStream) {
		super(encoder, outputStream);
	}

	@Override
	public void encode(SinglePrecisionFloat dataItem) throws CborException {
		write((7 << 5) | 26);
		int bits = Float.floatToRawIntBits(dataItem.getValue());
		write((bits >> 24) & 0xFF);
		write((bits >> 16) & 0xFF);
		write((bits >> 8) & 0xFF);
		write((bits >> 0) & 0xFF);
	}

}

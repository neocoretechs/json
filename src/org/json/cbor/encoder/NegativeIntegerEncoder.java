package org.json.cbor.encoder;

import java.io.OutputStream;
import java.math.BigInteger;

import org.json.cbor.CborEncoder;
import org.json.cbor.CborException;
import org.json.cbor.model.MajorType;
import org.json.cbor.model.NegativeInteger;

public class NegativeIntegerEncoder extends AbstractEncoder<NegativeInteger> {

	private static final BigInteger MINUS_ONE = BigInteger.valueOf(-1);

	public NegativeIntegerEncoder(CborEncoder encoder, OutputStream outputStream) {
		super(encoder, outputStream);
	}

	@Override
	public void encode(NegativeInteger dataItem) throws CborException {
		encodeTypeAndLength(MajorType.NEGATIVE_INTEGER, MINUS_ONE.subtract(dataItem.getValue()).abs());
	}

}

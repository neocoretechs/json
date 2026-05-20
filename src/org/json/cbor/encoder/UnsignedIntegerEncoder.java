package org.json.cbor.encoder;

import java.io.OutputStream;

import org.json.cbor.CborEncoder;
import org.json.cbor.CborException;
import org.json.cbor.model.MajorType;
import org.json.cbor.model.UnsignedInteger;

public class UnsignedIntegerEncoder extends AbstractEncoder<UnsignedInteger> {

    public UnsignedIntegerEncoder(CborEncoder encoder, OutputStream outputStream) {
        super(encoder, outputStream);
    }

    @Override
    public void encode(UnsignedInteger dataItem) throws CborException {
        encodeTypeAndLength(MajorType.UNSIGNED_INTEGER, dataItem.getValue());
    }

}

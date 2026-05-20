package org.json.cbor.decoder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.json.cbor.CborDecoder;
import org.json.cbor.CborException;
import org.json.cbor.model.ByteString;
import org.json.cbor.model.DataItem;
import org.json.cbor.model.MajorType;
import org.json.cbor.model.Special;

public class ByteStringDecoder extends AbstractDecoder<ByteString> {

    public ByteStringDecoder(CborDecoder decoder, InputStream inputStream) {
        super(decoder, inputStream);
    }

    @Override
    public ByteString decode(int initialByte) throws CborException {
        long length = getLength(initialByte);
        if (length == INFINITY) {
            if (decoder.isAutoDecodeInfinitiveByteStrings()) {
                return decodeInfinitiveLength();
            } else {
                ByteString byteString = new ByteString(null);
                byteString.setChunked(true);
                return byteString;
            }
        } else {
            return decodeFixedLength(length);
        }
    }

    private ByteString decodeInfinitiveLength() throws CborException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (;;) {
            DataItem dataItem = decoder.decodeNext();
            if (dataItem == null) {
                throw new CborException("Unexpected end of stream");
            }
            MajorType majorType = dataItem.getMajorType();
            if (Special.BREAK.equals(dataItem)) {
                break;
            } else if (majorType == MajorType.BYTE_STRING) {
                ByteString byteString = (ByteString) dataItem;
                byte[] byteArray = byteString.getBytes();
                if (byteArray != null) {
                    bytes.write(byteArray, 0, byteArray.length);
                }
            } else {
                throw new CborException("Unexpected major type "
                        + majorType);
            }
        }
        return new ByteString(bytes.toByteArray());
    }

    private ByteString decodeFixedLength(long length) throws CborException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream((int) length);
        for (long i = 0; i < length; i++) {
            bytes.write(nextSymbol());
        }
        return new ByteString(bytes.toByteArray());
    }

}

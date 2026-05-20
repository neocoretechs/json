package org.json.cbor.decoder;

import java.io.InputStream;

import org.json.cbor.CborDecoder;
import org.json.cbor.CborException;
import org.json.cbor.model.Array;
import org.json.cbor.model.DataItem;
import org.json.cbor.model.Special;

public class ArrayDecoder extends AbstractDecoder<Array> {

    public ArrayDecoder(CborDecoder decoder, InputStream inputStream) {
        super(decoder, inputStream);
    }

    @Override
    public Array decode(int initialByte) throws CborException {
        long length = getLength(initialByte);
        if (length == INFINITY) {
            return decodeInfinitiveLength();
        } else {
            return decodeFixedLength(length);
        }
    }

    private Array decodeInfinitiveLength() throws CborException {
        Array array = new Array();
        array.setChunked(true);
        if (decoder.isAutoDecodeInfinitiveArrays()) {
            DataItem dataItem;
            for (;;) {
                dataItem = decoder.decodeNext();
                if (dataItem == null) {
                    throw new CborException("Unexpected end of stream");
                }
                if (Special.BREAK.equals(dataItem)) {
                    array.add(Special.BREAK);
                    break;
                }
                array.add(dataItem);
            }
        }
        return array;
    }

    private Array decodeFixedLength(long length) throws CborException {
        Array array = new Array();
        for (long i = 0; i < length; i++) {
            DataItem dataItem = decoder.decodeNext();
            if (dataItem == null) {
                throw new CborException("Unexpected end of stream");
            }
            array.add(dataItem);
        }
        return array;
    }

}

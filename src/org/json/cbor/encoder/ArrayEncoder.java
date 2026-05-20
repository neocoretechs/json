package org.json.cbor.encoder;

import java.io.OutputStream;
import java.util.List;

import org.json.cbor.CborEncoder;
import org.json.cbor.CborException;
import org.json.cbor.model.Array;
import org.json.cbor.model.DataItem;
import org.json.cbor.model.MajorType;

public class ArrayEncoder extends AbstractEncoder<Array> {

    public ArrayEncoder(CborEncoder encoder, OutputStream outputStream) {
        super(encoder, outputStream);
    }

    @Override
    public void encode(Array array) throws CborException {
        List<DataItem> dataItems = array.getDataItems();
        if (array.isChunked()) {
            encodeTypeChunked(MajorType.ARRAY);
        } else {
            encodeTypeAndLength(MajorType.ARRAY, dataItems.size());
        }
        for (DataItem dataItem : dataItems) {
            encoder.encode(dataItem);
        }
    }

}

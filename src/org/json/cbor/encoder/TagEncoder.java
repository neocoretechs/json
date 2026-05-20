package org.json.cbor.encoder;

import java.io.OutputStream;

import org.json.cbor.CborEncoder;
import org.json.cbor.CborException;
import org.json.cbor.model.MajorType;
import org.json.cbor.model.Tag;

public class TagEncoder extends AbstractEncoder<Tag> {

    public TagEncoder(CborEncoder encoder, OutputStream outputStream) {
        super(encoder, outputStream);
    }

    @Override
    public void encode(Tag tag) throws CborException {
        encodeTypeAndLength(MajorType.TAG, tag.getValue());
    }

}

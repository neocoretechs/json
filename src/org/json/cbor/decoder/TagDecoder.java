package org.json.cbor.decoder;

import java.io.InputStream;

import org.json.cbor.CborDecoder;
import org.json.cbor.CborException;
import org.json.cbor.model.Tag;

public class TagDecoder extends AbstractDecoder<Tag> {

    public TagDecoder(CborDecoder decoder, InputStream inputStream) {
        super(decoder, inputStream);
    }

    @Override
    public Tag decode(int initialByte) throws CborException {
        return new Tag(getLength(initialByte));
    }

}

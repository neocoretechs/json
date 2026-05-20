package org.json.cbor.encoder;

import java.io.OutputStream;

import org.json.cbor.CborEncoder;
import org.json.cbor.CborException;
import org.json.cbor.model.DoublePrecisionFloat;
import org.json.cbor.model.HalfPrecisionFloat;
import org.json.cbor.model.SimpleValue;
import org.json.cbor.model.SimpleValueType;
import org.json.cbor.model.SinglePrecisionFloat;
import org.json.cbor.model.Special;

public class SpecialEncoder extends AbstractEncoder<Special> {

    private final HalfPrecisionFloatEncoder halfPrecisionFloatEncoder;
    private final SinglePrecisionFloatEncoder singlePrecisionFloatEncoder;
    private final DoublePrecisionFloatEncoder doublePrecisionFloatEncoder;

    public SpecialEncoder(CborEncoder encoder, OutputStream outputStream) {
        super(encoder, outputStream);
        halfPrecisionFloatEncoder = new HalfPrecisionFloatEncoder(encoder, outputStream);
        singlePrecisionFloatEncoder = new SinglePrecisionFloatEncoder(encoder, outputStream);
        doublePrecisionFloatEncoder = new DoublePrecisionFloatEncoder(encoder, outputStream);
    }

    @Override
    public void encode(Special dataItem) throws CborException {
        switch (dataItem.getSpecialType()) {
        case BREAK:
            write((7 << 5) | 31);
            break;
        case SIMPLE_VALUE:
            SimpleValue simpleValue = (SimpleValue) dataItem;
            switch (simpleValue.getSimpleValueType()) {
            case FALSE:
            case NULL:
            case TRUE:
            case UNDEFINED:
                SimpleValueType type = simpleValue.getSimpleValueType();
                write((7 << 5) | type.getValue());
                break;
            case UNALLOCATED:
                write((7 << 5) | simpleValue.getValue());
                break;
            case RESERVED:
                break;
            }
            break;
        case UNALLOCATED:
            throw new CborException("Unallocated special type");
        case IEEE_754_HALF_PRECISION_FLOAT:
            if (!(dataItem instanceof HalfPrecisionFloat)) {
                throw new CborException("Wrong data item type");
            }
            halfPrecisionFloatEncoder.encode((HalfPrecisionFloat) dataItem);
            break;
        case IEEE_754_SINGLE_PRECISION_FLOAT:
            if (!(dataItem instanceof SinglePrecisionFloat)) {
                throw new CborException("Wrong data item type");
            }
            singlePrecisionFloatEncoder.encode((SinglePrecisionFloat) dataItem);
            break;
        case IEEE_754_DOUBLE_PRECISION_FLOAT:
            if (!(dataItem instanceof DoublePrecisionFloat)) {
                throw new CborException("Wrong data item type");
            }
            doublePrecisionFloatEncoder.encode((DoublePrecisionFloat) dataItem);
            break;
        case SIMPLE_VALUE_NEXT_BYTE:
            if (!(dataItem instanceof SimpleValue)) {
                throw new CborException("Wrong data item type");
            }
            SimpleValue simpleValueNextByte = (SimpleValue) dataItem;
            write((7 << 5) | 24);
            write(simpleValueNextByte.getValue());
            break;
        }
    }

}

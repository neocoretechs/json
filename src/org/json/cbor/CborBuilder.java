package org.json.cbor;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import org.json.cbor.builder.AbstractBuilder;
import org.json.cbor.builder.ArrayBuilder;
import org.json.cbor.builder.ByteStringBuilder;
import org.json.cbor.builder.MapBuilder;
import org.json.cbor.builder.UnicodeStringBuilder;
import org.json.cbor.model.Array;
import org.json.cbor.model.ByteString;
import org.json.cbor.model.DataItem;
import org.json.cbor.model.Map;
import org.json.cbor.model.UnicodeString;

public class CborBuilder extends AbstractBuilder<CborBuilder> {

    private final List<DataItem> dataItems = new LinkedList<>();

    public CborBuilder() {
        super(null);
    }

    public CborBuilder reset() {
        dataItems.clear();
        return this;
    }

    public List<DataItem> build() {
        return dataItems;
    }

    public CborBuilder add(DataItem dataItem) {
        dataItems.add(dataItem);
        return this;
    }

    public CborBuilder add(long value) {
        add(convert(value));
        return this;
    }

    public CborBuilder add(BigInteger value) {
        add(convert(value));
        return this;
    }

    public CborBuilder add(boolean value) {
        add(convert(value));
        return this;
    }

    public CborBuilder add(float value) {
        add(convert(value));
        return this;
    }

    public CborBuilder add(double value) {
        add(convert(value));
        return this;
    }

    public CborBuilder add(byte[] bytes) {
        add(convert(bytes));
        return this;
    }

    public ByteStringBuilder<CborBuilder> startByteString() {
        return startByteString(null);
    }

    public ByteStringBuilder<CborBuilder> startByteString(byte[] bytes) {
        add(new ByteString(bytes).setChunked(true));
        return new ByteStringBuilder<CborBuilder>(this);
    }

    public CborBuilder add(String string) {
        add(convert(string));
        return this;
    }

    public UnicodeStringBuilder<CborBuilder> startString() {
        return startString(null);
    }

    public UnicodeStringBuilder<CborBuilder> startString(String string) {
        add(new UnicodeString(string).setChunked(true));
        return new UnicodeStringBuilder<CborBuilder>(this);
    }

    public CborBuilder addTag(long value) {
        add(tag(value));
        return this;
    }

    public ArrayBuilder<CborBuilder> startArray() {
        Array array = new Array();
        array.setChunked(true);
        add(array);
        return new ArrayBuilder<CborBuilder>(this, array);
    }

    public ArrayBuilder<CborBuilder> addArray() {
        Array array = new Array();
        add(array);
        return new ArrayBuilder<CborBuilder>(this, array);
    }

    public MapBuilder<CborBuilder> addMap() {
        Map map = new Map();
        add(map);
        return new MapBuilder<CborBuilder>(this, map);
    }

    public MapBuilder<CborBuilder> startMap() {
        Map map = new Map();
        map.setChunked(true);
        add(map);
        return new MapBuilder<CborBuilder>(this, map);
    }

    @Override
    protected void addChunk(DataItem dataItem) {
        add(dataItem);
    }

}

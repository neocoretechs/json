package org.json.cbor;

import org.json.cbor.model.DataItem;

/**
 * Callback interface for a streaming {@link CborDecoder}.
 */
public interface DataItemListener {

	/**
	 * Gets called on every decoded {@link DataItem}.
	 */
	void onDataItem(DataItem dataItem);

}

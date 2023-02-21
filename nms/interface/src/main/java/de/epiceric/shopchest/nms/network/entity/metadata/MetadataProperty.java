package de.epiceric.shopchest.nms.network.entity.metadata;

public interface MetadataProperty<T> {

    /**
     * Create a new {@link MetadataValue} for this property
     *
     * @param value The value of the {@link MetadataValue}
     * @return a new {@link MetadataValue}
     */
    MetadataValue set(T value);


}

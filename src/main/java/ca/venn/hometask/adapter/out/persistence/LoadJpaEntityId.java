package ca.venn.hometask.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;

public class LoadJpaEntityId implements Serializable {

    private String loadId;
    private String customerId;

    protected LoadJpaEntityId() { }

    public LoadJpaEntityId(String loadId, String customerId) {
        this.loadId = loadId;
        this.customerId = customerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoadJpaEntityId that)) {
            return false;
        }
        return Objects.equals(loadId, that.loadId) && Objects.equals(customerId, that.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loadId, customerId);
    }
}

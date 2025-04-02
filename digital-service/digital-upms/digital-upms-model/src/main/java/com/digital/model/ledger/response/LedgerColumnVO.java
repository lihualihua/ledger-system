package com.digital.model.ledger.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "台账表头响应体")
public class LedgerColumnVO implements Serializable {
    private static final long serialVersionUID = -571937148725461295L;

    private String label;

    private String prop;

    private LedgerColumnAttr attrs;

    private LedgerColumnVO(Builder builder) {
        this.label = builder.label;
        this.prop = builder.prop;
        this.attrs = builder.attrs;
    }

    public static class Builder {
        private String label;
        private String prop;
        private LedgerColumnAttr attrs;

        public Builder setLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder setProp(String prop) {
            this.prop = prop;
            return this;
        }

        public Builder setAttrs(LedgerColumnAttr attrs) {
            this.attrs = attrs;
            return this;
        }

        public LedgerColumnVO build() {
            return new LedgerColumnVO(this);
        }
    }

    @Data
    public static class LedgerColumnAttr implements Serializable {

        private static final long serialVersionUID = -9007088846928900947L;

        private String width;

        private String fixed;
    }
}

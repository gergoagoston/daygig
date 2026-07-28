package edu.bbte.daygig.backend.dto.rating;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;

public record RatingSummaryOutDto(
                                  @JsonInclude(JsonInclude.Include.ALWAYS)
                                  BigDecimal average,
                                  long count) {
}

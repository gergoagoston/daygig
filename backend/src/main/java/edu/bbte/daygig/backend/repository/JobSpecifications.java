package edu.bbte.daygig.backend.repository;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import edu.bbte.daygig.backend.dto.job.JobSearchInDto;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.model.Job;

public final class JobSpecifications {

    private static final char LIKE_ESCAPE_CHARACTER = '\\';

    private JobSpecifications() {
    }

    public static Specification<Job> openJobsMatching(JobSearchInDto search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), JobStatus.OPEN));

            if (search.category() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), search.category()));
            }

            if (search.minHourlyRate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("hourlyRate"),
                        search.minHourlyRate()));
            }

            if (search.maxHourlyRate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("hourlyRate"),
                        search.maxHourlyRate()));
            }

            addTextSearchPredicates(search.q(), root.get("title"), root.get("description"),
                    root.get("locationName"), criteriaBuilder, predicates);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addTextSearchPredicates(String query,
                                                Expression<String> title,
                                                Expression<String> description,
                                                Expression<String> locationName,
                                                jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                                List<Predicate> predicates) {
        if (query == null || query.isBlank()) {
            return;
        }

        Expression<String> searchableText = criteriaBuilder.concat(
                criteriaBuilder.concat(criteriaBuilder.concat(title, " "), description),
                criteriaBuilder.concat(" ", locationName));
        Expression<String> normalizedText = criteriaBuilder.function("unaccent", String.class,
                criteriaBuilder.lower(searchableText));

        for (String token : query.strip().split("\\s+")) {
            String normalizedToken = normalizeToken(token);
            predicates.add(criteriaBuilder.like(normalizedText, "%" + escapeLike(normalizedToken) + "%",
                    LIKE_ESCAPE_CHARACTER));
        }
    }

    private static String normalizeToken(String token) {
        return unaccent(token).toLowerCase(Locale.ROOT);
    }

    public static String unaccent(String value) {
        return Normalizer
                .normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}

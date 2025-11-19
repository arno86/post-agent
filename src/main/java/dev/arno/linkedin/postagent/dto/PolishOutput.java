package dev.arno.linkedin.postagent.dto;

import java.util.List;

public record PolishOutput(String polished, int charCount, List<DiffItem> diffs) {}

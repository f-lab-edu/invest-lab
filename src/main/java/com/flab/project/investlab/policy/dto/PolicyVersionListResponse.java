package com.flab.project.investlab.policy.dto;

import com.flab.project.investlab.policy.domain.PolicyVersion;

import java.time.Instant;
import java.util.List;

public record PolicyVersionListResponse(
        List<Item> items
) {

    public record Item(
            Long versionId,
            int versionNo,
            Long parentVersionId,
            String changeReason,
            Instant createdAt
    ) {

        public static Item from(final PolicyVersion version) {
            final PolicyVersion parentVersion = version.getParentVersion();
            return new Item(
                    version.getId(),
                    version.getVersionNo(),
                    parentVersion == null ? null : parentVersion.getId(),
                    version.getChangeReason(),
                    version.getCreatedAt()
            );
        }
    }
}

package com.be16261117report.post.postComment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCommentWriteReqBody(
        @NotBlank
        @Size(min = 2, max = 500)
        String content
) {
}

package dev.crepe.infra.redis.util;

import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CachedSliceWrapper {
    private List<GetTransactionHistoryResponse> content;
    private int pageNumber;
    private int pageSize;
    private boolean hasNext;

    public static CachedSliceWrapper from(Slice<GetTransactionHistoryResponse> slice) {
        return new CachedSliceWrapper(
                slice.getContent(),
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext()
        );
    }

    public Slice<GetTransactionHistoryResponse> toSlice() {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return new SliceImpl<>(content, pageRequest, hasNext);
    }
}

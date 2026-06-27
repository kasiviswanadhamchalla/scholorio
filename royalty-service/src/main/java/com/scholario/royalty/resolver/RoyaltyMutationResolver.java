package com.scholario.royalty.resolver;

import com.scholario.royalty.dto.RoyaltyPolicyInput;
import com.scholario.royalty.model.RoyaltyPolicy;
import com.scholario.royalty.model.RoyaltyRecord;
import com.scholario.royalty.service.RoyaltyService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class RoyaltyMutationResolver {
    private final RoyaltyService royaltyService;

    public RoyaltyMutationResolver(RoyaltyService royaltyService) {
        this.royaltyService = royaltyService;
    }

    @MutationMapping
    public RoyaltyPolicy defineRoyaltyPolicy(@Valid @Argument RoyaltyPolicyInput input) {
        return royaltyService.defineRoyaltyPolicy(input);
    }

    @MutationMapping
    public RoyaltyRecord calculateRoyalty(@Argument Long bookId, @Argument BigDecimal totalRevenue) {
        return royaltyService.calculateRoyalty(bookId, totalRevenue);
    }

    @MutationMapping
    public RoyaltyRecord distributeRoyalty(@Argument Long recordId) {
        return royaltyService.distributeRoyalty(recordId);
    }
}

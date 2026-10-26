package ru.redcarpet.stocks.verification.trades;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeVerificationController {

    private final TradeVerificationService verificationService;

    @PostMapping("/check")
    public CheckTradesResponse check(@Valid @RequestBody CheckTradesRequest request) {
        return verificationService.check(request);
    }
}
package com.aleos.filter.url;

import com.aleos.exception.servlet.RequestBodyParsingException;
import com.aleos.model.dto.in.CurrencyIdentifierPayload;
import com.aleos.model.dto.in.CurrencyPayload;
import com.aleos.model.dto.out.Error;
import com.aleos.util.RequestAttributeUtil;
import com.aleos.validator.CurrencyValidator;
import com.aleos.validator.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CurrencyUrlFilter extends AbstractUrlFilter {

    protected transient CurrencyValidator currencyValidator;

    @Override
    protected void initializePayload(HttpServletRequest req, HttpServletResponse resp) {
        if (isGet(req) || isDelete(req)) {
            extractCurrencyIdentifierPayload(req)
                    .ifPresent(payload -> RequestAttributeUtil.setPayload(req, payload));

        } else if (isPatch(req)) {
            extractCurrencyPayload(req)
                    .ifPresent(payload -> RequestAttributeUtil.setPayload(req, payload));
        }
    }

    @Override
    protected ValidationResult validatePayload(HttpServletRequest req, HttpServletResponse resp) {
        ValidationResult validationResult = new ValidationResult();

        if (isGet(req) || isDelete(req)) {
            validateCurrencyIdentifier(req, validationResult);
        }
        if (isPatch(req)) {
            validatePatchRequest(req, validationResult);
        }

        return validationResult;
    }

    private void validateCurrencyIdentifier(HttpServletRequest req, ValidationResult validationResult) {
        String identifier = RequestAttributeUtil.getPayload(req, CurrencyIdentifierPayload.class).identifier();

        if (isGet(req)) {
            currencyValidator.validateIdentifier(identifier).ifPresent(validationResult::add);

        } else if (isDelete(req)) {
            currencyValidator.validateNumericIdentifier(identifier).ifPresent(validationResult::add);
        }
    }

    private void validatePatchRequest(HttpServletRequest req, ValidationResult validationResult) {
        CurrencyPayload payload = RequestAttributeUtil.getPayload(req, CurrencyPayload.class);

        getPathInfo(req).ifPresentOrElse(
                pathId -> currencyValidator.validateNumericIdentifier(pathId).ifPresent(validationResult::add),
                () -> validationResult.add(Error.of("Path should contain numeric identifier."))
        );
        currencyValidator.validate(payload).getErrors().forEach(validationResult::add);
    }

    private Optional<CurrencyIdentifierPayload> extractCurrencyIdentifierPayload(HttpServletRequest req) {
        return getPathInfo(req).map(CurrencyIdentifierPayload::new);
    }

    private Optional<CurrencyPayload> extractCurrencyPayload(HttpServletRequest req) {
        var contentMap = extractContentForPatch(req);

        String name = contentMap.get("name");
        String code = contentMap.get("code");
        String sign = contentMap.get("sign");

        return Optional.of(new CurrencyPayload(name, code, sign));
    }

    private static Map<String, String> extractContentForPatch(HttpServletRequest req) {
        String contentType = req.getContentType();
        String supportedContentType = "application/x-www-form-urlencoded";
        if (supportedContentType.equals(contentType)) {
            try {
                return req.getReader().lines()
                        .flatMap(row -> Arrays.stream(row.split("&")))
                        .map(pair -> pair.split("=", 2))  // Split only into two parts, key and value
                        .collect(Collectors.toMap(
                                pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                                pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8),
                                (oldValue, newValue) -> newValue)  // In case of duplicates, take the latest
                        );
            } catch (IOException e) {
                throw new RequestBodyParsingException("Failed to parse PATCH request", e);
            }
        }
        throw new IllegalArgumentException("Unsupported content type: " + contentType);
    }
}

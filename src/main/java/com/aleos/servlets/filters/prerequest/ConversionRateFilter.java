package com.aleos.servlets.filters.prerequest;

import com.aleos.exceptions.servlets.RequestBodyParsingException;
import com.aleos.models.dtos.in.ConversionRateIdentifierPayload;
import com.aleos.models.dtos.in.ConversionRatePayload;
import com.aleos.models.dtos.out.ErrorResponse;
import com.aleos.util.AttributeNameUtil;
import com.aleos.validators.ConversionRateValidator;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UnknownFormatConversionException;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

public class ConversionRateFilter extends BaseRequestFilter {

    protected static final String CONVERSION_RATE_PATH_REGEX = "^/([a-zA-Z]{6})$";

    protected static final Pattern CONVERSION_RATE_REQUEST_PATTERN = Pattern.compile(CONVERSION_RATE_PATH_REGEX);

    protected ConversionRateValidator conversionRateValidator;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        super.init(filterConfig);
        conversionRateValidator = (ConversionRateValidator) filterConfig.getServletContext().getAttribute(
                AttributeNameUtil.getName(ConversionRateValidator.class));
    }

    @Override
    protected void putPayloadToContext(HttpServletRequest request, HttpServletResponse response) {

        String methodType = request.getMethod();
        int conversionRateCodeLength = 6;

        Optional<String> codeOptional = extractCodeFromPath(request);

        if ("GET".equalsIgnoreCase(methodType)) {

            codeOptional.ifPresent(code -> request.setAttribute(
                    AttributeNameUtil.PAYLOAD_MODEL_ATTR, new ConversionRateIdentifierPayload(code)));

        } else if ("PATCH".equalsIgnoreCase(methodType)
                   && codeOptional.isPresent()
                   && codeOptional.get().length() == conversionRateCodeLength) {

            var conversionRatePayload = getConversionRatePayload(request, response, codeOptional.get());

            request.setAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR, conversionRatePayload);
        }
    }

    @Override
    protected List<ErrorResponse> validateRequest(HttpServletRequest request, HttpServletResponse response) {

        String pathInfo = request.getPathInfo();
        if (isNull(pathInfo) || !CONVERSION_RATE_REQUEST_PATTERN.matcher(pathInfo).matches()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            return List.of(new ErrorResponse(INVALID_PATH_ERROR + "ConversionRate code must be present and correct."));
        }

        String methodType = request.getMethod();

        if ("GET".equalsIgnoreCase(methodType)) {

            var identifierPayload = (ConversionRateIdentifierPayload) request.getAttribute(
                    AttributeNameUtil.PAYLOAD_MODEL_ATTR);

            return conversionRateValidator.validateIdentifier(identifierPayload.code());
        }

        if ("PATCH".equalsIgnoreCase(methodType)) {
            var conversionRatePayload = (ConversionRatePayload) request.getAttribute(AttributeNameUtil.PAYLOAD_MODEL_ATTR);

            return conversionRateValidator.validate(conversionRatePayload);
        }

        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return List.of(new ErrorResponse(METHOD_IS_NOT_SUPPORTED.formatted(methodType)));
    }

    protected Optional<String> extractCodeFromPath(HttpServletRequest request) {

        var matcher = CONVERSION_RATE_REQUEST_PATTERN.matcher(request.getPathInfo());

        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private ConversionRatePayload getConversionRatePayload(HttpServletRequest request,
                                                           HttpServletResponse response,
                                                           String code) {
        int currencyCodeLength = 3;

        String baseCurrencyCode = code.substring(0, currencyCodeLength);
        String targetCurrencyCode = code.substring(currencyCodeLength);
        BigDecimal rate = extractRate(request, response);

        return new ConversionRatePayload(
                baseCurrencyCode,
                targetCurrencyCode,
                rate
        );
    }

    // patch request
    private BigDecimal extractRate(HttpServletRequest request, HttpServletResponse response) {

        try {
            return request.getReader().lines()
                    .filter(row -> row.startsWith("rate="))
                    .map(row -> row.split("=")[1])
                    .findFirst()
                    .map(this::toBigDecimal)
                    .orElseThrow(() -> new UnknownFormatConversionException("Unexpected error during rate conversion"));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new RequestBodyParsingException("Cannot convert passed value to BigDecimal.", e);
        } catch (IOException e) {
            throw new RequestBodyParsingException("Error reading request body.", e);
        }
    }

    // for an exception, not be swallowed by stream
    protected BigDecimal toBigDecimal(String value)  {
        return new BigDecimal(value);
    }
}

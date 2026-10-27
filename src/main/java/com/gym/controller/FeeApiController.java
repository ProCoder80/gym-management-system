package com.gym.controller;

import com.gym.model.dto.FeePreviewResponse;
import com.gym.model.entity.Membership;
import com.gym.model.prototype.MembershipPrototype;
import com.gym.model.prototype.MembershipPrototypeRegistry;
import com.gym.pattern.strategy.FeeStrategy;
import com.gym.pattern.strategy.FeeStrategyFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST API Controller providing real-time dynamic fee calculations
 * using Strategy Pattern, and prototype template configurations via Prototype Pattern.
 */
@RestController
@RequestMapping("/api")
public class FeeApiController {

    private final FeeStrategyFactory feeStrategyFactory;
    private final MembershipPrototypeRegistry prototypeRegistry;

    public FeeApiController(FeeStrategyFactory feeStrategyFactory,
                            MembershipPrototypeRegistry prototypeRegistry) {
        this.feeStrategyFactory = feeStrategyFactory;
        this.prototypeRegistry = prototypeRegistry;
    }

    /**
     * PATTERN: Prototype Pattern API
     * Returns an independent clone of the requested membership prototype template.
     */
    @GetMapping("/prototype/{templateKey}")
    public ResponseEntity<?> getPrototypeTemplate(@PathVariable String templateKey) {
        try {
            MembershipPrototype clone = prototypeRegistry.getClonedPrototype(templateKey);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("templateName", clone.getTemplateName());
            body.put("planType", clone.getPlanType());
            body.put("feeStrategy", clone.getFeeStrategy());
            body.put("baseFee", clone.getBaseFee());
            body.put("description", clone.getDescription());
            body.put("locker", clone.isLocker());
            body.put("dietPlan", clone.isDietPlan());
            body.put("ptSessions", clone.getPtSessions());
            body.put("features", clone.getFeatures());
            body.put("attributes", clone.getAttributes());
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/fee-preview")
    public ResponseEntity<FeePreviewResponse> getFeePreview(
            @RequestParam(defaultValue = "MONTHLY") String planType,
            @RequestParam(defaultValue = "false") boolean locker,
            @RequestParam(defaultValue = "false") boolean dietPlan,
            @RequestParam(defaultValue = "0") int ptSessions,
            @RequestParam(defaultValue = "NEW") String enrollmentType) {

        try {
            FeeStrategy strategy = feeStrategyFactory.getStrategy(planType);

            // Construct transient Membership for fee evaluation
            Membership dummy = new Membership.Builder(1L, planType, LocalDate.now())
                    .locker(locker)
                    .dietPlan(dietPlan)
                    .ptSessions(Math.max(0, ptSessions))
                    .build();

            double baseFee = strategy.getBaseFee();
            double subtotal = strategy.calculateFee(dummy);
            double discount = 0.0;

            // Apply 5% loyalty discount if Renewal
            if ("RENEWAL".equalsIgnoreCase(enrollmentType)) {
                discount = Math.round((subtotal * 0.05) * 100.0) / 100.0;
            }
            double totalFee = Math.round((subtotal - discount) * 100.0) / 100.0;

            FeePreviewResponse response = new FeePreviewResponse();
            response.setPlanType(planType);
            response.setStrategyName(strategy.getStrategyName());
            response.setBaseFee(baseFee);
            response.setLockerFee(locker ? (planType.equalsIgnoreCase("YEARLY") ? 2400.0 : (planType.equalsIgnoreCase("COUPLE") ? 500.0 : (planType.equalsIgnoreCase("STUDENT") ? 200.0 : 300.0))) : 0.0);
            response.setDietPlanFee(dietPlan ? (planType.equalsIgnoreCase("YEARLY") ? 4000.0 : (planType.equalsIgnoreCase("COUPLE") ? 800.0 : (planType.equalsIgnoreCase("STUDENT") ? 300.0 : 500.0))) : 0.0);
            response.setPtSessionsFee(ptSessions > 0 ? (subtotal - baseFee - response.getLockerFee() - response.getDietPlanFee()) : 0.0);
            response.setSubtotal(subtotal);
            response.setDiscount(discount);
            response.setTotalFee(totalFee);
            response.setDescription(strategy.getDescription());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

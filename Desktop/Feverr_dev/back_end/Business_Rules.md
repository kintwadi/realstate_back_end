
# Prorated Charges When Upgrading Subscription Plan

## Definition
**Prorated charges** refer to the adjusted cost of a subscription when you upgrade (or downgrade) your plan partway through a billing cycle. Instead of charging you the full price of the new plan immediately, the service calculates the difference based on how much time is left in your current billing period.

### How it works when upgrading:
1. **Current Plan Usage**: You’ve already paid for your current plan, so the service calculates how much of that plan you’ve used so far.
2. **New Plan Cost**: It then calculates how much the new plan would cost for the remaining time in the billing cycle.
3. **Prorated Charge**: You’re charged (or credited) the difference between the unused portion of your current plan and the cost of the new plan for the remaining time.

---

## Formula

To calculate the **prorated charge** when upgrading a subscription mid-cycle, use this formula:

The Prorated Charge Formula:


$$ \text{Prorated Charge} = \left( \frac{\text{New Plan Price}}{\text{Billing Cycle Days}} \times \text{Remaining Days} \right) - \left( \frac{\text{Current Plan Price}}{\text{Billing Cycle Days}} \times \text{Remaining Days} \right)$$

Or simplified:

$$\text{Prorated Charge} = \left( \frac{\text{New Plan Price} - \text{Current Plan Price}}{\text{Billing Cycle Days}} \right) \times \text{Remaining Days}$$

---
### Example

Using the dummy data:
- **Current plan**: €10/month  
- **New plan**: €25/month  
- **Billing cycle**: 30 days  
- **Upgrade on**: Day 12  

👉 **Prorated charge** = **€9.00**

This means you would pay an additional €9.00 at the time of the upgrade to cover the cost difference for the remaining 18 days of the billing cycle.

## Java Program Example

Java program that calculates the prorated charge:

```java
public class ProratedChargeCalculator {

    public static double calculateProratedCharge(double currentPlan, double newPlan, int billingCycleDays, int upgradeDay) {
        int remainingDays = billingCycleDays - upgradeDay;
        double dailyDifference = (newPlan - currentPlan) / billingCycleDays;
        return dailyDifference * remainingDays;
    }

    public static void main(String[] args) {
        double currentPlan = 10.0; // €10/month
        double newPlan = 25.0;     // €25/month
        int billingCycleDays = 30;
        int upgradeDay = 12;

        double proratedCharge = calculateProratedCharge(currentPlan, newPlan, billingCycleDays, upgradeDay);
        System.out.printf("The prorated charge is €%.2f%n", proratedCharge);
    }
}


```



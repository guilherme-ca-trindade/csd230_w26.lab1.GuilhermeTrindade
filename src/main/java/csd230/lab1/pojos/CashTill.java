package csd230.lab1.pojos;

public class CashTill {
    private double runningTotal;

    public CashTill() {
        this.runningTotal = 0.0;
    }

    public void showTotal() {
        System.out.println("Current Cash Till Total: $" + runningTotal);
    }

    public void sellItem(SaleableItem item) {
        item.sellItem();
        // Lecture 2.9: getPrice() returns Double now, so unwrap it before the arithmetic
        Double price = item.getPrice();
        double amount = (price == null ? 0.0 : price);
        runningTotal += amount;
        System.out.println("Added $" + amount + " to till.");
    }

    public double getRunningTotal() {
        return runningTotal;
    }
}

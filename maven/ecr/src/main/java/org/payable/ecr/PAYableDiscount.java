package org.payable.ecr;

import java.util.ArrayList;
import java.util.List;

public class PAYableDiscount {

    public String name;
    public List<Long> bin_ranges;
    public double amount;

    public PAYableDiscount(String name, double amount, long... bin_ranges) {
        this.name = name;
        this.amount = amount;
        this.bin_ranges = new ArrayList<>();
        for(int i = 0; i < bin_ranges.length; i++) {
            this.bin_ranges.add(bin_ranges[i]);
        }
    }

    public boolean isValid() {
        return amount > 0 && bin_ranges != null && bin_ranges.size() > 0;
    }
}

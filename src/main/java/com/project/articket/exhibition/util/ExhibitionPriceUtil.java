package com.project.articket.exhibition.util;

public class ExhibitionPriceUtil {
    private static final int DEFAULT_TICKET_PRICE = 10_000;

    private ExhibitionPriceUtil() {}

    public static boolean isFree(String price) {
        return price == null || price.contains("무료")
                || price.equals("0") || price.isBlank();
    }
    public static Integer resolveTicketPrice(boolean isFree) {
        return isFree ? null : DEFAULT_TICKET_PRICE;
    }
}

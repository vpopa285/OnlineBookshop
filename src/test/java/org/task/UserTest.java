package org.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserTest {

    User user = new User("test", "test@mail.com", "123");
    Book book = new Book("Java", "James", "Programming", "content", 20);

    @Test
    void addFundsTest() {
        user.addFunds(50);

        assertThat(user.getAmount()).isEqualTo(50);
    }

    @ParameterizedTest
    @CsvSource({
            "false, 1",
            "true, 0"
    })
    void reviewWithRestrictionTest(boolean restriction, int expectedComments) {
        user.addFunds(50);
        user.buy(book);

        user.setRestriction(restriction);
        user.review(book, 4, "");

        assertThat(book.getReviews().size()).isEqualTo(expectedComments);
    }

    @Test
    void reviewWithoutBuyingTest() {
        user.review(book, 4, "Test");

        assertThat(book.getReviews().size()).isEqualTo(0);
    }

    @ParameterizedTest
    @CsvSource({
            "20, true, 0",
            "10, false, 10",
    })
    void shouldBuyBookWithEnoughMoneyTest(double funds, boolean canAfford, double amount) {
        user.addFunds(funds);
        user.buy(book);

        assertAll(
                () -> assertThat(user.getPurchasedBooks().contains(book)).isEqualTo(canAfford),
                () -> assertThat(user.getAmount()).isEqualTo(amount)
        );

    }

}
